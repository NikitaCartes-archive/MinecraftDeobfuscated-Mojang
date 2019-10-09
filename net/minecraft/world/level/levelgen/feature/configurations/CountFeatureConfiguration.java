/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class CountFeatureConfiguration
implements FeatureConfiguration {
    public final int count;

    public CountFeatureConfiguration(int i) {
        this.count = i;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("count"), dynamicOps.createInt(this.count))));
    }

    public static <T> CountFeatureConfiguration deserialize(Dynamic<T> dynamic) {
        int i = dynamic.get("count").asInt(0);
        return new CountFeatureConfiguration(i);
    }
}

