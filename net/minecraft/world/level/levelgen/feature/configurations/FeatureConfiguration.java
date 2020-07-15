/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import java.util.stream.Stream;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public interface FeatureConfiguration {
    public static final NoneFeatureConfiguration NONE = NoneFeatureConfiguration.INSTANCE;

    default public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return Stream.empty();
    }
}

