package com.kukso.hytale.warps.command;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.kukso.hytale.warps.WarpManager;
import com.kukso.hytale.warps.WarpModel;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.kukso.hytale.warps.util.HytaleApiCompat;
import com.kukso.hytale.warps.util.HytaleApiCompat.Position;
import com.kukso.hytale.warps.util.PermissionUtil;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WarpCommand extends AbstractPlayerCommand {
    private final WarpManager warpManager;
    private final RequiredArg<String> nameArg;
    private static final String WARMUP_BYPASS_PERMISSION = "kukso.warps.bypass.warmup";
    private static final String COOLDOWN_BYPASS_PERMISSION = "kukso.warps.bypass.cooldown";
    private static final double MOVEMENT_THRESHOLD = 0.5;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public WarpCommand(WarpManager warpManager) {
        super("warp", "Teleport to a warp");
        requirePermission("kukso.command.warp");

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
        UUID playerUuid = player.getUuid();
        String name = context.get(nameArg);
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
        boolean bypassCooldown = PermissionUtil.hasPermission(playerUuid, COOLDOWN_BYPASS_PERMISSION);
        if (!bypassCooldown) {
            long remaining = warpManager.getRemainingCooldown(player.getUuid());
            if (remaining > 0) {
                long seconds = (remaining / 1000) + 1;
                player.sendMessage(Message.raw("You must wait " + seconds + " seconds before warping again."));
                return;
            }
        }

        if (warpManager.isWarmingUp(player.getUuid())) {
            player.sendMessage(Message.raw("Teleportation already in progress!"));
            return;
        }

        // Warmup Check
        int warmup = warpManager.getWarmup();
        boolean bypassWarmup = PermissionUtil.hasPermission(playerUuid, WARMUP_BYPASS_PERMISSION);

        if (warmup > 0 && !bypassWarmup) {
            startWarmupCountdown(player, playerUuid, store, ref, world, warp, name, warmup);
        } else {
            executeTeleport(player, playerUuid, store, ref, world, warp, name);
        }
    }

    private void startWarmupCountdown(PlayerRef player, UUID playerUuid, Store<EntityStore> store,
                                       Ref<EntityStore> ref, World world, WarpModel warp,
                                       String warpName, int warmupSeconds) {
        warpManager.setWarmingUp(playerUuid, true);
        Position startPosition = getCurrentPosition(store, ref);
        if (startPosition == null) {
            warpManager.setWarmingUp(playerUuid, false);
            player.sendMessage(Message.raw("Could not read your current position."));
            return;
        }
        final int[] secondsRemaining = {warmupSeconds};

        // Send initial notification only once (no clear method exists)
        sendWarmupNotification(player, warmupSeconds);

        ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];
        futureHolder[0] = scheduler.scheduleAtFixedRate(() -> {
            world.execute(() -> {
                // Guard: Check if warmup was already cancelled
                if (!warpManager.isWarmingUp(playerUuid)) {
                    futureHolder[0].cancel(true);
                    return;
                }

                // Check if player moved
                Position currentPosition = getCurrentPosition(store, ref);
                if (currentPosition == null) {
                    cancelWarmup(player, playerUuid, futureHolder[0]);
                    return;
                }
                if (hasPlayerMoved(startPosition, currentPosition)) {
                    cancelWarmup(player, playerUuid, futureHolder[0]);
                    return;
                }

                secondsRemaining[0]--;
                if (secondsRemaining[0] <= 0) {
                    futureHolder[0].cancel(true);
                    // Double-check warmup state before teleporting
                    if (warpManager.isWarmingUp(playerUuid)) {
                        executeTeleport(player, playerUuid, store, ref, world, warp, warpName);
                    }
                }
            });
        }, 1, 1, TimeUnit.SECONDS);
    }

    private Position getCurrentPosition(Store<EntityStore> store, Ref<EntityStore> ref) {
        return HytaleApiCompat.getPosition(store, ref);
    }

    private boolean hasPlayerMoved(Position start, Position current) {
        double dx = current.x() - start.x();
        double dy = current.y() - start.y();
        double dz = current.z() - start.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz) > MOVEMENT_THRESHOLD;
    }

    private void cancelWarmup(PlayerRef player, UUID playerUuid, ScheduledFuture<?> future) {
        warpManager.setWarmingUp(playerUuid, false);
        future.cancel(true);
        player.sendMessage(Message.raw("Teleportation cancelled - you moved!"));
    }

    private void executeTeleport(PlayerRef player, UUID playerUuid, Store<EntityStore> store,
                                  Ref<EntityStore> ref, World world, WarpModel warp, String warpName) {
        warpManager.setWarmingUp(playerUuid, false);

        Teleport teleport = HytaleApiCompat.createTeleport(world, warp.x, warp.y, warp.z, warp.yaw, 0.0F);
        store.addComponent(ref, Teleport.getComponentType(), teleport);
        player.sendMessage(Message.raw("Teleported to " + warpName));

        warpManager.setCooldown(playerUuid);
    }

    private static void sendWarmupNotification(PlayerRef player, int secondsLeft) {
        var packetHandler = player.getPacketHandler();

        var primaryMessage = Message.raw("TELEPORTING").color("#00FF00");
        var secondaryMessage = Message.raw("Do not move until " + secondsLeft + " seconds.").color("#228B22");
        var icon = new ItemStack("Ingredient_Void_Essence", 1).toPacket();

        NotificationUtil.sendNotification(
                packetHandler,
                primaryMessage,
                secondaryMessage,
                icon);
    }
}
