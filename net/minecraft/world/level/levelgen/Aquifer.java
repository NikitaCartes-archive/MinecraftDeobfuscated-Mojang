/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.SimpleRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;

public interface Aquifer {
    public static Aquifer create(NoiseChunk noiseChunk, ChunkPos chunkPos, NormalNoise normalNoise, NormalNoise normalNoise2, NormalNoise normalNoise3, PositionalRandomFactory positionalRandomFactory, NoiseSampler noiseSampler, int i, int j, FluidPicker fluidPicker) {
        return new NoiseBasedAquifer(noiseChunk, chunkPos, normalNoise, normalNoise2, normalNoise3, positionalRandomFactory, noiseSampler, i, j, fluidPicker);
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
        private final NoiseChunk noiseChunk;
        private final NormalNoise barrierNoise;
        private final NormalNoise waterLevelNoise;
        private final NormalNoise lavaNoise;
        private final PositionalRandomFactory positionalRandomFactory;
        private final FluidStatus[] aquiferCache;
        private final long[] aquiferLocationCache;
        private final FluidPicker globalFluidPicker;
        private boolean shouldScheduleFluidUpdate;
        private final NoiseSampler sampler;
        private final int minGridX;
        private final int minGridY;
        private final int minGridZ;
        private final int gridSizeX;
        private final int gridSizeZ;
        private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {3, 0}, {-3, 0}, {0, 3}, {0, -3}, {2, 2}, {2, -2}, {-2, 2}, {-2, 2}};

        NoiseBasedAquifer(NoiseChunk noiseChunk, ChunkPos chunkPos, NormalNoise normalNoise, NormalNoise normalNoise2, NormalNoise normalNoise3, PositionalRandomFactory positionalRandomFactory, NoiseSampler noiseSampler, int i, int j, FluidPicker fluidPicker) {
            this.noiseChunk = noiseChunk;
            this.barrierNoise = normalNoise;
            this.waterLevelNoise = normalNoise2;
            this.lavaNoise = normalNoise3;
            this.positionalRandomFactory = positionalRandomFactory;
            this.sampler = noiseSampler;
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
                                    SimpleRandomSource randomSource = this.positionalRandomFactory.at(x, y, z);
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
                    double g = this.similarity(o, p);
                    double h = this.similarity(o, q);
                    double ah = this.similarity(p, q);
                    boolean bl2 = bl = g > 0.0;
                    if (fluidStatus2.at(j).is(Blocks.WATER) && this.globalFluidPicker.computeFluid(i, j - 1, k).at(j - 1).is(Blocks.LAVA)) {
                        f = 1.0;
                    } else if (g > -1.0) {
                        double ai = 1.0 + (this.barrierNoise.getValue(i, j, k) + 0.05) / 4.0;
                        double aj = this.calculatePressure(j, ai, fluidStatus2, fluidStatus3);
                        double ak = this.calculatePressure(j, ai, fluidStatus2, fluidStatus4);
                        double al = this.calculatePressure(j, ai, fluidStatus3, fluidStatus4);
                        double am = Math.max(0.0, g);
                        double an = Math.max(0.0, h);
                        double ao = Math.max(0.0, ah);
                        double ap = 2.0 * am * Math.max(aj, Math.max(ak * an, al * ao));
                        float aq = 0.5f;
                        f = j <= fluidStatus2.fluidLevel && j <= fluidStatus3.fluidLevel && fluidStatus2.fluidLevel != fluidStatus3.fluidLevel && Math.abs(this.barrierNoise.getValue((float)i * 0.5f, (float)j * 0.5f, (float)k * 0.5f)) < 0.3 ? 1.0 : Math.max(0.0, ap);
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

        private double similarity(int i, int j) {
            double d = 25.0;
            return 1.0 - (double)Math.abs(j - i) / 25.0;
        }

        private double calculatePressure(int i, double d, FluidStatus fluidStatus, FluidStatus fluidStatus2) {
            BlockState blockState = fluidStatus.at(i);
            BlockState blockState2 = fluidStatus2.at(i);
            if (blockState.is(Blocks.LAVA) && blockState2.is(Blocks.WATER) || blockState.is(Blocks.WATER) && blockState2.is(Blocks.LAVA)) {
                return 1.0;
            }
            int j = Math.abs(fluidStatus.fluidLevel - fluidStatus2.fluidLevel);
            double e = 0.5 * (double)(fluidStatus.fluidLevel + fluidStatus2.fluidLevel);
            double f = Math.abs(e - (double)i - 0.5);
            return 0.5 * (double)j * d - f;
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
            int p;
            int o;
            int n;
            FluidStatus fluidStatus = this.globalFluidPicker.computeFluid(i, j, k);
            int l = Integer.MAX_VALUE;
            int m = j + 6;
            boolean bl = false;
            for (int[] is : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
                FluidStatus fluidStatus2;
                boolean bl3;
                n = i + SectionPos.sectionToBlockCoord(is[0]);
                o = k + SectionPos.sectionToBlockCoord(is[1]);
                p = this.sampler.getPreliminarySurfaceLevel(n, o, this.noiseChunk.terrainInfoWide(this.sampler, QuartPos.fromBlock(n), QuartPos.fromBlock(o)));
                int q = p + 8;
                boolean bl2 = is[0] == 0 && is[1] == 0;
                boolean bl4 = bl3 = m > q;
                if ((bl3 || bl2) && !(fluidStatus2 = this.globalFluidPicker.computeFluid(n, q, o)).at(q).isAir()) {
                    if (bl2) {
                        bl = true;
                    }
                    if (bl3) {
                        return fluidStatus2;
                    }
                }
                l = Math.min(l, p);
            }
            int r = j - 6;
            if (r > l) {
                return fluidStatus;
            }
            int s = 40;
            int t = Math.floorDiv(i, 64);
            int u = Math.floorDiv(j, 40);
            n = Math.floorDiv(k, 64);
            o = -20;
            p = 50;
            double d = this.waterLevelNoise.getValue(t, (double)u / 1.4, n) * 50.0 + -20.0;
            int v = u * 40 + 20;
            if (bl && v >= l - 30 && v < fluidStatus.fluidLevel) {
                if (d > -12.0) {
                    return fluidStatus;
                }
                if (d > -20.0) {
                    return new FluidStatus(l - 12 + (int)d, Blocks.WATER.defaultBlockState());
                }
                d = -40.0;
            } else {
                if (d > 4.0) {
                    d *= 4.0;
                }
                if (d < -10.0) {
                    d = -40.0;
                }
            }
            int w = v + Mth.floor(d);
            int x = Math.min(l, w);
            boolean bl4 = false;
            if (v == -20 && !bl) {
                double e = this.lavaNoise.getValue(t, (double)u / 1.4, n);
                bl4 = Math.abs(e) > (double)0.22f;
            }
            return new FluidStatus(x, bl4 ? Blocks.LAVA.defaultBlockState() : fluidStatus.fluidType);
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

