package com.kukso.hy.warps;

import java.util.UUID;

public class WarpModel {
    public String name;
    public UUID worldUuid;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public WarpModel() {}

    public WarpModel(String name, UUID worldUuid, double x, double y, double z, float yaw, float pitch) {
        this.name = name;
        this.worldUuid = worldUuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
