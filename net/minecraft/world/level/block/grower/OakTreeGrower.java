/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import org.jetbrains.annotations.Nullable;

public class OakTreeGrower
extends AbstractTreeGrower {
    @Override
    @Nullable
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random random, boolean bl) {
        return random.nextInt(10) == 0 ? Feature.TREE.configured(bl ? BiomeDefaultFeatures.FANCY_TREE_WITH_BEES_005_CONFIG : BiomeDefaultFeatures.FANCY_TREE_CONFIG) : Feature.TREE.configured(bl ? BiomeDefaultFeatures.NORMAL_TREE_WITH_BEES_005_CONFIG : BiomeDefaultFeatures.NORMAL_TREE_CONFIG);
    }
}

