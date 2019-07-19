/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.HugeMushroomFeatureConfig;

public class HugeBrownMushroomFeature
extends Feature<HugeMushroomFeatureConfig> {
    public HugeBrownMushroomFeature(Function<Dynamic<?>, ? extends HugeMushroomFeatureConfig> function) {
        super(function);
    }

    @Override
    public boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, HugeMushroomFeatureConfig hugeMushroomFeatureConfig) {
        int n;
        int m;
        int l;
        int j;
        int i = random.nextInt(3) + 4;
        if (random.nextInt(12) == 0) {
            i *= 2;
        }
        if ((j = blockPos.getY()) < 1 || j + i + 1 >= 256) {
            return false;
        }
        Block block = levelAccessor.getBlockState(blockPos.below()).getBlock();
        if (!Block.equalsDirt(block) && block != Blocks.GRASS_BLOCK && block != Blocks.MYCELIUM) {
            return false;
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int k = 0; k <= 1 + i; ++k) {
            l = k <= 3 ? 0 : 3;
            for (m = -l; m <= l; ++m) {
                for (n = -l; n <= l; ++n) {
                    BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.set(blockPos).move(m, k, n));
                    if (blockState.isAir() || blockState.is(BlockTags.LEAVES)) continue;
                    return false;
                }
            }
        }
        BlockState blockState2 = (BlockState)((BlockState)Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState().setValue(HugeMushroomBlock.UP, true)).setValue(HugeMushroomBlock.DOWN, false);
        l = 3;
        for (m = -3; m <= 3; ++m) {
            for (n = -3; n <= 3; ++n) {
                boolean bl6;
                boolean bl = m == -3;
                boolean bl2 = m == 3;
                boolean bl3 = n == -3;
                boolean bl4 = n == 3;
                boolean bl5 = bl || bl2;
                boolean bl7 = bl6 = bl3 || bl4;
                if (bl5 && bl6) continue;
                mutableBlockPos.set(blockPos).move(m, i, n);
                if (levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) continue;
                boolean bl72 = bl || bl6 && m == -2;
                boolean bl8 = bl2 || bl6 && m == 2;
                boolean bl9 = bl3 || bl5 && n == -2;
                boolean bl10 = bl4 || bl5 && n == 2;
                this.setBlock(levelAccessor, mutableBlockPos, (BlockState)((BlockState)((BlockState)((BlockState)blockState2.setValue(HugeMushroomBlock.WEST, bl72)).setValue(HugeMushroomBlock.EAST, bl8)).setValue(HugeMushroomBlock.NORTH, bl9)).setValue(HugeMushroomBlock.SOUTH, bl10));
            }
        }
        BlockState blockState3 = (BlockState)((BlockState)Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, false)).setValue(HugeMushroomBlock.DOWN, false);
        for (n = 0; n < i; ++n) {
            mutableBlockPos.set(blockPos).move(Direction.UP, n);
            if (levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) continue;
            if (hugeMushroomFeatureConfig.planted) {
                levelAccessor.setBlock(mutableBlockPos, blockState3, 3);
                continue;
            }
            this.setBlock(levelAccessor, mutableBlockPos, blockState3);
        }
        return true;
    }
}

