/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.BlockBlobConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;

public class BlockBlobFeature
extends Feature<BlockBlobConfiguration> {
    public BlockBlobFeature(Function<Dynamic<?>, ? extends BlockBlobConfiguration> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, BlockBlobConfiguration blockBlobConfiguration) {
        Block block;
        while (blockPos.getY() > 3 && (levelAccessor.isEmptyBlock(blockPos.below()) || (block = levelAccessor.getBlockState(blockPos.below()).getBlock()) != Blocks.GRASS_BLOCK && !Block.equalsDirt(block) && !Block.equalsStone(block))) {
            blockPos = blockPos.below();
        }
        if (blockPos.getY() <= 3) {
            return false;
        }
        int i = blockBlobConfiguration.startRadius;
        for (int j = 0; i >= 0 && j < 3; ++j) {
            int k = i + random.nextInt(2);
            int l = i + random.nextInt(2);
            int m = i + random.nextInt(2);
            float f = (float)(k + l + m) * 0.333f + 0.5f;
            for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-k, -l, -m), blockPos.offset(k, l, m))) {
                if (!(blockPos2.distSqr(blockPos) <= (double)(f * f))) continue;
                levelAccessor.setBlock(blockPos2, blockBlobConfiguration.state, 4);
            }
            blockPos = blockPos.offset(-(i + 1) + random.nextInt(2 + i * 2), 0 - random.nextInt(2), -(i + 1) + random.nextInt(2 + i * 2));
        }
        return true;
    }
}

