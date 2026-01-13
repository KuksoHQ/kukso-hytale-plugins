package com.kukso.hy.warps;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.UUID;

public class WarpModel {
    public static final BuilderCodec<WarpModel> CODEC = BuilderCodec.builder(WarpModel.class, WarpModel::new)
            .addField(new KeyedCodec<>("Name", Codec.STRING), (model, v) -> model.name = v, model -> model.name)
            .addField(new KeyedCodec<>("WorldUuid", Codec.UUID_STRING), (model, v) -> model.worldUuid = v, model -> model.worldUuid)
            .addField(new KeyedCodec<>("X", Codec.DOUBLE), (model, v) -> model.x = v, model -> model.x)
            .addField(new KeyedCodec<>("Y", Codec.DOUBLE), (model, v) -> model.y = v, model -> model.y)
            .addField(new KeyedCodec<>("Z", Codec.DOUBLE), (model, v) -> model.z = v, model -> model.z)
            .addField(new KeyedCodec<>("Yaw", Codec.FLOAT), (model, v) -> model.yaw = v, model -> model.yaw)
            .addField(new KeyedCodec<>("Pitch", Codec.FLOAT), (model, v) -> model.pitch = v, model -> model.pitch)
            .build();

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
