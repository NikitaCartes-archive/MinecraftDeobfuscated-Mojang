/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class SeagrassFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<SeagrassFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("count")).forGetter(seagrassFeatureConfiguration -> seagrassFeatureConfiguration.count), ((MapCodec)Codec.DOUBLE.fieldOf("probability")).forGetter(seagrassFeatureConfiguration -> seagrassFeatureConfiguration.tallSeagrassProbability)).apply((Applicative<SeagrassFeatureConfiguration, ?>)instance, SeagrassFeatureConfiguration::new));
    public final int count;
    public final double tallSeagrassProbability;

    public SeagrassFeatureConfiguration(int i, double d) {
        this.count = i;
        this.tallSeagrassProbability = d;
    }
}

