package com.kukso.hy.warps.commands.main;

import com.kukso.hy.warps.WarpManager;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;

public class ListWarpsCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;

    public ListWarpsCommand(WarpManager warpManager) {
        super("warps", "List all warps");
        this.warpManager = warpManager;
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef player, World world) {
        if (warpManager.getWarpNames().isEmpty()) {
            player.sendMessage(Message.raw("No warps set."));
            return;
        }
        player.sendMessage(Message.raw("Warps: " + String.join(", ", warpManager.getWarpNames())));
    }
}
