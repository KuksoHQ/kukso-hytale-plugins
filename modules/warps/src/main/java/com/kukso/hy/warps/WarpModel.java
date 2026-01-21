package com.kukso.hy.warps;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

public class WarpModel {
    @SerializedName("Name")
    public String name;

    @SerializedName("WorldUuid")
    public UUID worldUuid;

    @SerializedName("X")
    public double x;

    @SerializedName("Y")
    public double y;

    @SerializedName("Z")
    public double z;

    @SerializedName("Yaw")
    public float yaw;

    @SerializedName("Pitch")
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