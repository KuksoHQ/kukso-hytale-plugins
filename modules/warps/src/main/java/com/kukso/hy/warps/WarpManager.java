package com.kukso.hy.warps;

import com.hypixel.hytale.server.core.util.Config;
import java.util.Collection;
import java.util.UUID;

public class WarpManager {
    private final Config<KuksoWarpsConfig> config;

    public WarpManager(Config<KuksoWarpsConfig> config) {
        this.config = config;
    }

    public void createWarp(String name, UUID worldUuid, double x, double y, double z, float yaw, float pitch) {
        WarpModel warp = new WarpModel(name, worldUuid, x, y, z, yaw, pitch);
        this.config.get().warps.put(name, warp);
        this.config.save();
    }

    public void deleteWarp(String name) {
        this.config.get().warps.remove(name);
        this.config.save();
    }

    public WarpModel getWarp(String name) {
        return this.config.get().warps.get(name);
    }

    public Collection<String> getWarpNames() {
        return this.config.get().warps.keySet();
    }
}
