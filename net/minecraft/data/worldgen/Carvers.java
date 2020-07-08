/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class Carvers {
    public static final ConfiguredWorldCarver<ProbabilityFeatureConfiguration> CAVE = Carvers.register("cave", WorldCarver.CAVE.configured(new ProbabilityFeatureConfiguration(0.14285715f)));
    public static final ConfiguredWorldCarver<ProbabilityFeatureConfiguration> CANYON = Carvers.register("canyon", WorldCarver.CANYON.configured(new ProbabilityFeatureConfiguration(0.02f)));
    public static final ConfiguredWorldCarver<ProbabilityFeatureConfiguration> OCEAN_CAVE = Carvers.register("ocean_cave", WorldCarver.CAVE.configured(new ProbabilityFeatureConfiguration(0.06666667f)));
    public static final ConfiguredWorldCarver<ProbabilityFeatureConfiguration> UNDERWATER_CANYON = Carvers.register("underwater_canyon", WorldCarver.UNDERWATER_CANYON.configured(new ProbabilityFeatureConfiguration(0.02f)));
    public static final ConfiguredWorldCarver<ProbabilityFeatureConfiguration> UNDERWATER_CAVE = Carvers.register("underwater_cave", WorldCarver.UNDERWATER_CAVE.configured(new ProbabilityFeatureConfiguration(0.06666667f)));
    public static final ConfiguredWorldCarver<ProbabilityFeatureConfiguration> NETHER_CAVE = Carvers.register("nether_cave", WorldCarver.NETHER_CAVE.configured(new ProbabilityFeatureConfiguration(0.2f)));

    private static <WC extends CarverConfiguration> ConfiguredWorldCarver<WC> register(String string, ConfiguredWorldCarver<WC> configuredWorldCarver) {
        return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_CARVER, string, configuredWorldCarver);
    }
}

