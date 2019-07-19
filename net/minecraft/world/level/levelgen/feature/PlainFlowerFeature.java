/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FlowerFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public class PlainFlowerFeature
extends FlowerFeature {
    public PlainFlowerFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function);
    }

    @Override
    public BlockState getRandomFlower(Random random, BlockPos blockPos) {
        double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 200.0, (double)blockPos.getZ() / 200.0);
        if (d < -0.8) {
            int i = random.nextInt(4);
            switch (i) {
                case 0: {
                    return Blocks.ORANGE_TULIP.defaultBlockState();
                }
                case 1: {
                    return Blocks.RED_TULIP.defaultBlockState();
                }
                case 2: {
                    return Blocks.PINK_TULIP.defaultBlockState();
                }
            }
            return Blocks.WHITE_TULIP.defaultBlockState();
        }
        if (random.nextInt(3) > 0) {
            int i = random.nextInt(4);
            switch (i) {
                case 0: {
                    return Blocks.POPPY.defaultBlockState();
                }
                case 1: {
                    return Blocks.AZURE_BLUET.defaultBlockState();
                }
                case 2: {
                    return Blocks.OXEYE_DAISY.defaultBlockState();
                }
            }
            return Blocks.CORNFLOWER.defaultBlockState();
        }
        return Blocks.DANDELION.defaultBlockState();
    }
}

