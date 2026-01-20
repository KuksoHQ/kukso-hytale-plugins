package com.kukso.hy.lib;

import com.kukso.hy.lib.command.Commands;
import com.kukso.hy.lib.config.ConfigManager;
import com.kukso.hy.lib.economy.EconomyListener;
import com.kukso.hy.lib.economy.EconomyManager;
import com.kukso.hy.lib.locale.LocaleMan;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

public class Main extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private EconomyManager economyManager;
    private EconomyListener economyListener;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getIdentifier().getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());

        // Initialize configuration (must be first)
        ConfigManager.init(this);
        LOGGER.atInfo().log("Configuration loaded");

        // Initialize localization
        LocaleMan.init(this);

        // Initialize Economy Manager (registers ComponentType)
        economyManager = new EconomyManager();
        LOGGER.atInfo().log("Economy Manager initialized");

        // Register Economy Listener
        economyListener = new EconomyListener(this, economyManager);
        economyListener.register();
        LOGGER.atInfo().log("Economy Listener registered");

        // Register commands
        Commands.register(this);
    }

    @Override
    protected void start() {
        LOGGER.atInfo().log(this.getName() + " started successfully!");
    }

    @Override
    protected void shutdown() {
        // Unregister economy listener
        if (economyListener != null) {
            economyListener.unregister();
        }

        // Shutdown localization
        LocaleMan.shutdown();

        // Shutdown configuration (should be last)
        ConfigManager.shutdown();

        LOGGER.atInfo().log(this.getName() + " shutdown successfully!");
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}
