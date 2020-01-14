/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import org.jetbrains.annotations.Nullable;

public class SpruceTreeGrower
extends AbstractMegaTreeGrower {
    @Override
    @Nullable
    protected ConfiguredFeature<SmallTreeConfiguration, ?> getConfiguredFeature(Random random, boolean bl) {
        return Feature.NORMAL_TREE.configured(BiomeDefaultFeatures.SPRUCE_TREE_CONFIG);
    }

    @Override
    @Nullable
    protected ConfiguredFeature<MegaTreeConfiguration, ?> getConfiguredMegaFeature(Random random) {
        return Feature.MEGA_SPRUCE_TREE.configured(random.nextBoolean() ? BiomeDefaultFeatures.MEGA_SPRUCE_TREE_CONFIG : BiomeDefaultFeatures.MEGA_PINE_TREE_CONFIG);
    }
}

