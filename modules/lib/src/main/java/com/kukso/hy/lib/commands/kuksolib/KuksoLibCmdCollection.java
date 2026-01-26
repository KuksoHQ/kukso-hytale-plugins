package com.kukso.hy.lib.commands.kuksolib;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.kukso.hy.lib.commands.kuksolib.test.TestChatColorCommand;
import com.kukso.hy.lib.commands.kuksolib.test.TestLocaleCommand;

public class KuksoLibCmdCollection extends AbstractCommandCollection {
    public KuksoLibCmdCollection(PluginBase plugin) {
        super("kuksolib", "Root command for KuksoLib");
        addAliases("klib");
        requirePermission ("kukso.command.kuksolib.root");

        addSubCommand(new HelpCommand());
        addSubCommand(new VersionCommand(plugin));
        addSubCommand(new ReloadCommand(plugin));
        addSubCommand(new TestCmdCollection());
        addSubCommand(new PlayerInfoCommand());

    }

    private static class TestCmdCollection extends AbstractCommandCollection {

        public TestCmdCollection() {
            super("test", "Test KuksoLib features (chatcolor, locale, etc.)");

            addAliases("t");
            requirePermission("kukso.command.kuksolib.test");

            addSubCommand(new TestLocaleCommand());
            addSubCommand(new TestChatColorCommand());
        }
    }
}
