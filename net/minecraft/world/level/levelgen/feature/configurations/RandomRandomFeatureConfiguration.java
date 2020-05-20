/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class RandomRandomFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<RandomRandomFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ConfiguredFeature.CODEC.listOf().fieldOf("features")).forGetter(randomRandomFeatureConfiguration -> randomRandomFeatureConfiguration.features), ((MapCodec)Codec.INT.fieldOf("count")).withDefault(0).forGetter(randomRandomFeatureConfiguration -> randomRandomFeatureConfiguration.count)).apply((Applicative<RandomRandomFeatureConfiguration, ?>)instance, RandomRandomFeatureConfiguration::new));
    public final List<ConfiguredFeature<?, ?>> features;
    public final int count;

    public RandomRandomFeatureConfiguration(List<ConfiguredFeature<?, ?>> list, int i) {
        this.features = list;
        this.count = i;
    }
}

