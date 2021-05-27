/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class RangeDecoratorConfiguration
implements DecoratorConfiguration,
FeatureConfiguration {
    public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)HeightProvider.CODEC.fieldOf("height")).forGetter(rangeDecoratorConfiguration -> rangeDecoratorConfiguration.height)).apply((Applicative<RangeDecoratorConfiguration, ?>)instance, RangeDecoratorConfiguration::new));
    public final HeightProvider height;

    public RangeDecoratorConfiguration(HeightProvider heightProvider) {
        this.height = heightProvider;
    }
}

