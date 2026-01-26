package com.kukso.hy.lib.commands.kuksolib;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.kukso.hy.lib.util.VersionUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

public class VersionCommand extends AbstractAsyncCommand {
    private final PluginBase plugin;

    public VersionCommand(PluginBase plugin) {
        this.plugin = plugin;
        super("version", "Displays the current version of KuksoLib.");
        addAliases("ver");
        requirePermission ("kukso.command.kuksolib.version");
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(@NonNullDecl CommandContext ctx) {
        return CompletableFuture.runAsync(() -> {
            VersionUtil checker = new VersionUtil();

            String version = String.valueOf(plugin.getManifest().getVersion());
            String name = plugin.getName();

            checker.check(ctx.sender(), "KuksoHQ", "kukso-hy-lib", version);
        });
    }
}
