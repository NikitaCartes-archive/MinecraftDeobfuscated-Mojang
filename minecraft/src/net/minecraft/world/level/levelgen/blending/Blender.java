package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.TerrainInfo;
import org.apache.commons.lang3.mutable.MutableDouble;

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
	};
	private static final int HEIGHT_BLENDING_RANGE_CELLS = QuartPos.fromSection(7) - 1;
	private static final int HEIGHT_BLENDING_RANGE_CHUNKS = QuartPos.toSection(HEIGHT_BLENDING_RANGE_CELLS + 3);
	private static final int DENSITY_BLENDING_RANGE_CELLS = 2;
	private static final int DENSITY_BLENDING_RANGE_CHUNKS = QuartPos.toSection(5);
	private static final double BLENDING_FACTOR = 10.0;
	private static final double BLENDING_JAGGEDNESS = 0.0;
	private final WorldGenRegion region;
	private final List<BlendingData> heightData;
	private final List<BlendingData> densityData;

	public static Blender empty() {
		return EMPTY;
	}

	public static Blender of(@Nullable WorldGenRegion worldGenRegion) {
		if (worldGenRegion == null) {
			return EMPTY;
		} else {
			List<BlendingData> list = Lists.<BlendingData>newArrayList();
			List<BlendingData> list2 = Lists.<BlendingData>newArrayList();
			ChunkPos chunkPos = worldGenRegion.getCenter();

			for (int i = -HEIGHT_BLENDING_RANGE_CHUNKS; i <= HEIGHT_BLENDING_RANGE_CHUNKS; i++) {
				for (int j = -HEIGHT_BLENDING_RANGE_CHUNKS; j <= HEIGHT_BLENDING_RANGE_CHUNKS; j++) {
					BlendingData blendingData = BlendingData.getOrCreateAndStoreToChunk(worldGenRegion, chunkPos.x + i, chunkPos.z + j);
					if (blendingData != BlendingData.EMPTY) {
						list.add(blendingData);
						if (i >= -DENSITY_BLENDING_RANGE_CHUNKS
							&& i <= DENSITY_BLENDING_RANGE_CHUNKS
							&& j >= -DENSITY_BLENDING_RANGE_CHUNKS
							&& j <= DENSITY_BLENDING_RANGE_CHUNKS) {
							list2.add(blendingData);
						}
					}
				}
			}

			return list.isEmpty() && list2.isEmpty() ? EMPTY : new Blender(worldGenRegion, list, list2);
		}
	}

	Blender(WorldGenRegion worldGenRegion, List<BlendingData> list, List<BlendingData> list2) {
		this.region = worldGenRegion;
		this.heightData = list;
		this.densityData = list2;
	}

	public TerrainInfo blendOffsetAndFactor(int i, int j, TerrainInfo terrainInfo) {
		int k = SectionPos.blockToSectionCoord(i);
		int l = SectionPos.blockToSectionCoord(j);
		int m = QuartPos.fromBlock(i);
		int n = QuartPos.fromBlock(j);
		BlendingData blendingData = BlendingData.getOrCreateAndStoreToChunk(this.region, k, l);
		if (blendingData != BlendingData.EMPTY) {
			double d = blendingData.getHeight(m, n);
			if (d != Double.POSITIVE_INFINITY) {
				return new TerrainInfo(heightToOffset(d), 10.0, 0.0);
			}
		}

		MutableDouble mutableDouble = new MutableDouble(0.0);
		MutableDouble mutableDouble2 = new MutableDouble(0.0);
		MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);

		for (BlendingData blendingData2 : this.heightData) {
			blendingData2.iterateHeights((kx, lx, d) -> {
				double e = Mth.length(m - kx, (double)(n - lx));
				if (!(e > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
					if (e < mutableDouble3.doubleValue()) {
						mutableDouble3.setValue(e);
					}

					double fx = 1.0 / (e * e * e * e);
					mutableDouble2.add(d * fx);
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
			double o = Mth.lerp(f, 0.0, terrainInfo.jaggedness());
			return new TerrainInfo(g, h, o);
		}
	}

	private static double heightToOffset(double d) {
		double e = 1.0;
		double f = d + 0.5;
		double g = Mth.positiveModulo(f, 8.0);
		return 1.0 * (32.0 * (f - 128.0) - 3.0 * (f - 120.0) * g + 3.0 * g * g) / (128.0 * (32.0 - 3.0 * g));
	}

	public double blendDensity(int i, int j, int k, double d) {
		int l = SectionPos.blockToSectionCoord(i);
		int m = SectionPos.blockToSectionCoord(k);
		int n = QuartPos.fromBlock(i);
		int o = j / 8;
		int p = QuartPos.fromBlock(k);
		BlendingData blendingData = BlendingData.getOrCreateAndStoreToChunk(this.region, l, m);
		if (blendingData != BlendingData.EMPTY) {
			double e = blendingData.getDensity(n, o, p);
			if (e != Double.POSITIVE_INFINITY) {
				return e;
			}
		}

		MutableDouble mutableDouble = new MutableDouble(0.0);
		MutableDouble mutableDouble2 = new MutableDouble(0.0);
		MutableDouble mutableDouble3 = new MutableDouble(Double.POSITIVE_INFINITY);

		for (BlendingData blendingData2 : this.densityData) {
			blendingData2.iterateDensities(o - 2, o + 2, (lx, mx, nx, dx) -> {
				double e = Mth.length(n - lx, (double)(o - mx), p - nx);
				if (!(e > 2.0)) {
					if (e < mutableDouble3.doubleValue()) {
						mutableDouble3.setValue(e);
					}

					double f = 1.0 / (e * e * e * e);
					mutableDouble2.add(dx * f);
					mutableDouble.add(f);
				}
			});
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
