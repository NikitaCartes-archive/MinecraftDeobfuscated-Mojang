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

public class HugeRedMushroomFeature
extends Feature<HugeMushroomFeatureConfig> {
    public HugeRedMushroomFeature(Function<Dynamic<?>, ? extends HugeMushroomFeatureConfig> function) {
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
        for (int k = 0; k <= i; ++k) {
            l = 0;
            if (k < i && k >= i - 3) {
                l = 2;
            } else if (k == i) {
                l = 1;
            }
            for (m = -l; m <= l; ++m) {
                for (n = -l; n <= l; ++n) {
                    BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.set(blockPos).move(m, k, n));
                    if (blockState.isAir() || blockState.is(BlockTags.LEAVES)) continue;
                    return false;
                }
            }
        }
        BlockState blockState2 = (BlockState)Blocks.RED_MUSHROOM_BLOCK.defaultBlockState().setValue(HugeMushroomBlock.DOWN, false);
        for (l = i - 3; l <= i; ++l) {
            m = l < i ? 2 : 1;
            n = 0;
            for (int o = -m; o <= m; ++o) {
                for (int p = -m; p <= m; ++p) {
                    boolean bl6;
                    boolean bl = o == -m;
                    boolean bl2 = o == m;
                    boolean bl3 = p == -m;
                    boolean bl4 = p == m;
                    boolean bl5 = bl || bl2;
                    boolean bl7 = bl6 = bl3 || bl4;
                    if (l < i && bl5 == bl6) continue;
                    mutableBlockPos.set(blockPos).move(o, l, p);
                    if (levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) continue;
                    this.setBlock(levelAccessor, mutableBlockPos, (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)blockState2.setValue(HugeMushroomBlock.UP, l >= i - 1)).setValue(HugeMushroomBlock.WEST, o < 0)).setValue(HugeMushroomBlock.EAST, o > 0)).setValue(HugeMushroomBlock.NORTH, p < 0)).setValue(HugeMushroomBlock.SOUTH, p > 0));
                }
            }
        }
        BlockState blockState3 = (BlockState)((BlockState)Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, false)).setValue(HugeMushroomBlock.DOWN, false);
        for (m = 0; m < i; ++m) {
            mutableBlockPos.set(blockPos).move(Direction.UP, m);
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

