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

public class DarkOakTreeGrower
extends AbstractMegaTreeGrower {
    @Override
    @Nullable
    protected ConfiguredFeature<SmallTreeConfiguration, ?> getConfiguredFeature(Random random) {
        return null;
    }

    @Override
    @Nullable
    protected ConfiguredFeature<MegaTreeConfiguration, ?> getConfiguredMegaFeature(Random random) {
        return Feature.DARK_OAK_TREE.configured(BiomeDefaultFeatures.DARK_OAK_TREE_CONFIG);
    }
}

