package com.kukso.hy.lib.command;

import com.kukso.hy.lib.command.sub.*;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.PluginBase;

import java.util.logging.Level;

/**
 * Handles command registration for KuksoLib.
 */
public final class CmdRegistrar {

    private CmdRegistrar() {}

    /**
     * Registers all KuksoLib commands.
     * @param plugin The plugin instance
     */
    public static void register(PluginBase plugin) {
        HytaleLogger LOGGER = plugin.getLogger();

        CmdManager mgr = new CmdManager();

        // Register subcommands
        mgr.register(new HelpCmd(mgr));
        mgr.register(new ReloadCmd(plugin));
        mgr.register(new VersionCmd(plugin));
        mgr.register(new TestCmd());
        mgr.register(new EconomyCmd());

        // Register the main command with the plugin's command registry
        plugin.getCommandRegistry().registerCommand(mgr);

        LOGGER.atInfo().log( "Commands (5) registered for " + plugin.getIdentifier().getName());
    }
}
