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
        BlockState blockState6 = blockState3;
        BlockState blockState7 = blockState4;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int m = -1;
        int n = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
        int o = i & 0xF;
        int p = j & 0xF;
        for (int q = k; q >= 50; --q) {
            mutableBlockPos.set(o, q, p);
            BlockState blockState8 = chunkAccess.getBlockState(mutableBlockPos);
            if (blockState8.isAir()) {
                m = -1;
                continue;
            }
            if (!blockState8.is(blockState.getBlock())) continue;
            if (m == -1) {
                if (n <= 0) {
                    blockState6 = Blocks.AIR.defaultBlockState();
                    blockState7 = blockState;
                } else if (q >= l - 4 && q <= l + 1) {
                    blockState6 = blockState3;
                    blockState7 = blockState4;
                }
                if (q < l && (blockState6 == null || blockState6.isAir())) {
                    blockState6 = biome.getTemperature(mutableBlockPos.set(i, q, j)) < 0.15f ? Blocks.ICE.defaultBlockState() : blockState2;
                    mutableBlockPos.set(o, q, p);
                }
                m = n;
                if (q >= l - 1) {
                    chunkAccess.setBlockState(mutableBlockPos, blockState6, false);
                    continue;
                }
                if (q < l - 7 - n) {
                    blockState6 = Blocks.AIR.defaultBlockState();
                    blockState7 = blockState;
                    chunkAccess.setBlockState(mutableBlockPos, blockState5, false);
                    continue;
                }
                chunkAccess.setBlockState(mutableBlockPos, blockState7, false);
                continue;
            }
            if (m <= 0) continue;
            chunkAccess.setBlockState(mutableBlockPos, blockState7, false);
            if (--m != 0 || !blockState7.is(Blocks.SAND) || n <= 1) continue;
            m = random.nextInt(4) + Math.max(0, q - 63);
            blockState7 = blockState7.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
        }
    }
}

