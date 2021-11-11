/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class RangeConfiguration
implements FeatureConfiguration {
    public static final Codec<RangeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)HeightProvider.CODEC.fieldOf("height")).forGetter(rangeConfiguration -> rangeConfiguration.height)).apply((Applicative<RangeConfiguration, ?>)instance, RangeConfiguration::new));
    public final HeightProvider height;

    public RangeConfiguration(HeightProvider heightProvider) {
        this.height = heightProvider;
    }
}

