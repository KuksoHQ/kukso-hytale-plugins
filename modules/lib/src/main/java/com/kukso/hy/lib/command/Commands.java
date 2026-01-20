package com.kukso.hy.lib.command;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.PluginBase;

/**
 * Internal command registration for KuksoLib.
 * This class sets up all of KuksoLib's own commands.
 *
 * <p>Called from Main.start() to initialize KuksoLib's commands.</p>
 */
public final class Commands {

    private Commands() {
        // Utility class
    }

    /**
     * Registers all KuksoLib commands.
     *
     * @param plugin The KuksoLib plugin instance
     */
    public static void register(PluginBase plugin) {
        HytaleLogger logger = plugin.getLogger();

        // Register tree command: /kuksolib <subcommand>
        TreeManager mgr = CommandRegistrar.treeWithAliases(plugin, "kuksolib", "KuksoLib main command", "klib");
        mgr.register(new HelpCmd(mgr));
        mgr.register(new ReloadCmd(plugin));
        mgr.register(new VersionCmd(plugin));
        mgr.register(new TestCmd());
        mgr.register(new WalletCmd());

        // Register standalone commands
        CommandRegistrar.standalone(plugin, new PlayerInfoCmd());

        logger.atInfo().log("KuksoLib commands registered");
    }
}
