package com.kukso.hy.lib.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages KuksoLib configuration loading, saving, and access.
 * Provides a centralized way to access plugin configuration.
 */
public final class ConfigManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final String CONFIG_RESOURCE_PATH = "config.json";

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    private static KuksoConfig config;
    private static Path configPath;
    private static JavaPlugin plugin;

    private ConfigManager() {
        // Utility class
    }

    /**
     * Initializes the configuration manager.
     * Loads configuration from file or creates default if not exists.
     *
     * @param pluginInstance The plugin instance
     * @param dataDirectory  The base directory for configuration files
     */
    public static void init(JavaPlugin pluginInstance, Path dataDirectory) {
        plugin = pluginInstance;
        configPath = dataDirectory.resolve(CONFIG_FILE_NAME);

        // Extract default config if it doesn't exist
        extractDefaultConfig();

        // Load configuration
        load();
    }

    /**
     * Loads configuration from file.
     * Falls back to default configuration if loading fails.
     */
    public static void load() {
        if (configPath == null) {
            LOGGER.atWarning().log("ConfigManager not initialized, using default config");
            config = KuksoConfig.createDefault();
            return;
        }

        if (!Files.exists(configPath)) {
            LOGGER.atInfo().log("Config file not found, creating default config");
            config = KuksoConfig.createDefault();
            save();
            return;
        }

        try (InputStream is = Files.newInputStream(configPath);
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            config = GSON.fromJson(reader, KuksoConfig.class);
            if (config == null) {
                LOGGER.atWarning().log("Config file was empty, using default config");
                config = KuksoConfig.createDefault();
            }
            LOGGER.atInfo().log("Configuration loaded successfully");
        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to load configuration: " + e.getMessage());
            config = KuksoConfig.createDefault();
        }
    }

    /**
     * Saves current configuration to file.
     */
    public static void save() {
        if (configPath == null || config == null) {
            LOGGER.atWarning().log("Cannot save config: ConfigManager not initialized");
            return;
        }

        try {
            // Ensure parent directory exists
            Files.createDirectories(configPath.getParent());

            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
                LOGGER.atInfo().log("Configuration saved successfully");
            }
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to save configuration: " + e.getMessage());
        }
    }

    /**
     * Reloads configuration from file.
     */
    public static void reload() {
        LOGGER.atInfo().log("Reloading configuration...");
        load();
    }

    /**
     * Extracts the default configuration from plugin resources.
     */
    private static void extractDefaultConfig() {
        if (configPath == null) {
            return;
        }

        if (Files.exists(configPath)) {
            return;
        }

        try {
            // Ensure parent directory exists
            Files.createDirectories(configPath.getParent());

            try (InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_RESOURCE_PATH)) {
                if (is != null) {
                    Files.copy(is, configPath);
                    LOGGER.atInfo().log("Default configuration extracted");
                } else {
                    // Resource not found, create default programmatically
                    config = KuksoConfig.createDefault();
                    save();
                    LOGGER.atInfo().log("Default configuration created");
                }
            }
        } catch (IOException e) {
            LOGGER.atWarning().log("Failed to extract default config: " + e.getMessage());
        }
    }

    /**
     * Gets the current configuration.
     *
     * @return The KuksoConfig instance
     */
    public static KuksoConfig get() {
        if (config == null) {
            config = KuksoConfig.createDefault();
        }
        return config;
    }

    /**
     * Gets the permission configuration.
     *
     * @return Permission configuration
     */
    public static KuksoConfig.PermissionConfig getPermission() {
        return get().getPermission();
    }

    /**
     * Gets the locale configuration.
     *
     * @return Locale configuration
     */
    public static KuksoConfig.LocaleConfig getLocale() {
        return get().getLocale();
    }

    /**
     * Gets the chat configuration.
     *
     * @return Chat configuration
     */
    public static KuksoConfig.ChatConfig getChat() {
        return get().getChat();
    }

    /**
     * Gets the logging configuration.
     *
     * @return Logging configuration
     */
    public static KuksoConfig.LoggingConfig getLogging() {
        return get().getLogging();
    }

    /**
     * Checks if the permission module is enabled.
     *
     * @return true if permission is enabled
     */
    public static boolean isPermissionEnabled() {
        return get().getPermission().isEnabled();
    }

    /**
     * Checks if the locale module is enabled.
     *
     * @return true if locale is enabled
     */
    public static boolean isLocaleEnabled() {
        return get().getLocale().isEnabled();
    }

    /**
     * Checks if the chat module is enabled.
     *
     * @return true if chat is enabled
     */
    public static boolean isChatEnabled() {
        return get().getChat().isEnabled();
    }

    /**
     * Checks if debug mode is enabled.
     *
     * @return true if debug mode is enabled
     */
    public static boolean isDebugMode() {
        return get().getLogging().isDebugMode();
    }

    /**
     * Shuts down the configuration manager.
     */
    public static void shutdown() {
        // Save any pending changes
        if (config != null && configPath != null) {
            save();
        }
        config = null;
        configPath = null;
        plugin = null;
    }
}
