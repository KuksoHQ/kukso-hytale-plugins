package com.kukso.hy.lib.config;

/**
 * Root configuration class for KuksoLib.
 * Contains all module configurations.
 */
public class KuksoConfig {

    private PermissionConfig permission = new PermissionConfig();
    private LocaleConfig locale = new LocaleConfig();
    private ChatConfig chat = new ChatConfig();
    private LoggingConfig logging = new LoggingConfig();

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
     * Creates a default configuration.
     *
     * @return Default KuksoConfig
     */
    public static KuksoConfig createDefault() {
        return new KuksoConfig();
    }
}
