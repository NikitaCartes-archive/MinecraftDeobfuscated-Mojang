/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class Blender {
    private static final Blender EMPTY = new Blender(new Long2ObjectOpenHashMap(), new Long2ObjectOpenHashMap()){

        @Override
        public BlendingOutput blendOffsetAndFactor(int i, int j) {
            return new BlendingOutput(1.0, 0.0);
        }

        @Override
        public double blendDensity(DensityFunction.FunctionContext functionContext, double d) {
            return d;
        }

        @Override
        public BiomeResolver getBiomeResolver(BiomeResolver biomeResolver) {
            return biomeResolver;
        }
    };
    private static final NormalNoise SHIFT_NOISE = NormalNoise.create(new XoroshiroRandomSource(42L), NoiseData.DEFAULT_SHIFT);
    private static final int HEIGHT_BLENDING_RANGE_CELLS = QuartPos.fromSection(7) - 1;
    private static final int HEIGHT_BLENDING_RANGE_CHUNKS = QuartPos.toSection(HEIGHT_BLENDING_RANGE_CELLS + 3);
    private static final int DENSITY_BLENDING_RANGE_CELLS = 2;
    private static final int DENSITY_BLENDING_RANGE_CHUNKS = QuartPos.toSection(5);
    private static final double OLD_CHUNK_XZ_RADIUS = 8.0;
    private final Long2ObjectOpenHashMap<BlendingData> heightAndBiomeBlendingData;
    private final Long2ObjectOpenHashMap<BlendingData> densityBlendingData;

    public static Blender empty() {
        return EMPTY;
    }

    public static Blender of(@Nullable WorldGenRegion worldGenRegion) {
        if (worldGenRegion == null) {
            return EMPTY;
        }
        ChunkPos chunkPos = worldGenRegion.getCenter();
        if (!worldGenRegion.isOldChunkAround(chunkPos, HEIGHT_BLENDING_RANGE_CHUNKS)) {
            return EMPTY;
        }
        Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap = new Long2ObjectOpenHashMap<BlendingData>();
        Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap2 = new Long2ObjectOpenHashMap<BlendingData>();
        int i = Mth.square(HEIGHT_BLENDING_RANGE_CHUNKS + 1);
        for (int j = -HEIGHT_BLENDING_RANGE_CHUNKS; j <= HEIGHT_BLENDING_RANGE_CHUNKS; ++j) {
            for (int k = -HEIGHT_BLENDING_RANGE_CHUNKS; k <= HEIGHT_BLENDING_RANGE_CHUNKS; ++k) {
                int m;
                int l;
                BlendingData blendingData;
                if (j * j + k * k > i || (blendingData = BlendingData.getOrUpdateBlendingData(worldGenRegion, l = chunkPos.x + j, m = chunkPos.z + k)) == null) continue;
                long2ObjectOpenHashMap.put(ChunkPos.asLong(l, m), blendingData);
                if (j < -DENSITY_BLENDING_RANGE_CHUNKS || j > DENSITY_BLENDING_RANGE_CHUNKS || k < -DENSITY_BLENDING_RANGE_CHUNKS || k > DENSITY_BLENDING_RANGE_CHUNKS) continue;
                long2ObjectOpenHashMap2.put(ChunkPos.asLong(l, m), blendingData);
            }
        }
        if (long2ObjectOpenHashMap.isEmpty() && long2ObjectOpenHashMap2.isEmpty()) {
            return EMPTY;
        }
        return new Blender(long2ObjectOpenHashMap, long2ObjectOpenHashMap2);
    }

    Blender(Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap, Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap2) {
        this.heightAndBiomeBlendingData = long2ObjectOpenHashMap;
        this.densityBlendingData = long2ObjectOpenHashMap2;
    }

    public BlendingOutput blendOffsetAndFactor(int i, int j) {
        int l;
        int k = QuartPos.fromBlock(i);
        double d = this.getBlendingDataValue(k, 0, l = QuartPos.fromBlock(j), BlendingData::getHeight);
        if (d != Double.MAX_VALUE) {
            return new BlendingOutput(0.0, Blender.heightToOffset(d));
        }
        MutableDouble mutableDouble = new MutableDouble(0.0);
        MutableDouble mutableDouble2 = new MutableDouble(0.0);
        MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);
        this.heightAndBiomeBlendingData.forEach((long_, blendingData) -> blendingData.iterateHeights(QuartPos.fromSection(ChunkPos.getX(long_)), QuartPos.fromSection(ChunkPos.getZ(long_)), (k, l, d) -> {
            double e = Mth.length(k - k, l - l);
            if (e > (double)HEIGHT_BLENDING_RANGE_CELLS) {
                return;
            }
            if (e < mutableDouble3.doubleValue()) {
                mutableDouble3.setValue(e);
            }
            double f = 1.0 / (e * e * e * e);
            mutableDouble2.add(d * f);
            mutableDouble.add(f);
        }));
        if (mutableDouble3.doubleValue() == Double.POSITIVE_INFINITY) {
            return new BlendingOutput(1.0, 0.0);
        }
        double e = mutableDouble2.doubleValue() / mutableDouble.doubleValue();
        double f = Mth.clamp(mutableDouble3.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
        f = 3.0 * f * f - 2.0 * f * f * f;
        return new BlendingOutput(f, Blender.heightToOffset(e));
    }

    private static double heightToOffset(double d) {
        double e = 1.0;
        double f = d + 0.5;
        double g = Mth.positiveModulo(f, 8.0);
        return 1.0 * (32.0 * (f - 128.0) - 3.0 * (f - 120.0) * g + 3.0 * g * g) / (128.0 * (32.0 - 3.0 * g));
    }

    public double blendDensity(DensityFunction.FunctionContext functionContext, double d) {
        int k;
        int j;
        int i = QuartPos.fromBlock(functionContext.blockX());
        double e = this.getBlendingDataValue(i, j = functionContext.blockY() / 8, k = QuartPos.fromBlock(functionContext.blockZ()), BlendingData::getDensity);
        if (e != Double.MAX_VALUE) {
            return e;
        }
        MutableDouble mutableDouble = new MutableDouble(0.0);
        MutableDouble mutableDouble2 = new MutableDouble(0.0);
        MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);
        this.densityBlendingData.forEach((long_, blendingData) -> blendingData.iterateDensities(QuartPos.fromSection(ChunkPos.getX(long_)), QuartPos.fromSection(ChunkPos.getZ(long_)), j - 1, j + 1, (l, m, n, d) -> {
            double e = Mth.length(i - l, (j - m) * 2, k - n);
            if (e > 2.0) {
                return;
            }
            if (e < mutableDouble3.doubleValue()) {
                mutableDouble3.setValue(e);
            }
            double f = 1.0 / (e * e * e * e);
            mutableDouble2.add(d * f);
            mutableDouble.add(f);
        }));
        if (mutableDouble3.doubleValue() == Double.POSITIVE_INFINITY) {
            return d;
        }
        double f = mutableDouble2.doubleValue() / mutableDouble.doubleValue();
        double g = Mth.clamp(mutableDouble3.doubleValue() / 3.0, 0.0, 1.0);
        return Mth.lerp(g, f, d);
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
        BlendingData blendingData = this.heightAndBiomeBlendingData.get(ChunkPos.asLong(i, j));
        if (blendingData != null) {
            return cellValueGetter.get(blendingData, k - QuartPos.fromSection(i), l, m - QuartPos.fromSection(j));
        }
        return Double.MAX_VALUE;
    }

    public BiomeResolver getBiomeResolver(BiomeResolver biomeResolver) {
        return (i, j, k, sampler) -> {
            Holder<Biome> holder = this.blendBiome(i, j, k);
            if (holder == null) {
                return biomeResolver.getNoiseBiome(i, j, k, sampler);
            }
            return holder;
        };
    }

    @Nullable
    private Holder<Biome> blendBiome(int i, int j, int k) {
        MutableDouble mutableDouble = new MutableDouble(Double.POSITIVE_INFINITY);
        MutableObject mutableObject = new MutableObject();
        this.heightAndBiomeBlendingData.forEach((long_, blendingData) -> blendingData.iterateBiomes(QuartPos.fromSection(ChunkPos.getX(long_)), j, QuartPos.fromSection(ChunkPos.getZ(long_)), (k, l, holder) -> {
            double d = Mth.length(i - k, k - l);
            if (d > (double)HEIGHT_BLENDING_RANGE_CELLS) {
                return;
            }
            if (d < mutableDouble.doubleValue()) {
                mutableObject.setValue(holder);
                mutableDouble.setValue(d);
            }
        }));
        if (mutableDouble.doubleValue() == Double.POSITIVE_INFINITY) {
            return null;
        }
        double d = SHIFT_NOISE.getValue(i, 0.0, k) * 12.0;
        double e = Mth.clamp((mutableDouble.doubleValue() + d) / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
        if (e > 0.5) {
            return null;
        }
        return (Holder)mutableObject.getValue();
    }

    public static void generateBorderTicks(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
        ChunkPos chunkPos = chunkAccess.getPos();
        boolean bl = chunkAccess.isOldNoiseGeneration();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
        BlendingData blendingData = chunkAccess.getBlendingData();
        if (blendingData == null) {
            return;
        }
        int i = blendingData.getAreaWithOldGeneration().getMinBuildHeight();
        int j = blendingData.getAreaWithOldGeneration().getMaxBuildHeight() - 1;
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
            chunkAccess.markPosForPostprocessing(blockPos);
        }
        if (!(fluidState = chunkAccess.getFluidState(blockPos)).isEmpty()) {
            chunkAccess.markPosForPostprocessing(blockPos);
        }
    }

    public static void addAroundOldChunksCarvingMaskFilter(WorldGenLevel worldGenLevel, ProtoChunk protoChunk) {
        ChunkPos chunkPos = protoChunk.getPos();
        ImmutableMap.Builder<Direction8, BlendingData> builder = ImmutableMap.builder();
        for (Direction8 direction8 : Direction8.values()) {
            int j2;
            int i2 = chunkPos.x + direction8.getStepX();
            BlendingData blendingData = worldGenLevel.getChunk(i2, j2 = chunkPos.z + direction8.getStepZ()).getBlendingData();
            if (blendingData == null) continue;
            builder.put(direction8, blendingData);
        }
        ImmutableMap<Direction8, BlendingData> immutableMap = builder.build();
        if (!protoChunk.isOldNoiseGeneration() && immutableMap.isEmpty()) {
            return;
        }
        DistanceGetter distanceGetter = Blender.makeOldChunkDistanceGetter(protoChunk.getBlendingData(), immutableMap);
        CarvingMask.Mask mask = (i, j, k) -> {
            double f;
            double e;
            double d = (double)i + 0.5 + SHIFT_NOISE.getValue(i, j, k) * 4.0;
            return distanceGetter.getDistance(d, e = (double)j + 0.5 + SHIFT_NOISE.getValue(j, k, i) * 4.0, f = (double)k + 0.5 + SHIFT_NOISE.getValue(k, i, j) * 4.0) < 4.0;
        };
        Stream.of(GenerationStep.Carving.values()).map(protoChunk::getOrCreateCarvingMask).forEach(carvingMask -> carvingMask.setAdditionalMask(mask));
    }

    public static DistanceGetter makeOldChunkDistanceGetter(@Nullable BlendingData blendingData2, Map<Direction8, BlendingData> map) {
        ArrayList<DistanceGetter> list = Lists.newArrayList();
        if (blendingData2 != null) {
            list.add(Blender.makeOffsetOldChunkDistanceGetter(null, blendingData2));
        }
        map.forEach((direction8, blendingData) -> list.add(Blender.makeOffsetOldChunkDistanceGetter(direction8, blendingData)));
        return (d, e, f) -> {
            double g = Double.POSITIVE_INFINITY;
            for (DistanceGetter distanceGetter : list) {
                double h = distanceGetter.getDistance(d, e, f);
                if (!(h < g)) continue;
                g = h;
            }
            return g;
        };
    }

    private static DistanceGetter makeOffsetOldChunkDistanceGetter(@Nullable Direction8 direction8, BlendingData blendingData) {
        double d = 0.0;
        double e = 0.0;
        if (direction8 != null) {
            for (Direction direction : direction8.getDirections()) {
                d += (double)(direction.getStepX() * 16);
                e += (double)(direction.getStepZ() * 16);
            }
        }
        double f = d;
        double g = e;
        double h2 = (double)blendingData.getAreaWithOldGeneration().getHeight() / 2.0;
        double i2 = (double)blendingData.getAreaWithOldGeneration().getMinBuildHeight() + h2;
        return (h, i, j) -> Blender.distanceToCube(h - 8.0 - f, i - i2, j - 8.0 - g, 8.0, h2, 8.0);
    }

    private static double distanceToCube(double d, double e, double f, double g, double h, double i) {
        double j = Math.abs(d) - g;
        double k = Math.abs(e) - h;
        double l = Math.abs(f) - i;
        return Mth.length(Math.max(0.0, j), Math.max(0.0, k), Math.max(0.0, l));
    }

    static interface CellValueGetter {
        public double get(BlendingData var1, int var2, int var3, int var4);
    }

    public record BlendingOutput(double alpha, double blendingOffset) {
    }

    public static interface DistanceGetter {
        public double getDistance(double var1, double var3, double var5);
    }
}

