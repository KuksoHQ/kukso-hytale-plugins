package com.kukso.hy.lib.commands.kuksolib.test;

import com.kukso.hy.lib.util.MessageUtil;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

class TestChatColorCommand extends AbstractAsyncCommand {

    MessageUtil messageUtil = new MessageUtil();

    TestChatColorCommand() {
        super("chatcolor", "Test color formatting");
        addAliases("color", "colors");
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@NonNullDecl CommandContext context) {

        messageUtil.sendStringFormatted(context, "&e&l=== ColorMan Test ===");
        messageUtil.sendStringFormatted(context, "");
        messageUtil.sendStringFormatted(context, "&7Rainbow: &4R&ca&6i&en&ab&bo&9w&5!");
        messageUtil.sendStringFormatted(context, "&7Mixed: &l&cERROR: &r&7Something went &4&lwrong&r&7! &a(no it didn't)");
        messageUtil.sendStringFormatted(context, "&7Hex: &#FF0000Red &#00FF00Green &#0000FFBlue");
        messageUtil.sendStringFormatted(context, "&7Complex: &e&lKuksoLib &r&8» &aReady &7to &broll&7!");
        messageUtil.sendStringFormatted(context, "&7Standard: &00 &11 &22 &33 &44 &55 &66 &77 &88 &99 &aa &bb &cc &dd &ee &ff");
        messageUtil.sendStringFormatted(context, "&lBold &r&oItalic &r&7Normal");
        messageUtil.sendStringFormatted(context, "");
        messageUtil.sendStringFormatted(context, "&aAll color tests completed!");

        return CompletableFuture.completedFuture(null);
    }
}