package com.kukso.hy.lib.commands.kuksolib;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

import static com.kukso.hy.lib.util.HelpUtil.openHelpUI;

public class HelpCommand extends AbstractAsyncCommand {

    @Nonnull
    public HelpCommand() {
        super("help", "Opens the native help UI for KuksoLib");
        this.setPermissionGroup(GameMode.Adventure);
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        return openHelpUI(ctx, "kuksolib");
    }
}
