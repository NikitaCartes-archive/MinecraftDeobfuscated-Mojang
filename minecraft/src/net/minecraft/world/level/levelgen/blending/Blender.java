package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

public class Blender {
	private static final Blender EMPTY = new Blender(new Long2ObjectOpenHashMap(), new Long2ObjectOpenHashMap()) {
		@Override
		public Blender.BlendingOutput blendOffsetAndFactor(int i, int j) {
			return new Blender.BlendingOutput(1.0, 0.0);
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
		} else {
			ChunkPos chunkPos = worldGenRegion.getCenter();
			if (!worldGenRegion.isOldChunkAround(chunkPos, HEIGHT_BLENDING_RANGE_CHUNKS)) {
				return EMPTY;
			} else {
				Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap = new Long2ObjectOpenHashMap<>();
				Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap2 = new Long2ObjectOpenHashMap<>();
				int i = Mth.square(HEIGHT_BLENDING_RANGE_CHUNKS + 1);

				for (int j = -HEIGHT_BLENDING_RANGE_CHUNKS; j <= HEIGHT_BLENDING_RANGE_CHUNKS; j++) {
					for (int k = -HEIGHT_BLENDING_RANGE_CHUNKS; k <= HEIGHT_BLENDING_RANGE_CHUNKS; k++) {
						if (j * j + k * k <= i) {
							int l = chunkPos.x + j;
							int m = chunkPos.z + k;
							BlendingData blendingData = BlendingData.getOrUpdateBlendingData(worldGenRegion, l, m);
							if (blendingData != null) {
								long2ObjectOpenHashMap.put(ChunkPos.asLong(l, m), blendingData);
								if (j >= -DENSITY_BLENDING_RANGE_CHUNKS
									&& j <= DENSITY_BLENDING_RANGE_CHUNKS
									&& k >= -DENSITY_BLENDING_RANGE_CHUNKS
									&& k <= DENSITY_BLENDING_RANGE_CHUNKS) {
									long2ObjectOpenHashMap2.put(ChunkPos.asLong(l, m), blendingData);
								}
							}
						}
					}
				}

				return long2ObjectOpenHashMap.isEmpty() && long2ObjectOpenHashMap2.isEmpty() ? EMPTY : new Blender(long2ObjectOpenHashMap, long2ObjectOpenHashMap2);
			}
		}
	}

	Blender(Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap, Long2ObjectOpenHashMap<BlendingData> long2ObjectOpenHashMap2) {
		this.heightAndBiomeBlendingData = long2ObjectOpenHashMap;
		this.densityBlendingData = long2ObjectOpenHashMap2;
	}

	public Blender.BlendingOutput blendOffsetAndFactor(int i, int j) {
		int k = QuartPos.fromBlock(i);
		int l = QuartPos.fromBlock(j);
		double d = this.getBlendingDataValue(k, 0, l, BlendingData::getHeight);
		if (d != Double.MAX_VALUE) {
			return new Blender.BlendingOutput(0.0, heightToOffset(d));
		} else {
			MutableDouble mutableDouble = new MutableDouble(0.0);
			MutableDouble mutableDouble2 = new MutableDouble(0.0);
			MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);
			this.heightAndBiomeBlendingData
				.forEach(
					(long_, blendingData) -> blendingData.iterateHeights(
							QuartPos.fromSection(ChunkPos.getX(long_)), QuartPos.fromSection(ChunkPos.getZ(long_)), (kx, lx, dx) -> {
								double ex = (double)Mth.length((float)(k - kx), (float)(l - lx));
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
				return new Blender.BlendingOutput(1.0, 0.0);
			} else {
				double e = mutableDouble2.doubleValue() / mutableDouble.doubleValue();
				double f = Mth.clamp(mutableDouble3.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
				f = 3.0 * f * f - 2.0 * f * f * f;
				return new Blender.BlendingOutput(f, heightToOffset(e));
			}
		}
	}

	private static double heightToOffset(double d) {
		double e = 1.0;
		double f = d + 0.5;
		double g = Mth.positiveModulo(f, 8.0);
		return 1.0 * (32.0 * (f - 128.0) - 3.0 * (f - 120.0) * g + 3.0 * g * g) / (128.0 * (32.0 - 3.0 * g));
	}

	public double blendDensity(DensityFunction.FunctionContext functionContext, double d) {
		int i = QuartPos.fromBlock(functionContext.blockX());
		int j = functionContext.blockY() / 8;
		int k = QuartPos.fromBlock(functionContext.blockZ());
		double e = this.getBlendingDataValue(i, j, k, BlendingData::getDensity);
		if (e != Double.MAX_VALUE) {
			return e;
		} else {
			MutableDouble mutableDouble = new MutableDouble(0.0);
			MutableDouble mutableDouble2 = new MutableDouble(0.0);
			MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);
			this.densityBlendingData
				.forEach(
					(long_, blendingData) -> blendingData.iterateDensities(
							QuartPos.fromSection(ChunkPos.getX(long_)), QuartPos.fromSection(ChunkPos.getZ(long_)), j - 1, j + 1, (l, m, n, dx) -> {
								double ex = Mth.length((double)(i - l), (double)((j - m) * 2), (double)(k - n));
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
		BlendingData blendingData = this.heightAndBiomeBlendingData.get(ChunkPos.asLong(i, j));
		return blendingData != null ? cellValueGetter.get(blendingData, k - QuartPos.fromSection(i), l, m - QuartPos.fromSection(j)) : Double.MAX_VALUE;
	}

	public BiomeResolver getBiomeResolver(BiomeResolver biomeResolver) {
		return (i, j, k, sampler) -> {
			Holder<Biome> holder = this.blendBiome(i, j, k);
			return holder == null ? biomeResolver.getNoiseBiome(i, j, k, sampler) : holder;
		};
	}

	@Nullable
	private Holder<Biome> blendBiome(int i, int j, int k) {
		MutableDouble mutableDouble = new MutableDouble(Double.POSITIVE_INFINITY);
		MutableObject<Holder<Biome>> mutableObject = new MutableObject<>();
		this.heightAndBiomeBlendingData
			.forEach(
				(long_, blendingData) -> blendingData.iterateBiomes(
						QuartPos.fromSection(ChunkPos.getX(long_)), j, QuartPos.fromSection(ChunkPos.getZ(long_)), (kxx, l, holder) -> {
							double dx = (double)Mth.length((float)(i - kxx), (float)(k - l));
							if (!(dx > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
								if (dx < mutableDouble.doubleValue()) {
									mutableObject.setValue(holder);
									mutableDouble.setValue(dx);
								}
							}
						}
					)
			);
		if (mutableDouble.doubleValue() == Double.POSITIVE_INFINITY) {
			return null;
		} else {
			double d = SHIFT_NOISE.getValue((double)i, 0.0, (double)k) * 12.0;
			double e = Mth.clamp((mutableDouble.doubleValue() + d) / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
			return e > 0.5 ? null : mutableObject.getValue();
		}
	}

	public static void generateBorderTicks(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		boolean bl = chunkAccess.isOldNoiseGeneration();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
		BlendingData blendingData = chunkAccess.getBlendingData();
		if (blendingData != null) {
			int i = blendingData.getAreaWithOldGeneration().getMinY();
			int j = blendingData.getAreaWithOldGeneration().getMaxY();
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
		Builder<Direction8, BlendingData> builder = ImmutableMap.builder();

		for (Direction8 direction8 : Direction8.values()) {
			int i = chunkPos.x + direction8.getStepX();
			int j = chunkPos.z + direction8.getStepZ();
			BlendingData blendingData = worldGenLevel.getChunk(i, j).getBlendingData();
			if (blendingData != null) {
				builder.put(direction8, blendingData);
			}
		}

		ImmutableMap<Direction8, BlendingData> immutableMap = builder.build();
		if (protoChunk.isOldNoiseGeneration() || !immutableMap.isEmpty()) {
			Blender.DistanceGetter distanceGetter = makeOldChunkDistanceGetter(protoChunk.getBlendingData(), immutableMap);
			CarvingMask.Mask mask = (ix, jx, k) -> {
				double d = (double)ix + 0.5 + SHIFT_NOISE.getValue((double)ix, (double)jx, (double)k) * 4.0;
				double e = (double)jx + 0.5 + SHIFT_NOISE.getValue((double)jx, (double)k, (double)ix) * 4.0;
				double f = (double)k + 0.5 + SHIFT_NOISE.getValue((double)k, (double)ix, (double)jx) * 4.0;
				return distanceGetter.getDistance(d, e, f) < 4.0;
			};
			protoChunk.getOrCreateCarvingMask().setAdditionalMask(mask);
		}
	}

	public static Blender.DistanceGetter makeOldChunkDistanceGetter(@Nullable BlendingData blendingData, Map<Direction8, BlendingData> map) {
		List<Blender.DistanceGetter> list = Lists.<Blender.DistanceGetter>newArrayList();
		if (blendingData != null) {
			list.add(makeOffsetOldChunkDistanceGetter(null, blendingData));
		}

		map.forEach((direction8, blendingDatax) -> list.add(makeOffsetOldChunkDistanceGetter(direction8, blendingDatax)));
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

	private static Blender.DistanceGetter makeOffsetOldChunkDistanceGetter(@Nullable Direction8 direction8, BlendingData blendingData) {
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
		double h = (double)blendingData.getAreaWithOldGeneration().getHeight() / 2.0;
		double i = (double)blendingData.getAreaWithOldGeneration().getMinY() + h;
		return (hx, ix, j) -> distanceToCube(hx - 8.0 - f, ix - i, j - 8.0 - g, 8.0, h, 8.0);
	}

	private static double distanceToCube(double d, double e, double f, double g, double h, double i) {
		double j = Math.abs(d) - g;
		double k = Math.abs(e) - h;
		double l = Math.abs(f) - i;
		return Mth.length(Math.max(0.0, j), Math.max(0.0, k), Math.max(0.0, l));
	}

	public static record BlendingOutput(double alpha, double blendingOffset) {
	}

	interface CellValueGetter {
		double get(BlendingData blendingData, int i, int j, int k);
	}

	public interface DistanceGetter {
		double getDistance(double d, double e, double f);
	}
}
