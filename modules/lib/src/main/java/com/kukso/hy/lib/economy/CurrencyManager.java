package com.kukso.hy.lib.economy;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.kukso.hy.lib.util.ECS;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages currency definitions and their ECS ComponentTypes.
 * Economy plugins register their currencies via the register() method.
 *
 * <p>Architecture:</p>
 * <ul>
 *   <li>Each currency registered gets its own ComponentType&lt;EntityStore, CurrencyComponent&gt;</li>
 *   <li>ComponentType IDs follow the pattern specified in the Currency record (e.g., "myeconomy:gold")</li>
 *   <li>The same CurrencyComponent class is used for all currencies</li>
 *   <li>Multiple ComponentTypes allow players to have separate balances per currency</li>
 * </ul>
 *
 * <p>Example usage (in an economy plugin):</p>
 * <pre>{@code
 * CurrencyManager.register(new Currency(
 *     "gold", "Gold", "Gold", "Gold", "G", "{amount}{symbol}", 0, 100.0, "myeconomy:gold"
 * ));
 * CurrencyManager.register(new Currency(
 *     "gems", "Gems", "Gem", "Gems", "💎", "{amount} {symbol}", 0, 0.0, "myeconomy:gems"
 * ));
 * CurrencyManager.setDefaultCurrency("gold");
 * }</pre>
 */
public final class CurrencyManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // Currency ID -> Currency metadata
    private static final Map<String, Currency> currencies = new ConcurrentHashMap<>();

    // Currency ID -> ComponentType for ECS
    private static final Map<String, ComponentType<EntityStore, CurrencyComponent>> componentTypes = new ConcurrentHashMap<>();

    // Default currency ID (set by the economy plugin)
    private static volatile String defaultCurrencyId = null;

    // EntityStore reference for ECS operations
    private static volatile EntityStore entityStore = null;

    private CurrencyManager() {
        // Static utility class
    }

    // ==================== Currency Registration API ====================

    /**
     * Registers a currency. Called by economy plugins to define their currencies.
     * This is the primary way currencies are added to the system.
     *
     * @param currency Currency to register
     * @throws IllegalArgumentException if currency with same ID already exists
     */
    public static void register(@Nonnull Currency currency) {
        if (currencies.containsKey(currency.id())) {
            throw new IllegalArgumentException("Currency '" + currency.id() + "' is already registered");
        }

        currencies.put(currency.id(), currency);
        registerComponentType(currency);

        LOGGER.atInfo().log("Registered currency: " + currency.id() +
            " (component: " + currency.componentId() + ")");

        // Set as default if this is the first currency
        if (defaultCurrencyId == null) {
            defaultCurrencyId = currency.id();
            LOGGER.atInfo().log("Set default currency: " + currency.id());
        }
    }

    /**
     * Unregisters a currency. Called when an economy plugin shuts down.
     *
     * @param currencyId Currency ID to unregister
     */
    public static void unregister(@Nonnull String currencyId) {
        currencies.remove(currencyId);
        componentTypes.remove(currencyId);

        // Update default if we removed the default currency
        if (currencyId.equals(defaultCurrencyId)) {
            defaultCurrencyId = currencies.isEmpty() ? null : currencies.keySet().iterator().next();
            if (defaultCurrencyId != null) {
                LOGGER.atInfo().log("Default currency changed to: " + defaultCurrencyId);
            }
        }

        LOGGER.atInfo().log("Unregistered currency: " + currencyId);
    }

    /**
     * Sets the default currency ID.
     * Called by the economy plugin after registering currencies.
     *
     * @param currencyId Currency ID to set as default
     * @throws IllegalArgumentException if currency is not registered
     */
    public static void setDefaultCurrency(@Nonnull String currencyId) {
        if (!currencies.containsKey(currencyId)) {
            throw new IllegalArgumentException("Currency '" + currencyId + "' is not registered");
        }
        defaultCurrencyId = currencyId;
        LOGGER.atInfo().log("Default currency set to: " + currencyId);
    }

    /**
     * Sets the EntityStore reference for ECS operations.
     *
     * @param store The EntityStore to use
     */
    public static void setEntityStore(@Nullable EntityStore store) {
        entityStore = store;
    }

    /**
     * Clears all registered currencies.
     * Called during shutdown.
     */
    public static void clear() {
        currencies.clear();
        componentTypes.clear();
        defaultCurrencyId = null;
        entityStore = null;
        LOGGER.atInfo().log("CurrencyManager cleared");
    }

    /**
     * Registers a ComponentType for a currency with the ECS system.
     */
    @SuppressWarnings("unchecked")
    private static void registerComponentType(@Nonnull Currency currency) {
        try {
            // Create ComponentType using reflection (constructor is protected)
            Constructor<ComponentType> constructor = ComponentType.class.getDeclaredConstructor(
                Class.class, String.class
            );
            constructor.setAccessible(true);
            ComponentType<EntityStore, CurrencyComponent> componentType = constructor.newInstance(
                CurrencyComponent.class,
                currency.componentId()
            );

            componentTypes.put(currency.id(), componentType);
            LOGGER.atInfo().log("Registered ComponentType for currency: " + currency.id());
        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to register ComponentType for " + currency.id() + ": " + e.getMessage());
        }
    }

    // ==================== Query API ====================

    /**
     * Gets all registered currency IDs.
     *
     * @return Unmodifiable set of currency IDs
     */
    @Nonnull
    public static Set<String> getCurrencyIds() {
        return Collections.unmodifiableSet(currencies.keySet());
    }

    /**
     * Gets all registered currencies.
     *
     * @return Unmodifiable map of currency ID to Currency
     */
    @Nonnull
    public static Map<String, Currency> getCurrencies() {
        return Collections.unmodifiableMap(currencies);
    }

    /**
     * Gets the default currency ID.
     *
     * @return Default currency ID, or null if no currencies registered
     */
    @Nullable
    public static String getDefaultCurrencyId() {
        return defaultCurrencyId;
    }

    /**
     * Gets the default currency.
     *
     * @return Default currency, or null if no currencies registered
     */
    @Nullable
    public static Currency getDefaultCurrency() {
        return defaultCurrencyId != null ? currencies.get(defaultCurrencyId) : null;
    }

    /**
     * Gets currency metadata by ID.
     *
     * @param currencyId Currency identifier
     * @return Currency or null if not found
     */
    @Nullable
    public static Currency getCurrency(@Nonnull String currencyId) {
        return currencies.get(currencyId);
    }

    /**
     * Checks if a currency is registered.
     *
     * @param currencyId Currency identifier
     * @return true if currency is registered
     */
    public static boolean hasCurrency(@Nonnull String currencyId) {
        return currencies.containsKey(currencyId);
    }

    /**
     * Checks if any currencies are registered.
     *
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
    @Nullable
    public static ComponentType<EntityStore, CurrencyComponent> getComponentType(@Nonnull String currencyId) {
        return componentTypes.get(currencyId);
    }

    // ==================== Player Balance Management ====================

    /**
     * Initializes all currency components for a player entity.
     * Called when a player joins or account is created.
     *
     * @param player Player to initialize
     * @param store  The EntityStore for the player
     */
    public static void initializePlayer(@Nonnull PlayerRef player, @Nonnull EntityStore store) {
        for (Currency currency : currencies.values()) {
            ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currency.id());
            if (type != null) {
                CurrencyComponent existing = ECS.getComponent(store, player, type);
                if (existing == null) {
                    CurrencyComponent component = new CurrencyComponent(
                        currency.id(),
                        currency.startingBalance()
                    );
                    ECS.addComponent(store, player, type, component);
                    LOGGER.atFine().log("Initialized currency " + currency.id() +
                        " for player " + player.getUsername() +
                        " with balance " + currency.startingBalance());
                }
            }
        }
    }

    /**
     * Gets the ECS balance of a specific currency for a player.
     *
     * @param player     Player to check
     * @param currencyId Currency identifier
     * @param store      The EntityStore for the player
     * @return Balance or 0.0 if not found
     */
    public static double getBalance(@Nonnull PlayerRef player, @Nonnull String currencyId, @Nonnull EntityStore store) {
        ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currencyId);
        if (type == null) {
            return 0.0;
        }
        CurrencyComponent component = ECS.getComponent(store, player, type);
        return component != null ? component.getBalance() : 0.0;
    }

    /**
     * Gets the ECS balance of the default currency for a player.
     *
     * @param player Player to check
     * @param store  The EntityStore for the player
     * @return Balance or 0.0 if not found
     */
    public static double getBalance(@Nonnull PlayerRef player, @Nonnull EntityStore store) {
        if (defaultCurrencyId == null) {
            return 0.0;
        }
        return getBalance(player, defaultCurrencyId, store);
    }

    /**
     * Sets the ECS balance of a specific currency for a player.
     *
     * @param player     Player to modify
     * @param currencyId Currency identifier
     * @param amount     New balance
     * @param store      The EntityStore for the player
     * @return true if successful
     */
    public static boolean setBalance(@Nonnull PlayerRef player, @Nonnull String currencyId,
                                     double amount, @Nonnull EntityStore store) {
        ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currencyId);
        if (type == null) {
            return false;
        }

        CurrencyComponent component = ECS.getComponent(store, player, type);
        if (component == null) {
            // Create new component if it doesn't exist
            Currency currency = currencies.get(currencyId);
            if (currency == null) {
                return false;
            }
            component = new CurrencyComponent(currencyId, amount);
        } else {
            component.setBalance(amount);
        }

        ECS.addComponent(store, player, type, component);
        return true;
    }

    /**
     * Deposits an amount into a player's currency balance.
     *
     * @param player     Player to modify
     * @param currencyId Currency identifier
     * @param amount     Amount to deposit (must be positive)
     * @param store      The EntityStore for the player
     * @return true if successful
     */
    public static boolean deposit(@Nonnull PlayerRef player, @Nonnull String currencyId,
                                  double amount, @Nonnull EntityStore store) {
        if (amount <= 0) {
            return false;
        }

        ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currencyId);
        if (type == null) {
            return false;
        }

        CurrencyComponent component = ECS.getComponent(store, player, type);
        if (component == null) {
            return false;
        }

        boolean success = component.deposit(amount);
        if (success) {
            ECS.addComponent(store, player, type, component);
        }
        return success;
    }

    /**
     * Withdraws an amount from a player's currency balance.
     *
     * @param player     Player to modify
     * @param currencyId Currency identifier
     * @param amount     Amount to withdraw (must be positive)
     * @param store      The EntityStore for the player
     * @return true if successful (sufficient funds)
     */
    public static boolean withdraw(@Nonnull PlayerRef player, @Nonnull String currencyId,
                                   double amount, @Nonnull EntityStore store) {
        if (amount <= 0) {
            return false;
        }

        ComponentType<EntityStore, CurrencyComponent> type = componentTypes.get(currencyId);
        if (type == null) {
            return false;
        }

        CurrencyComponent component = ECS.getComponent(store, player, type);
        if (component == null) {
            return false;
        }

        boolean success = component.withdraw(amount);
        if (success) {
            ECS.addComponent(store, player, type, component);
        }
        return success;
    }

    /**
     * Checks if a player has at least the specified amount of a currency.
     *
     * @param player     Player to check
     * @param currencyId Currency identifier
     * @param amount     Amount to check
     * @param store      The EntityStore for the player
     * @return true if player has at least that amount
     */
    public static boolean has(@Nonnull PlayerRef player, @Nonnull String currencyId,
                              double amount, @Nonnull EntityStore store) {
        return getBalance(player, currencyId, store) >= amount;
    }

    /**
     * Formats an amount using the specified currency's format pattern.
     *
     * @param currencyId Currency identifier
     * @param amount     Amount to format
     * @return Formatted string, or plain number if currency not found
     */
    @Nonnull
    public static String format(@Nonnull String currencyId, double amount) {
        Currency currency = currencies.get(currencyId);
        if (currency != null) {
            return currency.formatAmount(amount);
        }
        return String.format("%.2f", amount);
    }

    /**
     * Formats an amount using the default currency's format pattern.
     *
     * @param amount Amount to format
     * @return Formatted string
     */
    @Nonnull
    public static String format(double amount) {
        if (defaultCurrencyId != null) {
            return format(defaultCurrencyId, amount);
        }
        return String.format("%.2f", amount);
    }
}
