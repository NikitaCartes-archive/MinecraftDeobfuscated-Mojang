/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class SimpleRandomFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ((MapCodec)ConfiguredFeature.LIST_CODEC.fieldOf("features")).flatXmap(ExtraCodecs.nonEmptyListCheck(), ExtraCodecs.nonEmptyListCheck()).xmap(SimpleRandomFeatureConfiguration::new, simpleRandomFeatureConfiguration -> simpleRandomFeatureConfiguration.features).codec();
    public final List<Supplier<ConfiguredFeature<?, ?>>> features;

    public SimpleRandomFeatureConfiguration(List<Supplier<ConfiguredFeature<?, ?>>> list) {
        this.features = list;
    }

    @Override
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return this.features.stream().flatMap(supplier -> ((ConfiguredFeature)supplier.get()).getFeatures());
    }
}

