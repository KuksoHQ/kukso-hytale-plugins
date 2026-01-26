package com.kukso.hy.lib.commands;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.kukso.hy.lib.commands.kuksolib.KuksoLibCmdCollection;

/**
 * Internal command registration for KuksoLib.
 * This class sets up all of KuksoLib's own commands.
 *
 * <p>Called from Main.start() to initialize KuksoLib's commands.</p>
 */
public final class CommandBootstrap {

    private CommandBootstrap() {
        // Utility class
    }

    /**
     * Registers all KuksoLib commands.
     *
     * @param plugin The KuksoLib plugin instance
     */
    public static void register(PluginBase plugin) {
        HytaleLogger logger = plugin.getLogger();
        CommandRegistry registry = plugin.getCommandRegistry();
        registry.registerCommand(new KuksoLibCmdCollection(plugin));

        // Register tree command: /kuksolib <subcommand>
//        CommandTreeBase mgr = CommandRegistrar.treeWithAliases(plugin, "kuksolib", "KuksoLib main command", "klib");
//        mgr.register(new CmdSubHelp(mgr));
//        mgr.register(new CmdSubReload(plugin));
//        mgr.register(new CmdSubVersion(plugin));
//        mgr.register(new CmdSubTest());

        // Register standalone commands
        //CommandRegistrar.standalone(plugin, new CmdSinglePlayerInfo());

        logger.atInfo().log("KuksoLib commands registered");
    }
}
