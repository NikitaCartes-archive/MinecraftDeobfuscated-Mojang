/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;

public class PillagerOutpostConfiguration
implements FeatureConfiguration {
    public final double probability;

    public PillagerOutpostConfiguration(double d) {
        this.probability = d;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("probability"), dynamicOps.createDouble(this.probability))));
    }

    public static <T> PillagerOutpostConfiguration deserialize(Dynamic<T> dynamic) {
        float f = dynamic.get("probability").asFloat(0.0f);
        return new PillagerOutpostConfiguration(f);
    }
}

