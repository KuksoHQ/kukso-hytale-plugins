package com.kukso.hy.lib;

import com.kukso.hy.lib.command.CmdRegistrar;
import com.kukso.hy.lib.economy.EconomyListener;
import com.kukso.hy.lib.economy.EconomyManager;
import com.kukso.hy.lib.locale.LocaleMan;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

public class Main extends JavaPlugin {

    String MAIN_CMD = "kuksolib";
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static Main instance;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    public static Main getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        instance = this;
        LOGGER.atInfo().log("Setting up plugin " + this.getName());

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
        CmdRegistrar.register(instance);
    }

    @Override
    protected void start() {
        LOGGER.atInfo().log(this.getName() + " enabled successfully!");
    }

    @Override
    public void shutdown() {
        // Unregister economy listener
        if (economyListener != null) {
            economyListener.unregister();
        }

        // Shutdown localization
        LocaleMan.shutdown();

        LOGGER.atInfo().log(this.getName() + " shutdown successfully!");
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}
