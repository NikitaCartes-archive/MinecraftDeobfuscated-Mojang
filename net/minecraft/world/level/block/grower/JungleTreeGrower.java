/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.MegaJungleTreeFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import org.jetbrains.annotations.Nullable;

public class JungleTreeGrower
extends AbstractMegaTreeGrower {
    @Override
    @Nullable
    protected AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random random) {
        return new TreeFeature(NoneFeatureConfiguration::deserialize, true, 4 + random.nextInt(7), Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.JUNGLE_LEAVES.defaultBlockState(), false);
    }

    @Override
    @Nullable
    protected AbstractTreeFeature<NoneFeatureConfiguration> getMegaFeature(Random random) {
        return new MegaJungleTreeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize), true, 10, 20, Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.JUNGLE_LEAVES.defaultBlockState());
    }
}

