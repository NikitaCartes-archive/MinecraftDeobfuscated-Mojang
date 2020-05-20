/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class SimpleRandomFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ((MapCodec)ConfiguredFeature.CODEC.listOf().fieldOf("features")).xmap(SimpleRandomFeatureConfiguration::new, simpleRandomFeatureConfiguration -> simpleRandomFeatureConfiguration.features).codec();
    public final List<ConfiguredFeature<?, ?>> features;

    public SimpleRandomFeatureConfiguration(List<ConfiguredFeature<?, ?>> list) {
        this.features = list;
    }
}

