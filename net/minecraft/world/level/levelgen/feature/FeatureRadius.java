/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;

public class FeatureRadius
implements FeatureConfiguration {
    public final int radius;

    public FeatureRadius(int i) {
        this.radius = i;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("radius"), dynamicOps.createInt(this.radius))));
    }

    public static <T> FeatureRadius deserialize(Dynamic<T> dynamic) {
        int i = dynamic.get("radius").asInt(0);
        return new FeatureRadius(i);
    }
}

