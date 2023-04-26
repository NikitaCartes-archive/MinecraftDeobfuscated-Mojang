package net.minecraft.world.level.levelgen.blending;

import com.google.common.primitives.Doubles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

public class BlendingData {
	private static final double BLENDING_DENSITY_FACTOR = 0.1;
	protected static final int CELL_WIDTH = 4;
	protected static final int CELL_HEIGHT = 8;
	protected static final int CELL_RATIO = 2;
	private static final double SOLID_DENSITY = 1.0;
	private static final double AIR_DENSITY = -1.0;
	private static final int CELLS_PER_SECTION_Y = 2;
	private static final int QUARTS_PER_SECTION = QuartPos.fromBlock(16);
	private static final int CELL_HORIZONTAL_MAX_INDEX_INSIDE = QUARTS_PER_SECTION - 1;
	private static final int CELL_HORIZONTAL_MAX_INDEX_OUTSIDE = QUARTS_PER_SECTION;
	private static final int CELL_COLUMN_INSIDE_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX_INSIDE + 1;
	private static final int CELL_COLUMN_OUTSIDE_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX_OUTSIDE + 1;
	private static final int CELL_COLUMN_COUNT = CELL_COLUMN_INSIDE_COUNT + CELL_COLUMN_OUTSIDE_COUNT;
	private final LevelHeightAccessor areaWithOldGeneration;
	private static final List<Block> SURFACE_BLOCKS = List.of(
		Blocks.PODZOL,
		Blocks.GRAVEL,
		Blocks.GRASS_BLOCK,
		Blocks.STONE,
		Blocks.COARSE_DIRT,
		Blocks.SAND,
		Blocks.RED_SAND,
		Blocks.MYCELIUM,
		Blocks.SNOW_BLOCK,
		Blocks.TERRACOTTA,
		Blocks.DIRT
	);
	protected static final double NO_VALUE = Double.MAX_VALUE;
	private boolean hasCalculatedData;
	private final double[] heights;
	private final List<List<Holder<Biome>>> biomes;
	private final transient double[][] densities;
	private static final Codec<double[]> DOUBLE_ARRAY_CODEC = Codec.DOUBLE.listOf().xmap(Doubles::toArray, Doubles::asList);
	public static final Codec<BlendingData> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.INT.fieldOf("min_section").forGetter(blendingData -> blendingData.areaWithOldGeneration.getMinSection()),
						Codec.INT.fieldOf("max_section").forGetter(blendingData -> blendingData.areaWithOldGeneration.getMaxSection()),
						DOUBLE_ARRAY_CODEC.optionalFieldOf("heights")
							.forGetter(
								blendingData -> DoubleStream.of(blendingData.heights).anyMatch(d -> d != Double.MAX_VALUE) ? Optional.of(blendingData.heights) : Optional.empty()
							)
					)
					.apply(instance, BlendingData::new)
		)
		.comapFlatMap(BlendingData::validateArraySize, Function.identity());

	private static DataResult<BlendingData> validateArraySize(BlendingData blendingData) {
		return blendingData.heights.length != CELL_COLUMN_COUNT
			? DataResult.error(() -> "heights has to be of length " + CELL_COLUMN_COUNT)
			: DataResult.success(blendingData);
	}

	private BlendingData(int i, int j, Optional<double[]> optional) {
		this.heights = (double[])optional.orElse(Util.make(new double[CELL_COLUMN_COUNT], ds -> Arrays.fill(ds, Double.MAX_VALUE)));
		this.densities = new double[CELL_COLUMN_COUNT][];
		ObjectArrayList<List<Holder<Biome>>> objectArrayList = new ObjectArrayList<>(CELL_COLUMN_COUNT);
		objectArrayList.size(CELL_COLUMN_COUNT);
		this.biomes = objectArrayList;
		int k = SectionPos.sectionToBlockCoord(i);
		int l = SectionPos.sectionToBlockCoord(j) - k;
		this.areaWithOldGeneration = LevelHeightAccessor.create(k, l);
	}

	@Nullable
	public static BlendingData getOrUpdateBlendingData(WorldGenRegion worldGenRegion, int i, int j) {
		ChunkAccess chunkAccess = worldGenRegion.getChunk(i, j);
		BlendingData blendingData = chunkAccess.getBlendingData();
		if (blendingData != null && chunkAccess.getHighestGeneratedStatus().isOrAfter(ChunkStatus.BIOMES)) {
			blendingData.calculateData(chunkAccess, sideByGenerationAge(worldGenRegion, i, j, false));
			return blendingData;
		} else {
			return null;
		}
	}

	public static Set<Direction8> sideByGenerationAge(WorldGenLevel worldGenLevel, int i, int j, boolean bl) {
		Set<Direction8> set = EnumSet.noneOf(Direction8.class);

		for (Direction8 direction8 : Direction8.values()) {
			int k = i + direction8.getStepX();
			int l = j + direction8.getStepZ();
			if (worldGenLevel.getChunk(k, l).isOldNoiseGeneration() == bl) {
				set.add(direction8);
			}
		}

		return set;
	}

	private void calculateData(ChunkAccess chunkAccess, Set<Direction8> set) {
		if (!this.hasCalculatedData) {
			if (set.contains(Direction8.NORTH) || set.contains(Direction8.WEST) || set.contains(Direction8.NORTH_WEST)) {
				this.addValuesForColumn(getInsideIndex(0, 0), chunkAccess, 0, 0);
			}

			if (set.contains(Direction8.NORTH)) {
				for (int i = 1; i < QUARTS_PER_SECTION; i++) {
					this.addValuesForColumn(getInsideIndex(i, 0), chunkAccess, 4 * i, 0);
				}
			}

			if (set.contains(Direction8.WEST)) {
				for (int i = 1; i < QUARTS_PER_SECTION; i++) {
					this.addValuesForColumn(getInsideIndex(0, i), chunkAccess, 0, 4 * i);
				}
			}

			if (set.contains(Direction8.EAST)) {
				for (int i = 1; i < QUARTS_PER_SECTION; i++) {
					this.addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, i), chunkAccess, 15, 4 * i);
				}
			}

			if (set.contains(Direction8.SOUTH)) {
				for (int i = 0; i < QUARTS_PER_SECTION; i++) {
					this.addValuesForColumn(getOutsideIndex(i, CELL_HORIZONTAL_MAX_INDEX_OUTSIDE), chunkAccess, 4 * i, 15);
				}
			}

			if (set.contains(Direction8.EAST) && set.contains(Direction8.NORTH_EAST)) {
				this.addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, 0), chunkAccess, 15, 0);
			}

			if (set.contains(Direction8.EAST) && set.contains(Direction8.SOUTH) && set.contains(Direction8.SOUTH_EAST)) {
				this.addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, CELL_HORIZONTAL_MAX_INDEX_OUTSIDE), chunkAccess, 15, 15);
			}

			this.hasCalculatedData = true;
		}
	}

	private void addValuesForColumn(int i, ChunkAccess chunkAccess, int j, int k) {
		if (this.heights[i] == Double.MAX_VALUE) {
			this.heights[i] = (double)this.getHeightAtXZ(chunkAccess, j, k);
		}

		this.densities[i] = this.getDensityColumn(chunkAccess, j, k, Mth.floor(this.heights[i]));
		this.biomes.set(i, this.getBiomeColumn(chunkAccess, j, k));
	}

	private int getHeightAtXZ(ChunkAccess chunkAccess, int i, int j) {
		int k;
		if (chunkAccess.hasPrimedHeightmap(Heightmap.Types.WORLD_SURFACE_WG)) {
			k = Math.min(chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, j) + 1, this.areaWithOldGeneration.getMaxBuildHeight());
		} else {
			k = this.areaWithOldGeneration.getMaxBuildHeight();
		}

		int l = this.areaWithOldGeneration.getMinBuildHeight();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, k, j);

		while (mutableBlockPos.getY() > l) {
			mutableBlockPos.move(Direction.DOWN);
			if (SURFACE_BLOCKS.contains(chunkAccess.getBlockState(mutableBlockPos).getBlock())) {
				return mutableBlockPos.getY();
			}
		}

		return l;
	}

	private static double read1(ChunkAccess chunkAccess, BlockPos.MutableBlockPos mutableBlockPos) {
		return isGround(chunkAccess, mutableBlockPos.move(Direction.DOWN)) ? 1.0 : -1.0;
	}

	private static double read7(ChunkAccess chunkAccess, BlockPos.MutableBlockPos mutableBlockPos) {
		double d = 0.0;

		for (int i = 0; i < 7; i++) {
			d += read1(chunkAccess, mutableBlockPos);
		}

		return d;
	}

	private double[] getDensityColumn(ChunkAccess chunkAccess, int i, int j, int k) {
		double[] ds = new double[this.cellCountPerColumn()];
		Arrays.fill(ds, -1.0);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, this.areaWithOldGeneration.getMaxBuildHeight(), j);
		double d = read7(chunkAccess, mutableBlockPos);

		for (int l = ds.length - 2; l >= 0; l--) {
			double e = read1(chunkAccess, mutableBlockPos);
			double f = read7(chunkAccess, mutableBlockPos);
			ds[l] = (d + e + f) / 15.0;
			d = f;
		}

		int l = this.getCellYIndex(Mth.floorDiv(k, 8));
		if (l >= 0 && l < ds.length - 1) {
			double e = ((double)k + 0.5) % 8.0 / 8.0;
			double f = (1.0 - e) / e;
			double g = Math.max(f, 1.0) * 0.25;
			ds[l + 1] = -f / g;
			ds[l] = 1.0 / g;
		}

		return ds;
	}

	private List<Holder<Biome>> getBiomeColumn(ChunkAccess chunkAccess, int i, int j) {
		ObjectArrayList<Holder<Biome>> objectArrayList = new ObjectArrayList<>(this.quartCountPerColumn());
		objectArrayList.size(this.quartCountPerColumn());

		for (int k = 0; k < objectArrayList.size(); k++) {
			int l = k + QuartPos.fromBlock(this.areaWithOldGeneration.getMinBuildHeight());
			objectArrayList.set(k, chunkAccess.getNoiseBiome(QuartPos.fromBlock(i), l, QuartPos.fromBlock(j)));
		}

		return objectArrayList;
	}

	private static boolean isGround(ChunkAccess chunkAccess, BlockPos blockPos) {
		BlockState blockState = chunkAccess.getBlockState(blockPos);
		if (blockState.isAir()) {
			return false;
		} else if (blockState.is(BlockTags.LEAVES)) {
			return false;
		} else if (blockState.is(BlockTags.LOGS)) {
			return false;
		} else {
			return blockState.is(Blocks.BROWN_MUSHROOM_BLOCK) || blockState.is(Blocks.RED_MUSHROOM_BLOCK)
				? false
				: !blockState.getCollisionShape(chunkAccess, blockPos).isEmpty();
		}
	}

	protected double getHeight(int i, int j, int k) {
		if (i == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE || k == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE) {
			return this.heights[getOutsideIndex(i, k)];
		} else {
			return i != 0 && k != 0 ? Double.MAX_VALUE : this.heights[getInsideIndex(i, k)];
		}
	}

	private double getDensity(@Nullable double[] ds, int i) {
		if (ds == null) {
			return Double.MAX_VALUE;
		} else {
			int j = this.getCellYIndex(i);
			return j >= 0 && j < ds.length ? ds[j] * 0.1 : Double.MAX_VALUE;
		}
	}

	protected double getDensity(int i, int j, int k) {
		if (j == this.getMinY()) {
			return 0.1;
		} else if (i == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE || k == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE) {
			return this.getDensity(this.densities[getOutsideIndex(i, k)], j);
		} else {
			return i != 0 && k != 0 ? Double.MAX_VALUE : this.getDensity(this.densities[getInsideIndex(i, k)], j);
		}
	}

	protected void iterateBiomes(int i, int j, int k, BlendingData.BiomeConsumer biomeConsumer) {
		if (j >= QuartPos.fromBlock(this.areaWithOldGeneration.getMinBuildHeight()) && j < QuartPos.fromBlock(this.areaWithOldGeneration.getMaxBuildHeight())) {
			int l = j - QuartPos.fromBlock(this.areaWithOldGeneration.getMinBuildHeight());

			for (int m = 0; m < this.biomes.size(); m++) {
				if (this.biomes.get(m) != null) {
					Holder<Biome> holder = (Holder<Biome>)((List)this.biomes.get(m)).get(l);
					if (holder != null) {
						biomeConsumer.consume(i + getX(m), k + getZ(m), holder);
					}
				}
			}
		}
	}

	protected void iterateHeights(int i, int j, BlendingData.HeightConsumer heightConsumer) {
		for (int k = 0; k < this.heights.length; k++) {
			double d = this.heights[k];
			if (d != Double.MAX_VALUE) {
				heightConsumer.consume(i + getX(k), j + getZ(k), d);
			}
		}
	}

	protected void iterateDensities(int i, int j, int k, int l, BlendingData.DensityConsumer densityConsumer) {
		int m = this.getColumnMinY();
		int n = Math.max(0, k - m);
		int o = Math.min(this.cellCountPerColumn(), l - m);

		for (int p = 0; p < this.densities.length; p++) {
			double[] ds = this.densities[p];
			if (ds != null) {
				int q = i + getX(p);
				int r = j + getZ(p);

				for (int s = n; s < o; s++) {
					densityConsumer.consume(q, s + m, r, ds[s] * 0.1);
				}
			}
		}
	}

	private int cellCountPerColumn() {
		return this.areaWithOldGeneration.getSectionsCount() * 2;
	}

	private int quartCountPerColumn() {
		return QuartPos.fromSection(this.areaWithOldGeneration.getSectionsCount());
	}

	private int getColumnMinY() {
		return this.getMinY() + 1;
	}

	private int getMinY() {
		return this.areaWithOldGeneration.getMinSection() * 2;
	}

	private int getCellYIndex(int i) {
		return i - this.getColumnMinY();
	}

	private static int getInsideIndex(int i, int j) {
		return CELL_HORIZONTAL_MAX_INDEX_INSIDE - i + j;
	}

	private static int getOutsideIndex(int i, int j) {
		return CELL_COLUMN_INSIDE_COUNT + i + CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - j;
	}

	private static int getX(int i) {
		if (i < CELL_COLUMN_INSIDE_COUNT) {
			return zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX_INSIDE - i);
		} else {
			int j = i - CELL_COLUMN_INSIDE_COUNT;
			return CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - j);
		}
	}

	private static int getZ(int i) {
		if (i < CELL_COLUMN_INSIDE_COUNT) {
			return zeroIfNegative(i - CELL_HORIZONTAL_MAX_INDEX_INSIDE);
		} else {
			int j = i - CELL_COLUMN_INSIDE_COUNT;
			return CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - zeroIfNegative(j - CELL_HORIZONTAL_MAX_INDEX_OUTSIDE);
		}
	}

	private static int zeroIfNegative(int i) {
		return i & ~(i >> 31);
	}

	public LevelHeightAccessor getAreaWithOldGeneration() {
		return this.areaWithOldGeneration;
	}

	protected interface BiomeConsumer {
		void consume(int i, int j, Holder<Biome> holder);
	}

	protected interface DensityConsumer {
		void consume(int i, int j, int k, double d);
	}

	protected interface HeightConsumer {
		void consume(int i, int j, double d);
	}
}
