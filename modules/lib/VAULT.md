# VAULT.md - KuksoLib Service Provider API

This document outlines the design and implementation plan for a **Vault-like abstraction layer** in KuksoLib for Hytale. Similar to how Vault provides unified APIs for Minecraft plugins, KuksoLib will provide standardized interfaces for Economy, Permissions, and Chat systems.

## Overview

### What is Vault?

Vault is a Minecraft plugin that provides abstraction APIs for:
- **Economy** - Currency management, transactions, bank accounts
- **Permissions** - Permission node checking, group management
- **Chat** - Prefix/suffix management, chat formatting

Plugins depend on Vault's API instead of individual economy/permission plugins, allowing server admins to swap backends without breaking functionality.

### KuksoLib's Goal

KuksoLib will provide the same abstraction layer for Hytale:
- **Single dependency** - Plugins only depend on KuksoLib
- **Swappable backends** - Server admins choose their preferred economy/permission/chat plugins
- **Standardized API** - Consistent method signatures across all implementations
- **Response objects** - Rich feedback for transactions and operations
- **Library-only** - Provides API, database, and ECS infrastructure; user-facing commands (e.g., `/balance`, `/pay`) are implemented by separate plugins

---

## Architecture

### Service Provider Pattern (Lazy Activation)

```
┌─────────────────────────────────────────────────────────────────┐
│                        YOUR PLUGIN                              │
│                   (depends on KuksoLib API)                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         KUKSOLIB                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ Economy API │  │Permission API│ │   Chat API  │              │
│  │  (DORMANT)  │  │  (DORMANT)  │  │  (DORMANT)  │              │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
│         │                │                │                     │
│         ▼                ▼                ▼                     │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │              ServiceManager (Registry)                      ││
│  │   - Lazy module activation on first registration            ││
│  │   - Registers provider implementations                      ││
│  │   - Priority-based provider selection                       ││
│  │   - Runtime provider switching                              ││
│  └─────────────────────────────────────────────────────────────┘│
│                                                                 │
│  Activation triggers:                                           │
│  ServiceManager.registerEconomy()    → Activates Economy module │
│  ServiceManager.registerPermission() → Activates Permission     │
│  ServiceManager.registerChat()       → Activates Chat module    │
└─────────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│  MyEconomy      │ │   LuckPerms     │ │   ChatManager   │
│  (External)     │ │   (External)    │ │   (External)    │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

**Lazy Activation Benefits:**
- Zero overhead if no plugins use economy/permission/chat
- Faster startup for servers that only need some features
- Lower memory footprint
- Cleaner logs (no "Economy module loaded" if not used)

### Package Structure

```
com.kukso.hy.lib/
├── economy/
│   ├── Economy.java              # Main economy interface (multi-currency)
│   ├── EconomyResponse.java      # Transaction response object
│   ├── ResponseType.java         # SUCCESS, FAILURE, NOT_IMPLEMENTED
│   ├── Currency.java             # Currency metadata record
│   ├── CurrencyComponent.java    # ECS component for player balances
│   ├── CurrencyManager.java      # Currency registration & ECS management
│   └── impl/
│       └── KuksoEconomy.java     # Built-in SQLite/MySQL implementation
│
├── permission/
│   ├── Permission.java           # Main permission interface
│   └── impl/
│       └── DefaultPermission.java  # Fallback using Hytale's native system
│
├── chat/
│   ├── Chat.java                 # Main chat interface
│   └── impl/
│       └── DefaultChat.java      # Fallback using ColorMan
│
└── service/
    ├── ServiceManager.java       # Central registry for all services
    ├── ServicePriority.java      # Priority enum (Lowest, Low, Normal, High, Highest)
    └── ServiceProvider.java      # Wrapper class for registered providers
```

---

## Economy API

### Currency Record

```java
package com.kukso.hy.lib.economy;

/**
 * Represents currency metadata defined by economy plugins.
 * Economy plugins register their currencies via CurrencyManager.register().
 * Immutable record containing all configuration for a single currency type.
 */
public record Currency(
    String id,              // "gold" - internal identifier
    String displayName,     // "Gold" - display name for UI
    String nameSingular,    // "Gold" - singular form
    String namePlural,      // "Gold" - plural form
    String symbol,          // "G" - currency symbol
    String format,          // "{amount}{symbol}" - format pattern
    int decimalPlaces,      // 0 - number of decimal places
    double startingBalance, // 100.0 - initial balance for new players
    String componentId      // "kuksolib:gold" - ECS ComponentType identifier
) {
    /**
     * Formats an amount using this currency's format pattern.
     * @param amount Amount to format
     * @return Formatted string (e.g., "100G" or "50 💎")
     */
    public String formatAmount(double amount) {
        String formatted = decimalPlaces == 0
            ? String.valueOf((long) amount)
            : String.format("%." + decimalPlaces + "f", amount);
        return format
            .replace("{amount}", formatted)
            .replace("{symbol}", symbol);
    }

    /**
     * Gets the appropriate name form based on amount.
     * @param amount Amount to check
     * @return Singular or plural name
     */
    public String getName(double amount) {
        return amount == 1.0 ? nameSingular : namePlural;
    }
}
```

### Interface Definition

```java
package com.kukso.hy.lib.economy;

import hytale.server.world.entity.player.Player;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The main Economy interface that all economy providers must implement.
 * Supports multiple currencies with a default currency for convenience.
 * Plugins should use ServiceManager.getEconomy() to obtain an instance.
 */
public interface Economy {

    // ==================== Provider Info ====================

    /**
     * Gets the name of the economy provider.
     * @return Name of the economy provider
     */
    String getName();

    /**
     * Checks if this economy provider is enabled and ready.
     * @return true if the economy provider is enabled
     */
    boolean isEnabled();

    // ==================== Currency Registry ====================

    /**
     * Gets all registered currency IDs.
     * @return Set of currency IDs (e.g., {"gold", "gems", "coins"})
     */
    Set<String> getCurrencies();

    /**
     * Gets the default currency ID.
     * @return Default currency ID (e.g., "gold")
     */
    String getDefaultCurrency();

    /**
     * Gets currency metadata by ID.
     * @param currencyId Currency identifier
     * @return Currency info or null if not found
     */
    Currency getCurrency(String currencyId);

    // ==================== Currency Info (with currencyId) ====================

    /**
     * Gets the name of a currency in singular form.
     * @param currencyId Currency identifier
     * @return Currency name singular (e.g., "Gold", "Gem", "Coin")
     */
    String currencyNameSingular(String currencyId);

    /**
     * Gets the name of a currency in plural form.
     * @param currencyId Currency identifier
     * @return Currency name plural (e.g., "Gold", "Gems", "Coins")
     */
    String currencyNamePlural(String currencyId);

    /**
     * Format an amount into a human-readable string for a specific currency.
     * @param currencyId Currency identifier
     * @param amount Amount to format
     * @return Formatted string (e.g., "100G", "50 💎", "$1,234.56")
     */
    String format(String currencyId, double amount);

    /**
     * Returns the number of fractional digits supported by a currency.
     * @param currencyId Currency identifier
     * @return Number of digits after decimal (0 for whole numbers only)
     */
    int fractionalDigits(String currencyId);

    // ==================== Currency Info (default currency convenience) ====================

    /**
     * Gets the name of the default currency in singular form.
     * @return Currency name singular
     */
    default String currencyNameSingular() {
        return currencyNameSingular(getDefaultCurrency());
    }

    /**
     * Gets the name of the default currency in plural form.
     * @return Currency name plural
     */
    default String currencyNamePlural() {
        return currencyNamePlural(getDefaultCurrency());
    }

    /**
     * Format an amount using the default currency.
     * @param amount Amount to format
     * @return Formatted string
     */
    default String format(double amount) {
        return format(getDefaultCurrency(), amount);
    }

    /**
     * Returns the number of fractional digits for the default currency.
     * @return Number of digits after decimal
     */
    default int fractionalDigits() {
        return fractionalDigits(getDefaultCurrency());
    }

    // ==================== Account Methods ====================

    /**
     * Checks if a player has an account.
     * @param player Player to check
     * @return true if player has an account
     */
    boolean hasAccount(Player player);

    /**
     * Checks if a player has an account by UUID (for offline players).
     * @param uuid Player UUID
     * @return true if player has an account
     */
    boolean hasAccount(UUID uuid);

    /**
     * Creates a player account if one doesn't exist.
     * Initializes all currencies with their starting balances.
     * @param player Player to create account for
     * @return true if account was created, false if already exists
     */
    boolean createPlayerAccount(Player player);

    // ==================== Balance Methods (with currencyId) ====================

    /**
     * Gets the balance of a player for a specific currency.
     * @param player Player to check
     * @param currencyId Currency identifier
     * @return Current balance
     */
    double getBalance(Player player, String currencyId);

    /**
     * Gets the balance of a player by UUID for a specific currency.
     * @param uuid Player UUID
     * @param currencyId Currency identifier
     * @return Current balance
     */
    double getBalance(UUID uuid, String currencyId);

    /**
     * Checks if a player has at least the specified amount of a currency.
     * @param player Player to check
     * @param currencyId Currency identifier
     * @param amount Amount to check
     * @return true if player has at least that amount
     */
    boolean has(Player player, String currencyId, double amount);

    // ==================== Balance Methods (default currency convenience) ====================

    /**
     * Gets the balance of a player for the default currency.
     * @param player Player to check
     * @return Current balance
     */
    default double getBalance(Player player) {
        return getBalance(player, getDefaultCurrency());
    }

    /**
     * Gets the balance of a player by UUID for the default currency.
     * @param uuid Player UUID
     * @return Current balance
     */
    default double getBalance(UUID uuid) {
        return getBalance(uuid, getDefaultCurrency());
    }

    /**
     * Checks if a player has at least the specified amount of the default currency.
     * @param player Player to check
     * @param amount Amount to check
     * @return true if player has at least that amount
     */
    default boolean has(Player player, double amount) {
        return has(player, getDefaultCurrency(), amount);
    }

    // ==================== Transaction Methods (with currencyId) ====================

    /**
     * Withdraws an amount of a specific currency from a player's account.
     * @param player Player to withdraw from
     * @param currencyId Currency identifier
     * @param amount Amount to withdraw (must be positive)
     * @return EconomyResponse with transaction result
     */
    EconomyResponse withdraw(Player player, String currencyId, double amount);

    /**
     * Deposits an amount of a specific currency into a player's account.
     * @param player Player to deposit to
     * @param currencyId Currency identifier
     * @param amount Amount to deposit (must be positive)
     * @return EconomyResponse with transaction result
     */
    EconomyResponse deposit(Player player, String currencyId, double amount);

    /**
     * Transfers a specific currency from one player to another.
     * @param from Source player
     * @param to Destination player
     * @param currencyId Currency identifier
     * @param amount Amount to transfer
     * @return EconomyResponse with transaction result
     */
    default EconomyResponse transfer(Player from, Player to, String currencyId, double amount) {
        EconomyResponse withdrawResponse = withdraw(from, currencyId, amount);
        if (!withdrawResponse.isSuccess()) {
            return withdrawResponse;
        }
        EconomyResponse depositResponse = deposit(to, currencyId, amount);
        if (!depositResponse.isSuccess()) {
            // Rollback the withdrawal
            deposit(from, currencyId, amount);
            return depositResponse;
        }
        return new EconomyResponse(
            amount,
            getBalance(from, currencyId),
            ResponseType.SUCCESS,
            "Transfer successful"
        );
    }

    // ==================== Transaction Methods (default currency convenience) ====================

    /**
     * Withdraws an amount of the default currency from a player's account.
     * @param player Player to withdraw from
     * @param amount Amount to withdraw (must be positive)
     * @return EconomyResponse with transaction result
     */
    default EconomyResponse withdraw(Player player, double amount) {
        return withdraw(player, getDefaultCurrency(), amount);
    }

    /**
     * Deposits an amount of the default currency into a player's account.
     * @param player Player to deposit to
     * @param amount Amount to deposit (must be positive)
     * @return EconomyResponse with transaction result
     */
    default EconomyResponse deposit(Player player, double amount) {
        return deposit(player, getDefaultCurrency(), amount);
    }

    /**
     * Transfers the default currency from one player to another.
     * @param from Source player
     * @param to Destination player
     * @param amount Amount to transfer
     * @return EconomyResponse with transaction result
     */
    default EconomyResponse transfer(Player from, Player to, double amount) {
        return transfer(from, to, getDefaultCurrency(), amount);
    }

    // ==================== Bank Methods (Optional, with currencyId) ====================

    /**
     * Checks if this economy supports banks.
     * @return true if banks are supported
     */
    default boolean hasBankSupport() {
        return false;
    }

    /**
     * Creates a bank with the given name.
     * @param name Bank name
     * @param owner Owner player
     * @return EconomyResponse with result
     */
    default EconomyResponse createBank(String name, Player owner) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    /**
     * Deletes a bank.
     * @param name Bank name
     * @return EconomyResponse with result
     */
    default EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    /**
     * Gets the balance of a bank for a specific currency.
     * @param name Bank name
     * @param currencyId Currency identifier
     * @return EconomyResponse with balance
     */
    default EconomyResponse bankBalance(String name, String currencyId) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    /**
     * Checks if a bank exists.
     * @param name Bank name
     * @return true if bank exists
     */
    default boolean bankExists(String name) {
        return false;
    }

    /**
     * Withdraws a specific currency from a bank.
     * @param name Bank name
     * @param currencyId Currency identifier
     * @param amount Amount to withdraw
     * @return EconomyResponse with result
     */
    default EconomyResponse bankWithdraw(String name, String currencyId, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    /**
     * Deposits a specific currency to a bank.
     * @param name Bank name
     * @param currencyId Currency identifier
     * @param amount Amount to deposit
     * @return EconomyResponse with result
     */
    default EconomyResponse bankDeposit(String name, String currencyId, double amount) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }

    /**
     * Checks if a player is the owner of a bank.
     * @param name Bank name
     * @param player Player to check
     * @return true if player is the bank owner
     */
    default boolean isBankOwner(String name, Player player) {
        return false;
    }

    /**
     * Checks if a player is a member of a bank.
     * @param name Bank name
     * @param player Player to check
     * @return true if player is a bank member
     */
    default boolean isBankMember(String name, Player player) {
        return false;
    }

    /**
     * Gets list of all banks.
     * @return List of bank names
     */
    default List<String> getBanks() {
        return List.of();
    }
}
```

### Response Object

```java
package com.kukso.hy.lib.economy;

/**
 * Represents the result of an economy transaction.
 */
public class EconomyResponse {
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

    /**
     * Amount that was transacted.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * New balance after transaction.
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Response type (SUCCESS, FAILURE, NOT_IMPLEMENTED).
     */
    public ResponseType getType() {
        return type;
    }

    /**
     * Error message if transaction failed.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Convenience method to check if transaction was successful.
     */
    public boolean isSuccess() {
        return type == ResponseType.SUCCESS;
    }
}

/**
 * Response type enumeration.
 */
public enum ResponseType {
    SUCCESS,           // Transaction completed successfully
    FAILURE,           // Transaction failed (insufficient funds, etc.)
    NOT_IMPLEMENTED    // Feature not supported by provider
}
```

### CurrencyComponent (ECS Component)

```java
package com.kukso.hy.lib.economy;

import hytale.server.ecs.Component;
import hytale.server.ecs.EntityStore;

/**
 * ECS component for storing a player's balance in a specific currency.
 * One component instance per currency per player entity.
 *
 * Each currency has its own ComponentType registered dynamically via CurrencyManager.
 * For example:
 *   - ComponentType<EntityStore, CurrencyComponent> for "kuksolib:gold"
 *   - ComponentType<EntityStore, CurrencyComponent> for "kuksolib:gems"
 *
 * The same CurrencyComponent class is reused, but different ComponentType instances
 * allow the ECS to store multiple currency balances per entity.
 */
public class CurrencyComponent implements Component<EntityStore> {

    private double balance;
    private final String currencyId;

    /**
     * Creates a new currency component.
     * @param currencyId The currency identifier (e.g., "gold")
     * @param initialBalance Starting balance (clamped to 0 minimum)
     */
    public CurrencyComponent(String currencyId, double initialBalance) {
        this.currencyId = currencyId;
        this.balance = Math.max(0.0, initialBalance);
    }

    /**
     * Gets the currency identifier.
     * @return Currency ID (e.g., "gold", "gems")
     */
    public String getCurrencyId() {
        return currencyId;
    }

    /**
     * Gets the current balance.
     * @return Current balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Sets the balance directly.
     * @param balance New balance (clamped to 0 minimum)
     */
    public void setBalance(double balance) {
        this.balance = Math.max(0.0, balance);
    }

    /**
     * Deposits an amount into this balance.
     * @param amount Amount to deposit (must be positive)
     * @return true if successful
     */
    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        this.balance += amount;
        return true;
    }

    /**
     * Withdraws an amount from this balance.
     * @param amount Amount to withdraw (must be positive)
     * @return true if successful (sufficient funds)
     */
    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        this.balance -= amount;
        return true;
    }

    /**
     * Checks if this balance has at least the specified amount.
     * @param amount Amount to check
     * @return true if balance >= amount
     */
    public boolean has(double amount) {
        return balance >= amount;
    }

    @Override
    public CurrencyComponent clone() {
        return new CurrencyComponent(currencyId, balance);
    }

    @Override
    public String toString() {
        return "CurrencyComponent{currencyId='" + currencyId + "', balance=" + balance + "}";
    }
}
```

### CurrencyManager

```java
package com.kukso.hy.lib.economy;

import hytale.server.ecs.ComponentType;
import hytale.server.ecs.EntityStore;
import hytale.server.world.entity.player.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages currency definitions and their ECS ComponentTypes.
 * Economy plugins register their currencies via the register() method.
 *
 * Architecture:
 * - Each currency registered gets its own ComponentType<EntityStore, CurrencyComponent>
 * - ComponentType IDs follow the pattern specified in the Currency record (e.g., "myeconomy:gold")
 * - The same CurrencyComponent class is used for all currencies
 * - Multiple ComponentTypes allow players to have separate balances per currency
 *
 * Example usage (in an economy plugin):
 * {@code
 * CurrencyManager.register(new Currency(
 *     "gold", "Gold", "Gold", "Gold", "G", "{amount}{symbol}", 0, 100.0, "myeconomy:gold"
 * ));
 * CurrencyManager.register(new Currency(
 *     "gems", "Gems", "Gem", "Gems", "💎", "{amount} {symbol}", 0, 0.0, "myeconomy:gems"
 * ));
 * CurrencyManager.setDefaultCurrency("gold");
 * }
 */
public class CurrencyManager {

    private static final Logger LOGGER = Logger.getLogger(CurrencyManager.class.getName());

    // Currency ID -> Currency metadata
    private static final Map<String, Currency> currencies = new ConcurrentHashMap<>();

    // Currency ID -> ComponentType for ECS
    private static final Map<String, ComponentType<EntityStore, CurrencyComponent>> componentTypes = new ConcurrentHashMap<>();

    // Default currency ID (set by the economy plugin)
    private static String defaultCurrencyId = null;

    // ==================== Currency Registration API ====================

    /**
     * Registers a currency. Called by economy plugins to define their currencies.
     * This is the primary way currencies are added to the system.
     *
     * @param currency Currency to register
     * @throws IllegalArgumentException if currency with same ID already exists
     */
    public static void register(Currency currency) {
        if (currencies.containsKey(currency.id())) {
            throw new IllegalArgumentException("Currency '" + currency.id() + "' is already registered");
        }

        currencies.put(currency.id(), currency);
        registerComponentType(currency);

        LOGGER.log(Level.INFO, "Registered currency: " + currency.id() +
            " (component: " + currency.componentId() + ")");

        // Set as default if this is the first currency
        if (defaultCurrencyId == null) {
            defaultCurrencyId = currency.id();
            LOGGER.log(Level.INFO, "Set default currency: " + currency.id());
        }
    }

    /**
     * Unregisters a currency. Called when an economy plugin shuts down.
     *
     * @param currencyId Currency ID to unregister
     */
    public static void unregister(String currencyId) {
        currencies.remove(currencyId);
        componentTypes.remove(currencyId);

        // Update default if we removed the default currency
        if (currencyId.equals(defaultCurrencyId)) {
            defaultCurrencyId = currencies.isEmpty() ? null : currencies.keySet().iterator().next();
        }

        LOGGER.log(Level.INFO, "Unregistered currency: " + currencyId);
    }

    /**
     * Sets the default currency ID.
     * Called by the economy plugin after registering currencies.
     *
     * @param currencyId Currency ID to set as default
     * @throws IllegalArgumentException if currency is not registered
     */
    public static void setDefaultCurrency(String currencyId) {
        if (!currencies.containsKey(currencyId)) {
            throw new IllegalArgumentException("Currency '" + currencyId + "' is not registered");
        }
        defaultCurrencyId = currencyId;
        LOGGER.log(Level.INFO, "Default currency set to: " + currencyId);
    }

    /**
     * Clears all registered currencies.
     * Called during shutdown.
     */
    public static void clear() {
        currencies.clear();
        componentTypes.clear();
        defaultCurrencyId = null;
    }

    /**
     * Registers a ComponentType for a currency with the ECS system.
     */
    private static void registerComponentType(Currency currency) {
        // Create ComponentType with the currency's componentId (e.g., "myeconomy:gold")
        ComponentType<EntityStore, CurrencyComponent> componentType =
            ComponentType.create(currency.componentId(), CurrencyComponent.class);

        componentTypes.put(currency.id(), componentType);
    }

    // ==================== Query API ====================

    /**
     * Gets all registered currency IDs.
     * @return Unmodifiable set of currency IDs
     */
    public static Set<String> getCurrencyIds() {
        return Collections.unmodifiableSet(currencies.keySet());
    }

    /**
     * Gets the default currency ID.
     * @return Default currency ID, or null if no currencies registered
     */
    public static String getDefaultCurrencyId() {
        return defaultCurrencyId;
    }

    /**
     * Gets currency metadata by ID.
     * @param currencyId Currency identifier
     * @return Currency or null if not found
     */
    public static Currency getCurrency(String currencyId) {
        return currencies.get(currencyId);
    }

    /**
     * Checks if any currencies are registered.
     * @return true if at least one currency is registered
     */
    public static boolean hasCurrencies() {
        return !currencies.isEmpty();
    }

    /**
     * Gets the ComponentType for a currency.
     * Used to access the currency component on player entities.
     *
     * @param currencyId Currency identifier
     * @return ComponentType or null if not found
     */
    public static ComponentType<EntityStore, CurrencyComponent> getComponentType(String currencyId) {
        return componentTypes.get(currencyId);
    }

    // ==================== Player Management ====================

    /**
     * Initializes all currency components for a player entity.
     * Called when a player joins or account is created.
     *
     * @param player Player to initialize
     */
    public static void initializePlayer(Player player) {
        EntityStore store = player.getEntityStore();
        for (Currency currency : currencies.values()) {
            ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currency.id());
            if (type != null && !store.hasComponent(type)) {
                CurrencyComponent component = new CurrencyComponent(
                    currency.id(),
                    currency.startingBalance()
                );
                store.addComponent(type, component);
            }
        }
    }

    /**
     * Gets the ECS balance of a specific currency for a player.
     * This reads directly from the ECS component.
     *
     * @param player Player to check
     * @param currencyId Currency identifier
     * @return Balance or 0.0 if not found
     */
    public static double getEcsBalance(Player player, String currencyId) {
        ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currencyId);
        if (type == null) {
            return 0.0;
        }
        CurrencyComponent component = player.getEntityStore().getComponent(type);
        return component != null ? component.getBalance() : 0.0;
    }

    /**
     * Modifies the ECS balance of a specific currency for a player.
     *
     * @param player Player to modify
     * @param currencyId Currency identifier
     * @param amount Amount to add (positive) or subtract (negative)
     * @return true if successful
     */
    public static boolean modifyEcsBalance(Player player, String currencyId, double amount) {
        ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currencyId);
        if (type == null) {
            return false;
        }
        CurrencyComponent component = player.getEntityStore().getComponent(type);
        if (component == null) {
            return false;
        }
        if (amount >= 0) {
            return component.deposit(amount);
        } else {
            return component.withdraw(-amount);
        }
    }
}
```

---

## Permission API

### Interface Definition

```java
package com.kukso.hy.lib.permission;

import hytale.server.world.entity.player.Player;
import java.util.List;
import java.util.UUID;

/**
 * The main Permission interface for permission providers.
 * Plugins should use ServiceManager.getPermission() to obtain an instance.
 */
public interface Permission {

    // ==================== Provider Info ====================

    /**
     * Gets the name of the permission provider.
     * @return Name of the permission provider
     */
    String getName();

    /**
     * Checks if this permission provider is enabled.
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Checks if this provider supports groups.
     * @return true if groups are supported
     */
    boolean hasGroupSupport();

    // ==================== Permission Check ====================

    /**
     * Checks if a player has a permission node.
     * @param player Player to check
     * @param permission Permission node
     * @return true if player has the permission
     */
    boolean has(Player player, String permission);

    /**
     * Checks if an offline player has a permission node.
     * @param uuid Player UUID
     * @param permission Permission node
     * @return true if player has the permission
     */
    boolean has(UUID uuid, String permission);

    /**
     * Checks if a player has a permission in a specific dimension.
     * @param player Player to check
     * @param dimension Dimension name
     * @param permission Permission node
     * @return true if player has the permission in that dimension
     */
    default boolean has(Player player, String dimension, String permission) {
        return has(player, permission);
    }

    // ==================== Permission Add/Remove ====================

    /**
     * Adds a permission to a player.
     * @param player Player to modify
     * @param permission Permission to add
     * @return true if successful
     */
    boolean playerAdd(Player player, String permission);

    /**
     * Removes a permission from a player.
     * @param player Player to modify
     * @param permission Permission to remove
     * @return true if successful
     */
    boolean playerRemove(Player player, String permission);

    /**
     * Adds a temporary permission to a player.
     * @param player Player to modify
     * @param permission Permission to add
     * @param seconds Duration in seconds
     * @return true if successful
     */
    default boolean playerAddTransient(Player player, String permission, long seconds) {
        return false; // Not all providers support this
    }

    // ==================== Group Methods ====================

    /**
     * Gets all groups defined in the permission system.
     * @return Array of group names
     */
    String[] getGroups();

    /**
     * Gets the primary group of a player.
     * @param player Player to check
     * @return Primary group name, or empty string if none
     */
    String getPrimaryGroup(Player player);

    /**
     * Gets all groups a player is in.
     * @param player Player to check
     * @return Array of group names
     */
    String[] getPlayerGroups(Player player);

    /**
     * Checks if a player is in a specific group.
     * @param player Player to check
     * @param group Group name
     * @return true if player is in the group
     */
    boolean playerInGroup(Player player, String group);

    /**
     * Adds a player to a group.
     * @param player Player to add
     * @param group Group name
     * @return true if successful
     */
    boolean playerAddGroup(Player player, String group);

    /**
     * Removes a player from a group.
     * @param player Player to remove
     * @param group Group name
     * @return true if successful
     */
    boolean playerRemoveGroup(Player player, String group);

    // ==================== Group Permission Methods ====================

    /**
     * Checks if a group has a permission.
     * @param group Group name
     * @param permission Permission node
     * @return true if group has the permission
     */
    default boolean groupHas(String group, String permission) {
        return false;
    }

    /**
     * Adds a permission to a group.
     * @param group Group name
     * @param permission Permission to add
     * @return true if successful
     */
    default boolean groupAdd(String group, String permission) {
        return false;
    }

    /**
     * Removes a permission from a group.
     * @param group Group name
     * @param permission Permission to remove
     * @return true if successful
     */
    default boolean groupRemove(String group, String permission) {
        return false;
    }
}
```

---

## Chat API

### Interface Definition

```java
package com.kukso.hy.lib.chat;

import hytale.server.world.entity.player.Player;
import java.util.UUID;

/**
 * The main Chat interface for chat formatting providers.
 * Plugins should use ServiceManager.getChat() to obtain an instance.
 */
public interface Chat {

    // ==================== Provider Info ====================

    /**
     * Gets the name of the chat provider.
     * @return Name of the chat provider
     */
    String getName();

    /**
     * Checks if this chat provider is enabled.
     * @return true if enabled
     */
    boolean isEnabled();

    // ==================== Prefix/Suffix ====================

    /**
     * Gets the prefix for a player.
     * @param player Player to check
     * @return Player's prefix, or empty string if none
     */
    String getPlayerPrefix(Player player);

    /**
     * Gets the suffix for a player.
     * @param player Player to check
     * @return Player's suffix, or empty string if none
     */
    String getPlayerSuffix(Player player);

    /**
     * Sets the prefix for a player.
     * @param player Player to modify
     * @param prefix New prefix
     */
    void setPlayerPrefix(Player player, String prefix);

    /**
     * Sets the suffix for a player.
     * @param player Player to modify
     * @param suffix New suffix
     */
    void setPlayerSuffix(Player player, String suffix);

    // ==================== Group Prefix/Suffix ====================

    /**
     * Gets the prefix for a group.
     * @param group Group name
     * @return Group's prefix, or empty string if none
     */
    String getGroupPrefix(String group);

    /**
     * Gets the suffix for a group.
     * @param group Group name
     * @return Group's suffix, or empty string if none
     */
    String getGroupSuffix(String group);

    /**
     * Sets the prefix for a group.
     * @param group Group name
     * @param prefix New prefix
     */
    void setGroupPrefix(String group, String prefix);

    /**
     * Sets the suffix for a group.
     * @param group Group name
     * @param suffix New suffix
     */
    void setGroupSuffix(String group, String suffix);

    // ==================== Display Name ====================

    /**
     * Gets the display name for a player (with formatting).
     * @param player Player to check
     * @return Formatted display name
     */
    default String getPlayerDisplayName(Player player) {
        String prefix = getPlayerPrefix(player);
        String suffix = getPlayerSuffix(player);
        String name = player.getName();
        return prefix + name + suffix;
    }

    // ==================== Meta Data (Optional) ====================

    /**
     * Gets a metadata value for a player.
     * @param player Player to check
     * @param key Metadata key
     * @return Metadata value, or null if not set
     */
    default String getPlayerMeta(Player player, String key) {
        return null;
    }

    /**
     * Sets a metadata value for a player.
     * @param player Player to modify
     * @param key Metadata key
     * @param value Metadata value
     */
    default void setPlayerMeta(Player player, String key, String value) {
        // No-op by default
    }

    /**
     * Gets a metadata value for a group.
     * @param group Group name
     * @param key Metadata key
     * @return Metadata value, or null if not set
     */
    default String getGroupMeta(String group, String key) {
        return null;
    }

    /**
     * Sets a metadata value for a group.
     * @param group Group name
     * @param key Metadata key
     * @param value Metadata value
     */
    default void setGroupMeta(String group, String key, String value) {
        // No-op by default
    }
}
```

---

## Service Manager

### Central Registry (with Lazy Activation)

```java
package com.kukso.hy.lib.service;

import com.kukso.hy.lib.economy.Economy;
import com.kukso.hy.lib.economy.CurrencyManager;
import com.kukso.hy.lib.permission.Permission;
import com.kukso.hy.lib.chat.Chat;
import hytale.server.plugin.JavaPlugin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central registry for all KuksoLib services.
 * Manages registration, priority, and retrieval of service providers.
 *
 * Uses LAZY ACTIVATION pattern:
 * - Economy, Permission, and Chat modules are dormant by default
 * - Modules only activate when a plugin registers to use that service
 * - No database connections, event listeners, or ECS components are loaded until needed
 * - Servers that don't need all features have zero overhead from unused modules
 */
public class ServiceManager {

    private static final Logger LOGGER = Logger.getLogger(ServiceManager.class.getName());

    private static final Map<Class<?>, TreeSet<ServiceProvider<?>>> providers = new ConcurrentHashMap<>();

    // ==================== Lazy Activation State ====================

    private static final AtomicBoolean economyActive = new AtomicBoolean(false);
    private static final AtomicBoolean permissionActive = new AtomicBoolean(false);
    private static final AtomicBoolean chatActive = new AtomicBoolean(false);

    // Database manager instance (created lazily)
    private static EconomyDatabaseManager databaseManager = null;

    // ==================== Registration with Lazy Activation ====================

    /**
     * Registers an economy provider.
     * On first registration, activates the Economy module (database, ECS, listeners).
     *
     * @param plugin Plugin registering the provider
     * @param provider Economy implementation
     * @param priority Registration priority
     */
    public static void registerEconomy(JavaPlugin plugin, Economy provider, ServicePriority priority) {
        // Lazy activation on first economy registration
        if (economyActive.compareAndSet(false, true)) {
            activateEconomyModule(plugin);
        }
        register(Economy.class, plugin, provider, priority);
    }

    /**
     * Registers a permission provider.
     * On first registration, activates the Permission module.
     *
     * @param plugin Plugin registering the provider
     * @param provider Permission implementation
     * @param priority Registration priority
     */
    public static void registerPermission(JavaPlugin plugin, Permission provider, ServicePriority priority) {
        // Lazy activation on first permission registration
        if (permissionActive.compareAndSet(false, true)) {
            activatePermissionModule(plugin);
        }
        register(Permission.class, plugin, provider, priority);
    }

    /**
     * Registers a chat provider.
     * On first registration, activates the Chat module.
     *
     * @param plugin Plugin registering the provider
     * @param provider Chat implementation
     * @param priority Registration priority
     */
    public static void registerChat(JavaPlugin plugin, Chat provider, ServicePriority priority) {
        // Lazy activation on first chat registration
        if (chatActive.compareAndSet(false, true)) {
            activateChatModule(plugin);
        }
        register(Chat.class, plugin, provider, priority);
    }

    // ==================== Module Activation ====================

    /**
     * Activates the Economy module.
     * Called automatically on first economy provider registration.
     */
    private static void activateEconomyModule(JavaPlugin plugin) {
        LOGGER.log(Level.INFO, "Economy module activated by " + plugin.getName());

        // Initialize database connection (using KuksoLib's config)
        databaseManager = new EconomyDatabaseManager();
        databaseManager.initialize();

        // Register player join/quit listeners for ECS component initialization
        registerEconomyListeners(plugin);

        LOGGER.log(Level.INFO, "Economy module ready - database connected, listeners registered");
    }

    /**
     * Activates the Permission module.
     * Called automatically on first permission provider registration.
     */
    private static void activatePermissionModule(JavaPlugin plugin) {
        LOGGER.log(Level.INFO, "Permission module activated by " + plugin.getName());
        // Permission module initialization (if needed)
    }

    /**
     * Activates the Chat module.
     * Called automatically on first chat provider registration.
     */
    private static void activateChatModule(JavaPlugin plugin) {
        LOGGER.log(Level.INFO, "Chat module activated by " + plugin.getName());
        // Chat module initialization (if needed)
    }

    /**
     * Registers event listeners for economy (player join/quit).
     */
    private static void registerEconomyListeners(JavaPlugin plugin) {
        // Register listeners for:
        // - Player join: Initialize CurrencyComponents, load from DB
        // - Player quit: Sync ECS to DB, cleanup
    }

    // ==================== Database Access ====================

    /**
     * Gets the database manager for economy operations.
     * Only available after economy module is activated.
     *
     * @return Database manager, or null if economy not active
     */
    public static EconomyDatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    // ==================== Generic Registration ====================

    /**
     * Generic service registration method.
     */
    private static <T> void register(Class<T> serviceClass, JavaPlugin plugin, T provider, ServicePriority priority) {
        providers.computeIfAbsent(serviceClass, k -> new TreeSet<>((a, b) -> {
            // Higher priority first
            int priorityCompare = Integer.compare(b.getPriority().ordinal(), a.getPriority().ordinal());
            if (priorityCompare != 0) return priorityCompare;
            // Then by plugin name for consistency
            return a.getPlugin().getName().compareTo(b.getPlugin().getName());
        })).add(new ServiceProvider<>(plugin, provider, priority));

        LOGGER.log(Level.INFO,
            "Registered " + serviceClass.getSimpleName() + " provider: " +
            provider.getClass().getSimpleName() + " (priority: " + priority + ")"
        );
    }

    // ==================== Retrieval ====================

    /**
     * Gets the highest priority economy provider.
     * @return Economy instance, or null if none registered
     */
    public static Economy getEconomy() {
        return getProvider(Economy.class);
    }

    /**
     * Gets the highest priority permission provider.
     * @return Permission instance, or null if none registered
     */
    public static Permission getPermission() {
        return getProvider(Permission.class);
    }

    /**
     * Gets the highest priority chat provider.
     * @return Chat instance, or null if none registered
     */
    public static Chat getChat() {
        return getProvider(Chat.class);
    }

    /**
     * Generic provider retrieval.
     */
    @SuppressWarnings("unchecked")
    private static <T> T getProvider(Class<T> serviceClass) {
        TreeSet<ServiceProvider<?>> set = providers.get(serviceClass);
        if (set == null || set.isEmpty()) {
            return null;
        }
        return (T) set.first().getProvider();
    }

    /**
     * Gets all registered providers for a service type.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getProviders(Class<T> serviceClass) {
        TreeSet<ServiceProvider<?>> set = providers.get(serviceClass);
        if (set == null) {
            return List.of();
        }
        List<T> result = new ArrayList<>();
        for (ServiceProvider<?> sp : set) {
            result.add((T) sp.getProvider());
        }
        return result;
    }

    // ==================== Unregistration ====================

    /**
     * Unregisters all services from a plugin.
     * @param plugin Plugin to unregister
     */
    public static void unregisterAll(JavaPlugin plugin) {
        for (TreeSet<ServiceProvider<?>> set : providers.values()) {
            set.removeIf(sp -> sp.getPlugin().equals(plugin));
        }
    }

    /**
     * Clears all registered services and shuts down activated modules.
     */
    public static void shutdown() {
        // Shutdown database if economy was active
        if (economyActive.get() && databaseManager != null) {
            databaseManager.shutdown();
            databaseManager = null;
        }

        // Clear currencies
        CurrencyManager.clear();

        // Reset activation state
        economyActive.set(false);
        permissionActive.set(false);
        chatActive.set(false);

        providers.clear();
        LOGGER.log(Level.INFO, "ServiceManager shutdown complete");
    }

    // ==================== Status ====================

    /**
     * Checks if any economy provider is registered.
     */
    public static boolean hasEconomy() {
        return getEconomy() != null;
    }

    /**
     * Checks if any permission provider is registered.
     */
    public static boolean hasPermission() {
        return getPermission() != null;
    }

    /**
     * Checks if any chat provider is registered.
     */
    public static boolean hasChat() {
        return getChat() != null;
    }

    /**
     * Checks if the economy module has been activated.
     */
    public static boolean isEconomyActive() {
        return economyActive.get();
    }

    /**
     * Checks if the permission module has been activated.
     */
    public static boolean isPermissionActive() {
        return permissionActive.get();
    }

    /**
     * Checks if the chat module has been activated.
     */
    public static boolean isChatActive() {
        return chatActive.get();
    }
}
```

### Priority Enum

```java
package com.kukso.hy.lib.service;

/**
 * Priority levels for service registration.
 * Higher priority providers are preferred.
 */
public enum ServicePriority {
    LOWEST,    // Fallback/default implementations
    LOW,       // Low priority
    NORMAL,    // Standard priority
    HIGH,      // High priority
    HIGHEST    // Override all others
}
```

### Provider Wrapper

```java
package com.kukso.hy.lib.service;

import hytale.server.plugin.JavaPlugin;

/**
 * Wrapper for a registered service provider.
 */
public class ServiceProvider<T> {
    private final JavaPlugin plugin;
    private final T provider;
    private final ServicePriority priority;

    public ServiceProvider(JavaPlugin plugin, T provider, ServicePriority priority) {
        this.plugin = plugin;
        this.provider = provider;
        this.priority = priority;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public T getProvider() {
        return provider;
    }

    public ServicePriority getPriority() {
        return priority;
    }
}
```

---

## Usage Examples

### For Plugin Developers (Using KuksoLib API)

```java
import com.kukso.hy.lib.service.ServiceManager;
import com.kukso.hy.lib.economy.Economy;
import com.kukso.hy.lib.economy.EconomyResponse;
import com.kukso.hy.lib.economy.Currency;
import com.kukso.hy.lib.permission.Permission;
import com.kukso.hy.lib.chat.Chat;

public class MyPlugin extends JavaPlugin {

    @Override
    public void start() {
        // Check if economy is available
        if (!ServiceManager.hasEconomy()) {
            getLogger().at(Level.WARNING).log("No economy provider found!");
            return;
        }

        Economy econ = ServiceManager.getEconomy();
        Permission perms = ServiceManager.getPermission();
        Chat chat = ServiceManager.getChat();

        Player player = /* get player */;

        // Example: Give player gold (default currency)
        EconomyResponse response = econ.deposit(player, 100.0);
        if (response.isSuccess()) {
            player.sendMessage("You received " + econ.format(100.0) + "!");
        }

        // Example: Give player gems (specific currency)
        EconomyResponse gemsResponse = econ.deposit(player, "gems", 50.0);
        if (gemsResponse.isSuccess()) {
            player.sendMessage("You received " + econ.format("gems", 50.0) + "!");
        }

        // Example: Check player's balance in multiple currencies
        double goldBalance = econ.getBalance(player);           // Default currency
        double gemsBalance = econ.getBalance(player, "gems");   // Specific currency

        // Example: List all available currencies
        for (String currencyId : econ.getCurrencies()) {
            Currency currency = econ.getCurrency(currencyId);
            double balance = econ.getBalance(player, currencyId);
            player.sendMessage(currency.displayName() + ": " + currency.formatAmount(balance));
        }

        // Example: Transfer gems between players
        Player otherPlayer = /* get other player */;
        EconomyResponse transferResponse = econ.transfer(player, otherPlayer, "gems", 10.0);
        if (transferResponse.isSuccess()) {
            player.sendMessage("Transferred 10 gems!");
        }

        // Example: Check permission
        if (perms != null && perms.has(player, "myshop.admin")) {
            // Player has admin access
        }

        // Example: Get player display name
        if (chat != null) {
            String displayName = chat.getPlayerDisplayName(player);
            // Use displayName in chat messages
        }
    }
}
```

### For Provider Developers (Implementing the API)

```java
import com.kukso.hy.lib.service.ServiceManager;
import com.kukso.hy.lib.service.ServicePriority;
import com.kukso.hy.lib.economy.Economy;
import com.kukso.hy.lib.economy.EconomyResponse;
import com.kukso.hy.lib.economy.ResponseType;

public class MyEconomyPlugin extends JavaPlugin {

    @Override
    public void start() {
        // Register our economy implementation
        ServiceManager.registerEconomy(this, new MyEconomyImpl(), ServicePriority.NORMAL);
    }

    @Override
    public void shutdown() {
        // Unregister when shutting down
        ServiceManager.unregisterAll(this);
    }

    // Our economy implementation
    private class MyEconomyImpl implements Economy {
        @Override
        public String getName() {
            return "MyEconomyPlugin";
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public String currencyNameSingular() {
            return "Coin";
        }

        @Override
        public String currencyNamePlural() {
            return "Coins";
        }

        @Override
        public String format(double amount) {
            return String.format("%.2f Coins", amount);
        }

        @Override
        public int fractionalDigits() {
            return 2;
        }

        // ... implement remaining methods
    }
}
```

---

## Built-in Implementations

### Economy Infrastructure

KuksoLib provides the infrastructure for economy systems that plugins can use:

- **Multi-currency support** - Economy plugins define their currencies via `CurrencyManager.register()`
- **ECS-based storage** - Uses Hytale's native ECS with `CurrencyComponent` per currency
- **SQLite backend** (default) - Zero configuration required
- **MySQL backend** (optional) - For multi-server setups
- **Transaction logging** - Full audit trail
- **Admin query** - `/kuksolib wallet <player>` to inspect player balances and verify ECS/DB sync
- **Lazy activation** - Database and listeners only load when an economy plugin registers

**Note:** KuksoLib is a library/API layer. Currencies are defined by economy plugins, not in KuksoLib's config. User-facing commands like `/balance`, `/pay`, `/give`, `/take` should be implemented by a separate economy plugin that depends on KuksoLib.

**KuksoLib Configuration** (`plugins/KuksoLib/config.json`):

KuksoLib's config only contains database and logging settings. Currencies are defined by economy plugins.

```json
{
  "database": {
    "type": "sqlite",
    "mysql": {
      "host": "localhost",
      "port": 3306,
      "database": "kuksolib",
      "username": "root",
      "password": ""
    }
  },
  "logging": {
    "enabled": true,
    "logFile": "transactions.log"
  }
}
```

**Economy Plugin Example** (defines currencies and registers as provider):

```java
public class MyEconomyPlugin extends JavaPlugin implements Economy {

    @Override
    public void start() {
        // Define currencies (these are NOT in KuksoLib's config)
        CurrencyManager.register(new Currency(
            "gold", "Gold", "Gold", "Gold", "G", "{amount}{symbol}", 0, 100.0, "myeconomy:gold"
        ));
        CurrencyManager.register(new Currency(
            "gems", "Gems", "Gem", "Gems", "💎", "{amount} {symbol}", 0, 0.0, "myeconomy:gems"
        ));
        CurrencyManager.register(new Currency(
            "coins", "Coins", "Coin", "Coins", "$", "{symbol}{amount}", 2, 0.0, "myeconomy:coins"
        ));
        CurrencyManager.setDefaultCurrency("gold");

        // Register as economy provider (this activates KuksoLib's economy module)
        ServiceManager.registerEconomy(this, this, ServicePriority.NORMAL);
    }

    @Override
    public void shutdown() {
        // Unregister currencies
        CurrencyManager.unregister("gold");
        CurrencyManager.unregister("gems");
        CurrencyManager.unregister("coins");

        // Unregister as provider
        ServiceManager.unregisterAll(this);
    }

    // Implement Economy interface methods...
    @Override
    public String getName() { return "MyEconomyPlugin"; }

    @Override
    public Set<String> getCurrencies() { return CurrencyManager.getCurrencyIds(); }

    @Override
    public String getDefaultCurrency() { return CurrencyManager.getDefaultCurrencyId(); }

    // ... other Economy methods
}
```

### `/kuksolib wallet <player>` Command

Shows both ECS component data and database backend data side-by-side for debugging and verification:

```
/kuksolib wallet Steve
────────────────────────────────────
Wallet Info for: Steve
────────────────────────────────────
Currency    │ ECS Component │ Database │ Status
────────────────────────────────────
gold        │ 150.0         │ 150.0    │ ✓ Synced
gems        │ 50.0          │ 50.0     │ ✓ Synced
coins       │ 25.50         │ 25.00    │ ⚠ Mismatch!
────────────────────────────────────
```

This helps admins verify that ECS components (in-memory, fast access) are in sync with the database backend (persistent storage).

### DefaultPermission (Fallback Permission)

Uses Hytale's native permission system:
- Checks `Player.hasPermission()` directly
- No group support
- Basic permission checking only

### DefaultChat (Fallback Chat)

Uses ColorMan for basic formatting:
- No prefix/suffix storage
- Returns empty strings for missing data
- Integrates with ColorMan for color code translation

---

## Implementation Roadmap

### Phase 0: Configuration System ✅ COMPLETED
- [x] Create general `config.json` structure for KuksoLib
- [x] Add config sections for each module (economy, permission, locale, chat)
- [x] Add multi-currency configuration support in `economy.currencies`
- [x] Add `Currency` record for currency metadata

### Phase 1: Core Infrastructure
- [x] Implement `ServiceManager` with registration/retrieval
- [x] Implement `ServicePriority` enum
- [x] Implement `ServiceProvider` wrapper class
- [ ] Add service status commands to `/kuksolib`

### Phase 2: Economy API ✅ COMPLETED
- [x] Define `Economy` interface with multi-currency support
- [x] Define `EconomyResponse` and `ResponseType`
- [x] Define `Currency` record for currency metadata
- [x] Implement `CurrencyManager` for dynamic currency registration
- [x] Implement `CurrencyComponent` (ECS component for player balances)
- [x] Implement `KuksoEconomy` with SQLite backend and multi-currency
- [ ] Add MySQL support to `KuksoEconomy`
- [x] Add `/kuksolib wallet <player>` admin query command
- [x] Add transaction logging

### Phase 3: Permission API
- [ ] Define `Permission` interface
- [ ] Implement `DefaultPermission` fallback
- [ ] Add group support infrastructure
- [ ] Document integration with external permission plugins

### Phase 4: Chat API
- [ ] Define `Chat` interface
- [ ] Implement `DefaultChat` fallback
- [ ] Add prefix/suffix storage
- [ ] Integrate with LocaleMan for localized prefixes

### Phase 5: Testing & Documentation
- [ ] Write unit tests for all services
- [ ] Create example plugins demonstrating usage
- [ ] Write developer documentation
- [ ] Create migration guide from other economy plugins

---

## Permissions

KuksoLib library commands use the following permissions:

| Permission | Description | Default |
|------------|-------------|---------|
| `kuksolib.admin` | Full admin access | OP |
| `kuksolib.wallet` | Use `/kuksolib wallet` to query player balances | OP |
| `kuksolib.reload` | Reload KuksoLib configuration | OP |

**Note:** User-facing economy permissions (e.g., `economy.balance`, `economy.pay`) should be defined by the economy plugin that implements the commands, not by KuksoLib.

---

## Events

KuksoLib will fire events that other plugins can listen to:

```java
// Economy events
EconomyTransactionEvent      // Fired on deposit/withdraw
EconomyTransferEvent         // Fired on player-to-player transfer
EconomyBalanceChangeEvent    // Fired when balance changes

// Service events
ServiceProviderRegisterEvent // Fired when a provider registers
ServiceProviderChangeEvent   // Fired when active provider changes
```

---

## Differences from Minecraft Vault

| Feature | Vault (Minecraft) | KuksoLib (Hytale) |
|---------|-------------------|-------------------|
| Registration | Bukkit ServiceManager | KuksoLib ServiceManager |
| Thread Safety | Plugin-dependent | ConcurrentHashMap-based |
| Default Economy | None | Built-in SQLite economy |
| Localization | None | Integrated LocaleMan |
| Color Codes | Bukkit ChatColor | ColorMan (§ and & codes) |
| Bank Support | Full | Optional (via interface) |

---

## FAQ

**Q: Does KuksoLib provide user-facing economy commands?**
A: No. KuksoLib is a library that provides the Economy API, database backend, and ECS infrastructure. Commands like `/balance`, `/pay`, `/give`, `/take` should be implemented by a separate economy plugin that depends on KuksoLib. KuksoLib only provides `/kuksolib wallet <player>` for admin queries.

**Q: Do I need to soft-depend on KuksoLib?**
A: Yes, if you want to use the services but not require them. Use `depend` if your plugin requires economy/permissions.

**Q: Can I have multiple economy plugins?**
A: Yes, but only the highest priority one will be used. Use `ServiceManager.getProviders(Economy.class)` to access all.

**Q: How do I migrate from another economy plugin?**
A: Migration should be handled by the economy plugin using KuksoLib, not by KuksoLib itself.

**Q: Is the economy persistent across server restarts?**
A: Yes, the built-in KuksoEconomy backend uses SQLite/MySQL for persistent storage.

---

## Contributing

To add support for a new economy/permission/chat plugin:

1. Create a bridge plugin that depends on both KuksoLib and the target plugin
2. Implement the appropriate interface (`Economy`, `Permission`, or `Chat`)
3. Register with `ServiceManager` in your plugin's `start()` method
4. Submit a PR to be listed as an official bridge

---

## License

This service provider API is part of KuksoLib and follows the same license.
