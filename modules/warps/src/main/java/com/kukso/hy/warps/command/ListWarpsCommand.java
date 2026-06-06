package com.kukso.hy.warps.command;

import com.kukso.hy.warps.WarpManager;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;

public class ListWarpsCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;

    public ListWarpsCommand(WarpManager warpManager) {
        super("warps", "List all warps");
        requirePermission("kukso.command.warp");

        this.warpManager = warpManager;
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef player,
            @Nonnull World world) {
        if (warpManager.getWarpNames().isEmpty()) {
            player.sendMessage(Message.raw("No warps set."));
            return;
        }
        player.sendMessage(Message.raw("Warps: " + String.join(", ", warpManager.getWarpNames())));
    }
}
