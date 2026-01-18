package com.kukso.hy.lib.config;

import com.kukso.hy.lib.economy.Currency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Root configuration class for KuksoLib.
 * Contains all module configurations.
 */
public class KuksoConfig {

    private DatabaseConfig database = new DatabaseConfig();
    private EconomyConfig economy = new EconomyConfig();
    private PermissionConfig permission = new PermissionConfig();
    private LocaleConfig locale = new LocaleConfig();
    private ChatConfig chat = new ChatConfig();
    private LoggingConfig logging = new LoggingConfig();

    public DatabaseConfig getDatabase() {
        return database;
    }

    public EconomyConfig getEconomy() {
        return economy;
    }

    public PermissionConfig getPermission() {
        return permission;
    }

    public LocaleConfig getLocale() {
        return locale;
    }

    public ChatConfig getChat() {
        return chat;
    }

    public LoggingConfig getLogging() {
        return logging;
    }

    /**
     * Database configuration.
     */
    public static class DatabaseConfig {
        private String type = "sqlite";
        private MysqlConfig mysql = new MysqlConfig();

        public String getType() {
            return type;
        }

        public MysqlConfig getMysql() {
            return mysql;
        }

        public boolean isSqlite() {
            return "sqlite".equalsIgnoreCase(type);
        }

        public boolean isMysql() {
            return "mysql".equalsIgnoreCase(type);
        }
    }

    /**
     * MySQL-specific configuration.
     */
    public static class MysqlConfig {
        private String host = "localhost";
        private int port = 3306;
        private String database = "kuksolib";
        private String username = "root";
        private String password = "";
        private int poolSize = 10;
        private long connectionTimeout = 30000;

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getDatabase() {
            return database;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public int getPoolSize() {
            return poolSize;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public String getJdbcUrl() {
            return "jdbc:mysql://" + host + ":" + port + "/" + database;
        }
    }

    /**
     * Economy module configuration.
     */
    public static class EconomyConfig {
        private boolean enabled = true;
        private String defaultCurrency = "coins";
        private List<CurrencyConfig> currencies = new ArrayList<>();
        private boolean transactionLogging = true;

        public boolean isEnabled() {
            return enabled;
        }

        public String getDefaultCurrency() {
            return defaultCurrency;
        }

        public List<CurrencyConfig> getCurrencies() {
            return currencies;
        }

        public boolean isTransactionLogging() {
            return transactionLogging;
        }

        /**
         * Converts configured currencies to Currency records.
         *
         * @return List of Currency records
         */
        public List<Currency> toCurrencyRecords() {
            List<Currency> result = new ArrayList<>();
            for (CurrencyConfig cc : currencies) {
                result.add(cc.toCurrency());
            }
            return result;
        }
    }

    /**
     * Single currency configuration.
     */
    public static class CurrencyConfig {
        private String id;
        private String displayName;
        private String nameSingular;
        private String namePlural;
        private String symbol = "$";
        private String format = "{symbol}{amount}";
        private int decimalPlaces = 2;
        private double startingBalance = 0.0;
        private String componentId;

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName != null ? displayName : id;
        }

        public String getNameSingular() {
            return nameSingular != null ? nameSingular : getDisplayName();
        }

        public String getNamePlural() {
            return namePlural != null ? namePlural : getNameSingular();
        }

        public String getSymbol() {
            return symbol;
        }

        public String getFormat() {
            return format;
        }

        public int getDecimalPlaces() {
            return decimalPlaces;
        }

        public double getStartingBalance() {
            return startingBalance;
        }

        public String getComponentId() {
            return componentId != null ? componentId : "kuksolib:" + id;
        }

        /**
         * Converts this config to a Currency record.
         *
         * @return Currency record
         */
        public Currency toCurrency() {
            return new Currency(
                id,
                getDisplayName(),
                getNameSingular(),
                getNamePlural(),
                symbol,
                format,
                decimalPlaces,
                startingBalance,
                getComponentId()
            );
        }
    }

    /**
     * Permission module configuration.
     */
    public static class PermissionConfig {
        private boolean enabled = true;
        private String defaultProvider = "default";

        public boolean isEnabled() {
            return enabled;
        }

        public String getDefaultProvider() {
            return defaultProvider;
        }
    }

    /**
     * Locale module configuration.
     */
    public static class LocaleConfig {
        private boolean enabled = true;
        private String defaultLocale = "en_US";
        private boolean useClientLocale = true;

        public boolean isEnabled() {
            return enabled;
        }

        public String getDefaultLocale() {
            return defaultLocale;
        }

        public boolean isUseClientLocale() {
            return useClientLocale;
        }
    }

    /**
     * Chat module configuration.
     */
    public static class ChatConfig {
        private boolean enabled = true;
        private String defaultPrefix = "";
        private String defaultSuffix = "";

        public boolean isEnabled() {
            return enabled;
        }

        public String getDefaultPrefix() {
            return defaultPrefix;
        }

        public String getDefaultSuffix() {
            return defaultSuffix;
        }
    }

    /**
     * Logging configuration.
     */
    public static class LoggingConfig {
        private boolean enabled = true;
        private String transactionLogFile = "transactions.log";
        private boolean debugMode = false;

        public boolean isEnabled() {
            return enabled;
        }

        public String getTransactionLogFile() {
            return transactionLogFile;
        }

        public boolean isDebugMode() {
            return debugMode;
        }
    }

    /**
     * Creates a default configuration with a single "coins" currency.
     *
     * @return Default KuksoConfig
     */
    public static KuksoConfig createDefault() {
        KuksoConfig config = new KuksoConfig();

        // Add default "coins" currency
        CurrencyConfig coins = new CurrencyConfig();
        coins.id = "coins";
        coins.displayName = "Coins";
        coins.nameSingular = "Coin";
        coins.namePlural = "Coins";
        coins.symbol = "$";
        coins.format = "{symbol}{amount}";
        coins.decimalPlaces = 2;
        coins.startingBalance = 100.0;
        coins.componentId = "kuksolib:coins";
        config.economy.currencies.add(coins);

        return config;
    }
}
