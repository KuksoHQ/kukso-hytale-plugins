package com.kukso.hy.warps;

import com.hypixel.hytale.logger.HytaleLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Handles migration from the old config path to the new custom data directory.
 *
 * Old path: mods/com.kukso.hy.warps_KuksoHyWarps/KuksoWarps.json
 * New path: mods/KuksoHyWarps/warps.json
 */
public final class ConfigMigration {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final Path OLD_CONFIG_PATH = Path.of("mods", "com.kukso.hy_KuksoWarps", "KuksoWarps.json");
    private static final Path OLD_CONFIG_PATH2 = Path.of("mods", "com.kukso.hy_KuksoWarps", "KuksoWarps.json.bak");
    private static final Path OLD_CONFIG_DIR = Path.of("mods", "com.kukso.hy_KuksoWarps");

    private ConfigMigration() {}

    /**
     * Attempts to migrate config from old location to new location.
     *
     * @param newConfigPath The new config file path
     * @return true if migration was performed, false if no migration was needed
     */
    public static boolean migrate(Path newConfigPath) {
        // Check if old config exists
        if (!Files.exists(OLD_CONFIG_PATH)) {
            return false;
        }

        // Check if new config already exists (don't overwrite)
        if (Files.exists(newConfigPath)) {
            LOGGER.atInfo().log("New config already exists, skipping migration. Old config at: %s", OLD_CONFIG_PATH);
            return false;
        }

        try {
            // Ensure new config directory exists
            Files.createDirectories(newConfigPath.getParent());

            // Copy old config to new location
            Files.copy(OLD_CONFIG_PATH, newConfigPath, StandardCopyOption.COPY_ATTRIBUTES);
            LOGGER.atInfo().log("Migrated config from %s to %s", OLD_CONFIG_PATH, newConfigPath);

            // Rename old config file to .bak for backup
            Path backupPath = OLD_CONFIG_PATH.resolveSibling(OLD_CONFIG_PATH.getFileName() + ".bak");
            Files.move(OLD_CONFIG_PATH, backupPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.atInfo().log("Renamed old config file to: %s", backupPath);

            // Try to delete old directory if empty
            deleteDirectoryIfEmpty(OLD_CONFIG_DIR);

            return true;
        } catch (IOException e) {
            LOGGER.atSevere().log("Failed to migrate config: %s", e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a directory if it's empty.
     */
    private static void deleteDirectoryIfEmpty(Path directory) {
        try {
            if (Files.isDirectory(directory) && isDirectoryEmpty(directory)) {
                Files.delete(directory);
                LOGGER.atInfo().log("Deleted empty old config directory: %s", directory);
            }
        } catch (IOException e) {
            LOGGER.atWarning().log("Could not delete old config directory: %s", e.getMessage());
        }
    }

    private static boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var entries = Files.list(directory)) {
            return entries.findFirst().isEmpty();
        }
    }
}
