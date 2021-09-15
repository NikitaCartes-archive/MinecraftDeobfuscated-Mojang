/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;

public class DefaultSurfaceBuilder
extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    public DefaultSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override
    public void apply(Random random, BlockColumn blockColumn, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, int m, long n, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        this.apply(random, blockColumn, biome, i, j, k, d, blockState, blockState2, surfaceBuilderBaseConfiguration.getTopMaterial(), surfaceBuilderBaseConfiguration.getUnderMaterial(), surfaceBuilderBaseConfiguration.getUnderwaterMaterial(), l, m);
    }

    protected void apply(Random random, BlockColumn blockColumn, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, BlockState blockState3, BlockState blockState4, BlockState blockState5, int l, int m) {
        l = Integer.MIN_VALUE;
        int n = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
        BlockState blockState6 = blockState4;
        int o = -1;
        for (int p = k; p >= m; --p) {
            BlockState blockState7 = blockColumn.getBlock(p);
            if (blockState7.isAir()) {
                o = -1;
                l = Integer.MIN_VALUE;
                continue;
            }
            if (!blockState7.is(blockState.getBlock())) {
                l = Math.max(p + 1, l);
                continue;
            }
            if (o == -1) {
                BlockState blockState8;
                o = n;
                if (p >= l + 2) {
                    blockState8 = blockState3;
                } else if (p >= l - 1) {
                    blockState6 = blockState4;
                    blockState8 = blockState3;
                } else if (p >= l - 4) {
                    blockState6 = blockState4;
                    blockState8 = blockState4;
                } else if (p >= l - (7 + n)) {
                    blockState8 = blockState6;
                } else {
                    blockState6 = blockState;
                    blockState8 = blockState5;
                }
                blockColumn.setBlock(p, DefaultSurfaceBuilder.maybeReplaceState(blockState8, blockColumn, p, l));
                continue;
            }
            if (o <= 0) continue;
            blockColumn.setBlock(p, DefaultSurfaceBuilder.maybeReplaceState(blockState6, blockColumn, p, l));
            if (--o != 0 || !blockState6.is(Blocks.SAND) || n <= 1) continue;
            o = random.nextInt(4) + Math.max(0, p - l);
            blockState6 = blockState6.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
        }
    }

    private static BlockState maybeReplaceState(BlockState blockState, BlockColumn blockColumn, int i, int j) {
        if (i <= j && blockState.is(Blocks.GRASS_BLOCK)) {
            return Blocks.DIRT.defaultBlockState();
        }
        if (blockState.is(Blocks.SAND) && DefaultSurfaceBuilder.isEmptyBelow(blockColumn, i)) {
            return Blocks.SANDSTONE.defaultBlockState();
        }
        if (blockState.is(Blocks.RED_SAND) && DefaultSurfaceBuilder.isEmptyBelow(blockColumn, i)) {
            return Blocks.RED_SANDSTONE.defaultBlockState();
        }
        if (blockState.is(Blocks.GRAVEL) && DefaultSurfaceBuilder.isEmptyBelow(blockColumn, i)) {
            return Blocks.STONE.defaultBlockState();
        }
        return blockState;
    }

    private static boolean isEmptyBelow(BlockColumn blockColumn, int i) {
        BlockState blockState = blockColumn.getBlock(i - 1);
        return blockState.isAir() || !blockState.getFluidState().isEmpty();
    }
}

