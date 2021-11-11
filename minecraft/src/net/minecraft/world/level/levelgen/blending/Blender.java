package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.TerrainInfo;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

public class Blender {
	private static final Blender EMPTY = new Blender(null, List.of(), List.of()) {
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
	private final List<Blender.PositionedBlendingData> heightData;
	private final List<Blender.PositionedBlendingData> densityData;

	public static Blender empty() {
		return EMPTY;
	}

	public static Blender of(@Nullable WorldGenRegion worldGenRegion) {
		if (worldGenRegion == null) {
			return EMPTY;
		} else {
			List<Blender.PositionedBlendingData> list = Lists.<Blender.PositionedBlendingData>newArrayList();
			List<Blender.PositionedBlendingData> list2 = Lists.<Blender.PositionedBlendingData>newArrayList();
			ChunkPos chunkPos = worldGenRegion.getCenter();

			for (int i = -HEIGHT_BLENDING_RANGE_CHUNKS; i <= HEIGHT_BLENDING_RANGE_CHUNKS; i++) {
				for (int j = -HEIGHT_BLENDING_RANGE_CHUNKS; j <= HEIGHT_BLENDING_RANGE_CHUNKS; j++) {
					int k = chunkPos.x + i;
					int l = chunkPos.z + j;
					BlendingData blendingData = BlendingData.getOrUpdateBlendingData(worldGenRegion, k, l);
					if (blendingData != null) {
						Blender.PositionedBlendingData positionedBlendingData = new Blender.PositionedBlendingData(k, l, blendingData);
						list.add(positionedBlendingData);
						if (i >= -DENSITY_BLENDING_RANGE_CHUNKS
							&& i <= DENSITY_BLENDING_RANGE_CHUNKS
							&& j >= -DENSITY_BLENDING_RANGE_CHUNKS
							&& j <= DENSITY_BLENDING_RANGE_CHUNKS) {
							list2.add(positionedBlendingData);
						}
					}
				}
			}

			return list.isEmpty() && list2.isEmpty() ? EMPTY : new Blender(worldGenRegion, list, list2);
		}
	}

	Blender(WorldGenRegion worldGenRegion, List<Blender.PositionedBlendingData> list, List<Blender.PositionedBlendingData> list2) {
		this.region = worldGenRegion;
		this.heightData = list;
		this.densityData = list2;
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

			for (Blender.PositionedBlendingData positionedBlendingData : this.heightData) {
				positionedBlendingData.blendingData
					.iterateHeights(QuartPos.fromSection(positionedBlendingData.chunkX), QuartPos.fromSection(positionedBlendingData.chunkZ), (kx, lx, dx) -> {
						double e = Mth.length((double)(k - kx), (double)(l - lx));
						if (!(e > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
							if (e < mutableDouble3.doubleValue()) {
								mutableDouble3.setValue(e);
							}

							double fx = 1.0 / (e * e * e * e);
							mutableDouble2.add(dx * fx);
							mutableDouble.add(fx);
						}
					});
			}

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

			for (Blender.PositionedBlendingData positionedBlendingData : this.densityData) {
				positionedBlendingData.blendingData
					.iterateDensities(
						QuartPos.fromSection(positionedBlendingData.chunkX), QuartPos.fromSection(positionedBlendingData.chunkZ), m - 2, m + 2, (lx, mx, nx, dx) -> {
							double ex = Mth.length((double)(l - lx), (double)(m - mx), (double)(n - nx));
							if (!(ex > 2.0)) {
								if (ex < mutableDouble3.doubleValue()) {
									mutableDouble3.setValue(ex);
								}

								double f = 1.0 / (ex * ex * ex * ex);
								mutableDouble2.add(dx * f);
								mutableDouble.add(f);
							}
						}
					);
			}

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
		BlendingData blendingData = BlendingData.getOrUpdateBlendingData(this.region, i, j);
		return blendingData != null ? cellValueGetter.get(blendingData, k - QuartPos.fromSection(i), l, m - QuartPos.fromSection(j)) : Double.MAX_VALUE;
	}

	public BiomeResolver getBiomeResolver(BiomeResolver biomeResolver) {
		return (i, j, k, sampler) -> {
			Biome biome = this.blendBiome(i, j, k);
			return biome == null ? biomeResolver.getNoiseBiome(i, j, k, sampler) : biome;
		};
	}

	@Nullable
	private Biome blendBiome(int i, int j, int k) {
		double d = (double)i + SHIFT_NOISE.getValue((double)i, 0.0, (double)k) * 12.0;
		double e = (double)k + SHIFT_NOISE.getValue((double)k, (double)i, 0.0) * 12.0;
		MutableDouble mutableDouble = new MutableDouble(Double.POSITIVE_INFINITY);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		MutableObject<ChunkPos> mutableObject = new MutableObject<>();

		for (Blender.PositionedBlendingData positionedBlendingData : this.heightData) {
			positionedBlendingData.blendingData
				.iterateHeights(QuartPos.fromSection(positionedBlendingData.chunkX), QuartPos.fromSection(positionedBlendingData.chunkZ), (ix, jx, f) -> {
					double g = Mth.length(d - (double)ix, e - (double)jx);
					if (!(g > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
						if (g < mutableDouble.doubleValue()) {
							mutableObject.setValue(new ChunkPos(positionedBlendingData.chunkX, positionedBlendingData.chunkZ));
							mutableBlockPos.set(ix, QuartPos.fromBlock(Mth.floor(f)), jx);
							mutableDouble.setValue(g);
						}
					}
				});
		}

		if (mutableDouble.doubleValue() == Double.POSITIVE_INFINITY) {
			return null;
		} else {
			double f = Mth.clamp(mutableDouble.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
			if (f > 0.5) {
				return null;
			} else {
				ChunkAccess chunkAccess = this.region.getChunk(mutableObject.getValue().x, mutableObject.getValue().z);
				return chunkAccess.getNoiseBiome(Math.min(mutableBlockPos.getX() & 3, 3), mutableBlockPos.getY(), Math.min(mutableBlockPos.getZ() & 3, 3));
			}
		}
	}

	interface CellValueGetter {
		double get(BlendingData blendingData, int i, int j, int k);
	}

	static record PositionedBlendingData() {
		final int chunkX;
		final int chunkZ;
		final BlendingData blendingData;

		PositionedBlendingData(int i, int j, BlendingData blendingData) {
			this.chunkX = i;
			this.chunkZ = j;
			this.blendingData = blendingData;
		}
	}
}
