/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.TerrainInfo;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.Nullable;

public class NoiseChunk {
    final int cellWidth;
    final int cellHeight;
    final int cellCountY;
    final int cellCountXZ;
    final int cellNoiseMinY;
    final int firstCellX;
    final int firstCellZ;
    private final int firstNoiseX;
    private final int firstNoiseZ;
    final List<NoiseInterpolator> interpolators;
    private final NoiseSampler.FlatNoiseData[][] noiseData;
    private final Long2ObjectMap<TerrainInfo> terrainInfo = new Long2ObjectOpenHashMap<TerrainInfo>();
    private final Aquifer aquifer;
    private final BlockStateFiller baseNoise;
    private final BlockStateFiller oreVeins;
    private final Blender blender;

    public NoiseChunk(int i, int j, int k, int l, int m, NoiseSampler noiseSampler, int n, int o, NoiseFiller noiseFiller, Supplier<NoiseGeneratorSettings> supplier, Aquifer.FluidPicker fluidPicker, Blender blender) {
        this.cellWidth = i;
        this.cellHeight = j;
        this.cellCountY = l;
        this.cellCountXZ = k;
        this.cellNoiseMinY = m;
        this.firstCellX = Math.floorDiv(n, i);
        this.firstCellZ = Math.floorDiv(o, i);
        this.interpolators = Lists.newArrayList();
        this.firstNoiseX = QuartPos.fromBlock(n);
        this.firstNoiseZ = QuartPos.fromBlock(o);
        int p = QuartPos.fromBlock(k * i);
        this.noiseData = new NoiseSampler.FlatNoiseData[p + 1][];
        this.blender = blender;
        for (int q = 0; q <= p; ++q) {
            int r = this.firstNoiseX + q;
            this.noiseData[q] = new NoiseSampler.FlatNoiseData[p + 1];
            for (int s = 0; s <= p; ++s) {
                int t = this.firstNoiseZ + s;
                this.noiseData[q][s] = noiseSampler.noiseData(r, t, blender);
            }
        }
        this.aquifer = noiseSampler.createAquifer(this, n, o, m, l, fluidPicker, supplier.get().isAquifersEnabled());
        this.baseNoise = noiseSampler.makeBaseNoiseFiller(this, noiseFiller, supplier.get().isNoodleCavesEnabled());
        this.oreVeins = noiseSampler.makeOreVeinifier(this, supplier.get().isOreVeinsEnabled());
    }

    public NoiseSampler.FlatNoiseData noiseData(int i, int j) {
        return this.noiseData[i - this.firstNoiseX][j - this.firstNoiseZ];
    }

    public TerrainInfo terrainInfoWide(NoiseSampler noiseSampler, int i, int j) {
        int k = i - this.firstNoiseX;
        int l2 = j - this.firstNoiseZ;
        int m = this.noiseData.length;
        if (k >= 0 && l2 >= 0 && k < m && l2 < m) {
            return this.noiseData[k][l2].terrainInfo();
        }
        return this.terrainInfo.computeIfAbsent(ChunkPos.asLong(i, j), l -> noiseSampler.noiseData(ChunkPos.getX(l), ChunkPos.getZ(l), this.blender).terrainInfo());
    }

    public TerrainInfo terrainInfoInterpolated(int i, int j) {
        int k = QuartPos.fromBlock(i) - this.firstNoiseX;
        int l = QuartPos.fromBlock(j) - this.firstNoiseZ;
        TerrainInfo terrainInfo = this.noiseData[k][l].terrainInfo();
        TerrainInfo terrainInfo2 = this.noiseData[k][l + 1].terrainInfo();
        TerrainInfo terrainInfo3 = this.noiseData[k + 1][l].terrainInfo();
        TerrainInfo terrainInfo4 = this.noiseData[k + 1][l + 1].terrainInfo();
        double d = (double)Math.floorMod(i, 4) / 4.0;
        double e = (double)Math.floorMod(j, 4) / 4.0;
        double f = Mth.lerp2(d, e, terrainInfo.offset(), terrainInfo3.offset(), terrainInfo2.offset(), terrainInfo4.offset());
        double g = Mth.lerp2(d, e, terrainInfo.factor(), terrainInfo3.factor(), terrainInfo2.factor(), terrainInfo4.factor());
        double h = Mth.lerp2(d, e, terrainInfo.jaggedness(), terrainInfo3.jaggedness(), terrainInfo2.jaggedness(), terrainInfo4.jaggedness());
        return new TerrainInfo(f, g, h);
    }

    protected NoiseInterpolator createNoiseInterpolator(NoiseFiller noiseFiller) {
        return new NoiseInterpolator(noiseFiller);
    }

    public Blender getBlender() {
        return this.blender;
    }

    public void initializeForFirstCellX() {
        this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.initializeForFirstCellX());
    }

    public void advanceCellX(int i) {
        this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.advanceCellX(i));
    }

    public void selectCellYZ(int i, int j) {
        this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.selectCellYZ(i, j));
    }

    public void updateForY(double d) {
        this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.updateForY(d));
    }

    public void updateForX(double d) {
        this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.updateForX(d));
    }

    public void updateForZ(double d) {
        this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.updateForZ(d));
    }

    public void swapSlices() {
        this.interpolators.forEach(NoiseInterpolator::swapSlices);
    }

    public Aquifer aquifer() {
        return this.aquifer;
    }

    @Nullable
    protected BlockState updateNoiseAndGenerateBaseState(int i, int j, int k) {
        return this.baseNoise.calculate(i, j, k);
    }

    @Nullable
    protected BlockState oreVeinify(int i, int j, int k) {
        return this.oreVeins.calculate(i, j, k);
    }

    @FunctionalInterface
    public static interface NoiseFiller {
        public double calculateNoise(int var1, int var2, int var3);
    }

    @FunctionalInterface
    public static interface BlockStateFiller {
        @Nullable
        public BlockState calculate(int var1, int var2, int var3);
    }

    public class NoiseInterpolator
    implements Sampler {
        private double[][] slice0;
        private double[][] slice1;
        private final NoiseFiller noiseFiller;
        private double noise000;
        private double noise001;
        private double noise100;
        private double noise101;
        private double noise010;
        private double noise011;
        private double noise110;
        private double noise111;
        private double valueXZ00;
        private double valueXZ10;
        private double valueXZ01;
        private double valueXZ11;
        private double valueZ0;
        private double valueZ1;
        private double value;

        NoiseInterpolator(NoiseFiller noiseFiller) {
            this.noiseFiller = noiseFiller;
            this.slice0 = this.allocateSlice(NoiseChunk.this.cellCountY, NoiseChunk.this.cellCountXZ);
            this.slice1 = this.allocateSlice(NoiseChunk.this.cellCountY, NoiseChunk.this.cellCountXZ);
            NoiseChunk.this.interpolators.add(this);
        }

        private double[][] allocateSlice(int i, int j) {
            int k = j + 1;
            int l = i + 1;
            double[][] ds = new double[k][l];
            for (int m = 0; m < k; ++m) {
                ds[m] = new double[l];
            }
            return ds;
        }

        void initializeForFirstCellX() {
            this.fillSlice(this.slice0, NoiseChunk.this.firstCellX);
        }

        void advanceCellX(int i) {
            this.fillSlice(this.slice1, NoiseChunk.this.firstCellX + i + 1);
        }

        private void fillSlice(double[][] ds, int i) {
            for (int j = 0; j < NoiseChunk.this.cellCountXZ + 1; ++j) {
                int k = NoiseChunk.this.firstCellZ + j;
                for (int l = 0; l < NoiseChunk.this.cellCountY + 1; ++l) {
                    double d;
                    int m = l + NoiseChunk.this.cellNoiseMinY;
                    int n = m * NoiseChunk.this.cellHeight;
                    ds[j][l] = d = this.noiseFiller.calculateNoise(i * NoiseChunk.this.cellWidth, n, k * NoiseChunk.this.cellWidth);
                }
            }
        }

        void selectCellYZ(int i, int j) {
            this.noise000 = this.slice0[j][i];
            this.noise001 = this.slice0[j + 1][i];
            this.noise100 = this.slice1[j][i];
            this.noise101 = this.slice1[j + 1][i];
            this.noise010 = this.slice0[j][i + 1];
            this.noise011 = this.slice0[j + 1][i + 1];
            this.noise110 = this.slice1[j][i + 1];
            this.noise111 = this.slice1[j + 1][i + 1];
        }

        void updateForY(double d) {
            this.valueXZ00 = Mth.lerp(d, this.noise000, this.noise010);
            this.valueXZ10 = Mth.lerp(d, this.noise100, this.noise110);
            this.valueXZ01 = Mth.lerp(d, this.noise001, this.noise011);
            this.valueXZ11 = Mth.lerp(d, this.noise101, this.noise111);
        }

        void updateForX(double d) {
            this.valueZ0 = Mth.lerp(d, this.valueXZ00, this.valueXZ10);
            this.valueZ1 = Mth.lerp(d, this.valueXZ01, this.valueXZ11);
        }

        void updateForZ(double d) {
            this.value = Mth.lerp(d, this.valueZ0, this.valueZ1);
        }

        @Override
        public double sample() {
            return this.value;
        }

        private void swapSlices() {
            double[][] ds = this.slice0;
            this.slice0 = this.slice1;
            this.slice1 = ds;
        }
    }

    @FunctionalInterface
    public static interface Sampler {
        public double sample();
    }

    @FunctionalInterface
    public static interface InterpolatableNoise {
        public Sampler instantiate(NoiseChunk var1);
    }
}

