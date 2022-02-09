package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.data.BuiltinRegistries;
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
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.TerrainInfo;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

public class Blender {
	private static final Blender EMPTY = new Blender(new Long2ObjectOpenHashMap(), new Long2ObjectOpenHashMap()) {
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
	private static final double OLD_CHUNK_Y_RADIUS = (double)BlendingData.AREA_WITH_OLD_GENERATION.getHeight() / 2.0;
	private static final double OLD_CHUNK_CENTER_Y = (double)BlendingData.AREA_WITH_OLD_GENERATION.getMinBuildHeight() + OLD_CHUNK_Y_RADIUS;
	private static final double OLD_CHUNK_XZ_RADIUS = 8.0;
	private final Long2ObjectOpenHashMap<BlendingData> blendingData;
	private final Long2ObjectOpenHashMap<BlendingData> blendingDataForDensityBlending;

	public static Blender empty() {
		return EMPTY;
	}

	public static Blender of(@Nullable WorldGenRegion worldGenRegion) {
		if (worldGenRegion == null) {
			return EMPTY;
		} else {
			Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap = new Long2ObjectOpenHashMap<>();
			Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap2 = new Long2ObjectOpenHashMap<>();
			ChunkPos chunkPos = worldGenRegion.getCenter();

			for (int i = -HEIGHT_BLENDING_RANGE_CHUNKS; i <= HEIGHT_BLENDING_RANGE_CHUNKS; i++) {
				for (int j = -HEIGHT_BLENDING_RANGE_CHUNKS; j <= HEIGHT_BLENDING_RANGE_CHUNKS; j++) {
					int k = chunkPos.x + i;
					int l = chunkPos.z + j;
					BlendingData blendingData = BlendingData.getOrUpdateBlendingData(worldGenRegion, k, l);
					if (blendingData != null) {
						long2ObjectOpenHashMap.put(ChunkPos.asLong(k, l), blendingData);
						if (i >= -DENSITY_BLENDING_RANGE_CHUNKS
							&& i <= DENSITY_BLENDING_RANGE_CHUNKS
							&& j >= -DENSITY_BLENDING_RANGE_CHUNKS
							&& j <= DENSITY_BLENDING_RANGE_CHUNKS) {
							long2ObjectOpenHashMap2.put(ChunkPos.asLong(k, l), blendingData);
						}
					}
				}
			}

			return long2ObjectOpenHashMap.isEmpty() && long2ObjectOpenHashMap2.isEmpty() ? EMPTY : new Blender(long2ObjectOpenHashMap, long2ObjectOpenHashMap2);
		}
	}

	Blender(Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap, Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap2) {
		this.blendingData = long2ObjectOpenHashMap;
		this.blendingDataForDensityBlending = long2ObjectOpenHashMap2;
	}

	public TerrainInfo blendOffsetAndFactor(int i, int j, TerrainInfo terrainInfo) {
		int k = QuartPos.fromBlock(i);
		int l = QuartPos.fromBlock(j);
		double d = this.getBlendingDataValue(k, 0, l, BlendingData::getHeight);
		if (d != Double.MAX_VALUE) {
			return new TerrainInfo(heightToOffset(d), 10.0, 0.0);
		} else {
			MutableDouble mutableDouble = new MutableDouble(0.0);
			MutableDouble mutableDouble2 = new MutableDouble(0.0);
			MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);
			this.blendingData
				.forEach(
					(long_, blendingData) -> blendingData.iterateHeights(
							QuartPos.fromSection(ChunkPos.getX(long_)), QuartPos.fromSection(ChunkPos.getZ(long_)), (kx, lx, dx) -> {
								double ex = Mth.length((double)(k - kx), (double)(l - lx));
								if (!(ex > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
									if (ex < mutableDouble3.doubleValue()) {
										mutableDouble3.setValue(ex);
									}

									double fx = 1.0 / (ex * ex * ex * ex);
									mutableDouble2.add(dx * fx);
									mutableDouble.add(fx);
								}
							}
						)
				);
			if (mutableDouble3.doubleValue() == Double.POSITIVE_INFINITY) {
				return terrainInfo;
			} else {
				double e = mutableDouble2.doubleValue() / mutableDouble.doubleValue();
				double f = Mth.clamp(mutableDouble3.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
				f = 3.0 * f * f - 2.0 * f * f * f;
				double g = Mth.lerp(f, heightToOffset(e), terrainInfo.offset());
				double h = Mth.lerp(f, 10.0, terrainInfo.factor());
				double m = Mth.lerp(f, 0.0, terrainInfo.jaggedness());
				return new TerrainInfo(g, h, m);
			}
		}
	}

	private static double heightToOffset(double d) {
		double e = 1.0;
		double f = d + 0.5;
		double g = Mth.positiveModulo(f, 8.0);
		return 1.0 * (32.0 * (f - 128.0) - 3.0 * (f - 120.0) * g + 3.0 * g * g) / (128.0 * (32.0 - 3.0 * g));
	}

	public double blendDensity(int i, int j, int k, double d) {
		int l = QuartPos.fromBlock(i);
		int m = j / 8;
		int n = QuartPos.fromBlock(k);
		double e = this.getBlendingDataValue(l, m, n, BlendingData::getDensity);
		if (e != Double.MAX_VALUE) {
			return e;
		} else {
			MutableDouble mutableDouble = new MutableDouble(0.0);
			MutableDouble mutableDouble2 = new MutableDouble(0.0);
			MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);
			this.blendingDataForDensityBlending
				.forEach(
					(long_, blendingData) -> blendingData.iterateDensities(
							QuartPos.fromSection(ChunkPos.getX(long_)), QuartPos.fromSection(ChunkPos.getZ(long_)), m - 1, m + 1, (lx, mx, nx, dx) -> {
								double ex = Mth.length((double)(l - lx), (double)((m - mx) * 2), (double)(n - nx));
								if (!(ex > 2.0)) {
									if (ex < mutableDouble3.doubleValue()) {
										mutableDouble3.setValue(ex);
									}

									double fx = 1.0 / (ex * ex * ex * ex);
									mutableDouble2.add(dx * fx);
									mutableDouble.add(fx);
								}
							}
						)
				);
			if (mutableDouble3.doubleValue() == Double.POSITIVE_INFINITY) {
				return d;
			} else {
				double f = mutableDouble2.doubleValue() / mutableDouble.doubleValue();
				double g = Mth.clamp(mutableDouble3.doubleValue() / 3.0, 0.0, 1.0);
				return Mth.lerp(g, f, d);
			}
		}
	}

	private double getBlendingDataValue(int i, int j, int k, Blender.CellValueGetter cellValueGetter) {
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

	private double getBlendingDataValue(Blender.CellValueGetter cellValueGetter, int i, int j, int k, int l, int m) {
		BlendingData blendingData = this.blendingData.get(ChunkPos.asLong(i, j));
		return blendingData != null ? cellValueGetter.get(blendingData, k - QuartPos.fromSection(i), l, m - QuartPos.fromSection(j)) : Double.MAX_VALUE;
	}

	public BiomeResolver getBiomeResolver(BiomeResolver biomeResolver) {
		return (i, j, k, sampler) -> {
			Holder<Biome> holder = this.blendBiome(i, k);
			return holder == null ? biomeResolver.getNoiseBiome(i, j, k, sampler) : holder;
		};
	}

	@Nullable
	private Holder<Biome> blendBiome(int i, int j) {
		double d = (double)i + SHIFT_NOISE.getValue((double)i, 0.0, (double)j) * 12.0;
		double e = (double)j + SHIFT_NOISE.getValue((double)j, (double)i, 0.0) * 12.0;
		MutableDouble mutableDouble = new MutableDouble(Double.POSITIVE_INFINITY);
		MutableObject<Holder<Biome>> mutableObject = new MutableObject<>();
		this.blendingData
			.forEach(
				(long_, blendingData) -> blendingData.iterateBiomes(
						QuartPos.fromSection(ChunkPos.getX(long_)), QuartPos.fromSection(ChunkPos.getZ(long_)), (ix, jx, holder) -> {
							double fx = Mth.length(d - (double)ix, e - (double)jx);
							if (!(fx > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
								if (fx < mutableDouble.doubleValue()) {
									mutableObject.setValue(holder);
									mutableDouble.setValue(fx);
								}
							}
						}
					)
			);
		if (mutableDouble.doubleValue() == Double.POSITIVE_INFINITY) {
			return null;
		} else {
			double f = Mth.clamp(mutableDouble.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
			return f > 0.5 ? null : mutableObject.getValue();
		}
	}

	public static void generateBorderTicks(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		boolean bl = chunkAccess.isOldNoiseGeneration();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
		int i = BlendingData.AREA_WITH_OLD_GENERATION.getMinBuildHeight();
		int j = BlendingData.AREA_WITH_OLD_GENERATION.getMaxBuildHeight() - 1;
		if (bl) {
			for (int k = 0; k < 16; k++) {
				for (int l = 0; l < 16; l++) {
					generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, k, i - 1, l));
					generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, k, i, l));
					generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, k, j, l));
					generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, k, j + 1, l));
				}
			}
		}

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (worldGenRegion.getChunk(chunkPos.x + direction.getStepX(), chunkPos.z + direction.getStepZ()).isOldNoiseGeneration() != bl) {
				int m = direction == Direction.EAST ? 15 : 0;
				int n = direction == Direction.WEST ? 0 : 15;
				int o = direction == Direction.SOUTH ? 15 : 0;
				int p = direction == Direction.NORTH ? 0 : 15;

				for (int q = m; q <= n; q++) {
					for (int r = o; r <= p; r++) {
						int s = Math.min(j, chunkAccess.getHeight(Heightmap.Types.MOTION_BLOCKING, q, r)) + 1;

						for (int t = i; t < s; t++) {
							generateBorderTick(chunkAccess, mutableBlockPos.setWithOffset(blockPos, q, t, r));
						}
					}
				}
			}
		}
	}

	private static void generateBorderTick(ChunkAccess chunkAccess, BlockPos blockPos) {
		BlockState blockState = chunkAccess.getBlockState(blockPos);
		if (blockState.is(BlockTags.LEAVES)) {
			chunkAccess.markPosForPostprocessing(blockPos);
		}

		FluidState fluidState = chunkAccess.getFluidState(blockPos);
		if (!fluidState.isEmpty()) {
			chunkAccess.markPosForPostprocessing(blockPos);
		}
	}

	public static void addAroundOldChunksCarvingMaskFilter(WorldGenLevel worldGenLevel, ProtoChunk protoChunk) {
		ChunkPos chunkPos = protoChunk.getPos();
		Blender.DistanceGetter distanceGetter = makeOldChunkDistanceGetter(
			protoChunk.isOldNoiseGeneration(), BlendingData.sideByGenerationAge(worldGenLevel, chunkPos.x, chunkPos.z, true)
		);
		if (distanceGetter != null) {
			CarvingMask.Mask mask = (i, j, k) -> {
				double d = (double)i + 0.5 + SHIFT_NOISE.getValue((double)i, (double)j, (double)k) * 4.0;
				double e = (double)j + 0.5 + SHIFT_NOISE.getValue((double)j, (double)k, (double)i) * 4.0;
				double f = (double)k + 0.5 + SHIFT_NOISE.getValue((double)k, (double)i, (double)j) * 4.0;
				return distanceGetter.getDistance(d, e, f) < 4.0;
			};
			Stream.of(GenerationStep.Carving.values()).map(protoChunk::getOrCreateCarvingMask).forEach(carvingMask -> carvingMask.setAdditionalMask(mask));
		}
	}

	@Nullable
	public static Blender.DistanceGetter makeOldChunkDistanceGetter(boolean bl, Set<Direction8> set) {
		if (!bl && set.isEmpty()) {
			return null;
		} else {
			List<Blender.DistanceGetter> list = Lists.<Blender.DistanceGetter>newArrayList();
			if (bl) {
				list.add(makeOffsetOldChunkDistanceGetter(null));
			}

			set.forEach(direction8 -> list.add(makeOffsetOldChunkDistanceGetter(direction8)));
			return (d, e, f) -> {
				double g = Double.POSITIVE_INFINITY;

				for (Blender.DistanceGetter distanceGetter : list) {
					double h = distanceGetter.getDistance(d, e, f);
					if (h < g) {
						g = h;
					}
				}

				return g;
			};
		}
	}

	private static Blender.DistanceGetter makeOffsetOldChunkDistanceGetter(@Nullable Direction8 direction8) {
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
		return (fx, gx, h) -> distanceToCube(fx - 8.0 - f, gx - OLD_CHUNK_CENTER_Y, h - 8.0 - g, 8.0, OLD_CHUNK_Y_RADIUS, 8.0);
	}

	private static double distanceToCube(double d, double e, double f, double g, double h, double i) {
		double j = Math.abs(d) - g;
		double k = Math.abs(e) - h;
		double l = Math.abs(f) - i;
		return Mth.length(Math.max(0.0, j), Math.max(0.0, k), Math.max(0.0, l));
	}

	interface CellValueGetter {
		double get(BlendingData blendingData, int i, int j, int k);
	}

	public interface DistanceGetter {
		double getDistance(double d, double e, double f);
	}
}
