package com.kukso.hytale.warps.command;

//import com.kukso.hytale.lib.locale.LocaleMan;
import com.hypixel.hytale.server.core.Message;
import com.kukso.hytale.warps.WarpManager;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.kukso.hytale.warps.util.HytaleApiCompat;
import com.kukso.hytale.warps.util.HytaleApiCompat.Position;
import com.kukso.hytale.warps.util.HytaleApiCompat.Rotation;

import javax.annotation.Nonnull;

public class SetWarpCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;
    private final RequiredArg<String> nameArg;

    public SetWarpCommand(WarpManager warpManager) {
        super("setwarp", "Set a new warp");
        requirePermission("kukso.command.setwarp");

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
        Position pos = HytaleApiCompat.getPosition(store, ref);
        Rotation rot = HytaleApiCompat.getHeadRotation(store, ref);
        if (pos == null || rot == null) {
            player.sendMessage(Message.raw("Could not read your current position."));
            return;
        }

        warpManager.createWarp(name, player.getWorldUuid(), pos.x(), pos.y(), pos.z(), rot.yaw(), rot.pitch());
        //player.sendMessage(LocaleMan.get(player, "warps.set_success", Map.of("warp", name)));
        player.sendMessage(Message.raw("Warp " + name + " set successfully."));
    }
}
