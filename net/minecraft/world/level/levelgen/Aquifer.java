/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.Nullable;

public interface Aquifer {
    public static Aquifer create(NoiseChunk noiseChunk, ChunkPos chunkPos, NormalNoise normalNoise, NormalNoise normalNoise2, NormalNoise normalNoise3, NormalNoise normalNoise4, PositionalRandomFactory positionalRandomFactory, int i, int j, FluidPicker fluidPicker) {
        return new NoiseBasedAquifer(noiseChunk, chunkPos, normalNoise, normalNoise2, normalNoise3, normalNoise4, positionalRandomFactory, i, j, fluidPicker);
    }

    public static Aquifer createDisabled(final FluidPicker fluidPicker) {
        return new Aquifer(){

            @Override
            @Nullable
            public BlockState computeSubstance(int i, int j, int k, double d, double e) {
                if (e > 0.0) {
                    return null;
                }
                return fluidPicker.computeFluid(i, j, k).at(j);
            }

            @Override
            public boolean shouldScheduleFluidUpdate() {
                return false;
            }
        };
    }

    @Nullable
    public BlockState computeSubstance(int var1, int var2, int var3, double var4, double var6);

    public boolean shouldScheduleFluidUpdate();

    public static class NoiseBasedAquifer
    implements Aquifer,
    FluidPicker {
        private static final int X_RANGE = 10;
        private static final int Y_RANGE = 9;
        private static final int Z_RANGE = 10;
        private static final int X_SEPARATION = 6;
        private static final int Y_SEPARATION = 3;
        private static final int Z_SEPARATION = 6;
        private static final int X_SPACING = 16;
        private static final int Y_SPACING = 12;
        private static final int Z_SPACING = 16;
        private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;
        private static final double FLOWING_UPDATE_SIMULARITY = NoiseBasedAquifer.similarity(Mth.square(10), Mth.square(12));
        private final NoiseChunk noiseChunk;
        private final NormalNoise barrierNoise;
        private final NormalNoise fluidLevelFloodednessNoise;
        private final NormalNoise fluidLevelSpreadNoise;
        private final NormalNoise lavaNoise;
        private final PositionalRandomFactory positionalRandomFactory;
        private final FluidStatus[] aquiferCache;
        private final long[] aquiferLocationCache;
        private final FluidPicker globalFluidPicker;
        private boolean shouldScheduleFluidUpdate;
        private final int minGridX;
        private final int minGridY;
        private final int minGridZ;
        private final int gridSizeX;
        private final int gridSizeZ;
        private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{{-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {0, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}};

        NoiseBasedAquifer(NoiseChunk noiseChunk, ChunkPos chunkPos, NormalNoise normalNoise, NormalNoise normalNoise2, NormalNoise normalNoise3, NormalNoise normalNoise4, PositionalRandomFactory positionalRandomFactory, int i, int j, FluidPicker fluidPicker) {
            this.noiseChunk = noiseChunk;
            this.barrierNoise = normalNoise;
            this.fluidLevelFloodednessNoise = normalNoise2;
            this.fluidLevelSpreadNoise = normalNoise3;
            this.lavaNoise = normalNoise4;
            this.positionalRandomFactory = positionalRandomFactory;
            this.minGridX = this.gridX(chunkPos.getMinBlockX()) - 1;
            this.globalFluidPicker = fluidPicker;
            int k = this.gridX(chunkPos.getMaxBlockX()) + 1;
            this.gridSizeX = k - this.minGridX + 1;
            this.minGridY = this.gridY(i) - 1;
            int l = this.gridY(i + j) + 1;
            int m = l - this.minGridY + 1;
            this.minGridZ = this.gridZ(chunkPos.getMinBlockZ()) - 1;
            int n = this.gridZ(chunkPos.getMaxBlockZ()) + 1;
            this.gridSizeZ = n - this.minGridZ + 1;
            int o = this.gridSizeX * m * this.gridSizeZ;
            this.aquiferCache = new FluidStatus[o];
            this.aquiferLocationCache = new long[o];
            Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
        }

        private int getIndex(int i, int j, int k) {
            int l = i - this.minGridX;
            int m = j - this.minGridY;
            int n = k - this.minGridZ;
            return (m * this.gridSizeZ + n) * this.gridSizeX + l;
        }

        @Override
        @Nullable
        public BlockState computeSubstance(int i, int j, int k, double d, double e) {
            if (d <= -64.0) {
                return this.globalFluidPicker.computeFluid(i, j, k).at(j);
            }
            if (e <= 0.0) {
                boolean bl;
                double f;
                BlockState blockState;
                FluidStatus fluidStatus = this.globalFluidPicker.computeFluid(i, j, k);
                if (fluidStatus.at(j).is(Blocks.LAVA)) {
                    blockState = Blocks.LAVA.defaultBlockState();
                    f = 0.0;
                    bl = false;
                } else {
                    int l = Math.floorDiv(i - 5, 16);
                    int m = Math.floorDiv(j + 1, 12);
                    int n = Math.floorDiv(k - 5, 16);
                    int o = Integer.MAX_VALUE;
                    int p = Integer.MAX_VALUE;
                    int q = Integer.MAX_VALUE;
                    long r = 0L;
                    long s = 0L;
                    long t = 0L;
                    for (int u = 0; u <= 1; ++u) {
                        for (int v = -1; v <= 1; ++v) {
                            for (int w = 0; w <= 1; ++w) {
                                long ac;
                                int x = l + u;
                                int y = m + v;
                                int z = n + w;
                                int aa = this.getIndex(x, y, z);
                                long ab = this.aquiferLocationCache[aa];
                                if (ab != Long.MAX_VALUE) {
                                    ac = ab;
                                } else {
                                    RandomSource randomSource = this.positionalRandomFactory.at(x, y, z);
                                    this.aquiferLocationCache[aa] = ac = BlockPos.asLong(x * 16 + randomSource.nextInt(10), y * 12 + randomSource.nextInt(9), z * 16 + randomSource.nextInt(10));
                                }
                                int ad = BlockPos.getX(ac) - i;
                                int ae = BlockPos.getY(ac) - j;
                                int af = BlockPos.getZ(ac) - k;
                                int ag = ad * ad + ae * ae + af * af;
                                if (o >= ag) {
                                    t = s;
                                    s = r;
                                    r = ac;
                                    q = p;
                                    p = o;
                                    o = ag;
                                    continue;
                                }
                                if (p >= ag) {
                                    t = s;
                                    s = ac;
                                    q = p;
                                    p = ag;
                                    continue;
                                }
                                if (q < ag) continue;
                                t = ac;
                                q = ag;
                            }
                        }
                    }
                    FluidStatus fluidStatus2 = this.getAquiferStatus(r);
                    FluidStatus fluidStatus3 = this.getAquiferStatus(s);
                    FluidStatus fluidStatus4 = this.getAquiferStatus(t);
                    double g = NoiseBasedAquifer.similarity(o, p);
                    double h = NoiseBasedAquifer.similarity(o, q);
                    double ah = NoiseBasedAquifer.similarity(p, q);
                    boolean bl2 = bl = g >= FLOWING_UPDATE_SIMULARITY;
                    if (fluidStatus2.at(j).is(Blocks.WATER) && this.globalFluidPicker.computeFluid(i, j - 1, k).at(j - 1).is(Blocks.LAVA)) {
                        f = 1.0;
                    } else if (g > -1.0) {
                        MutableDouble mutableDouble = new MutableDouble(Double.NaN);
                        double ai = this.calculatePressure(i, j, k, mutableDouble, fluidStatus2, fluidStatus3);
                        double aj = this.calculatePressure(i, j, k, mutableDouble, fluidStatus2, fluidStatus4);
                        double ak = this.calculatePressure(i, j, k, mutableDouble, fluidStatus3, fluidStatus4);
                        double al = Math.max(0.0, g);
                        double am = Math.max(0.0, h);
                        double an = Math.max(0.0, ah);
                        double ao = 2.0 * al * Math.max(ai, Math.max(aj * am, ak * an));
                        f = Math.max(0.0, ao);
                    } else {
                        f = 0.0;
                    }
                    blockState = fluidStatus2.at(j);
                }
                if (e + f <= 0.0) {
                    this.shouldScheduleFluidUpdate = bl;
                    return blockState;
                }
            }
            this.shouldScheduleFluidUpdate = false;
            return null;
        }

        @Override
        public boolean shouldScheduleFluidUpdate() {
            return this.shouldScheduleFluidUpdate;
        }

        private static double similarity(int i, int j) {
            double d = 25.0;
            return 1.0 - (double)Math.abs(j - i) / 25.0;
        }

        private double calculatePressure(int i, int j, int k, MutableDouble mutableDouble, FluidStatus fluidStatus, FluidStatus fluidStatus2) {
            double r;
            BlockState blockState = fluidStatus.at(j);
            BlockState blockState2 = fluidStatus2.at(j);
            if (blockState.is(Blocks.LAVA) && blockState2.is(Blocks.WATER) || blockState.is(Blocks.WATER) && blockState2.is(Blocks.LAVA)) {
                return 1.0;
            }
            int l = Math.abs(fluidStatus.fluidLevel - fluidStatus2.fluidLevel);
            if (l == 0) {
                return 0.0;
            }
            double d = 0.5 * (double)(fluidStatus.fluidLevel + fluidStatus2.fluidLevel);
            double e = (double)j + 0.5 - d;
            double f = (double)l / 2.0;
            double g = 0.0;
            double h = 2.5;
            double m = 1.5;
            double n = 3.0;
            double o = 10.0;
            double p = 3.0;
            double q = f - Math.abs(e);
            double s = e > 0.0 ? ((r = 0.0 + q) > 0.0 ? r / 1.5 : r / 2.5) : ((r = 3.0 + q) > 0.0 ? r / 3.0 : r / 10.0);
            if (s < -2.0 || s > 2.0) {
                return s;
            }
            r = mutableDouble.getValue();
            if (Double.isNaN(r)) {
                double t = 0.5;
                double u = this.barrierNoise.getValue(i, (double)j * 0.5, k);
                mutableDouble.setValue(u);
                return u + s;
            }
            return r + s;
        }

        private int gridX(int i) {
            return Math.floorDiv(i, 16);
        }

        private int gridY(int i) {
            return Math.floorDiv(i, 12);
        }

        private int gridZ(int i) {
            return Math.floorDiv(i, 16);
        }

        private FluidStatus getAquiferStatus(long l) {
            FluidStatus fluidStatus2;
            int o;
            int n;
            int i = BlockPos.getX(l);
            int j = BlockPos.getY(l);
            int k = BlockPos.getZ(l);
            int m = this.gridX(i);
            int p = this.getIndex(m, n = this.gridY(j), o = this.gridZ(k));
            FluidStatus fluidStatus = this.aquiferCache[p];
            if (fluidStatus != null) {
                return fluidStatus;
            }
            this.aquiferCache[p] = fluidStatus2 = this.computeFluid(i, j, k);
            return fluidStatus2;
        }

        @Override
        public FluidStatus computeFluid(int i, int j, int k) {
            double g;
            FluidStatus fluidStatus = this.globalFluidPicker.computeFluid(i, j, k);
            int l = Integer.MAX_VALUE;
            int m = j + 12;
            int n = j - 12;
            boolean bl = false;
            for (int[] is : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
                FluidStatus fluidStatus2;
                boolean bl3;
                boolean bl2;
                int o = i + SectionPos.sectionToBlockCoord(is[0]);
                int p = k + SectionPos.sectionToBlockCoord(is[1]);
                int q = this.noiseChunk.preliminarySurfaceLevel(o, p);
                int r = q + 8;
                boolean bl4 = bl2 = is[0] == 0 && is[1] == 0;
                if (bl2 && n > r) {
                    return fluidStatus;
                }
                boolean bl5 = bl3 = m > r;
                if ((bl3 || bl2) && !(fluidStatus2 = this.globalFluidPicker.computeFluid(o, r, p)).at(r).isAir()) {
                    if (bl2) {
                        bl = true;
                    }
                    if (bl3) {
                        return fluidStatus2;
                    }
                }
                l = Math.min(l, q);
            }
            int s = l + 8 - j;
            int t = 64;
            double d = bl ? Mth.clampedMap((double)s, 0.0, 64.0, 1.0, 0.0) : 0.0;
            double e = 0.67;
            double f = Mth.clamp(this.fluidLevelFloodednessNoise.getValue(i, (double)j * 0.67, k), -1.0, 1.0);
            if (f > (g = Mth.map(d, 1.0, 0.0, -0.3, 0.8))) {
                return fluidStatus;
            }
            double h = Mth.map(d, 1.0, 0.0, -0.8, 0.4);
            if (f <= h) {
                return new FluidStatus(DimensionType.WAY_BELOW_MIN_Y, fluidStatus.fluidType);
            }
            int u = 16;
            int v = 40;
            int w = Math.floorDiv(i, 16);
            int x = Math.floorDiv(j, 40);
            int y = Math.floorDiv(k, 16);
            int z = x * 40 + 20;
            int aa = 10;
            double ab = this.fluidLevelSpreadNoise.getValue(w, (double)x / 1.4, y) * 10.0;
            int ac = Mth.quantize(ab, 3);
            int ad = z + ac;
            int ae = Math.min(l, ad);
            BlockState blockState = this.getFluidType(i, j, k, fluidStatus, ad);
            return new FluidStatus(ae, blockState);
        }

        private BlockState getFluidType(int i, int j, int k, FluidStatus fluidStatus, int l) {
            if (l <= -10) {
                int q;
                int p;
                int m = 64;
                int n = 40;
                int o = Math.floorDiv(i, 64);
                double d = this.lavaNoise.getValue(o, p = Math.floorDiv(j, 40), q = Math.floorDiv(k, 64));
                if (Math.abs(d) > 0.3) {
                    return Blocks.LAVA.defaultBlockState();
                }
            }
            return fluidStatus.fluidType;
        }
    }

    public static interface FluidPicker {
        public FluidStatus computeFluid(int var1, int var2, int var3);
    }

    public static final class FluidStatus {
        final int fluidLevel;
        final BlockState fluidType;

        public FluidStatus(int i, BlockState blockState) {
            this.fluidLevel = i;
            this.fluidType = blockState;
        }

        public BlockState at(int i) {
            return i < this.fluidLevel ? this.fluidType : Blocks.AIR.defaultBlockState();
        }
    }
}

