/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GlowstoneFeature
extends Feature<NoneFeatureConfiguration> {
    public GlowstoneFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        if (!worldGenLevel.isEmptyBlock(blockPos)) {
            return false;
        }
        BlockState blockState = worldGenLevel.getBlockState(blockPos.above());
        if (!(blockState.is(Blocks.NETHERRACK) || blockState.is(Blocks.BASALT) || blockState.is(Blocks.BLACKSTONE))) {
            return false;
        }
        worldGenLevel.setBlock(blockPos, Blocks.GLOWSTONE.defaultBlockState(), 2);
        for (int i = 0; i < 1500; ++i) {
            BlockPos blockPos2 = blockPos.offset(random.nextInt(8) - random.nextInt(8), -random.nextInt(12), random.nextInt(8) - random.nextInt(8));
            if (!worldGenLevel.getBlockState(blockPos2).isAir()) continue;
            int j = 0;
            for (Direction direction : Direction.values()) {
                if (worldGenLevel.getBlockState(blockPos2.relative(direction)).is(Blocks.GLOWSTONE)) {
                    ++j;
                }
                if (j > 1) break;
            }
            if (j != true) continue;
            worldGenLevel.setBlock(blockPos2, Blocks.GLOWSTONE.defaultBlockState(), 2);
        }
        return true;
    }
}

