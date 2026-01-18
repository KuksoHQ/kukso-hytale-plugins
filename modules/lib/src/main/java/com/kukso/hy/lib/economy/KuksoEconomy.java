package com.kukso.hy.lib.economy;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Built-in Economy implementation for KuksoLib.
 * Uses SQLite for persistent storage and CurrencyManager for ECS-based runtime data.
 *
 * <p>This is the default economy provider that plugins can use or replace with their own.</p>
 *
 * <p>Data is stored in two places:</p>
 * <ul>
 *   <li><b>ECS Components</b> - Fast runtime access via CurrencyComponent</li>
 *   <li><b>SQLite Database</b> - Persistent storage for offline players and backups</li>
 * </ul>
 *
 * <p>When a player joins, their balances are loaded from SQLite into ECS components.
 * When balances change, both ECS and SQLite are updated.</p>
 */
public class KuksoEconomy implements Economy {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String DB_FILE = "economy.db";

    private final PluginBase plugin;
    private final Path dbPath;
    private Connection connection;
    private boolean enabled = false;

    // Cache for offline player accounts (UUID -> exists)
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
        this.dbPath = plugin.getDataDirectory().resolve(DB_FILE);
    }

    /**
     * Initializes the economy provider.
     * Creates database tables and loads account cache.
     *
     * @return true if initialization was successful
     */
    public boolean initialize() {
        try {
            // Create database connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            LOGGER.atInfo().log("Connected to SQLite database: " + dbPath);

            // Create tables
            createTables();

            // Load account cache
            loadAccountCache();

            enabled = true;
            LOGGER.atInfo().log("KuksoEconomy initialized successfully");
            return true;
        } catch (SQLException e) {
            LOGGER.atSevere().log("Failed to initialize KuksoEconomy: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Shuts down the economy provider.
     * Closes database connection.
     */
    public void shutdown() {
        enabled = false;
        if (connection != null) {
            try {
                connection.close();
                LOGGER.atInfo().log("KuksoEconomy database connection closed");
            } catch (SQLException e) {
                LOGGER.atWarning().log("Error closing database connection: " + e.getMessage());
            }
        }
        accountCache.clear();
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

    /**
     * Creates database tables if they don't exist.
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Accounts table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS accounts (
                    uuid TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))
                )
                """);

            // Balances table (supports multiple currencies)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS balances (
                    uuid TEXT NOT NULL,
                    currency_id TEXT NOT NULL,
                    balance REAL NOT NULL DEFAULT 0.0,
                    PRIMARY KEY (uuid, currency_id),
                    FOREIGN KEY (uuid) REFERENCES accounts(uuid) ON DELETE CASCADE
                )
                """);

            // Transaction log table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid TEXT NOT NULL,
                    currency_id TEXT NOT NULL,
                    type TEXT NOT NULL,
                    amount REAL NOT NULL,
                    balance_after REAL NOT NULL,
                    description TEXT,
                    timestamp INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))
                )
                """);

            // Create indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_balances_uuid ON balances(uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_uuid ON transactions(uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_timestamp ON transactions(timestamp)");

            LOGGER.atInfo().log("Database tables created/verified");
        }
    }

    /**
     * Loads account UUIDs into cache for quick hasAccount() checks.
     */
    private void loadAccountCache() throws SQLException {
        accountCache.clear();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid FROM accounts")) {
            while (rs.next()) {
                accountCache.put(UUID.fromString(rs.getString("uuid")), true);
            }
        }
        LOGGER.atInfo().log("Loaded " + accountCache.size() + " accounts into cache");
    }

    // ==================== Provider Info ====================

    @Override
    @Nonnull
    public String getName() {
        return "KuksoEconomy";
    }

    @Override
    public boolean isEnabled() {
        return enabled && connection != null;
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
        // Check cache first
        Boolean cached = accountCache.get(uuid);
        if (cached != null) {
            return cached;
        }

        // Check database
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT 1 FROM accounts WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                boolean exists = rs.next();
                accountCache.put(uuid, exists);
                return exists;
            }
        } catch (SQLException e) {
            LOGGER.atWarning().log("Error checking account existence: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean createPlayerAccount(@Nonnull PlayerRef player) {
        UUID uuid = player.getUuid();
        String username = player.getUsername();

        if (hasAccount(uuid)) {
            return false; // Account already exists
        }

        try {
            connection.setAutoCommit(false);
            try {
                // Create account
                try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO accounts (uuid, username) VALUES (?, ?)")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, username);
                    stmt.executeUpdate();
                }

                // Initialize balances for all registered currencies
                for (Currency currency : CurrencyManager.getCurrencies().values()) {
                    try (PreparedStatement stmt = connection.prepareStatement(
                            "INSERT INTO balances (uuid, currency_id, balance) VALUES (?, ?, ?)")) {
                        stmt.setString(1, uuid.toString());
                        stmt.setString(2, currency.id());
                        stmt.setDouble(3, currency.startingBalance());
                        stmt.executeUpdate();
                    }
                }

                connection.commit();
                accountCache.put(uuid, true);

                // Initialize ECS components if EntityStore is available
                if (entityStore != null) {
                    CurrencyManager.initializePlayer(player, entityStore);
                }

                LOGGER.atInfo().log("Created account for " + username + " (" + uuid + ")");
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.atSevere().log("Failed to create account for " + username + ": " + e.getMessage());
            return false;
        }
    }

    // ==================== Balance Methods ====================

    @Override
    public double getBalance(@Nonnull PlayerRef player, @Nonnull String currencyId) {
        // Try ECS first for online players (faster)
        if (entityStore != null) {
            double ecsBalance = CurrencyManager.getBalance(player, currencyId, entityStore);
            if (ecsBalance > 0 || hasAccount(player)) {
                return ecsBalance;
            }
        }

        // Fall back to database
        return getBalance(player.getUuid(), currencyId);
    }

    @Override
    public double getBalance(@Nonnull UUID uuid, @Nonnull String currencyId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT balance FROM balances WHERE uuid = ? AND currency_id = ?")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, currencyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            LOGGER.atWarning().log("Error getting balance: " + e.getMessage());
        }
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

        double currentBalance = getBalance(player, currencyId);
        if (currentBalance < amount) {
            return EconomyResponse.failure(currentBalance, "Insufficient funds");
        }

        double newBalance = currentBalance - amount;

        try {
            // Update database
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE balances SET balance = ? WHERE uuid = ? AND currency_id = ?")) {
                stmt.setDouble(1, newBalance);
                stmt.setString(2, uuid.toString());
                stmt.setString(3, currencyId);
                stmt.executeUpdate();
            }

            // Log transaction
            logTransaction(uuid, currencyId, "WITHDRAW", amount, newBalance, null);

            // Update ECS component if available
            if (entityStore != null) {
                CurrencyManager.setBalance(player, currencyId, newBalance, entityStore);
            }

            return EconomyResponse.success(amount, newBalance);
        } catch (SQLException e) {
            LOGGER.atSevere().log("Failed to withdraw: " + e.getMessage());
            return EconomyResponse.failure("Database error");
        }
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

        double currentBalance = getBalance(player, currencyId);
        double newBalance = currentBalance + amount;

        try {
            // Update database
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT OR REPLACE INTO balances (uuid, currency_id, balance) VALUES (?, ?, ?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, currencyId);
                stmt.setDouble(3, newBalance);
                stmt.executeUpdate();
            }

            // Log transaction
            logTransaction(uuid, currencyId, "DEPOSIT", amount, newBalance, null);

            // Update ECS component if available
            if (entityStore != null) {
                CurrencyManager.setBalance(player, currencyId, newBalance, entityStore);
            }

            return EconomyResponse.success(amount, newBalance);
        } catch (SQLException e) {
            LOGGER.atSevere().log("Failed to deposit: " + e.getMessage());
            return EconomyResponse.failure("Database error");
        }
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

        try {
            // Update database
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT OR REPLACE INTO balances (uuid, currency_id, balance) VALUES (?, ?, ?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, currencyId);
                stmt.setDouble(3, amount);
                stmt.executeUpdate();
            }

            // Log transaction
            logTransaction(uuid, currencyId, "SET", amount, amount, "Admin set balance");

            // Update ECS component if available
            if (entityStore != null) {
                CurrencyManager.setBalance(player, currencyId, amount, entityStore);
            }

            return EconomyResponse.success(amount, amount);
        } catch (SQLException e) {
            LOGGER.atSevere().log("Failed to set balance: " + e.getMessage());
            return EconomyResponse.failure("Database error");
        }
    }

    // ==================== Transaction Logging ====================

    /**
     * Logs a transaction to the database.
     */
    private void logTransaction(UUID uuid, String currencyId, String type,
                                double amount, double balanceAfter, String description) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO transactions (uuid, currency_id, type, amount, balance_after, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, currencyId);
            stmt.setString(3, type);
            stmt.setDouble(4, amount);
            stmt.setDouble(5, balanceAfter);
            stmt.setString(6, description);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.atWarning().log("Failed to log transaction: " + e.getMessage());
        }
    }

    // ==================== Admin Query Methods ====================

    /**
     * Gets all balances for a player across all currencies.
     *
     * @param uuid Player UUID
     * @return Map of currency ID to balance
     */
    @Nonnull
    public Map<String, Double> getAllBalances(@Nonnull UUID uuid) {
        Map<String, Double> balances = new LinkedHashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT currency_id, balance FROM balances WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    balances.put(rs.getString("currency_id"), rs.getDouble("balance"));
                }
            }
        } catch (SQLException e) {
            LOGGER.atWarning().log("Error getting all balances: " + e.getMessage());
        }
        return balances;
    }

    /**
     * Gets top balances for a currency.
     *
     * @param currencyId Currency identifier
     * @param limit      Maximum number of results
     * @return List of entries (UUID, balance) sorted by balance descending
     */
    @Nonnull
    public List<Map.Entry<UUID, Double>> getTopBalances(@Nonnull String currencyId, int limit) {
        List<Map.Entry<UUID, Double>> topList = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT uuid, balance FROM balances WHERE currency_id = ? ORDER BY balance DESC LIMIT ?")) {
            stmt.setString(1, currencyId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    double balance = rs.getDouble("balance");
                    topList.add(Map.entry(uuid, balance));
                }
            }
        } catch (SQLException e) {
            LOGGER.atWarning().log("Error getting top balances: " + e.getMessage());
        }
        return topList;
    }

    /**
     * Syncs a player's ECS balances from the database.
     * Called when a player joins.
     *
     * @param player Player to sync
     * @param store  The EntityStore for the player
     */
    public void syncPlayerFromDatabase(@Nonnull PlayerRef player, @Nonnull EntityStore store) {
        UUID uuid = player.getUuid();
        Map<String, Double> balances = getAllBalances(uuid);

        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            CurrencyManager.setBalance(player, entry.getKey(), entry.getValue(), store);
        }

        LOGGER.atFine().log("Synced balances for " + player.getUsername() + " from database");
    }
}
