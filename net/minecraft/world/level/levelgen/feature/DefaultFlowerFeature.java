/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FlowerFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public class DefaultFlowerFeature
extends FlowerFeature {
    public DefaultFlowerFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function);
    }

    @Override
    public BlockState getRandomFlower(Random random, BlockPos blockPos) {
        if (random.nextFloat() > 0.6666667f) {
            return Blocks.DANDELION.defaultBlockState();
        }
        return Blocks.POPPY.defaultBlockState();
    }
}

