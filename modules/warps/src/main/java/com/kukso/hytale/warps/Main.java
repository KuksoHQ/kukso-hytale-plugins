package com.kukso.hytale.warps;

import com.kukso.hytale.warps.command.DelWarpCommand;
import com.kukso.hytale.warps.command.ListWarpsCommand;
import com.kukso.hytale.warps.command.SetWarpCommand;
import com.kukso.hytale.warps.command.WarpCommand;
//import com.kukso.hytale.lib.locale.LocaleMan;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class Main extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.get("KuksoWarps");
    private static final Path DATA_DIR = Path.of("mods", "KuksoWarps");

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // Initialize config at custom path
        WarpConfigManager.init(this, DATA_DIR);

        // Check if KuksoLib is available
        if (!isKuksoLibAvailable()) {
            LOGGER.atSevere().log("[KuksoWarps] KuksoLib is not available! Some features may not work.");
        } else {
            LOGGER.atInfo().log("[KuksoWarps] KuksoLib detected. Using centralized LocaleMan and ColorMan.");
            //LOGGER.atInfo().log("[KuksoWarps] Loaded locales: %s", LocaleMan.getLoadedLocales());
        }

        WarpManager warpManager = new WarpManager();

        // Register commands
        this.getCommandRegistry().registerCommand(new WarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new SetWarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new ListWarpsCommand(warpManager));

        this.getCommandRegistry().registerCommand(new DelWarpCommand(warpManager));
    }

    private boolean isKuksoLibAvailable() {
        try {
            Class.forName("com.kukso.hytale.lib.Main");
            Class.forName("com.kukso.hytale.lib.command.CmdInterface");
            Class.forName("com.kukso.hytale.lib.locale.LocaleMan");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
