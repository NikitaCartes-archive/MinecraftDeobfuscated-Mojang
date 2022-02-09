/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SimpleRandomFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ((MapCodec)ExtraCodecs.nonEmptyHolderSet(PlacedFeature.LIST_CODEC).fieldOf("features")).xmap(SimpleRandomFeatureConfiguration::new, simpleRandomFeatureConfiguration -> simpleRandomFeatureConfiguration.features).codec();
    public final HolderSet<PlacedFeature> features;

    public SimpleRandomFeatureConfiguration(HolderSet<PlacedFeature> holderSet) {
        this.features = holderSet;
    }

    @Override
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return this.features.stream().flatMap(holder -> ((PlacedFeature)holder.value()).getFeatures());
    }
}

