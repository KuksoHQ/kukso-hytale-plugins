package com.kukso.hy.warps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages KuksoWarps configuration loading, saving, and access.
 */
public final class WarpConfigManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String CONFIG_FILE_NAME = "warps.json";

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    private static KuksoWarpsConfig config;
    private static Path configPath;

    private WarpConfigManager() {
        // Utility class
    }

    /**
     * Initializes the configuration manager.
     *
     * @param pluginInstance The plugin instance
     * @param dataDirectory  The base directory for configuration files
     */
    public static void init(JavaPlugin pluginInstance, Path dataDirectory) {
        configPath = dataDirectory.resolve(CONFIG_FILE_NAME);

        // Attempt migration from old config location
        ConfigMigration.migrate(configPath);

        load();
    }

    /**
     * Loads configuration from file.
     */
    public static void load() {
        if (configPath == null) {
            LOGGER.atWarning().log("WarpConfigManager not initialized, using default config");
            config = new KuksoWarpsConfig();
            return;
        }

        if (!Files.exists(configPath)) {
            LOGGER.atInfo().log("Warps config file not found, creating default config");
            config = new KuksoWarpsConfig();
            save();
            return;
        }

        try (InputStream is = Files.newInputStream(configPath);
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            config = GSON.fromJson(reader, KuksoWarpsConfig.class);
            if (config == null) {
                LOGGER.atWarning().log("Warps config file was empty, using default config");
                config = new KuksoWarpsConfig();
            }
            LOGGER.atInfo().log("Warps configuration loaded successfully");
        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to load warps configuration: " + e.getMessage());
            config = new KuksoWarpsConfig();
        }
    }

    /**
     * Saves current configuration to file.
     */
    public static void save() {
        if (configPath == null || config == null) {
            LOGGER.atWarning().log("Cannot save config: WarpConfigManager not initialized");
            return;
        }

        try {
            Files.createDirectories(configPath.getParent());

            // Create backup before saving if file exists
            if (Files.exists(configPath)) {
                Path backupPath = configPath.resolveSibling(CONFIG_FILE_NAME + ".bak");
                Files.copy(configPath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to save warps configuration: " + e.getMessage());
        }
    }

    /**
     * Gets the current configuration.
     *
     * @return The KuksoWarpsConfig instance
     */
    public static KuksoWarpsConfig get() {
        if (config == null) {
            config = new KuksoWarpsConfig();
        }
        return config;
    }

    /**
     * Shuts down the configuration manager.
     */
    public static void shutdown() {
        if (config != null && configPath != null) {
            save();
        }
        config = null;
        configPath = null;
    }
}
