/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;

public class DefaultSurfaceBuilder
extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    public DefaultSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        this.apply(random, chunkAccess, biome, i, j, k, d, blockState, blockState2, surfaceBuilderBaseConfiguration.getTopMaterial(), surfaceBuilderBaseConfiguration.getUnderMaterial(), surfaceBuilderBaseConfiguration.getUnderwaterMaterial(), l);
    }

    protected void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, BlockState blockState3, BlockState blockState4, BlockState blockState5, int l) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int m = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
        if (m == 0) {
            boolean bl = false;
            for (int n = k; n >= 50; --n) {
                mutableBlockPos.set(i, n, j);
                BlockState blockState6 = chunkAccess.getBlockState(mutableBlockPos);
                if (blockState6.isAir()) {
                    bl = false;
                    continue;
                }
                if (!blockState6.is(blockState.getBlock())) continue;
                if (!bl) {
                    BlockState blockState7 = n >= l ? Blocks.AIR.defaultBlockState() : (n == l - 1 ? (biome.getTemperature(mutableBlockPos) < 0.15f ? Blocks.ICE.defaultBlockState() : blockState2) : (n >= l - (7 + m) ? blockState : blockState5));
                    chunkAccess.setBlockState(mutableBlockPos, blockState7, false);
                }
                bl = true;
            }
        } else {
            BlockState blockState8 = blockState4;
            int n = -1;
            for (int o = k; o >= 50; --o) {
                mutableBlockPos.set(i, o, j);
                BlockState blockState7 = chunkAccess.getBlockState(mutableBlockPos);
                if (blockState7.isAir()) {
                    n = -1;
                    continue;
                }
                if (!blockState7.is(blockState.getBlock())) continue;
                if (n == -1) {
                    BlockState blockState9;
                    n = m;
                    if (o >= l + 2) {
                        blockState9 = blockState3;
                    } else if (o >= l - 1) {
                        blockState8 = blockState4;
                        blockState9 = blockState3;
                    } else if (o >= l - 4) {
                        blockState8 = blockState4;
                        blockState9 = blockState4;
                    } else if (o >= l - (7 + m)) {
                        blockState9 = blockState8;
                    } else {
                        blockState8 = blockState;
                        blockState9 = blockState5;
                    }
                    chunkAccess.setBlockState(mutableBlockPos, blockState9, false);
                    continue;
                }
                if (n <= 0) continue;
                chunkAccess.setBlockState(mutableBlockPos, blockState8, false);
                if (--n != 0 || !blockState8.is(Blocks.SAND) || m <= 1) continue;
                n = random.nextInt(4) + Math.max(0, o - l);
                blockState8 = blockState8.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
            }
        }
    }
}

