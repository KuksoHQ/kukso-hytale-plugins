package com.kukso.hy.warps;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import java.util.HashMap;
import java.util.Map;

public class KuksoWarpsConfig {
    public static final BuilderCodec<KuksoWarpsConfig> CODEC = BuilderCodec.builder(KuksoWarpsConfig.class, KuksoWarpsConfig::new)
            .addField(new KeyedCodec<>("Warps", new MapCodec<>(WarpModel.CODEC, HashMap::new)), (config, v) -> config.warps = new HashMap<>(v), config -> config.warps)
            .build();

    public Map<String, WarpModel> warps = new HashMap<>();
}
