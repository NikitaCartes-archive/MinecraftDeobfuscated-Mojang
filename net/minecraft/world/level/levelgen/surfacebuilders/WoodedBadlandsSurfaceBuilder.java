/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.surfacebuilders.BadlandsSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;

public class WoodedBadlandsSurfaceBuilder
extends BadlandsSurfaceBuilder {
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();

    public WoodedBadlandsSurfaceBuilder(Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration> function) {
        super(function);
    }

    @Override
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        int n = i & 0xF;
        int o = j & 0xF;
        BlockState blockState3 = WHITE_TERRACOTTA;
        BlockState blockState4 = biome.getSurfaceBuilderConfig().getUnderMaterial();
        int p = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
        boolean bl = Math.cos(d / 3.0 * Math.PI) > 0.0;
        int q = -1;
        boolean bl2 = false;
        int r = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int s = k; s >= 0; --s) {
            if (r >= 15) continue;
            mutableBlockPos.set(n, s, o);
            BlockState blockState5 = chunkAccess.getBlockState(mutableBlockPos);
            if (blockState5.isAir()) {
                q = -1;
                continue;
            }
            if (blockState5.getBlock() != blockState.getBlock()) continue;
            if (q == -1) {
                bl2 = false;
                if (p <= 0) {
                    blockState3 = Blocks.AIR.defaultBlockState();
                    blockState4 = blockState;
                } else if (s >= l - 4 && s <= l + 1) {
                    blockState3 = WHITE_TERRACOTTA;
                    blockState4 = biome.getSurfaceBuilderConfig().getUnderMaterial();
                }
                if (s < l && (blockState3 == null || blockState3.isAir())) {
                    blockState3 = blockState2;
                }
                q = p + Math.max(0, s - l);
                if (s >= l - 1) {
                    if (s > 86 + p * 2) {
                        if (bl) {
                            chunkAccess.setBlockState(mutableBlockPos, Blocks.COARSE_DIRT.defaultBlockState(), false);
                        } else {
                            chunkAccess.setBlockState(mutableBlockPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
                        }
                    } else if (s > l + 3 + p) {
                        BlockState blockState6 = s < 64 || s > 127 ? ORANGE_TERRACOTTA : (bl ? TERRACOTTA : this.getBand(i, s, j));
                        chunkAccess.setBlockState(mutableBlockPos, blockState6, false);
                    } else {
                        chunkAccess.setBlockState(mutableBlockPos, biome.getSurfaceBuilderConfig().getTopMaterial(), false);
                        bl2 = true;
                    }
                } else {
                    chunkAccess.setBlockState(mutableBlockPos, blockState4, false);
                    if (blockState4 == WHITE_TERRACOTTA) {
                        chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
                    }
                }
            } else if (q > 0) {
                --q;
                if (bl2) {
                    chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
                } else {
                    chunkAccess.setBlockState(mutableBlockPos, this.getBand(i, s, j), false);
                }
            }
            ++r;
        }
    }
}

