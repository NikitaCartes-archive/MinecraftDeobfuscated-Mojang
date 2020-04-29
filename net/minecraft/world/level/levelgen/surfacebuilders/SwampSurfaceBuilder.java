/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;

public class SwampSurfaceBuilder
extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    public SwampSurfaceBuilder(Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration> function) {
        super(function);
    }

    @Override
    public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        double e = Biome.BIOME_INFO_NOISE.getValue((double)i * 0.25, (double)j * 0.25, false);
        if (e > 0.0) {
            int n = i & 0xF;
            int o = j & 0xF;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int p = k; p >= 0; --p) {
                mutableBlockPos.set(n, p, o);
                if (chunkAccess.getBlockState(mutableBlockPos).isAir()) continue;
                if (p != 62 || chunkAccess.getBlockState(mutableBlockPos).is(blockState2.getBlock())) break;
                chunkAccess.setBlockState(mutableBlockPos, blockState2, false);
                break;
            }
        }
        SurfaceBuilder.DEFAULT.apply(random, chunkAccess, biome, i, j, k, d, blockState, blockState2, l, m, surfaceBuilderBaseConfiguration);
    }
}

