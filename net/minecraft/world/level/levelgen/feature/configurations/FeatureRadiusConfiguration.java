/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class FeatureRadiusConfiguration
implements FeatureConfiguration {
    public static final Codec<FeatureRadiusConfiguration> CODEC = ((MapCodec)Codec.INT.fieldOf("radius")).xmap(FeatureRadiusConfiguration::new, featureRadiusConfiguration -> featureRadiusConfiguration.radius).codec();
    public final int radius;

    public FeatureRadiusConfiguration(int i) {
        this.radius = i;
    }
}

