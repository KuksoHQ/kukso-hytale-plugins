package com.kukso.hy.warps;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import java.util.HashMap;
import java.util.Map;

public class KuksoWarpsConfig {
    public static final BuilderCodec<KuksoWarpsConfig> CODEC = BuilderCodec.builder(KuksoWarpsConfig.class, KuksoWarpsConfig::new)
            .addField(new KeyedCodec<>("Warps", new MapCodec<>(WarpModel.CODEC, HashMap::new)), (config, v) -> config.warps = new HashMap<>(v), config -> config.warps)
            .addField(new KeyedCodec<>("Warmup", Codec.INTEGER), (config, v) -> config.warmup = v, config -> config.warmup)
            .addField(new KeyedCodec<>("Cooldown", Codec.INTEGER), (config, v) -> config.cooldown = v, config -> config.cooldown)
            .build();

    public Map<String, WarpModel> warps = new HashMap<>();
    public int warmup = 3;
    public int cooldown = 5;
}
