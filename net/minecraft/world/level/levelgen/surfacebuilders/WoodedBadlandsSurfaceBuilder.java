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
import net.minecraft.world.level.levelgen.surfacebuilders.BadlandsSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;

public class WoodedBadlandsSurfaceBuilder
extends BadlandsSurfaceBuilder {
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();

    public WoodedBadlandsSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, int m, long n, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        int o = i & 0xF;
        int p = j & 0xF;
        BlockState blockState3 = WHITE_TERRACOTTA;
        SurfaceBuilderConfiguration surfaceBuilderConfiguration = biome.getGenerationSettings().getSurfaceBuilderConfig();
        BlockState blockState4 = surfaceBuilderConfiguration.getUnderMaterial();
        BlockState blockState5 = surfaceBuilderConfiguration.getTopMaterial();
        BlockState blockState6 = blockState4;
        int q = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
        boolean bl = Math.cos(d / 3.0 * Math.PI) > 0.0;
        int r = -1;
        boolean bl2 = false;
        int s = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int t = k; t >= m; --t) {
            if (s >= 15) continue;
            mutableBlockPos.set(o, t, p);
            BlockState blockState7 = chunkAccess.getBlockState(mutableBlockPos);
            if (blockState7.isAir()) {
                r = -1;
                continue;
            }
            if (!blockState7.is(blockState.getBlock())) continue;
            if (r == -1) {
                bl2 = false;
                if (q <= 0) {
                    blockState3 = Blocks.AIR.defaultBlockState();
                    blockState6 = blockState;
                } else if (t >= l - 4 && t <= l + 1) {
                    blockState3 = WHITE_TERRACOTTA;
                    blockState6 = blockState4;
                }
                if (t < l && (blockState3 == null || blockState3.isAir())) {
                    blockState3 = blockState2;
                }
                r = q + Math.max(0, t - l);
                if (t >= l - 1) {
                    if (t > 86 + q * 2) {
                        if (bl) {
                            chunkAccess.setBlockState(mutableBlockPos, Blocks.COARSE_DIRT.defaultBlockState(), false);
                        } else {
                            chunkAccess.setBlockState(mutableBlockPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
                        }
                    } else if (t > l + 3 + q) {
                        BlockState blockState8 = t < 64 || t > 127 ? ORANGE_TERRACOTTA : (bl ? TERRACOTTA : this.getBand(i, t, j));
                        chunkAccess.setBlockState(mutableBlockPos, blockState8, false);
                    } else {
                        chunkAccess.setBlockState(mutableBlockPos, blockState5, false);
                        bl2 = true;
                    }
                } else {
                    chunkAccess.setBlockState(mutableBlockPos, blockState6, false);
                    if (blockState6 == WHITE_TERRACOTTA) {
                        chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
                    }
                }
            } else if (r > 0) {
                --r;
                if (bl2) {
                    chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
                } else {
                    chunkAccess.setBlockState(mutableBlockPos, this.getBand(i, t, j), false);
                }
            }
            ++s;
        }
    }
}

