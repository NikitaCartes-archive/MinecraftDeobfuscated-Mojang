/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class NetherForestSurfaceBuilder
extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected long seed;
    private PerlinNoise decorationNoise;

    public NetherForestSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, int m, long n, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        int o = l;
        int p = i & 0xF;
        int q = j & 0xF;
        double e = this.decorationNoise.getValue((double)i * 0.1, l, (double)j * 0.1);
        boolean bl = e > 0.15 + random.nextDouble() * 0.35;
        double f = this.decorationNoise.getValue((double)i * 0.1, 109.0, (double)j * 0.1);
        boolean bl2 = f > 0.25 + random.nextDouble() * 0.9;
        int r = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int s = -1;
        BlockState blockState3 = surfaceBuilderBaseConfiguration.getUnderMaterial();
        for (int t = 127; t >= m; --t) {
            mutableBlockPos.set(p, t, q);
            BlockState blockState4 = surfaceBuilderBaseConfiguration.getTopMaterial();
            BlockState blockState5 = chunkAccess.getBlockState(mutableBlockPos);
            if (blockState5.isAir()) {
                s = -1;
                continue;
            }
            if (!blockState5.is(blockState.getBlock())) continue;
            if (s == -1) {
                boolean bl3 = false;
                if (r <= 0) {
                    bl3 = true;
                    blockState3 = surfaceBuilderBaseConfiguration.getUnderMaterial();
                }
                if (bl) {
                    blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();
                } else if (bl2) {
                    blockState4 = surfaceBuilderBaseConfiguration.getUnderwaterMaterial();
                }
                if (t < o && bl3) {
                    blockState4 = blockState2;
                }
                s = r;
                if (t >= o - 1) {
                    chunkAccess.setBlockState(mutableBlockPos, blockState4, false);
                    continue;
                }
                chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
                continue;
            }
            if (s <= 0) continue;
            --s;
            chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
        }
    }

    @Override
    public void initNoise(long l) {
        if (this.seed != l || this.decorationNoise == null) {
            this.decorationNoise = new PerlinNoise((RandomSource)new WorldgenRandom(l), ImmutableList.of(Integer.valueOf(0)));
        }
        this.seed = l;
    }
}

