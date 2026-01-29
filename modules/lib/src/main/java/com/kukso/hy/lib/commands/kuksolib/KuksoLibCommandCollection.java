package com.kukso.hy.lib.commands.kuksolib;

import com.kukso.hy.lib.commands.kuksolib.test.TestCommandCollection;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.plugin.PluginBase;

public class KuksoLibCommandCollection extends AbstractCommandCollection {
    public KuksoLibCommandCollection(PluginBase plugin) {
        super("kuksolib", "Root command for KuksoLib");
        addAliases("klib");
        requirePermission ("kukso.command.kuksolib.root");

        addSubCommand(new HelpCommand());
        addSubCommand(new VersionCommand(plugin));
        addSubCommand(new ReloadCommand(plugin));

        addSubCommand(new PlayerInfoCommand());

        addSubCommand(new TestCommandCollection());
    }
}
