package com.kukso.hy.warps.command;

//import com.kukso.hy.lib.locale.LocaleMan;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
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
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.swing.text.html.parser.Entity;
import java.util.Map;

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
        Vector3d pos = player.getTransform().getPosition();
        Vector3f rot = player.getHeadRotation();

        warpManager.createWarp(name, player.getWorldUuid(), pos.x, pos.y, pos.z, rot.getYaw(), rot.getPitch());
        //player.sendMessage(LocaleMan.get(player, "warps.set_success", Map.of("warp", name)));
        player.sendMessage(Message.raw("Warp " + name + " set successfully."));
    }
}
