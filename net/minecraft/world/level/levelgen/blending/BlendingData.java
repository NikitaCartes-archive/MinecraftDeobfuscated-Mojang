/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blending;

import com.google.common.primitives.Doubles;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

public class BlendingData {
    private static final double BLENDING_DENSITY_FACTOR = 0.1;
    protected static final LevelHeightAccessor AREA_WITH_OLD_GENERATION = new LevelHeightAccessor(){

        @Override
        public int getHeight() {
            return 256;
        }

        @Override
        public int getMinBuildHeight() {
            return 0;
        }
    };
    public static final int CELL_HEIGHT = 8;
    private static final int CELLS_PER_SECTION_Y = 2;
    private static final int QUARTS_PER_SECTION = QuartPos.fromBlock(16);
    private static final int CELL_HORIZONTAL_MAX_INDEX_INSIDE = QUARTS_PER_SECTION - 1;
    private static final int CELL_HORIZONTAL_MAX_INDEX_OUTSIDE = QUARTS_PER_SECTION;
    private static final int CELL_COLUMN_INSIDE_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX_INSIDE + 1;
    private static final int CELL_COLUMN_OUTSIDE_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX_OUTSIDE + 1;
    private static final int CELL_COLUMN_COUNT = CELL_COLUMN_INSIDE_COUNT + CELL_COLUMN_OUTSIDE_COUNT;
    private static final int CELL_HORIZONTAL_FLOOR_COUNT = QUARTS_PER_SECTION + 1;
    private static final List<Block> SURFACE_BLOCKS = List.of(Blocks.PODZOL, Blocks.GRAVEL, Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.COARSE_DIRT, Blocks.SAND, Blocks.RED_SAND, Blocks.MYCELIUM, Blocks.SNOW_BLOCK, Blocks.TERRACOTTA, Blocks.DIRT);
    protected static final double NO_VALUE = Double.MAX_VALUE;
    private final boolean oldNoise;
    private boolean hasCalculatedData;
    private final boolean hasSavedHeights;
    private final double[] heights;
    private final transient double[][] densities;
    private final transient double[] floorDensities;
    private static final Codec<double[]> DOUBLE_ARRAY_CODEC = Codec.DOUBLE.listOf().xmap(Doubles::toArray, Doubles::asList);
    public static final Codec<BlendingData> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.BOOL.fieldOf("old_noise")).forGetter(BlendingData::oldNoise), DOUBLE_ARRAY_CODEC.optionalFieldOf("heights").forGetter(blendingData -> DoubleStream.of(blendingData.heights).anyMatch(d -> d != Double.MAX_VALUE) ? Optional.of(blendingData.heights) : Optional.empty())).apply((Applicative<BlendingData, ?>)instance, BlendingData::new)).comapFlatMap(BlendingData::validateArraySize, Function.identity());

    private static DataResult<BlendingData> validateArraySize(BlendingData blendingData) {
        if (blendingData.heights.length != CELL_COLUMN_COUNT) {
            return DataResult.error("heights has to be of length " + CELL_COLUMN_COUNT);
        }
        return DataResult.success(blendingData);
    }

    private BlendingData(boolean bl, Optional<double[]> optional) {
        this.oldNoise = bl;
        this.heights = optional.orElse(Util.make(new double[CELL_COLUMN_COUNT], ds -> Arrays.fill(ds, Double.MAX_VALUE)));
        this.hasSavedHeights = optional.isPresent();
        this.densities = new double[CELL_COLUMN_COUNT][];
        this.floorDensities = new double[CELL_HORIZONTAL_FLOOR_COUNT * CELL_HORIZONTAL_FLOOR_COUNT];
    }

    public boolean oldNoise() {
        return this.oldNoise;
    }

    @Nullable
    public static BlendingData getOrUpdateBlendingData(WorldGenRegion worldGenRegion, int i, int j) {
        ChunkAccess chunkAccess = worldGenRegion.getChunk(i, j);
        BlendingData blendingData = chunkAccess.getBlendingData();
        if (blendingData == null || !blendingData.oldNoise()) {
            return null;
        }
        blendingData.calculateData(chunkAccess, BlendingData.sideByGenerationAge(worldGenRegion, i, j, false));
        return blendingData;
    }

    public static Set<Direction8> sideByGenerationAge(WorldGenLevel worldGenLevel, int i, int j, boolean bl) {
        EnumSet<Direction8> set = EnumSet.noneOf(Direction8.class);
        for (Direction8 direction8 : Direction8.values()) {
            int k = i;
            int l = j;
            for (Direction direction : direction8.getDirections()) {
                k += direction.getStepX();
                l += direction.getStepZ();
            }
            if (worldGenLevel.getChunk(k, l).isOldNoiseGeneration() != bl) continue;
            set.add(direction8);
        }
        return set;
    }

    private void calculateData(ChunkAccess chunkAccess, Set<Direction8> set) {
        int i;
        if (this.hasCalculatedData) {
            return;
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(0, AREA_WITH_OLD_GENERATION.getMinBuildHeight(), 0);
        for (i = 0; i < this.floorDensities.length; ++i) {
            mutableBlockPos.setX(Math.max(QuartPos.toBlock(this.getFloorX(i)), 15));
            mutableBlockPos.setZ(Math.max(QuartPos.toBlock(this.getFloorZ(i)), 15));
            this.floorDensities[i] = BlendingData.isGround(chunkAccess, mutableBlockPos) ? 1.0 : -1.0;
        }
        if (set.contains((Object)Direction8.NORTH) || set.contains((Object)Direction8.WEST) || set.contains((Object)Direction8.NORTH_WEST)) {
            this.addValuesForColumn(BlendingData.getInsideIndex(0, 0), chunkAccess, 0, 0);
        }
        if (set.contains((Object)Direction8.NORTH)) {
            for (i = 1; i < QUARTS_PER_SECTION; ++i) {
                this.addValuesForColumn(BlendingData.getInsideIndex(i, 0), chunkAccess, 4 * i, 0);
            }
        }
        if (set.contains((Object)Direction8.WEST)) {
            for (i = 1; i < QUARTS_PER_SECTION; ++i) {
                this.addValuesForColumn(BlendingData.getInsideIndex(0, i), chunkAccess, 0, 4 * i);
            }
        }
        if (set.contains((Object)Direction8.EAST)) {
            for (i = 1; i < QUARTS_PER_SECTION; ++i) {
                this.addValuesForColumn(BlendingData.getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, i), chunkAccess, 15, 4 * i);
            }
        }
        if (set.contains((Object)Direction8.SOUTH)) {
            for (i = 0; i < QUARTS_PER_SECTION; ++i) {
                this.addValuesForColumn(BlendingData.getOutsideIndex(i, CELL_HORIZONTAL_MAX_INDEX_OUTSIDE), chunkAccess, 4 * i, 15);
            }
        }
        if (set.contains((Object)Direction8.EAST) && set.contains((Object)Direction8.NORTH_EAST)) {
            this.addValuesForColumn(BlendingData.getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, 0), chunkAccess, 15, 0);
        }
        if (set.contains((Object)Direction8.EAST) && set.contains((Object)Direction8.SOUTH) && set.contains((Object)Direction8.SOUTH_EAST)) {
            this.addValuesForColumn(BlendingData.getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, CELL_HORIZONTAL_MAX_INDEX_OUTSIDE), chunkAccess, 15, 15);
        }
        this.hasCalculatedData = true;
    }

    private void addValuesForColumn(int i, ChunkAccess chunkAccess, int j, int k) {
        if (!this.hasSavedHeights) {
            this.heights[i] = BlendingData.getHeightAtXZ(chunkAccess, j, k);
        }
        this.densities[i] = BlendingData.getDensityColumn(chunkAccess, j, k);
    }

    private static int getHeightAtXZ(ChunkAccess chunkAccess, int i, int j) {
        int k = chunkAccess.hasPrimedHeightmap(Heightmap.Types.WORLD_SURFACE_WG) ? Math.min(chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, j), AREA_WITH_OLD_GENERATION.getMaxBuildHeight()) : AREA_WITH_OLD_GENERATION.getMaxBuildHeight();
        int l = AREA_WITH_OLD_GENERATION.getMinBuildHeight();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, k, j);
        while (mutableBlockPos.getY() > l) {
            mutableBlockPos.move(Direction.DOWN);
            if (!SURFACE_BLOCKS.contains(chunkAccess.getBlockState(mutableBlockPos).getBlock())) continue;
            return mutableBlockPos.getY();
        }
        return l;
    }

    private static double[] getDensityColumn(ChunkAccess chunkAccess, int i, int j) {
        double[] ds = new double[BlendingData.cellCountPerColumn()];
        int k = BlendingData.getColumnMinY();
        double d = 30.0;
        double e = 0.0;
        double f = 0.0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        double g = 15.0;
        for (int l = AREA_WITH_OLD_GENERATION.getMaxBuildHeight() - 1; l >= AREA_WITH_OLD_GENERATION.getMinBuildHeight(); --l) {
            double h = BlendingData.isGround(chunkAccess, mutableBlockPos.set(i, l, j)) ? 1.0 : -1.0;
            int m = l % 8;
            if (m == 0) {
                double n = e / 15.0;
                int o = l / 8 + 1;
                ds[o - k] = n * d;
                e = f;
                f = 0.0;
                if (n > 0.0) {
                    d = 1.0;
                }
            } else {
                f += h;
            }
            e += h;
        }
        return ds;
    }

    private static boolean isGround(ChunkAccess chunkAccess, BlockPos blockPos) {
        BlockState blockState = chunkAccess.getBlockState(blockPos);
        if (blockState.isAir()) {
            return false;
        }
        if (blockState.is(BlockTags.LEAVES)) {
            return false;
        }
        if (blockState.is(BlockTags.LOGS)) {
            return false;
        }
        if (blockState.is(Blocks.BROWN_MUSHROOM_BLOCK) || blockState.is(Blocks.RED_MUSHROOM_BLOCK)) {
            return false;
        }
        return !blockState.getCollisionShape(chunkAccess, blockPos).isEmpty();
    }

    protected double getHeight(int i, int j, int k) {
        if (i == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE || k == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE) {
            return this.heights[BlendingData.getOutsideIndex(i, k)];
        }
        if (i == 0 || k == 0) {
            return this.heights[BlendingData.getInsideIndex(i, k)];
        }
        return Double.MAX_VALUE;
    }

    private static double getDensity(@Nullable double[] ds, int i) {
        if (ds == null) {
            return Double.MAX_VALUE;
        }
        int j = i - BlendingData.getColumnMinY();
        if (j < 0 || j >= ds.length) {
            return Double.MAX_VALUE;
        }
        return ds[j] * 0.1;
    }

    protected double getDensity(int i, int j, int k) {
        if (j == BlendingData.getMinY()) {
            return this.floorDensities[this.getFloorIndex(i, k)] * 0.1;
        }
        if (i == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE || k == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE) {
            return BlendingData.getDensity(this.densities[BlendingData.getOutsideIndex(i, k)], j);
        }
        if (i == 0 || k == 0) {
            return BlendingData.getDensity(this.densities[BlendingData.getInsideIndex(i, k)], j);
        }
        return Double.MAX_VALUE;
    }

    protected void iterateHeights(int i, int j, HeightConsumer heightConsumer) {
        for (int k = 0; k < this.densities.length; ++k) {
            double d = this.heights[k];
            if (d == Double.MAX_VALUE) continue;
            heightConsumer.consume(i + BlendingData.getX(k), j + BlendingData.getZ(k), d);
        }
    }

    protected void iterateDensities(int i, int j, int k, int l, DensityConsumer densityConsumer) {
        int q;
        int p;
        int m = BlendingData.getColumnMinY();
        int n = Math.max(0, k - m);
        int o = Math.min(BlendingData.cellCountPerColumn(), l - m);
        for (p = 0; p < this.densities.length; ++p) {
            double[] ds = this.densities[p];
            if (ds == null) continue;
            q = i + BlendingData.getX(p);
            int r = j + BlendingData.getZ(p);
            for (int s = n; s < o; ++s) {
                densityConsumer.consume(q, s + m, r, ds[s] * 0.1);
            }
        }
        if (m >= k && m <= l) {
            for (p = 0; p < this.floorDensities.length; ++p) {
                int t = this.getFloorX(p);
                q = this.getFloorZ(p);
                densityConsumer.consume(t, m, q, this.floorDensities[p] * 0.1);
            }
        }
    }

    private int getFloorIndex(int i, int j) {
        return i * CELL_HORIZONTAL_FLOOR_COUNT + j;
    }

    private int getFloorX(int i) {
        return i / CELL_HORIZONTAL_FLOOR_COUNT;
    }

    private int getFloorZ(int i) {
        return i % CELL_HORIZONTAL_FLOOR_COUNT;
    }

    private static int cellCountPerColumn() {
        return AREA_WITH_OLD_GENERATION.getSectionsCount() * 2;
    }

    private static int getColumnMinY() {
        return BlendingData.getMinY() + 1;
    }

    private static int getMinY() {
        return AREA_WITH_OLD_GENERATION.getMinSection() * 2;
    }

    private static int getInsideIndex(int i, int j) {
        return CELL_HORIZONTAL_MAX_INDEX_INSIDE - i + j;
    }

    private static int getOutsideIndex(int i, int j) {
        return CELL_COLUMN_INSIDE_COUNT + i + CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - j;
    }

    private static int getX(int i) {
        if (i < CELL_COLUMN_INSIDE_COUNT) {
            return BlendingData.zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX_INSIDE - i);
        }
        int j = i - CELL_COLUMN_INSIDE_COUNT;
        return CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - BlendingData.zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - j);
    }

    private static int getZ(int i) {
        if (i < CELL_COLUMN_INSIDE_COUNT) {
            return BlendingData.zeroIfNegative(i - CELL_HORIZONTAL_MAX_INDEX_INSIDE);
        }
        int j = i - CELL_COLUMN_INSIDE_COUNT;
        return CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - BlendingData.zeroIfNegative(j - CELL_HORIZONTAL_MAX_INDEX_OUTSIDE);
    }

    private static int zeroIfNegative(int i) {
        return i & ~(i >> 31);
    }

    protected static interface HeightConsumer {
        public void consume(int var1, int var2, double var3);
    }

    protected static interface DensityConsumer {
        public void consume(int var1, int var2, int var3, double var4);
    }
}

