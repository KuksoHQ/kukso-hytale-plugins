package com.kukso.hytale.warps;

import com.kukso.hytale.warps.command.DelWarpCommand;
import com.kukso.hytale.warps.command.ListWarpsCommand;
import com.kukso.hytale.warps.command.SetWarpCommand;
import com.kukso.hytale.warps.command.WarpCommand;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class Main extends JavaPlugin {

    private static final Path DATA_DIR = Path.of("mods", "KuksoWarps");

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        WarpConfigManager.init(this, DATA_DIR);

        WarpManager warpManager = new WarpManager();

        this.getCommandRegistry().registerCommand(new WarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new SetWarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new ListWarpsCommand(warpManager));
        this.getCommandRegistry().registerCommand(new DelWarpCommand(warpManager));
    }
}
