package com.kukso.hy.warps.command;

import com.hypixel.hytale.server.core.Message;
//import com.kukso.hy.lib.locale.LocaleMan;
import com.kukso.hy.warps.WarpManager;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;

import javax.annotation.Nonnull;
import java.util.Map;

public class DelWarpCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;
    private final RequiredArg<String> nameArg;

    public DelWarpCommand(WarpManager warpManager) {
        super("delwarp", "Delete a warp");
        requirePermission("kukso.command.delwarp");

        this.warpManager = warpManager;
        this.nameArg = this.withRequiredArg("name", "Warp name", ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef player,
            @Nonnull World world) {
        String name = context.get(nameArg);

        if (warpManager.getWarp(name) == null) {
            //player.sendMessage(LocaleMan.get(player, "warps.not_found", Map.of("warp", name)));
            player.sendMessage(Message.raw("Warp " + name + " not found!"));
            return;
        }

        warpManager.deleteWarp(name);
        //player.sendMessage(LocaleMan.get(player, "warps.delete_success", Map.of("warp", name)));
        player.sendMessage(Message.raw("Warp " + name + " deleted!"));
    }
}
