/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen.features;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class AquaticFeatures {
    public static final Holder<ConfiguredFeature<ProbabilityFeatureConfiguration, ?>> SEAGRASS_SHORT = FeatureUtils.register("seagrass_short", Feature.SEAGRASS, new ProbabilityFeatureConfiguration(0.3f));
    public static final Holder<ConfiguredFeature<ProbabilityFeatureConfiguration, ?>> SEAGRASS_SLIGHTLY_LESS_SHORT = FeatureUtils.register("seagrass_slightly_less_short", Feature.SEAGRASS, new ProbabilityFeatureConfiguration(0.4f));
    public static final Holder<ConfiguredFeature<ProbabilityFeatureConfiguration, ?>> SEAGRASS_MID = FeatureUtils.register("seagrass_mid", Feature.SEAGRASS, new ProbabilityFeatureConfiguration(0.6f));
    public static final Holder<ConfiguredFeature<ProbabilityFeatureConfiguration, ?>> SEAGRASS_TALL = FeatureUtils.register("seagrass_tall", Feature.SEAGRASS, new ProbabilityFeatureConfiguration(0.8f));
    public static final Holder<ConfiguredFeature<CountConfiguration, ?>> SEA_PICKLE = FeatureUtils.register("sea_pickle", Feature.SEA_PICKLE, new CountConfiguration(20));
    public static final Holder<ConfiguredFeature<SimpleBlockConfiguration, ?>> SEAGRASS_SIMPLE = FeatureUtils.register("seagrass_simple", Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SEAGRASS)));
    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> KELP = FeatureUtils.register("kelp", Feature.KELP);
    public static final Holder<ConfiguredFeature<SimpleRandomFeatureConfiguration, ?>> WARM_OCEAN_VEGETATION = FeatureUtils.register("warm_ocean_vegetation", Feature.SIMPLE_RANDOM_SELECTOR, new SimpleRandomFeatureConfiguration(HolderSet.direct(PlacementUtils.inlinePlaced(Feature.CORAL_TREE, FeatureConfiguration.NONE, new PlacementModifier[0]), PlacementUtils.inlinePlaced(Feature.CORAL_CLAW, FeatureConfiguration.NONE, new PlacementModifier[0]), PlacementUtils.inlinePlaced(Feature.CORAL_MUSHROOM, FeatureConfiguration.NONE, new PlacementModifier[0]))));
}

