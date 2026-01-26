package com.kukso.hy.lib;

import com.kukso.hy.lib.commands.CommandBootstrap;
import com.kukso.hy.lib.config.ConfigManager;
import com.kukso.hy.lib.locale.LocaleMan;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class Main extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getIdentifier().getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());

        Path dataDir = Path.of("mods/KuksoHyLib");

        // Initialize configuration (must be first)
        ConfigManager.init(this, dataDir);
        LOGGER.atInfo().log("Configuration loaded");

        // Initialize localization
        LocaleMan.init(this, dataDir);

        // Register commands
        CommandBootstrap.register(this);
    }

    @Override
    protected void start() {
        LOGGER.atInfo().log(this.getName() + " started successfully!");
    }

    @Override
    protected void shutdown() {
        // Shutdown localization
        LocaleMan.shutdown();

        // Shutdown configuration (should be last)
        ConfigManager.shutdown();

        LOGGER.atInfo().log(this.getName() + " shutdown successfully!");
    }
}
