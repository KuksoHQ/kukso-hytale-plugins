package com.kukso.hy.warps;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.kukso.hy.warps.commands.DelWarpCommand;
import com.kukso.hy.warps.commands.ListWarpsCommand;
import com.kukso.hy.warps.commands.SetWarpCommand;
import com.kukso.hy.warps.commands.WarpCommand;

import javax.annotation.Nonnull;

public class Main extends JavaPlugin {

    private final Config<KuksoWarpsConfig> config;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        this.config = this.withConfig("KuksoWarps", KuksoWarpsConfig.CODEC);
    }

    @Override
    protected void setup() {
        super.setup();
        
        WarpManager warpManager = new WarpManager(this.config);
        
        this.getCommandRegistry().registerCommand(new WarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new SetWarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new DelWarpCommand(warpManager));
        this.getCommandRegistry().registerCommand(new ListWarpsCommand(warpManager));
    }
}