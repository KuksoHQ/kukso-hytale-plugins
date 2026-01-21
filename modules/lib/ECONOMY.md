# ECONOMY.md - Economy Module Reference

This document contains the economy module architecture that was originally part of KuksoHyLib.
Use this as a reference when implementing your own economy plugin for Hytale.

---

## Architecture Overview

```
Economy (interface)           - Main API for economy operations
  |
YourEconomy (impl)           - Your plugin's implementation
  |
CurrencyManager (static)     - Manages currency definitions and ECS ComponentTypes
  |
CurrencyComponent (ECS)      - Stores per-player balance for each currency
```

### Design Decision: Separate Components vs HashMap

| Approach | Pros | Cons |
|----------|------|------|
| **Separate ComponentTypes** | ECS-native queries, currency-specific systems, isolated data | More components, archetype changes when adding currencies |
| **HashMap single component** | Simple, no archetype churn, atomic multi-currency ops | Can't query by currency, less ECS-idiomatic |

**Recommendation:** Use separate ComponentTypes if you need:
- ECS queries like "find all players with gold > 1000"
- Currency-specific systems (e.g., `GoldDecaySystem`, `InterestSystem`)
- Different persistence strategies per currency

---

## Core Components

### Currency Record

Immutable record containing currency metadata:

```java
package com.yourplugin.economy;

/**
 * Currency metadata definition.
 *
 * @param id              Internal identifier (e.g., "gold")
 * @param displayName     Display name for UI (e.g., "Gold")
 * @param nameSingular    Singular form (e.g., "Gold")
 * @param namePlural      Plural form (e.g., "Gold")
 * @param symbol          Currency symbol (e.g., "G")
 * @param format          Format pattern (e.g., "{amount}{symbol}")
 * @param decimalPlaces   Number of decimal places (0 for whole numbers)
 * @param startingBalance Initial balance for new players
 * @param componentId     ECS ComponentType identifier (e.g., "yourplugin:gold")
 */
public record Currency(
    String id,
    String displayName,
    String nameSingular,
    String namePlural,
    String symbol,
    String format,
    int decimalPlaces,
    double startingBalance,
    String componentId
) {
    public Currency {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Currency id cannot be null or blank");
        }
        if (displayName == null || displayName.isBlank()) displayName = id;
        if (nameSingular == null || nameSingular.isBlank()) nameSingular = displayName;
        if (namePlural == null || namePlural.isBlank()) namePlural = nameSingular;
        if (symbol == null) symbol = "";
        if (format == null || format.isBlank()) format = "{symbol}{amount}";
        if (decimalPlaces < 0) decimalPlaces = 0;
        if (startingBalance < 0) startingBalance = 0.0;
        if (componentId == null || componentId.isBlank()) componentId = "economy:" + id;
    }

    /**
     * Creates a simple currency with minimal configuration.
     */
    public static Currency simple(String id, String displayName, String symbol, double startingBalance) {
        return new Currency(id, displayName, displayName, displayName, symbol,
            "{symbol}{amount}", 2, startingBalance, "economy:" + id);
    }

    /**
     * Formats an amount using this currency's format pattern.
     */
    public String formatAmount(double amount) {
        String formatted = decimalPlaces == 0
            ? String.valueOf((long) amount)
            : String.format("%." + decimalPlaces + "f", amount);
        return format
            .replace("{amount}", formatted)
            .replace("{symbol}", symbol)
            .replace("{name}", amount == 1.0 ? nameSingular : namePlural);
    }
}
```

### CurrencyComponent (ECS)

One component instance per currency per player:

```java
package com.yourplugin.economy;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * ECS component for storing a player's balance in a specific currency.
 * Each currency has its own ComponentType registered dynamically.
 */
public class CurrencyComponent implements Component<EntityStore> {

    private final String currencyId;
    private double balance;

    public CurrencyComponent(String currencyId) {
        this.currencyId = currencyId;
        this.balance = 0.0;
    }

    public CurrencyComponent(String currencyId, double initialBalance) {
        this.currencyId = currencyId;
        this.balance = Math.max(0.0, initialBalance);
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = Math.max(0.0, balance);
    }

    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        this.balance += amount;
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) return false;
        this.balance -= amount;
        return true;
    }

    public boolean has(double amount) {
        return balance >= amount;
    }

    @Override
    public CurrencyComponent clone() {
        return new CurrencyComponent(currencyId, balance);
    }
}
```

### CurrencyManager

Static manager for currency definitions and ECS ComponentTypes:

```java
package com.yourplugin.economy;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages currency definitions and their ECS ComponentTypes.
 */
public final class CurrencyManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // Currency ID -> Currency metadata
    private static final Map<String, Currency> currencies = new ConcurrentHashMap<>();

    // Currency ID -> ComponentType for ECS
    private static final Map<String, ComponentType<EntityStore, CurrencyComponent>> componentTypes = new ConcurrentHashMap<>();

    // Default currency ID
    private static volatile String defaultCurrencyId = null;

    private CurrencyManager() {}

    // ==================== Registration ====================

    /**
     * Registers a currency and creates its ComponentType.
     */
    public static void register(Currency currency) {
        if (currencies.containsKey(currency.id())) {
            throw new IllegalArgumentException("Currency '" + currency.id() + "' already registered");
        }

        currencies.put(currency.id(), currency);
        registerComponentType(currency);

        LOGGER.atInfo().log("Registered currency: " + currency.id());

        if (defaultCurrencyId == null) {
            defaultCurrencyId = currency.id();
        }
    }

    /**
     * Unregisters a currency.
     */
    public static void unregister(String currencyId) {
        currencies.remove(currencyId);
        componentTypes.remove(currencyId);

        if (currencyId.equals(defaultCurrencyId)) {
            defaultCurrencyId = currencies.isEmpty() ? null : currencies.keySet().iterator().next();
        }
    }

    /**
     * Sets the default currency.
     */
    public static void setDefaultCurrency(String currencyId) {
        if (!currencies.containsKey(currencyId)) {
            throw new IllegalArgumentException("Currency '" + currencyId + "' not registered");
        }
        defaultCurrencyId = currencyId;
    }

    @SuppressWarnings("unchecked")
    private static void registerComponentType(Currency currency) {
        try {
            Constructor<ComponentType> ctor = ComponentType.class.getDeclaredConstructor(Class.class, String.class);
            ctor.setAccessible(true);
            ComponentType<EntityStore, CurrencyComponent> type = ctor.newInstance(
                CurrencyComponent.class,
                currency.componentId()
            );
            componentTypes.put(currency.id(), type);
        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to register ComponentType for " + currency.id() + ": " + e.getMessage());
        }
    }

    /**
     * Clears all currencies. Call on shutdown.
     */
    public static void clear() {
        currencies.clear();
        componentTypes.clear();
        defaultCurrencyId = null;
    }

    // ==================== Queries ====================

    public static Set<String> getCurrencyIds() {
        return Collections.unmodifiableSet(currencies.keySet());
    }

    public static Currency getCurrency(String currencyId) {
        return currencies.get(currencyId);
    }

    public static Currency getDefaultCurrency() {
        return defaultCurrencyId != null ? currencies.get(defaultCurrencyId) : null;
    }

    public static String getDefaultCurrencyId() {
        return defaultCurrencyId;
    }

    public static ComponentType<EntityStore, CurrencyComponent> getComponentType(String currencyId) {
        return componentTypes.get(currencyId);
    }

    public static boolean hasCurrency(String currencyId) {
        return currencies.containsKey(currencyId);
    }

    // ==================== Player Operations ====================

    /**
     * Initializes all currency components for a new player.
     */
    public static void initializePlayer(PlayerRef player, EntityStore store) {
        for (Currency currency : currencies.values()) {
            ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currency.id());
            if (type != null) {
                CurrencyComponent existing = store.get(player, type);
                if (existing == null) {
                    CurrencyComponent comp = new CurrencyComponent(currency.id(), currency.startingBalance());
                    store.add(player, type, comp);
                }
            }
        }
    }

    /**
     * Gets a player's balance for a currency.
     */
    public static double getBalance(PlayerRef player, String currencyId, EntityStore store) {
        ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currencyId);
        if (type == null) return 0.0;
        CurrencyComponent comp = store.get(player, type);
        return comp != null ? comp.getBalance() : 0.0;
    }

    /**
     * Sets a player's balance for a currency.
     */
    public static boolean setBalance(PlayerRef player, String currencyId, double amount, EntityStore store) {
        ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currencyId);
        if (type == null) return false;

        store.modify(player, type, comp -> comp.setBalance(amount));
        return true;
    }

    /**
     * Deposits into a player's balance.
     */
    public static boolean deposit(PlayerRef player, String currencyId, double amount, EntityStore store) {
        if (amount <= 0) return false;
        ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currencyId);
        if (type == null) return false;

        store.modify(player, type, comp -> comp.deposit(amount));
        return true;
    }

    /**
     * Withdraws from a player's balance.
     */
    public static boolean withdraw(PlayerRef player, String currencyId, double amount, EntityStore store) {
        if (amount <= 0) return false;
        ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currencyId);
        if (type == null) return false;

        final boolean[] success = {false};
        store.modify(player, type, comp -> {
            success[0] = comp.withdraw(amount);
        });
        return success[0];
    }

    /**
     * Checks if player has enough of a currency.
     */
    public static boolean has(PlayerRef player, String currencyId, double amount, EntityStore store) {
        return getBalance(player, currencyId, store) >= amount;
    }

    /**
     * Formats an amount for display.
     */
    public static String format(String currencyId, double amount) {
        Currency currency = currencies.get(currencyId);
        return currency != null ? currency.formatAmount(amount) : String.format("%.2f", amount);
    }
}
```

---

## Economy Interface

Standard interface for economy providers (similar to Vault pattern):

```java
package com.yourplugin.economy;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.Set;
import java.util.UUID;

/**
 * Economy provider interface.
 */
public interface Economy {

    String getName();
    boolean isEnabled();

    // Currency Registry
    Set<String> getCurrencies();
    String getDefaultCurrency();
    Currency getCurrency(String currencyId);

    // Currency Info
    String currencyNameSingular(String currencyId);
    String currencyNamePlural(String currencyId);
    String format(String currencyId, double amount);
    int fractionalDigits(String currencyId);

    // Default currency convenience
    default String currencyNameSingular() { return currencyNameSingular(getDefaultCurrency()); }
    default String currencyNamePlural() { return currencyNamePlural(getDefaultCurrency()); }
    default String format(double amount) { return format(getDefaultCurrency(), amount); }
    default int fractionalDigits() { return fractionalDigits(getDefaultCurrency()); }

    // Account Methods
    boolean hasAccount(PlayerRef player);
    boolean hasAccount(UUID uuid);
    boolean createPlayerAccount(PlayerRef player);

    // Balance Methods (with currencyId)
    double getBalance(PlayerRef player, String currencyId);
    double getBalance(UUID uuid, String currencyId);
    boolean has(PlayerRef player, String currencyId, double amount);

    // Balance Methods (default currency)
    default double getBalance(PlayerRef player) { return getBalance(player, getDefaultCurrency()); }
    default double getBalance(UUID uuid) { return getBalance(uuid, getDefaultCurrency()); }
    default boolean has(PlayerRef player, double amount) { return has(player, getDefaultCurrency(), amount); }

    // Transaction Methods (with currencyId)
    EconomyResponse withdraw(PlayerRef player, String currencyId, double amount);
    EconomyResponse deposit(PlayerRef player, String currencyId, double amount);
    EconomyResponse setBalance(PlayerRef player, String currencyId, double amount);

    default EconomyResponse transfer(PlayerRef from, PlayerRef to, String currencyId, double amount) {
        EconomyResponse withdrawResp = withdraw(from, currencyId, amount);
        if (!withdrawResp.isSuccess()) return withdrawResp;

        EconomyResponse depositResp = deposit(to, currencyId, amount);
        if (!depositResp.isSuccess()) {
            deposit(from, currencyId, amount); // Rollback
            return depositResp;
        }
        return EconomyResponse.success(amount, getBalance(from, currencyId), "Transfer successful");
    }

    // Transaction Methods (default currency)
    default EconomyResponse withdraw(PlayerRef player, double amount) {
        return withdraw(player, getDefaultCurrency(), amount);
    }
    default EconomyResponse deposit(PlayerRef player, double amount) {
        return deposit(player, getDefaultCurrency(), amount);
    }
    default EconomyResponse setBalance(PlayerRef player, double amount) {
        return setBalance(player, getDefaultCurrency(), amount);
    }
    default EconomyResponse transfer(PlayerRef from, PlayerRef to, double amount) {
        return transfer(from, to, getDefaultCurrency(), amount);
    }
}
```

---

## EconomyResponse

Transaction result object:

```java
package com.yourplugin.economy;

public class EconomyResponse {

    public enum ResponseType { SUCCESS, FAILURE, NOT_IMPLEMENTED }

    private final double amount;
    private final double balance;
    private final ResponseType type;
    private final String errorMessage;

    public EconomyResponse(double amount, double balance, ResponseType type, String errorMessage) {
        this.amount = amount;
        this.balance = balance;
        this.type = type;
        this.errorMessage = errorMessage;
    }

    public double getAmount() { return amount; }
    public double getBalance() { return balance; }
    public ResponseType getType() { return type; }
    public String getErrorMessage() { return errorMessage; }

    public boolean isSuccess() { return type == ResponseType.SUCCESS; }
    public boolean isFailure() { return type == ResponseType.FAILURE; }

    // Factory methods
    public static EconomyResponse success(double amount, double balance) {
        return new EconomyResponse(amount, balance, ResponseType.SUCCESS, null);
    }

    public static EconomyResponse success(double amount, double balance, String message) {
        return new EconomyResponse(amount, balance, ResponseType.SUCCESS, message);
    }

    public static EconomyResponse failure(String errorMessage) {
        return new EconomyResponse(0, 0, ResponseType.FAILURE, errorMessage);
    }

    public static EconomyResponse failure(double balance, String errorMessage) {
        return new EconomyResponse(0, balance, ResponseType.FAILURE, errorMessage);
    }

    public static EconomyResponse notImplemented(String feature) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, feature + " not supported");
    }
}
```

---

## Transaction Helper

Simple transaction result for internal use:

```java
package com.yourplugin.economy;

public class Transaction {
    private final boolean success;
    private final String failureReason;
    private final double amount;

    private Transaction(boolean success, String failureReason, double amount) {
        this.success = success;
        this.failureReason = failureReason;
        this.amount = amount;
    }

    public static Transaction success(double amount) {
        return new Transaction(true, null, amount);
    }

    public static Transaction fail(String reason) {
        return new Transaction(false, reason, 0);
    }

    public boolean isSuccessful() { return success; }
    public String getFailureReason() { return failureReason; }
    public double getAmount() { return amount; }
}
```

---

## Currency-Specific Systems (ECS)

Example systems that leverage ECS queries:

### Interest System

```java
package com.yourplugin.economy.systems;

import com.hypixel.hytale.ecs.EntityTickingSystem;
import com.hypixel.hytale.ecs.Query;
import com.hypixel.hytale.server.core.universe.EntityRef;
import com.yourplugin.economy.CurrencyComponent;
import com.yourplugin.economy.CurrencyManager;

/**
 * Adds interest to savings accounts every tick.
 */
public class InterestSystem extends EntityTickingSystem {

    private static final double INTEREST_RATE = 0.001; // 0.1% per tick
    private static final double MIN_BALANCE_FOR_INTEREST = 1000.0;

    @Override
    protected Query getQuery() {
        // Only process entities that have the "savings" currency component
        return Query.select(CurrencyManager.getComponentType("savings"));
    }

    @Override
    protected void tick(EntityRef ref, float dt) {
        getStore().modify(ref, CurrencyManager.getComponentType("savings"), comp -> {
            if (comp.getBalance() >= MIN_BALANCE_FOR_INTEREST) {
                double interest = comp.getBalance() * INTEREST_RATE * dt;
                comp.deposit(interest);
            }
        });
    }
}
```

### Gold Decay System

```java
package com.yourplugin.economy.systems;

import com.hypixel.hytale.ecs.EntityTickingSystem;
import com.hypixel.hytale.ecs.Query;
import com.hypixel.hytale.server.core.universe.EntityRef;
import com.yourplugin.economy.CurrencyManager;

/**
 * Applies inflation/decay to gold over time.
 */
public class GoldDecaySystem extends EntityTickingSystem {

    private static final double DECAY_RATE = 0.0001; // 0.01% per tick

    @Override
    protected Query getQuery() {
        return Query.select(CurrencyManager.getComponentType("gold"));
    }

    @Override
    protected void tick(EntityRef ref, float dt) {
        getStore().modify(ref, CurrencyManager.getComponentType("gold"), comp -> {
            double decay = comp.getBalance() * DECAY_RATE * dt;
            comp.withdraw(decay);
        });
    }
}
```

### Rich Players Query

```java
// Find all players with more than 10000 gold
public List<PlayerRef> findRichPlayers(EntityStore store) {
    List<PlayerRef> richPlayers = new ArrayList<>();

    Query query = Query.select(CurrencyManager.getComponentType("gold"));
    store.forEach(query, (ref, components) -> {
        CurrencyComponent gold = components.get(CurrencyManager.getComponentType("gold"));
        if (gold != null && gold.getBalance() > 10000) {
            if (ref instanceof PlayerRef playerRef) {
                richPlayers.add(playerRef);
            }
        }
    });

    return richPlayers;
}
```

---

## Service Provider Pattern

If you want other plugins to hook into your economy:

```java
package com.yourplugin.economy;

import com.hypixel.hytale.server.core.plugin.PluginBase;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ServiceManager {

    public enum ServicePriority { LOWEST, LOW, NORMAL, HIGH, HIGHEST }

    private static final Map<Class<?>, TreeSet<ServiceProvider<?>>> providers = new ConcurrentHashMap<>();

    public static <T> void register(Class<T> serviceClass, PluginBase plugin, T provider, ServicePriority priority) {
        ServiceProvider<T> sp = new ServiceProvider<>(plugin, provider, priority);
        providers.computeIfAbsent(serviceClass, k -> new TreeSet<>(
            Comparator.comparingInt((ServiceProvider<?> a) -> -a.priority().ordinal())
                      .thenComparing(a -> a.plugin().getIdentifier().getName())
        )).add(sp);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getProvider(Class<T> serviceClass) {
        TreeSet<ServiceProvider<?>> set = providers.get(serviceClass);
        if (set == null || set.isEmpty()) return null;
        return (T) set.first().provider();
    }

    public static void unregisterAll(PluginBase plugin) {
        providers.values().forEach(set -> set.removeIf(sp -> sp.plugin().equals(plugin)));
    }

    public record ServiceProvider<T>(PluginBase plugin, T provider, ServicePriority priority) {}
}
```

Usage in your economy plugin:

```java
@Override
public void start() {
    // Register currencies
    CurrencyManager.register(Currency.simple("gold", "Gold", "G", 100.0));
    CurrencyManager.register(Currency.simple("gems", "Gems", "💎", 0.0));
    CurrencyManager.setDefaultCurrency("gold");

    // Register as economy provider
    MyEconomy econ = new MyEconomy();
    ServiceManager.register(Economy.class, this, econ, ServiceManager.ServicePriority.NORMAL);
}

@Override
public void shutdown() {
    ServiceManager.unregisterAll(this);
    CurrencyManager.clear();
}
```

Other plugins can then use:

```java
Economy econ = ServiceManager.getProvider(Economy.class);
if (econ != null) {
    econ.deposit(player, "gold", 100.0);
}
```

---

## Player Join Listener

Initialize wallets on player join:

```java
package com.yourplugin.economy;

import com.hypixel.hytale.server.core.event.events.PlayerJoinEvent;
import com.hypixel.hytale.server.core.event.EventListener;
import com.hypixel.hytale.server.core.plugin.PluginBase;

public class EconomyListener implements EventListener<PlayerJoinEvent> {

    private final PluginBase plugin;

    public EconomyListener(PluginBase plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getEventManager().register(PlayerJoinEvent.class, this);
    }

    public void unregister() {
        plugin.getEventManager().unregister(PlayerJoinEvent.class, this);
    }

    @Override
    public void onEvent(PlayerJoinEvent event) {
        PlayerRef player = event.getPlayer().getPlayerRef();
        EntityStore store = event.getPlayer().getWorld().getEntityStore();

        // Initialize all currencies for this player
        CurrencyManager.initializePlayer(player, store);
    }
}
```

---

## Summary

This document provides a complete economy module reference. Copy the relevant classes into your economy plugin and adapt as needed.

**Key files to create in your economy plugin:**
- `Currency.java` - Currency metadata record
- `CurrencyComponent.java` - ECS component for balances
- `CurrencyManager.java` - Static currency registry
- `Economy.java` - Provider interface
- `EconomyResponse.java` - Transaction results
- `EconomyListener.java` - Player join initialization
- (Optional) `ServiceManager.java` - For plugin hooks
- (Optional) Currency-specific systems

For utilities like `ColorMan` and `LocaleMan`, depend on KuksoHyLib.

KuksoConfig config = new KuksoConfig();                                                                                                                                                                         
116 -                                                                                                                                                                                                                        
117 -        // Add default "coins" currency                                                                                                                                                                                 
118 -        CurrencyConfig coins = new CurrencyConfig();                                                                                                                                                                    
119 -        coins.id = "coins";                                                                                                                                                                                             
120 -        coins.displayName = "Coins";                                                                                                                                                                                    
121 -        coins.nameSingular = "Coin";                                                                                                                                                                                    
122 -        coins.namePlural = "Coins";                                                                                                                                                                                     
123 -        coins.symbol = "$";                                                                                                                                                                                             
124 -        coins.format = "{symbol}{amount}";                                                                                                                                                                              
125 -        coins.decimalPlaces = 2;                                                                                                                                                                                        
126 -        coins.startingBalance = 100.0;                                                                                                                                                                                  
127 -        coins.componentId = "kuksolib:coins";                                                                                                                                                                           
128 -        config.economy.currencies.add(coins);                                                                                                                                                                           
129 -                                                                                                                                                                                                                        
130 -        return config; 

"economy": {
"enabled": true,
"defaultCurrency": "coins",
"currencies": [
{
"id": "coins",
"displayName": "Coins",
"nameSingular": "Coin",
"namePlural": "Coins",
"symbol": "$",
"format": "{symbol}{amount}",
"decimalPlaces": 2,
"startingBalance": 100.0,
"componentId": "kuksohylib:coins"
}
],
"transactionLogging": true
},