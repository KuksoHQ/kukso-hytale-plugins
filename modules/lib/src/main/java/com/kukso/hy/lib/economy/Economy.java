package com.kukso.hy.lib.economy;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The main Economy interface that all economy providers must implement.
 * Supports multiple currencies with a default currency for convenience.
 *
 * <p>Plugins should use {@code ServiceManager.getProvider(Economy.class)} to obtain an instance.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Economy econ = ServiceManager.getProvider(Economy.class);
 * if (econ != null) {
 *     // Using default currency
 *     double balance = econ.getBalance(player);
 *     EconomyResponse response = econ.deposit(player, 100.0);
 *
 *     // Using specific currency
 *     double gems = econ.getBalance(player, "gems");
 *     EconomyResponse gemResponse = econ.deposit(player, "gems", 50.0);
 * }
 * }</pre>
 */
public interface Economy {

    // ==================== Provider Info ====================

    /**
     * Gets the name of the economy provider.
     *
     * @return Name of the economy provider
     */
    @Nonnull
    String getName();

    /**
     * Checks if this economy provider is enabled and ready.
     *
     * @return true if the economy provider is enabled
     */
    boolean isEnabled();

    // ==================== Currency Registry ====================

    /**
     * Gets all registered currency IDs.
     *
     * @return Set of currency IDs (e.g., {"gold", "gems", "coins"})
     */
    @Nonnull
    Set<String> getCurrencies();

    /**
     * Gets the default currency ID.
     *
     * @return Default currency ID (e.g., "gold")
     */
    @Nonnull
    String getDefaultCurrency();

    /**
     * Gets currency metadata by ID.
     *
     * @param currencyId Currency identifier
     * @return Currency info or null if not found
     */
    @Nullable
    Currency getCurrency(@Nonnull String currencyId);

    // ==================== Currency Info (with currencyId) ====================

    /**
     * Gets the name of a currency in singular form.
     *
     * @param currencyId Currency identifier
     * @return Currency name singular (e.g., "Gold", "Gem", "Coin")
     */
    @Nonnull
    String currencyNameSingular(@Nonnull String currencyId);

    /**
     * Gets the name of a currency in plural form.
     *
     * @param currencyId Currency identifier
     * @return Currency name plural (e.g., "Gold", "Gems", "Coins")
     */
    @Nonnull
    String currencyNamePlural(@Nonnull String currencyId);

    /**
     * Format an amount into a human-readable string for a specific currency.
     *
     * @param currencyId Currency identifier
     * @param amount     Amount to format
     * @return Formatted string (e.g., "100G", "50 Gems", "$1,234.56")
     */
    @Nonnull
    String format(@Nonnull String currencyId, double amount);

    /**
     * Returns the number of fractional digits supported by a currency.
     *
     * @param currencyId Currency identifier
     * @return Number of digits after decimal (0 for whole numbers only)
     */
    int fractionalDigits(@Nonnull String currencyId);

    // ==================== Currency Info (default currency convenience) ====================

    /**
     * Gets the name of the default currency in singular form.
     *
     * @return Currency name singular
     */
    @Nonnull
    default String currencyNameSingular() {
        return currencyNameSingular(getDefaultCurrency());
    }

    /**
     * Gets the name of the default currency in plural form.
     *
     * @return Currency name plural
     */
    @Nonnull
    default String currencyNamePlural() {
        return currencyNamePlural(getDefaultCurrency());
    }

    /**
     * Format an amount using the default currency.
     *
     * @param amount Amount to format
     * @return Formatted string
     */
    @Nonnull
    default String format(double amount) {
        return format(getDefaultCurrency(), amount);
    }

    /**
     * Returns the number of fractional digits for the default currency.
     *
     * @return Number of digits after decimal
     */
    default int fractionalDigits() {
        return fractionalDigits(getDefaultCurrency());
    }

    // ==================== Account Methods ====================

    /**
     * Checks if a player has an account.
     *
     * @param player Player to check
     * @return true if player has an account
     */
    boolean hasAccount(@Nonnull PlayerRef player);

    /**
     * Checks if a player has an account by UUID (for offline players).
     *
     * @param uuid Player UUID
     * @return true if player has an account
     */
    boolean hasAccount(@Nonnull UUID uuid);

    /**
     * Creates a player account if one doesn't exist.
     * Initializes all currencies with their starting balances.
     *
     * @param player Player to create account for
     * @return true if account was created, false if already exists
     */
    boolean createPlayerAccount(@Nonnull PlayerRef player);

    // ==================== Balance Methods (with currencyId) ====================

    /**
     * Gets the balance of a player for a specific currency.
     *
     * @param player     Player to check
     * @param currencyId Currency identifier
     * @return Current balance
     */
    double getBalance(@Nonnull PlayerRef player, @Nonnull String currencyId);

    /**
     * Gets the balance of a player by UUID for a specific currency.
     *
     * @param uuid       Player UUID
     * @param currencyId Currency identifier
     * @return Current balance
     */
    double getBalance(@Nonnull UUID uuid, @Nonnull String currencyId);

    /**
     * Checks if a player has at least the specified amount of a currency.
     *
     * @param player     Player to check
     * @param currencyId Currency identifier
     * @param amount     Amount to check
     * @return true if player has at least that amount
     */
    boolean has(@Nonnull PlayerRef player, @Nonnull String currencyId, double amount);

    // ==================== Balance Methods (default currency convenience) ====================

    /**
     * Gets the balance of a player for the default currency.
     *
     * @param player Player to check
     * @return Current balance
     */
    default double getBalance(@Nonnull PlayerRef player) {
        return getBalance(player, getDefaultCurrency());
    }

    /**
     * Gets the balance of a player by UUID for the default currency.
     *
     * @param uuid Player UUID
     * @return Current balance
     */
    default double getBalance(@Nonnull UUID uuid) {
        return getBalance(uuid, getDefaultCurrency());
    }

    /**
     * Checks if a player has at least the specified amount of the default currency.
     *
     * @param player Player to check
     * @param amount Amount to check
     * @return true if player has at least that amount
     */
    default boolean has(@Nonnull PlayerRef player, double amount) {
        return has(player, getDefaultCurrency(), amount);
    }

    // ==================== Transaction Methods (with currencyId) ====================

    /**
     * Withdraws an amount of a specific currency from a player's account.
     *
     * @param player     Player to withdraw from
     * @param currencyId Currency identifier
     * @param amount     Amount to withdraw (must be positive)
     * @return EconomyResponse with transaction result
     */
    @Nonnull
    EconomyResponse withdraw(@Nonnull PlayerRef player, @Nonnull String currencyId, double amount);

    /**
     * Deposits an amount of a specific currency into a player's account.
     *
     * @param player     Player to deposit to
     * @param currencyId Currency identifier
     * @param amount     Amount to deposit (must be positive)
     * @return EconomyResponse with transaction result
     */
    @Nonnull
    EconomyResponse deposit(@Nonnull PlayerRef player, @Nonnull String currencyId, double amount);

    /**
     * Sets a player's balance for a specific currency.
     *
     * @param player     Player to modify
     * @param currencyId Currency identifier
     * @param amount     New balance amount
     * @return EconomyResponse with transaction result
     */
    @Nonnull
    EconomyResponse setBalance(@Nonnull PlayerRef player, @Nonnull String currencyId, double amount);

    /**
     * Transfers a specific currency from one player to another.
     *
     * @param from       Source player
     * @param to         Destination player
     * @param currencyId Currency identifier
     * @param amount     Amount to transfer
     * @return EconomyResponse with transaction result
     */
    @Nonnull
    default EconomyResponse transfer(@Nonnull PlayerRef from, @Nonnull PlayerRef to,
                                     @Nonnull String currencyId, double amount) {
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
        return EconomyResponse.success(amount, getBalance(from, currencyId), "Transfer successful");
    }

    // ==================== Transaction Methods (default currency convenience) ====================

    /**
     * Withdraws an amount of the default currency from a player's account.
     *
     * @param player Player to withdraw from
     * @param amount Amount to withdraw (must be positive)
     * @return EconomyResponse with transaction result
     */
    @Nonnull
    default EconomyResponse withdraw(@Nonnull PlayerRef player, double amount) {
        return withdraw(player, getDefaultCurrency(), amount);
    }

    /**
     * Deposits an amount of the default currency into a player's account.
     *
     * @param player Player to deposit to
     * @param amount Amount to deposit (must be positive)
     * @return EconomyResponse with transaction result
     */
    @Nonnull
    default EconomyResponse deposit(@Nonnull PlayerRef player, double amount) {
        return deposit(player, getDefaultCurrency(), amount);
    }

    /**
     * Sets a player's balance for the default currency.
     *
     * @param player Player to modify
     * @param amount New balance amount
     * @return EconomyResponse with transaction result
     */
    @Nonnull
    default EconomyResponse setBalance(@Nonnull PlayerRef player, double amount) {
        return setBalance(player, getDefaultCurrency(), amount);
    }

    /**
     * Transfers the default currency from one player to another.
     *
     * @param from   Source player
     * @param to     Destination player
     * @param amount Amount to transfer
     * @return EconomyResponse with transaction result
     */
    @Nonnull
    default EconomyResponse transfer(@Nonnull PlayerRef from, @Nonnull PlayerRef to, double amount) {
        return transfer(from, to, getDefaultCurrency(), amount);
    }

    // ==================== Bank Methods (Optional) ====================

    /**
     * Checks if this economy supports banks.
     *
     * @return true if banks are supported
     */
    default boolean hasBankSupport() {
        return false;
    }

    /**
     * Creates a bank with the given name.
     *
     * @param name  Bank name
     * @param owner Owner player
     * @return EconomyResponse with result
     */
    @Nonnull
    default EconomyResponse createBank(@Nonnull String name, @Nonnull PlayerRef owner) {
        return EconomyResponse.notImplemented("Banks");
    }

    /**
     * Deletes a bank.
     *
     * @param name Bank name
     * @return EconomyResponse with result
     */
    @Nonnull
    default EconomyResponse deleteBank(@Nonnull String name) {
        return EconomyResponse.notImplemented("Banks");
    }

    /**
     * Gets the balance of a bank for a specific currency.
     *
     * @param name       Bank name
     * @param currencyId Currency identifier
     * @return EconomyResponse with balance
     */
    @Nonnull
    default EconomyResponse bankBalance(@Nonnull String name, @Nonnull String currencyId) {
        return EconomyResponse.notImplemented("Banks");
    }

    /**
     * Checks if a bank exists.
     *
     * @param name Bank name
     * @return true if bank exists
     */
    default boolean bankExists(@Nonnull String name) {
        return false;
    }

    /**
     * Withdraws a specific currency from a bank.
     *
     * @param name       Bank name
     * @param currencyId Currency identifier
     * @param amount     Amount to withdraw
     * @return EconomyResponse with result
     */
    @Nonnull
    default EconomyResponse bankWithdraw(@Nonnull String name, @Nonnull String currencyId, double amount) {
        return EconomyResponse.notImplemented("Banks");
    }

    /**
     * Deposits a specific currency to a bank.
     *
     * @param name       Bank name
     * @param currencyId Currency identifier
     * @param amount     Amount to deposit
     * @return EconomyResponse with result
     */
    @Nonnull
    default EconomyResponse bankDeposit(@Nonnull String name, @Nonnull String currencyId, double amount) {
        return EconomyResponse.notImplemented("Banks");
    }

    /**
     * Checks if a player is the owner of a bank.
     *
     * @param name   Bank name
     * @param player Player to check
     * @return true if player is the bank owner
     */
    default boolean isBankOwner(@Nonnull String name, @Nonnull PlayerRef player) {
        return false;
    }

    /**
     * Checks if a player is a member of a bank.
     *
     * @param name   Bank name
     * @param player Player to check
     * @return true if player is a bank member
     */
    default boolean isBankMember(@Nonnull String name, @Nonnull PlayerRef player) {
        return false;
    }

    /**
     * Gets list of all banks.
     *
     * @return List of bank names
     */
    @Nonnull
    default List<String> getBanks() {
        return List.of();
    }
}
