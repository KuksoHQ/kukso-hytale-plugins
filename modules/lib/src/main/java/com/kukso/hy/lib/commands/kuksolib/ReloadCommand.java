package com.kukso.hy.lib.commands.kuksolib;

import com.hypixel.hytale.logger.HytaleLogger;
import com.kukso.hy.lib.util.LocaleUtil;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.kukso.hy.lib.util.LocaleUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class ReloadCommand extends AbstractAsyncCommand {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ReloadCommand(PluginBase plugin) {
        super("reload", "Reloads config and localization strings");
        requirePermission ("kukso.command.kuksolib.reload");
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(@NonNullDecl CommandContext ctx) {
        return CompletableFuture.runAsync(() -> {
            LOGGER.atInfo().log("Reload command used by " +ctx.senderAsPlayerRef());

            LocaleUtil.reload();
            ctx.sender().sendMessage(Message.raw("Reloaded localization strings").color("#55FF55"));
            LOGGER.atInfo().log("Reloaded by " + ctx.senderAsPlayerRef());

        });
    }
}
