/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import java.util.Comparator;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public abstract class NetherCappedSurfaceBuilder
extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
    private long seed;
    private ImmutableMap<BlockState, PerlinNoise> floorNoises = ImmutableMap.of();
    private ImmutableMap<BlockState, PerlinNoise> ceilingNoises = ImmutableMap.of();
    private PerlinNoise patchNoise;

    public NetherCappedSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override
    public void apply(Random random, BlockColumn blockColumn, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, int m, long n, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        int o = l + 1;
        int p = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
        int q = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
        double e = 0.03125;
        boolean bl = this.patchNoise.getValue((double)i * 0.03125, 109.0, (double)j * 0.03125) * 75.0 + random.nextDouble() > 0.0;
        BlockState blockState3 = (BlockState)this.ceilingNoises.entrySet().stream().max(Comparator.comparing(entry -> ((PerlinNoise)entry.getValue()).getValue(i, l, j))).get().getKey();
        BlockState blockState4 = (BlockState)this.floorNoises.entrySet().stream().max(Comparator.comparing(entry -> ((PerlinNoise)entry.getValue()).getValue(i, l, j))).get().getKey();
        BlockState blockState5 = blockColumn.getBlock(128);
        for (int r = 127; r >= m; --r) {
            int s;
            BlockState blockState6 = blockColumn.getBlock(r);
            if (blockState5.is(blockState.getBlock()) && (blockState6.isAir() || blockState6 == blockState2)) {
                for (s = 0; s < p && blockColumn.getBlock(r + s).is(blockState.getBlock()); ++s) {
                    blockColumn.setBlock(r + s, blockState3);
                }
            }
            if ((blockState5.isAir() || blockState5 == blockState2) && blockState6.is(blockState.getBlock())) {
                for (s = 0; s < q && blockColumn.getBlock(r - s).is(blockState.getBlock()); ++s) {
                    if (bl && r >= o - 4 && r <= o + 1) {
                        blockColumn.setBlock(r - s, this.getPatchBlockState());
                        continue;
                    }
                    blockColumn.setBlock(r - s, blockState4);
                }
            }
            blockState5 = blockState6;
        }
    }

    @Override
    public void initNoise(long l) {
        if (this.seed != l || this.patchNoise == null || this.floorNoises.isEmpty() || this.ceilingNoises.isEmpty()) {
            this.floorNoises = NetherCappedSurfaceBuilder.initPerlinNoises(this.getFloorBlockStates(), l);
            this.ceilingNoises = NetherCappedSurfaceBuilder.initPerlinNoises(this.getCeilingBlockStates(), l + (long)this.floorNoises.size());
            this.patchNoise = new PerlinNoise((RandomSource)new WorldgenRandom(l + (long)this.floorNoises.size() + (long)this.ceilingNoises.size()), ImmutableList.of(Integer.valueOf(0)));
        }
        this.seed = l;
    }

    private static ImmutableMap<BlockState, PerlinNoise> initPerlinNoises(ImmutableList<BlockState> immutableList, long l) {
        ImmutableMap.Builder<BlockState, PerlinNoise> builder = new ImmutableMap.Builder<BlockState, PerlinNoise>();
        for (BlockState blockState : immutableList) {
            builder.put(blockState, new PerlinNoise((RandomSource)new WorldgenRandom(l), ImmutableList.of(Integer.valueOf(-4))));
            ++l;
        }
        return builder.build();
    }

    protected abstract ImmutableList<BlockState> getFloorBlockStates();

    protected abstract ImmutableList<BlockState> getCeilingBlockStates();

    protected abstract BlockState getPatchBlockState();
}

