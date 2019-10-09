/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public class OceanRuinConfiguration
implements FeatureConfiguration {
    public final OceanRuinFeature.Type biomeTemp;
    public final float largeProbability;
    public final float clusterProbability;

    public OceanRuinConfiguration(OceanRuinFeature.Type type, float f, float g) {
        this.biomeTemp = type;
        this.largeProbability = f;
        this.clusterProbability = g;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("biome_temp"), dynamicOps.createString(this.biomeTemp.getName()), dynamicOps.createString("large_probability"), dynamicOps.createFloat(this.largeProbability), dynamicOps.createString("cluster_probability"), dynamicOps.createFloat(this.clusterProbability))));
    }

    public static <T> OceanRuinConfiguration deserialize(Dynamic<T> dynamic) {
        OceanRuinFeature.Type type = OceanRuinFeature.Type.byName(dynamic.get("biome_temp").asString(""));
        float f = dynamic.get("large_probability").asFloat(0.0f);
        float g = dynamic.get("cluster_probability").asFloat(0.0f);
        return new OceanRuinConfiguration(type, f, g);
    }
}

