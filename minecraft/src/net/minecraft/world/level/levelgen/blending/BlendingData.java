package net.minecraft.world.level.levelgen.blending;

import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class BlendingData {
	private static final double BLENDING_DENSITY_FACTOR = 1.0;
	private static final LevelHeightAccessor AREA_WITH_OLD_GENERATION = new LevelHeightAccessor() {
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
	private static final int CELL_HORIZONTAL_MAX_INDEX = QuartPos.fromBlock(16) - 1;
	private static final int CELL_COLUMN_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX + 1;
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
	public static final BlendingData EMPTY = new BlendingData(null);
	public static final double NO_VALUE = Double.POSITIVE_INFINITY;
	private final ChunkPos pos;
	private final double[] heightDataArray;
	private final double[][] densityDataArray;

	public static BlendingData getOrCreateAndStoreToChunk(WorldGenRegion worldGenRegion, int i, int j) {
		ChunkAccess chunkAccess = worldGenRegion.getChunk(i, j);
		BlendingData blendingData = chunkAccess.getBlendingData();
		if (blendingData != null) {
			return blendingData;
		} else {
			ChunkAccess chunkAccess2 = worldGenRegion.getChunk(i - 1, j);
			ChunkAccess chunkAccess3 = worldGenRegion.getChunk(i, j - 1);
			ChunkAccess chunkAccess4 = worldGenRegion.getChunk(i - 1, j - 1);
			boolean bl = chunkAccess.isOldNoiseGeneration();
			BlendingData blendingData2;
			if (bl == chunkAccess2.isOldNoiseGeneration() && bl == chunkAccess3.isOldNoiseGeneration() && bl == chunkAccess4.isOldNoiseGeneration()) {
				blendingData2 = EMPTY;
			} else {
				blendingData2 = new BlendingData(chunkAccess, chunkAccess2, chunkAccess3, chunkAccess4);
			}

			chunkAccess.setBlendingData(blendingData2);
			return blendingData2;
		}
	}

	private BlendingData(ChunkPos chunkPos) {
		this.pos = chunkPos;
		this.heightDataArray = new double[CELL_COLUMN_COUNT];
		Arrays.fill(this.heightDataArray, Double.POSITIVE_INFINITY);
		this.densityDataArray = new double[CELL_COLUMN_COUNT][];
	}

	private BlendingData(ChunkAccess chunkAccess, ChunkAccess chunkAccess2, ChunkAccess chunkAccess3, ChunkAccess chunkAccess4) {
		this(chunkAccess.getPos());
		if (chunkAccess.isOldNoiseGeneration()) {
			this.addValuesForColumn(getIndex(0, 0), chunkAccess, 0, 0);
			if (!chunkAccess2.isOldNoiseGeneration()) {
				this.addValuesForColumn(getIndex(0, 1), chunkAccess, 0, 4);
				this.addValuesForColumn(getIndex(0, 2), chunkAccess, 0, 8);
				this.addValuesForColumn(getIndex(0, 3), chunkAccess, 0, 12);
			}

			if (!chunkAccess3.isOldNoiseGeneration()) {
				this.addValuesForColumn(getIndex(1, 0), chunkAccess, 4, 0);
				this.addValuesForColumn(getIndex(2, 0), chunkAccess, 8, 0);
				this.addValuesForColumn(getIndex(3, 0), chunkAccess, 12, 0);
			}
		} else {
			if (chunkAccess2.isOldNoiseGeneration()) {
				this.addValuesForColumn(getIndex(0, 0), chunkAccess2, 15, 0);
				this.addValuesForColumn(getIndex(0, 1), chunkAccess2, 15, 4);
				this.addValuesForColumn(getIndex(0, 2), chunkAccess2, 15, 8);
				this.addValuesForColumn(getIndex(0, 3), chunkAccess2, 15, 12);
			}

			if (chunkAccess3.isOldNoiseGeneration()) {
				if (!chunkAccess2.isOldNoiseGeneration()) {
					this.addValuesForColumn(getIndex(0, 0), chunkAccess3, 0, 15);
				}

				this.addValuesForColumn(getIndex(1, 0), chunkAccess3, 4, 15);
				this.addValuesForColumn(getIndex(2, 0), chunkAccess3, 8, 15);
				this.addValuesForColumn(getIndex(3, 0), chunkAccess3, 12, 15);
			}

			if (chunkAccess4.isOldNoiseGeneration() && !chunkAccess2.isOldNoiseGeneration() && !chunkAccess3.isOldNoiseGeneration()) {
				this.addValuesForColumn(getIndex(0, 0), chunkAccess4, 15, 15);
			}
		}
	}

	private void addValuesForColumn(int i, ChunkAccess chunkAccess, int j, int k) {
		this.heightDataArray[i] = (double)getHeightAtXZ(chunkAccess, j, k);
		this.densityDataArray[i] = getDensityColumn(chunkAccess, j, k);
	}

	private static int getHeightAtXZ(ChunkAccess chunkAccess, int i, int j) {
		int k;
		if (chunkAccess.hasPrimedHeightmap(Heightmap.Types.WORLD_SURFACE_WG)) {
			k = Math.min(chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, j), AREA_WITH_OLD_GENERATION.getMaxBuildHeight());
		} else {
			k = AREA_WITH_OLD_GENERATION.getMaxBuildHeight();
		}

		int l = AREA_WITH_OLD_GENERATION.getMinBuildHeight();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, k, j);

		while (mutableBlockPos.getY() > l) {
			mutableBlockPos.move(Direction.DOWN);
			if (SURFACE_BLOCKS.contains(chunkAccess.getBlockState(mutableBlockPos).getBlock())) {
				return mutableBlockPos.getY();
			}
		}

		return l;
	}

	private static double[] getDensityColumn(ChunkAccess chunkAccess, int i, int j) {
		int k = AREA_WITH_OLD_GENERATION.getSectionsCount() * 2 + 1;
		int l = AREA_WITH_OLD_GENERATION.getMinSection() * 2;
		double[] ds = new double[k];
		double d = 3.0;
		double e = 0.0;
		double f = 0.0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		double g = 15.0;

		for (int m = AREA_WITH_OLD_GENERATION.getMaxBuildHeight() - 1; m >= AREA_WITH_OLD_GENERATION.getMinBuildHeight(); m--) {
			double h = isGround(chunkAccess, mutableBlockPos.set(i, m, j)) ? 1.0 : -1.0;
			int n = m % 8;
			if (n == 0) {
				double o = e / 15.0;
				int p = m / 8 + 1;
				ds[p - l] = o * d;
				e = f;
				f = 0.0;
				if (o > 0.0) {
					d = 0.1;
				}
			} else {
				f += h;
			}

			e += h;
		}

		ds[0] = e / 8.0 * d;
		return ds;
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

	protected double getHeight(int i, int j) {
		int k = i & 3;
		int l = j & 3;
		return k != 0 && l != 0 ? Double.POSITIVE_INFINITY : this.heightDataArray[getIndex(k, l)];
	}

	protected double getDensity(int i, int j, int k) {
		int l = i & 3;
		int m = k & 3;
		if (l != 0 && m != 0) {
			return Double.POSITIVE_INFINITY;
		} else {
			double[] ds = this.densityDataArray[getIndex(l, m)];
			if (ds == null) {
				return Double.POSITIVE_INFINITY;
			} else {
				int n = j - AREA_WITH_OLD_GENERATION.getMinSection() * 2;
				return n >= 0 && n < ds.length ? ds[n] * 1.0 : Double.POSITIVE_INFINITY;
			}
		}
	}

	protected void iterateHeights(BlendingData.HeightConsumer heightConsumer) {
		for (int i = 0; i < this.heightDataArray.length; i++) {
			double d = this.heightDataArray[i];
			if (d != Double.POSITIVE_INFINITY) {
				heightConsumer.consume(getX(i) + QuartPos.fromSection(this.pos.x), getZ(i) + QuartPos.fromSection(this.pos.z), d);
			}
		}
	}

	protected void iterateDensities(int i, int j, BlendingData.DensityConsumer densityConsumer) {
		int k = Math.max(0, i - AREA_WITH_OLD_GENERATION.getMinSection() * 2);
		int l = Math.min(AREA_WITH_OLD_GENERATION.getSectionsCount() * 2 + 1, j - AREA_WITH_OLD_GENERATION.getMinSection() * 2);

		for (int m = 0; m < this.densityDataArray.length; m++) {
			double[] ds = this.densityDataArray[m];
			if (ds != null) {
				for (int n = k; n < l; n++) {
					densityConsumer.consume(
						getX(m) + QuartPos.fromSection(this.pos.x), n + AREA_WITH_OLD_GENERATION.getMinSection() * 2, getZ(m) + QuartPos.fromSection(this.pos.z), ds[n] * 1.0
					);
				}
			}
		}
	}

	private static int getIndex(int i, int j) {
		return CELL_HORIZONTAL_MAX_INDEX - i + j;
	}

	private static int getX(int i) {
		return zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX - i);
	}

	private static int getZ(int i) {
		return zeroIfNegative(i - CELL_HORIZONTAL_MAX_INDEX);
	}

	private static int zeroIfNegative(int i) {
		return i & ~(i >> 31);
	}

	protected interface DensityConsumer {
		void consume(int i, int j, int k, double d);
	}

	protected interface HeightConsumer {
		void consume(int i, int j, double d);
	}
}
