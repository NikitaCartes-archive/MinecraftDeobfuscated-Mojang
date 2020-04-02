/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import org.jetbrains.annotations.Nullable;

public class JungleTreeGrower
extends AbstractMegaTreeGrower {
    @Override
    @Nullable
    protected ConfiguredFeature<? extends TreeConfiguration, ?> getConfiguredFeature(Random random, boolean bl) {
        return new TreeFeature((Function<Dynamic<?>, ? extends SmallTreeConfiguration>)((Function<Dynamic<?>, SmallTreeConfiguration>)SmallTreeConfiguration::deserialize)).configured(BiomeDefaultFeatures.JUNGLE_TREE_NOVINE_CONFIG);
    }

    @Override
    @Nullable
    protected ConfiguredFeature<? extends TreeConfiguration, ?> getConfiguredMegaFeature(Random random) {
        return Feature.MEGA_JUNGLE_TREE.configured(BiomeDefaultFeatures.MEGA_JUNGLE_TREE_CONFIG);
    }
}

