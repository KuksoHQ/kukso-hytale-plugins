package com.kukso.hy.warps;

import java.util.Collection;
import java.util.UUID;

public class WarpManager {
    private final java.util.Map<UUID, Long> cooldowns = new java.util.HashMap<>();
    private final java.util.Set<UUID> warmingUp = new java.util.HashSet<>();

    public WarpManager() {
    }

    public boolean isWarmingUp(UUID uuid) {
        return warmingUp.contains(uuid);
    }

    public void setWarmingUp(UUID uuid, boolean isWarmingUp) {
        if (isWarmingUp) {
            warmingUp.add(uuid);
        } else {
            warmingUp.remove(uuid);
        }
    }

    public int getWarmup() {
        return WarpConfigManager.get().warmup;
    }

    public int getCooldown() {
        return WarpConfigManager.get().cooldown;
    }

    public long getRemainingCooldown(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) return 0;
        long remaining = cooldowns.get(uuid) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public void setCooldown(UUID uuid) {
        if (getCooldown() <= 0) return;
        cooldowns.put(uuid, System.currentTimeMillis() + (getCooldown() * 1000L));
    }

    public void createWarp(String name, UUID worldUuid, double x, double y, double z, float yaw, float pitch) {
        WarpModel warp = new WarpModel(name, worldUuid, x, y, z, yaw, pitch);
        WarpConfigManager.get().warps.put(name, warp);
        WarpConfigManager.save();
    }

    public void deleteWarp(String name) {
        WarpConfigManager.get().warps.remove(name);
        WarpConfigManager.save();
    }

    public WarpModel getWarp(String name) {
        return WarpConfigManager.get().warps.get(name);
    }

    public Collection<String> getWarpNames() {
        return WarpConfigManager.get().warps.keySet();
    }
}
