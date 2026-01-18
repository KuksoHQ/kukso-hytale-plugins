package com.kukso.hy.lib.economy;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.kukso.hy.lib.config.ConfigManager;
import com.kukso.hy.lib.config.KuksoConfig;
import com.kukso.hy.lib.util.HytaleUtils;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages economy transactions using Hytale's Entity Component System (ECS).
 * Thread-safe singleton implementation for managing player wallets.
 * Reads currency configuration from ConfigManager.
 */
public class EconomyManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static EconomyManager instance;
    private static final double DEFAULT_BALANCE = 0.0;

    private EntityStore entityStore;
    private final Map<String, Currency> currencies = new ConcurrentHashMap<>();
    private String defaultCurrencyId;

    public EconomyManager() {
        instance = this;
        initializeComponentType();
        loadCurrenciesFromConfig();
    }

    /**
     * Loads currencies from the ConfigManager.
     * Called during initialization and on config reload.
     */
    private void loadCurrenciesFromConfig() {
        currencies.clear();

        KuksoConfig.EconomyConfig economyConfig = ConfigManager.getEconomy();
        if (economyConfig == null || !economyConfig.isEnabled()) {
            LOGGER.atWarning().log("Economy is disabled in config");
            return;
        }

        // Load currencies from config
        for (Currency currency : economyConfig.toCurrencyRecords()) {
            currencies.put(currency.id(), currency);
            LOGGER.atInfo().log("Loaded currency: " + currency.id() +
                " (" + currency.displayName() + ") starting balance: " + currency.startingBalance());
        }

        // Set default currency
        defaultCurrencyId = economyConfig.getDefaultCurrency();
        if (defaultCurrencyId != null && !currencies.containsKey(defaultCurrencyId)) {
            LOGGER.atWarning().log("Default currency '" + defaultCurrencyId + "' not found in config");
            if (!currencies.isEmpty()) {
                defaultCurrencyId = currencies.keySet().iterator().next();
                LOGGER.atInfo().log("Using '" + defaultCurrencyId + "' as default currency");
            }
        }

        LOGGER.atInfo().log("Loaded " + currencies.size() + " currencies, default: " + defaultCurrencyId);
    }

    /**
     * Reloads currencies from config.
     * Called when config is reloaded.
     */
    public void reloadConfig() {
        loadCurrenciesFromConfig();
    }

    /**
     * Gets the default currency.
     *
     * @return The default currency, or null if none configured
     */
    public Currency getDefaultCurrency() {
        return defaultCurrencyId != null ? currencies.get(defaultCurrencyId) : null;
    }

    /**
     * Gets a currency by ID.
     *
     * @param currencyId The currency ID
     * @return The currency, or null if not found
     */
    public Currency getCurrency(String currencyId) {
        return currencies.get(currencyId);
    }

    /**
     * Gets all registered currencies.
     *
     * @return Map of currency ID to Currency
     */
    public Map<String, Currency> getCurrencies() {
        return currencies;
    }

    /**
     * Gets the starting balance for new players using the default currency.
     *
     * @return The starting balance
     */
    public double getStartingBalance() {
        Currency defaultCurrency = getDefaultCurrency();
        return defaultCurrency != null ? defaultCurrency.startingBalance() : DEFAULT_BALANCE;
    }

    /**
     * Initialize the WalletComponent ComponentType using reflection.
     * This is necessary because ComponentType constructors are protected.
     */
    private void initializeComponentType() {
        if (WalletComponent.TYPE == null) {
            try {
                Constructor<ComponentType> constructor = ComponentType.class.getDeclaredConstructor(
                    Class.class, String.class
                );
                constructor.setAccessible(true);
                WalletComponent.TYPE = constructor.newInstance(
                    WalletComponent.class,
                    "kuksolib:wallet"
                );
                LOGGER.atInfo().log("WalletComponent TYPE registered successfully");
            } catch (Exception e) {
                LOGGER.atSevere().log("Failed to initialize WalletComponent TYPE: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Lazy initialization of EntityStore from the plugin.
     * EntityStore is accessed through the Universe API.
     * NOTE: This is a placeholder implementation. The actual EntityStore access
     * will depend on the final Hytale API structure.
     */
    private EntityStore getEntityStore() {
        if (entityStore == null) {
            try {
                // TODO: Replace with actual EntityStore access when API is available
                // Example: entityStore = Main.getInstance().getUniverse().getWorlds().get(0).getEntityStore();
                LOGGER.atWarning().log("EntityStore access not yet implemented - using fallback");
            } catch (Exception e) {
                LOGGER.atWarning().log("Could not access EntityStore: " + e.getMessage());
            }
        }
        return entityStore;
    }

    public static EconomyManager getInstance() {
        return instance;
    }

    /**
     * Gets the wallet component for a player.
     * Creates one with starting balance if it doesn't exist.
     *
     * @param player The player reference
     * @return The wallet component, or null if player is invalid
     */
    public WalletComponent getWallet(PlayerRef player) {
        if (player == null) {
            return null;
        }

        EntityStore store = getEntityStore();
        if (store == null || WalletComponent.TYPE == null) {
            LOGGER.atWarning().log("EntityStore or WalletComponent.TYPE not initialized");
            return null;
        }

        try {
            // Try to get existing wallet
            WalletComponent wallet = HytaleUtils.getComponent(store, player, WalletComponent.TYPE);

            // Create new wallet with starting balance if it doesn't exist
            if (wallet == null) {
                double startingBalance = getStartingBalance();
                wallet = new WalletComponent(startingBalance);
                HytaleUtils.addComponent(store, player, WalletComponent.TYPE, wallet);
                LOGGER.atInfo().log("Created new wallet for player: " + player.getUsername()
                    + " with starting balance: " + startingBalance);
            }

            return wallet;
        } catch (Exception e) {
            LOGGER.atSevere().log("Error accessing wallet for " + player.getUsername() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Ensures a player has a wallet component.
     * Called when a player joins the server.
     *
     * @param player The player reference
     */
    public void ensureWallet(PlayerRef player) {
        getWallet(player);
    }

    /**
     * Gets a player's balance.
     *
     * @param player The player reference
     * @return The balance, or 0.0 if wallet not found
     */
    public double getBalance(PlayerRef player) {
        WalletComponent wallet = getWallet(player);
        return wallet != null ? wallet.getBalance() : DEFAULT_BALANCE;
    }

    /**
     * Deposits money into a player's wallet.
     *
     * @param player The player reference
     * @param amount The amount to deposit (must be positive)
     * @return true if successful
     */
    public boolean deposit(PlayerRef player, double amount) {
        if (amount <= 0) {
            return false;
        }

        WalletComponent wallet = getWallet(player);
        if (wallet != null) {
            wallet.deposit(amount);
            updateComponent(player, wallet);
            return true;
        }
        return false;
    }

    /**
     * Withdraws money from a player's wallet.
     *
     * @param player The player reference
     * @param amount The amount to withdraw
     * @return Transaction result
     */
    public Transaction withdraw(PlayerRef player, double amount) {
        if (amount <= 0) {
            return Transaction.fail("Invalid amount");
        }

        WalletComponent wallet = getWallet(player);
        if (wallet == null) {
            return Transaction.fail("Wallet not found");
        }

        if (!wallet.hasEnough(amount)) {
            return Transaction.fail("Insufficient funds");
        }

        boolean success = wallet.withdraw(amount);
        if (success) {
            updateComponent(player, wallet);
            return Transaction.success(amount);
        }

        return Transaction.fail("Withdrawal failed");
    }

    /**
     * Sets a player's balance to a specific amount.
     * Admin command only.
     *
     * @param player The player reference
     * @param amount The new balance
     * @return true if successful
     */
    public boolean setBalance(PlayerRef player, double amount) {
        WalletComponent wallet = getWallet(player);
        if (wallet != null) {
            wallet.setBalance(amount);
            updateComponent(player, wallet);
            return true;
        }
        return false;
    }

    /**
     * Transfers money from one player to another.
     *
     * @param from The sender
     * @param to The receiver
     * @param amount The amount to transfer
     * @return Transaction result
     */
    public Transaction transfer(PlayerRef from, PlayerRef to, double amount) {
        if (amount <= 0) {
            return Transaction.fail("Invalid amount");
        }

        if (from.equals(to)) {
            return Transaction.fail("Cannot transfer to yourself");
        }

        WalletComponent fromWallet = getWallet(from);
        WalletComponent toWallet = getWallet(to);

        if (fromWallet == null || toWallet == null) {
            return Transaction.fail("Player not found");
        }

        if (!fromWallet.hasEnough(amount)) {
            return Transaction.fail("Insufficient funds");
        }

        // Perform transfer
        fromWallet.withdraw(amount);
        toWallet.deposit(amount);

        // Update both components
        updateComponent(from, fromWallet);
        updateComponent(to, toWallet);

        return Transaction.success(amount);
    }

    /**
     * Updates a wallet component in the EntityStore.
     *
     * @param player The player reference
     * @param wallet The wallet component
     */
    private void updateComponent(PlayerRef player, WalletComponent wallet) {
        EntityStore store = getEntityStore();
        if (store != null && WalletComponent.TYPE != null) {
            try {
                HytaleUtils.addComponent(store, player, WalletComponent.TYPE, wallet);
            } catch (Exception e) {
                LOGGER.atSevere().log("Error updating wallet for " + player.getUsername() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Formats a currency amount for display using the default currency.
     *
     * @param amount The amount to format
     * @return Formatted string
     */
    public String format(double amount) {
        Currency defaultCurrency = getDefaultCurrency();
        if (defaultCurrency != null) {
            return defaultCurrency.formatAmount(amount);
        }
        return String.format("%.2f", amount);
    }

    /**
     * Formats a currency amount for display using a specific currency.
     *
     * @param currencyId The currency ID
     * @param amount The amount to format
     * @return Formatted string
     */
    public String format(String currencyId, double amount) {
        Currency currency = getCurrency(currencyId);
        if (currency != null) {
            return currency.formatAmount(amount);
        }
        return String.format("%.2f", amount);
    }
}
