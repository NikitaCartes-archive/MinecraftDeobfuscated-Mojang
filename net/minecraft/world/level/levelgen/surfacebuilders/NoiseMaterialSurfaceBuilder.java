/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.surfacebuilders.DefaultSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;

public abstract class NoiseMaterialSurfaceBuilder
extends DefaultSurfaceBuilder {
    private long seed;
    protected NormalNoise surfaceNoise;

    public NoiseMaterialSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
        super(codec);
    }

    @Override
    public void apply(Random random, BlockColumn blockColumn, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, int m, long n, SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration) {
        BlockState blockState4;
        BlockState blockState3;
        if (this.getSteepMaterial() != null && this.isSteepTerrain(blockColumn, i, j, this.getSteepMaterial())) {
            blockState3 = this.getSteepMaterial().getState();
            blockState4 = this.getSteepMaterial().getState();
        } else {
            blockState3 = this.getTopMaterial(surfaceBuilderBaseConfiguration, i, j);
            blockState4 = this.getMidMaterial(surfaceBuilderBaseConfiguration, i, j);
        }
        this.apply(random, blockColumn, biome, i, j, k, d, blockState, blockState2, blockState3, blockState4, surfaceBuilderBaseConfiguration.getUnderwaterMaterial(), l, m);
    }

    protected BlockState getMaterial(double d, int i, int j, BlockState blockState, BlockState blockState2, double e, double f) {
        double g = this.surfaceNoise.getValue((double)i * d, 0.0, (double)j * d);
        BlockState blockState3 = g >= e && g <= f ? blockState2 : blockState;
        return blockState3;
    }

    @Override
    public void initNoise(long l) {
        if (this.seed != l) {
            WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(l));
            this.surfaceNoise = NormalNoise.create((RandomSource)worldgenRandom, -3, 1.0, 1.0, 1.0, 1.0);
        }
        this.seed = l;
    }

    public boolean isSteepTerrain(BlockColumn blockColumn, int i, int j, SteepMaterial steepMaterial) {
        return false;
    }

    @Nullable
    protected abstract SteepMaterial getSteepMaterial();

    protected abstract BlockState getTopMaterial(SurfaceBuilderBaseConfiguration var1, int var2, int var3);

    protected abstract BlockState getMidMaterial(SurfaceBuilderBaseConfiguration var1, int var2, int var3);

    public static class SteepMaterial {
        private final BlockState state;
        private final boolean northSlopes;
        private final boolean southSlopes;
        private final boolean westSlopes;
        private final boolean eastSlopes;

        public SteepMaterial(BlockState blockState, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
            this.state = blockState;
            this.northSlopes = bl;
            this.southSlopes = bl2;
            this.westSlopes = bl3;
            this.eastSlopes = bl4;
        }

        public BlockState getState() {
            return this.state;
        }

        public boolean hasNorthSlopes() {
            return this.northSlopes;
        }

        public boolean hasSouthSlopes() {
            return this.southSlopes;
        }

        public boolean hasWestSlopes() {
            return this.westSlopes;
        }

        public boolean hasEastSlopes() {
            return this.eastSlopes;
        }
    }
}

