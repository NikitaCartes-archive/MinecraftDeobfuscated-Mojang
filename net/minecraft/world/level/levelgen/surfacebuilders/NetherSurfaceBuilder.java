/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
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

public class NetherSurfaceBuilder
extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
    protected long seed;
    protected PerlinNoise decorationNoise;

    public NetherSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, int m, long n, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        int o = l;
        int p = i & 0xF;
        int q = j & 0xF;
        double e = 0.03125;
        boolean bl = this.decorationNoise.getValue((double)i * 0.03125, (double)j * 0.03125, 0.0) * 75.0 + random.nextDouble() > 0.0;
        boolean bl2 = this.decorationNoise.getValue((double)i * 0.03125, 109.0, (double)j * 0.03125) * 75.0 + random.nextDouble() > 0.0;
        int r = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int s = -1;
        BlockState blockState3 = surfaceBuilderBaseConfiguration.getTopMaterial();
        BlockState blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();
        for (int t = 127; t >= m; --t) {
            mutableBlockPos.set(p, t, q);
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
                    blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();
                } else if (t >= o - 4 && t <= o + 1) {
                    blockState3 = surfaceBuilderBaseConfiguration.getTopMaterial();
                    blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();
                    if (bl2) {
                        blockState3 = GRAVEL;
                        blockState4 = surfaceBuilderBaseConfiguration.getUnderMaterial();
                    }
                    if (bl) {
                        blockState3 = SOUL_SAND;
                        blockState4 = SOUL_SAND;
                    }
                }
                if (t < o && bl3) {
                    blockState3 = blockState2;
                }
                s = r;
                if (t >= o - 1) {
                    chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
                    continue;
                }
                chunkAccess.setBlockState(mutableBlockPos, blockState4, false);
                continue;
            }
            if (s <= 0) continue;
            --s;
            chunkAccess.setBlockState(mutableBlockPos, blockState4, false);
        }
    }

    @Override
    public void initNoise(long l) {
        if (this.seed != l || this.decorationNoise == null) {
            this.decorationNoise = new PerlinNoise((RandomSource)new WorldgenRandom(l), IntStream.rangeClosed(-3, 0));
        }
        this.seed = l;
    }
}

