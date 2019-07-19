/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;

public class BuriedTreasureConfiguration
implements FeatureConfiguration {
    public final float probability;

    public BuriedTreasureConfiguration(float f) {
        this.probability = f;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("probability"), dynamicOps.createFloat(this.probability))));
    }

    public static <T> BuriedTreasureConfiguration deserialize(Dynamic<T> dynamic) {
        float f = dynamic.get("probability").asFloat(0.0f);
        return new BuriedTreasureConfiguration(f);
    }
}

