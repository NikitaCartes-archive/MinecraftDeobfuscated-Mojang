/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public class OceanRuinConfiguration
implements FeatureConfiguration {
    public static final Codec<OceanRuinConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)OceanRuinFeature.Type.CODEC.fieldOf("biome_temp")).forGetter(oceanRuinConfiguration -> oceanRuinConfiguration.biomeTemp), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("large_probability")).forGetter(oceanRuinConfiguration -> Float.valueOf(oceanRuinConfiguration.largeProbability)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("cluster_probability")).forGetter(oceanRuinConfiguration -> Float.valueOf(oceanRuinConfiguration.clusterProbability))).apply((Applicative<OceanRuinConfiguration, ?>)instance, OceanRuinConfiguration::new));
    public final OceanRuinFeature.Type biomeTemp;
    public final float largeProbability;
    public final float clusterProbability;

    public OceanRuinConfiguration(OceanRuinFeature.Type type, float f, float g) {
        this.biomeTemp = type;
        this.largeProbability = f;
        this.clusterProbability = g;
    }
}

