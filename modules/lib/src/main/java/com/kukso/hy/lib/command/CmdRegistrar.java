package com.kukso.hy.lib.command;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.kukso.hy.lib.command.sub.*;

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
        HytaleLogger logger = plugin.getLogger();

        CmdManager mgr = new CmdManager();

        // Register subcommands
        mgr.register(new HelpCmd(mgr));
        mgr.register(new ReloadCmd(plugin));
        mgr.register(new VersionCmd(plugin));
        mgr.register(new TestCmd());

        // Register the main command with the plugin's command registry
        plugin.getCommandRegistry().registerCommand(mgr);

        logger.at(Level.INFO).log("Commands registered.");
    }
}
