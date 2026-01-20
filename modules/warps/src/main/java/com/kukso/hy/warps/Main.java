package com.kukso.hy.warps;

import com.kukso.hy.lib.locale.LocaleMan;
import com.kukso.hy.warps.command.DelWarpCommand;
import com.kukso.hy.warps.command.ListWarpsCommand;
import com.kukso.hy.warps.command.SetWarpCommand;
import com.kukso.hy.warps.command.WarpCommand;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;

import javax.annotation.Nonnull;

public class Main extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.get("KuksoWarps");
    private final Config<KuksoWarpsConfig> config;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        this.config = this.withConfig("KuksoWarps", KuksoWarpsConfig.CODEC);
    }

    @Override
    protected void setup() {
        super.setup();

        // Check if KuksoLib is available
        if (!isKuksoLibAvailable()) {
            LOGGER.atSevere().log("[KuksoWarps] KuksoLib is not available! Some features may not work.");
        } else {
            LOGGER.atInfo().log("[KuksoWarps] KuksoLib detected. Using centralized LocaleMan.");
            LOGGER.atInfo().log("[KuksoWarps] Loaded locales: %s", LocaleMan.getLoadedLocales());
        }

        WarpManager warpManager = new WarpManager(this.config);

        // Register commands
        this.getCommandRegistry().registerCommand(new WarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new SetWarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new ListWarpsCommand(warpManager));

        this.getCommandRegistry().registerCommand(new DelWarpCommand(warpManager));
    }

    private boolean isKuksoLibAvailable() {
        try {
            Class.forName("com.kukso.hy.lib.Main");
            Class.forName("com.kukso.hy.lib.command.CmdInterface");
            Class.forName("com.kukso.hy.lib.locale.LocaleMan");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
