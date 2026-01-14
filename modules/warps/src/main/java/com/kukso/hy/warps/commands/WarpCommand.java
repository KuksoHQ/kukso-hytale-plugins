package com.kukso.hy.warps.commands;

import com.kukso.hy.warps.WarpManager;
import com.kukso.hy.warps.WarpModel;
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
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WarpCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;
    private final RequiredArg<String> nameArg;

    public WarpCommand(WarpManager warpManager) {
        super("warp", "Teleport to a warp");
        this.warpManager = warpManager;
        this.nameArg = this.withRequiredArg("name", "Warp name", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef player, World world) {
        String name = ctx.get(nameArg);
        WarpModel warp = warpManager.getWarp(name);
        
        if (warp == null) {
            player.sendMessage(Message.raw("Warp not found: " + name));
            return;
        }

        if (!warp.worldUuid.equals(player.getWorldUuid())) {
             player.sendMessage(Message.raw("Warp is in another world! Cross-world teleportation is not supported yet."));
             return;
        }

        // Cooldown Check
        // if (!player.hasPermission("kukso.warps.bypass.cooldown")) {
            long remaining = warpManager.getRemainingCooldown(player.getUuid());
            if (remaining > 0) {
                long seconds = (remaining / 1000) + 1;
                player.sendMessage(Message.raw("You must wait " + seconds + " seconds before warping again."));
                return;
            }
        // }

        if (warpManager.isWarmingUp(player.getUuid())) {
            player.sendMessage(Message.raw("Teleportation already in progress!"));
            return;
        }

        Runnable teleportTask = () -> world.execute(() -> {
            warpManager.setWarmingUp(player.getUuid(), false);
            
            Vector3f rot = new Vector3f();
            rot.setYaw(warp.yaw);
            rot.setPitch(warp.pitch);

            Teleport teleport = new Teleport(world, new Vector3d(warp.x, warp.y, warp.z), rot);
            store.addComponent(ref, Teleport.getComponentType(), teleport);
            player.sendMessage(Message.raw("Teleported to " + name));

            // Set cooldown
            // if (!player.hasPermission("kukso.warps.bypass.cooldown")) {
                warpManager.setCooldown(player.getUuid());
            // }
        });

        // Warmup Check
        int warmup = warpManager.getWarmup();
        if (warmup > 0 /* && !player.hasPermission("kukso.warps.bypass.warmup") */) {
            warpManager.setWarmingUp(player.getUuid(), true);
            player.sendMessage(Message.raw("Teleporting in " + warmup + " seconds."));
            CompletableFuture.delayedExecutor(warmup, TimeUnit.SECONDS).execute(teleportTask);
        } else {
            teleportTask.run();
        }
    }
}
