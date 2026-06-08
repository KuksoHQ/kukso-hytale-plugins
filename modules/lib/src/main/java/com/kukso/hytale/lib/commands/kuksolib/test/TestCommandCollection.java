package com.kukso.hytale.lib.commands.kuksolib.test;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class TestCommandCollection extends AbstractCommandCollection {

    public TestCommandCollection() {
        super("test", "Test KuksoLib features (chatcolor, locale, etc.)");
        addAliases("t");
        requirePermission("kukso.command.kuksolib.test");

        addSubCommand(new TestChatColorCommand());
        addSubCommand(new TestHytaleUtilCommand());
        addSubCommand(new TestLocaleCommand());
    }
}
