/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import org.jetbrains.annotations.Nullable;

public class BirchTreeGrower
extends AbstractTreeGrower {
    @Override
    @Nullable
    protected ConfiguredFeature<SmallTreeConfiguration, ?> getConfiguredFeature(Random random) {
        return Feature.NORMAL_TREE.configured(BiomeDefaultFeatures.BIRCH_TREE_CONFIG);
    }
}

