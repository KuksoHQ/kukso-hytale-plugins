package com.kukso.hy.lib.commands.kuksolib.test;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.kukso.hy.lib.util.ColorUtil;
import java.util.concurrent.CompletableFuture;

public class TestChatColorCommand extends AbstractAsyncCommand {

    public TestChatColorCommand() {
        super("chatcolor", "Test color formatting");
        // Porting the switch cases: "color", "colors"
        addAliases("color", "colors");
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        // We use context.sendMessage instead of sender.sendMessage
        // We wrap ColorUtil output in Message.raw()

        send(context, "&e&l=== ColorMan Test ===");
        send(context, "");
        send(context, "&7Rainbow: &4R&ca&6i&en&ab&bo&9w&5!");
        send(context, "&7Mixed: &l&cERROR: &r&7Something went &4&lwrong&r&7! &a(no it didn't)");
        send(context, "&7Hex: &#FF0000Red &#00FF00Green &#0000FFBlue");
        send(context, "&7Complex: &e&lKuksoLib &r&8» &aReady &7to &broll&7!");
        send(context, "&7Standard: &00 &11 &22 &33 &44 &55 &66 &77 &88 &99 &aa &bb &cc &dd &ee &ff");
        send(context, "&7Format: &lBold &r&oItalic &r&7Normal");
        send(context, "");
        send(context, "&aAll color tests completed!");

        return CompletableFuture.completedFuture(null);
    }

    // Helper to keep code clean and compatible with your existing ColorUtil
    private void send(CommandContext ctx, String text) {
        ctx.sendMessage(ColorUtil.translation(text));
    }
}