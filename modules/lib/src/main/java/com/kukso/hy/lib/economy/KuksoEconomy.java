package com.kukso.hy.lib.economy;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Built-in Economy implementation for KuksoLib.
 * Uses CurrencyManager for ECS-based runtime data.
 *
 * <p>This is the default economy provider that plugins can use or replace with their own.</p>
 *
 * <p>Data is stored in ECS Components for fast runtime access via CurrencyComponent.</p>
 */
public class KuksoEconomy implements Economy {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final PluginBase plugin;
    private boolean enabled = false;

    // Cache for player accounts (UUID -> exists)
    private final Map<UUID, Boolean> accountCache = new ConcurrentHashMap<>();

    // EntityStore reference for ECS operations
    private EntityStore entityStore;

    /**
     * Creates a new KuksoEconomy instance.
     *
     * @param plugin The plugin instance
     */
    public KuksoEconomy(@Nonnull PluginBase plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the economy provider.
     *
     * @return true if initialization was successful
     */
    public boolean initialize() {
        enabled = true;
        LOGGER.atInfo().log("KuksoEconomy initialized successfully");
        return true;
    }

    /**
     * Shuts down the economy provider.
     */
    public void shutdown() {
        enabled = false;
        accountCache.clear();
        LOGGER.atInfo().log("KuksoEconomy shutdown complete");
    }

    /**
     * Sets the EntityStore reference for ECS operations.
     *
     * @param store The EntityStore to use
     */
    public void setEntityStore(@Nullable EntityStore store) {
        this.entityStore = store;
        CurrencyManager.setEntityStore(store);
    }

    // ==================== Provider Info ====================

    @Override
    @Nonnull
    public String getName() {
        return "KuksoEconomy";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // ==================== Currency Registry ====================

    @Override
    @Nonnull
    public Set<String> getCurrencies() {
        return CurrencyManager.getCurrencyIds();
    }

    @Override
    @Nonnull
    public String getDefaultCurrency() {
        String defaultId = CurrencyManager.getDefaultCurrencyId();
        return defaultId != null ? defaultId : "default";
    }

    @Override
    @Nullable
    public Currency getCurrency(@Nonnull String currencyId) {
        return CurrencyManager.getCurrency(currencyId);
    }

    // ==================== Currency Info ====================

    @Override
    @Nonnull
    public String currencyNameSingular(@Nonnull String currencyId) {
        Currency currency = CurrencyManager.getCurrency(currencyId);
        return currency != null ? currency.nameSingular() : currencyId;
    }

    @Override
    @Nonnull
    public String currencyNamePlural(@Nonnull String currencyId) {
        Currency currency = CurrencyManager.getCurrency(currencyId);
        return currency != null ? currency.namePlural() : currencyId;
    }

    @Override
    @Nonnull
    public String format(@Nonnull String currencyId, double amount) {
        return CurrencyManager.format(currencyId, amount);
    }

    @Override
    public int fractionalDigits(@Nonnull String currencyId) {
        Currency currency = CurrencyManager.getCurrency(currencyId);
        return currency != null ? currency.decimalPlaces() : 2;
    }

    // ==================== Account Methods ====================

    @Override
    public boolean hasAccount(@Nonnull PlayerRef player) {
        return hasAccount(player.getUuid());
    }

    @Override
    public boolean hasAccount(@Nonnull UUID uuid) {
        return accountCache.getOrDefault(uuid, false);
    }

    @Override
    public boolean createPlayerAccount(@Nonnull PlayerRef player) {
        UUID uuid = player.getUuid();

        if (hasAccount(uuid)) {
            return false; // Account already exists
        }

        accountCache.put(uuid, true);

        // Initialize ECS components if EntityStore is available
        if (entityStore != null) {
            CurrencyManager.initializePlayer(player, entityStore);
        }

        LOGGER.atInfo().log("Created account for " + player.getUsername() + " (" + uuid + ")");
        return true;
    }

    // ==================== Balance Methods ====================

    @Override
    public double getBalance(@Nonnull PlayerRef player, @Nonnull String currencyId) {
        if (entityStore != null) {
            return CurrencyManager.getBalance(player, currencyId, entityStore);
        }
        return 0.0;
    }

    @Override
    public double getBalance(@Nonnull UUID uuid, @Nonnull String currencyId) {
        // ECS-only mode: cannot get balance for offline players by UUID alone
        return 0.0;
    }

    @Override
    public boolean has(@Nonnull PlayerRef player, @Nonnull String currencyId, double amount) {
        return getBalance(player, currencyId) >= amount;
    }

    // ==================== Transaction Methods ====================

    @Override
    @Nonnull
    public EconomyResponse withdraw(@Nonnull PlayerRef player, @Nonnull String currencyId, double amount) {
        if (amount <= 0) {
            return EconomyResponse.failure("Amount must be positive");
        }

        if (!CurrencyManager.hasCurrency(currencyId)) {
            return EconomyResponse.failure("Unknown currency: " + currencyId);
        }

        UUID uuid = player.getUuid();
        if (!hasAccount(uuid)) {
            return EconomyResponse.failure("Account not found");
        }

        if (entityStore == null) {
            return EconomyResponse.failure("EntityStore not available");
        }

        double currentBalance = getBalance(player, currencyId);
        if (currentBalance < amount) {
            return EconomyResponse.failure(currentBalance, "Insufficient funds");
        }

        double newBalance = currentBalance - amount;

        // Update ECS component
        CurrencyManager.setBalance(player, currencyId, newBalance, entityStore);

        return EconomyResponse.success(amount, newBalance);
    }

    @Override
    @Nonnull
    public EconomyResponse deposit(@Nonnull PlayerRef player, @Nonnull String currencyId, double amount) {
        if (amount <= 0) {
            return EconomyResponse.failure("Amount must be positive");
        }

        if (!CurrencyManager.hasCurrency(currencyId)) {
            return EconomyResponse.failure("Unknown currency: " + currencyId);
        }

        UUID uuid = player.getUuid();
        if (!hasAccount(uuid)) {
            // Auto-create account
            if (!createPlayerAccount(player)) {
                return EconomyResponse.failure("Failed to create account");
            }
        }

        if (entityStore == null) {
            return EconomyResponse.failure("EntityStore not available");
        }

        double currentBalance = getBalance(player, currencyId);
        double newBalance = currentBalance + amount;

        // Update ECS component
        CurrencyManager.setBalance(player, currencyId, newBalance, entityStore);

        return EconomyResponse.success(amount, newBalance);
    }

    @Override
    @Nonnull
    public EconomyResponse setBalance(@Nonnull PlayerRef player, @Nonnull String currencyId, double amount) {
        if (amount < 0) {
            return EconomyResponse.failure("Amount cannot be negative");
        }

        if (!CurrencyManager.hasCurrency(currencyId)) {
            return EconomyResponse.failure("Unknown currency: " + currencyId);
        }

        UUID uuid = player.getUuid();
        if (!hasAccount(uuid)) {
            if (!createPlayerAccount(player)) {
                return EconomyResponse.failure("Failed to create account");
            }
        }

        if (entityStore == null) {
            return EconomyResponse.failure("EntityStore not available");
        }

        // Update ECS component
        CurrencyManager.setBalance(player, currencyId, amount, entityStore);

        return EconomyResponse.success(amount, amount);
    }

    // ==================== Admin Query Methods ====================

    /**
     * Gets all balances for an online player across all currencies.
     *
     * @param player Player reference
     * @return Map of currency ID to balance
     */
    @Nonnull
    public Map<String, Double> getAllBalances(@Nonnull PlayerRef player) {
        Map<String, Double> balances = new LinkedHashMap<>();
        if (entityStore != null) {
            for (String currencyId : CurrencyManager.getCurrencyIds()) {
                balances.put(currencyId, CurrencyManager.getBalance(player, currencyId, entityStore));
            }
        }
        return balances;
    }
}
