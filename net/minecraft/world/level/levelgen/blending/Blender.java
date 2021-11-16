/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.TerrainInfo;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.ScheduledTick;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class Blender {
    private static final Blender EMPTY = new Blender(null, List.of(), List.of()){

        @Override
        public TerrainInfo blendOffsetAndFactor(int i, int j, TerrainInfo terrainInfo) {
            return terrainInfo;
        }

        @Override
        public double blendDensity(int i, int j, int k, double d) {
            return d;
        }

        @Override
        public BiomeResolver getBiomeResolver(BiomeResolver biomeResolver) {
            return biomeResolver;
        }
    };
    private static final NormalNoise SHIFT_NOISE = NormalNoise.create(new XoroshiroRandomSource(42L), BuiltinRegistries.NOISE.getOrThrow(Noises.SHIFT));
    private static final int HEIGHT_BLENDING_RANGE_CELLS = QuartPos.fromSection(7) - 1;
    private static final int HEIGHT_BLENDING_RANGE_CHUNKS = QuartPos.toSection(HEIGHT_BLENDING_RANGE_CELLS + 3);
    private static final int DENSITY_BLENDING_RANGE_CELLS = 2;
    private static final int DENSITY_BLENDING_RANGE_CHUNKS = QuartPos.toSection(5);
    private static final double BLENDING_FACTOR = 10.0;
    private static final double BLENDING_JAGGEDNESS = 0.0;
    private final WorldGenRegion region;
    private final List<PositionedBlendingData> heightData;
    private final List<PositionedBlendingData> densityData;

    public static Blender empty() {
        return EMPTY;
    }

    public static Blender of(@Nullable WorldGenRegion worldGenRegion) {
        if (worldGenRegion == null) {
            return EMPTY;
        }
        ArrayList<PositionedBlendingData> list = Lists.newArrayList();
        ArrayList<PositionedBlendingData> list2 = Lists.newArrayList();
        ChunkPos chunkPos = worldGenRegion.getCenter();
        for (int i = -HEIGHT_BLENDING_RANGE_CHUNKS; i <= HEIGHT_BLENDING_RANGE_CHUNKS; ++i) {
            for (int j = -HEIGHT_BLENDING_RANGE_CHUNKS; j <= HEIGHT_BLENDING_RANGE_CHUNKS; ++j) {
                int k = chunkPos.x + i;
                int l = chunkPos.z + j;
                BlendingData blendingData = BlendingData.getOrUpdateBlendingData(worldGenRegion, k, l);
                if (blendingData == null) continue;
                PositionedBlendingData positionedBlendingData = new PositionedBlendingData(k, l, blendingData);
                list.add(positionedBlendingData);
                if (i < -DENSITY_BLENDING_RANGE_CHUNKS || i > DENSITY_BLENDING_RANGE_CHUNKS || j < -DENSITY_BLENDING_RANGE_CHUNKS || j > DENSITY_BLENDING_RANGE_CHUNKS) continue;
                list2.add(positionedBlendingData);
            }
        }
        if (list.isEmpty() && list2.isEmpty()) {
            return EMPTY;
        }
        return new Blender(worldGenRegion, list, list2);
    }

    Blender(WorldGenRegion worldGenRegion, List<PositionedBlendingData> list, List<PositionedBlendingData> list2) {
        this.region = worldGenRegion;
        this.heightData = list;
        this.densityData = list2;
    }

    public TerrainInfo blendOffsetAndFactor(int i, int j, TerrainInfo terrainInfo) {
        int l2;
        int k2 = QuartPos.fromBlock(i);
        double d2 = this.getBlendingDataValue(k2, 0, l2 = QuartPos.fromBlock(j), BlendingData::getHeight);
        if (d2 != Double.MAX_VALUE) {
            return new TerrainInfo(Blender.heightToOffset(d2), 10.0, 0.0);
        }
        MutableDouble mutableDouble = new MutableDouble(0.0);
        MutableDouble mutableDouble2 = new MutableDouble(0.0);
        MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);
        for (PositionedBlendingData positionedBlendingData : this.heightData) {
            positionedBlendingData.blendingData.iterateHeights(QuartPos.fromSection(positionedBlendingData.chunkX), QuartPos.fromSection(positionedBlendingData.chunkZ), (k, l, d) -> {
                double e = Mth.length(k2 - k, l2 - l);
                if (e > (double)HEIGHT_BLENDING_RANGE_CELLS) {
                    return;
                }
                if (e < mutableDouble3.doubleValue()) {
                    mutableDouble3.setValue(e);
                }
                double f = 1.0 / (e * e * e * e);
                mutableDouble2.add(d * f);
                mutableDouble.add(f);
            });
        }
        if (mutableDouble3.doubleValue() == Double.POSITIVE_INFINITY) {
            return terrainInfo;
        }
        double e = mutableDouble2.doubleValue() / mutableDouble.doubleValue();
        double f = Mth.clamp(mutableDouble3.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
        f = 3.0 * f * f - 2.0 * f * f * f;
        double g = Mth.lerp(f, Blender.heightToOffset(e), terrainInfo.offset());
        double h = Mth.lerp(f, 10.0, terrainInfo.factor());
        double m = Mth.lerp(f, 0.0, terrainInfo.jaggedness());
        return new TerrainInfo(g, h, m);
    }

    private static double heightToOffset(double d) {
        double e = 1.0;
        double f = d + 0.5;
        double g = Mth.positiveModulo(f, 8.0);
        return 1.0 * (32.0 * (f - 128.0) - 3.0 * (f - 120.0) * g + 3.0 * g * g) / (128.0 * (32.0 - 3.0 * g));
    }

    public double blendDensity(int i, int j, int k, double d2) {
        int n2;
        int m2;
        int l2 = QuartPos.fromBlock(i);
        double e = this.getBlendingDataValue(l2, m2 = j / 8, n2 = QuartPos.fromBlock(k), BlendingData::getDensity);
        if (e != Double.MAX_VALUE) {
            return e;
        }
        MutableDouble mutableDouble = new MutableDouble(0.0);
        MutableDouble mutableDouble2 = new MutableDouble(0.0);
        MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);
        for (PositionedBlendingData positionedBlendingData : this.densityData) {
            positionedBlendingData.blendingData.iterateDensities(QuartPos.fromSection(positionedBlendingData.chunkX), QuartPos.fromSection(positionedBlendingData.chunkZ), m2 - 2, m2 + 2, (l, m, n, d) -> {
                double e = Mth.length(l2 - l, m2 - m, n2 - n);
                if (e > 2.0) {
                    return;
                }
                if (e < mutableDouble3.doubleValue()) {
                    mutableDouble3.setValue(e);
                }
                double f = 1.0 / (e * e * e * e);
                mutableDouble2.add(d * f);
                mutableDouble.add(f);
            });
        }
        if (mutableDouble3.doubleValue() == Double.POSITIVE_INFINITY) {
            return d2;
        }
        double f = mutableDouble2.doubleValue() / mutableDouble.doubleValue();
        double g = Mth.clamp(mutableDouble3.doubleValue() / 3.0, 0.0, 1.0);
        return Mth.lerp(g, f, d2);
    }

    private double getBlendingDataValue(int i, int j, int k, CellValueGetter cellValueGetter) {
        int l = QuartPos.toSection(i);
        int m = QuartPos.toSection(k);
        boolean bl = (i & 3) == 0;
        boolean bl2 = (k & 3) == 0;
        double d = this.getBlendingDataValue(cellValueGetter, l, m, i, j, k);
        if (d == Double.MAX_VALUE) {
            if (bl && bl2) {
                d = this.getBlendingDataValue(cellValueGetter, l - 1, m - 1, i, j, k);
            }
            if (d == Double.MAX_VALUE) {
                if (bl) {
                    d = this.getBlendingDataValue(cellValueGetter, l - 1, m, i, j, k);
                }
                if (d == Double.MAX_VALUE && bl2) {
                    d = this.getBlendingDataValue(cellValueGetter, l, m - 1, i, j, k);
                }
            }
        }
        return d;
    }

    private double getBlendingDataValue(CellValueGetter cellValueGetter, int i, int j, int k, int l, int m) {
        BlendingData blendingData = BlendingData.getOrUpdateBlendingData(this.region, i, j);
        if (blendingData != null) {
            return cellValueGetter.get(blendingData, k - QuartPos.fromSection(i), l, m - QuartPos.fromSection(j));
        }
        return Double.MAX_VALUE;
    }

    public BiomeResolver getBiomeResolver(BiomeResolver biomeResolver) {
        return (i, j, k, sampler) -> {
            Biome biome = this.blendBiome(i, j, k);
            if (biome == null) {
                return biomeResolver.getNoiseBiome(i, j, k, sampler);
            }
            return biome;
        };
    }

    @Nullable
    private Biome blendBiome(int i2, int j2, int k) {
        double d = (double)i2 + SHIFT_NOISE.getValue(i2, 0.0, k) * 12.0;
        double e = (double)k + SHIFT_NOISE.getValue(k, i2, 0.0) * 12.0;
        MutableDouble mutableDouble = new MutableDouble(Double.POSITIVE_INFINITY);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        MutableObject mutableObject = new MutableObject();
        for (PositionedBlendingData positionedBlendingData : this.heightData) {
            positionedBlendingData.blendingData.iterateHeights(QuartPos.fromSection(positionedBlendingData.chunkX), QuartPos.fromSection(positionedBlendingData.chunkZ), (i, j, f) -> {
                double g = Mth.length(d - (double)i, e - (double)j);
                if (g > (double)HEIGHT_BLENDING_RANGE_CELLS) {
                    return;
                }
                if (g < mutableDouble.doubleValue()) {
                    mutableObject.setValue(new ChunkPos(positionedBlendingData.chunkX, positionedBlendingData.chunkZ));
                    mutableBlockPos.set(i, QuartPos.fromBlock(Mth.floor(f)), j);
                    mutableDouble.setValue(g);
                }
            });
        }
        if (mutableDouble.doubleValue() == Double.POSITIVE_INFINITY) {
            return null;
        }
        double f2 = Mth.clamp(mutableDouble.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
        if (f2 > 0.5) {
            return null;
        }
        ChunkAccess chunkAccess = this.region.getChunk(((ChunkPos)mutableObject.getValue()).x, ((ChunkPos)mutableObject.getValue()).z);
        return chunkAccess.getNoiseBiome(Math.min(mutableBlockPos.getX() & 3, 3), mutableBlockPos.getY(), Math.min(mutableBlockPos.getZ() & 3, 3));
    }

    public static void generateBorderTicks(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
        ChunkPos chunkPos = chunkAccess.getPos();
        boolean bl = chunkAccess.isOldNoiseGeneration();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
        int i = BlendingData.AREA_WITH_OLD_GENERATION.getMinBuildHeight();
        int j = BlendingData.AREA_WITH_OLD_GENERATION.getMaxBuildHeight() - 1;
        if (bl) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    Blender.generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, k, i - 1, l));
                    Blender.generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, k, i, l));
                    Blender.generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, k, j, l));
                    Blender.generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, k, j + 1, l));
                }
            }
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (worldGenRegion.getChunk(chunkPos.x + direction.getStepX(), chunkPos.z + direction.getStepZ()).isOldNoiseGeneration() == bl) continue;
            int m = direction == Direction.EAST ? 15 : 0;
            int n = direction == Direction.WEST ? 0 : 15;
            int o = direction == Direction.SOUTH ? 15 : 0;
            int p = direction == Direction.NORTH ? 0 : 15;
            for (int q = m; q <= n; ++q) {
                for (int r = o; r <= p; ++r) {
                    int s = Math.min(j, chunkAccess.getHeight(Heightmap.Types.MOTION_BLOCKING, q, r)) + 1;
                    for (int t = i; t < s; ++t) {
                        Blender.generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, q, t, r));
                    }
                }
            }
        }
    }

    private static void generateBorderTick(ChunkAccess chunkAccess, BlockPos blockPos) {
        FluidState fluidState;
        BlockState blockState = chunkAccess.getBlockState(blockPos);
        if (blockState.is(BlockTags.LEAVES)) {
            chunkAccess.getBlockTicks().schedule(ScheduledTick.worldgen(blockState.getBlock(), blockPos, 0L));
        }
        if (!(fluidState = chunkAccess.getFluidState(blockPos)).isEmpty()) {
            chunkAccess.getFluidTicks().schedule(ScheduledTick.worldgen(fluidState.getType(), blockPos, 0L));
        }
    }

    record PositionedBlendingData(int chunkX, int chunkZ, BlendingData blendingData) {
    }

    static interface CellValueGetter {
        public double get(BlendingData var1, int var2, int var3, int var4);
    }
}

