package net.minecraft.world.level.levelgen;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

public class NoiseChunk {
	final int cellWidth;
	final int cellHeight;
	final int cellCountY;
	final int cellCountXZ;
	final int cellNoiseMinY;
	final int firstCellX;
	final int firstCellZ;
	private final int firstNoiseX;
	private final int firstNoiseZ;
	final List<NoiseChunk.NoiseInterpolator> interpolators;
	private final double[][] shiftedX;
	private final double[][] shiftedZ;
	private final double[][] continentalness;
	private final double[][] weirdness;
	private final double[][] erosion;
	private final TerrainInfo[][] terrainInfoBuffer;
	private final Long2ObjectMap<TerrainInfo> terrainInfo = new Long2ObjectOpenHashMap<>();
	private final Aquifer aquifer;
	private final NoiseChunk.BlockStateFiller baseNoise;
	private final NoiseChunk.BlockStateFiller oreVeins;

	public NoiseChunk(
		int i,
		int j,
		int k,
		int l,
		int m,
		NoiseSampler noiseSampler,
		int n,
		int o,
		NoiseChunk.NoiseFiller noiseFiller,
		Supplier<NoiseGeneratorSettings> supplier,
		Aquifer.FluidPicker fluidPicker
	) {
		this.cellWidth = i;
		this.cellHeight = j;
		this.cellCountY = l;
		this.cellCountXZ = k;
		this.cellNoiseMinY = m;
		this.firstCellX = Math.floorDiv(n, i);
		this.firstCellZ = Math.floorDiv(o, i);
		this.interpolators = Lists.<NoiseChunk.NoiseInterpolator>newArrayList();
		this.firstNoiseX = QuartPos.fromBlock(n);
		this.firstNoiseZ = QuartPos.fromBlock(o);
		int p = QuartPos.fromBlock(k * i);
		this.shiftedX = new double[p + 1][];
		this.shiftedZ = new double[p + 1][];
		this.continentalness = new double[p + 1][];
		this.weirdness = new double[p + 1][];
		this.erosion = new double[p + 1][];
		this.terrainInfoBuffer = new TerrainInfo[p + 1][];

		for (int q = 0; q <= p; q++) {
			int r = this.firstNoiseX + q;
			this.shiftedX[q] = new double[p + 1];
			this.shiftedZ[q] = new double[p + 1];
			this.continentalness[q] = new double[p + 1];
			this.weirdness[q] = new double[p + 1];
			this.erosion[q] = new double[p + 1];
			this.terrainInfoBuffer[q] = new TerrainInfo[p + 1];

			for (int s = 0; s <= p; s++) {
				int t = this.firstNoiseZ + s;
				NoiseChunk.FlatNoiseData flatNoiseData = noiseData(noiseSampler, r, t);
				this.shiftedX[q][s] = flatNoiseData.shiftedX;
				this.shiftedZ[q][s] = flatNoiseData.shiftedZ;
				this.continentalness[q][s] = flatNoiseData.continentalness;
				this.weirdness[q][s] = flatNoiseData.weirdness;
				this.erosion[q][s] = flatNoiseData.erosion;
				this.terrainInfoBuffer[q][s] = flatNoiseData.terrainInfo;
			}
		}

		this.aquifer = noiseSampler.createAquifer(this, n, o, m, l, fluidPicker, ((NoiseGeneratorSettings)supplier.get()).isAquifersEnabled());
		this.baseNoise = noiseSampler.makeBaseNoiseFiller(this, noiseFiller, ((NoiseGeneratorSettings)supplier.get()).isNoodleCavesEnabled());
		this.oreVeins = noiseSampler.makeOreVeinifier(this, ((NoiseGeneratorSettings)supplier.get()).isOreVeinsEnabled());
	}

	@VisibleForDebug
	public static NoiseChunk.FlatNoiseData noiseData(NoiseSampler noiseSampler, int i, int j) {
		return new NoiseChunk.FlatNoiseData(noiseSampler, i, j);
	}

	public double shiftedX(int i, int j) {
		return this.shiftedX[i - this.firstNoiseX][j - this.firstNoiseZ];
	}

	public double shiftedZ(int i, int j) {
		return this.shiftedZ[i - this.firstNoiseX][j - this.firstNoiseZ];
	}

	public double continentalness(int i, int j) {
		return this.continentalness[i - this.firstNoiseX][j - this.firstNoiseZ];
	}

	public double weirdness(int i, int j) {
		return this.weirdness[i - this.firstNoiseX][j - this.firstNoiseZ];
	}

	public double erosion(int i, int j) {
		return this.erosion[i - this.firstNoiseX][j - this.firstNoiseZ];
	}

	public TerrainInfo terrainInfo(int i, int j) {
		return this.terrainInfoBuffer[i - this.firstNoiseX][j - this.firstNoiseZ];
	}

	public TerrainInfo terrainInfoWide(NoiseSampler noiseSampler, int i, int j) {
		int k = i - this.firstNoiseX;
		int l = j - this.firstNoiseZ;
		int m = this.terrainInfoBuffer.length;
		return k >= 0 && l >= 0 && k < m && l < m
			? this.terrainInfoBuffer[k][l]
			: this.terrainInfo.computeIfAbsent(ChunkPos.asLong(i, j), lx -> noiseData(noiseSampler, ChunkPos.getX(lx), ChunkPos.getZ(lx)).terrainInfo);
	}

	public TerrainInfo terrainInfoInterpolated(int i, int j) {
		int k = QuartPos.fromBlock(i) - this.firstNoiseX;
		int l = QuartPos.fromBlock(j) - this.firstNoiseZ;
		TerrainInfo terrainInfo = this.terrainInfoBuffer[k][l];
		TerrainInfo terrainInfo2 = this.terrainInfoBuffer[k][l + 1];
		TerrainInfo terrainInfo3 = this.terrainInfoBuffer[k + 1][l];
		TerrainInfo terrainInfo4 = this.terrainInfoBuffer[k + 1][l + 1];
		double d = (double)Math.floorMod(i, 4) / 4.0;
		double e = (double)Math.floorMod(j, 4) / 4.0;
		double f = Mth.lerp2(d, e, terrainInfo.offset(), terrainInfo3.offset(), terrainInfo2.offset(), terrainInfo4.offset());
		double g = Mth.lerp2(d, e, terrainInfo.factor(), terrainInfo3.factor(), terrainInfo2.factor(), terrainInfo4.factor());
		double h = Mth.lerp2(d, e, terrainInfo.jaggedness(), terrainInfo3.jaggedness(), terrainInfo2.jaggedness(), terrainInfo4.jaggedness());
		return new TerrainInfo(f, g, h);
	}

	protected NoiseChunk.NoiseInterpolator createNoiseInterpolator(NoiseChunk.NoiseFiller noiseFiller) {
		return new NoiseChunk.NoiseInterpolator(noiseFiller);
	}

	public void initializeForFirstCellX() {
		this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.initializeForFirstCellX());
	}

	public void advanceCellX(int i) {
		this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.advanceCellX(i));
	}

	public void selectCellYZ(int i, int j) {
		this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.selectCellYZ(i, j));
	}

	public void updateForY(double d) {
		this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.updateForY(d));
	}

	public void updateForX(double d) {
		this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.updateForX(d));
	}

	public void updateForZ(double d) {
		this.interpolators.forEach(noiseInterpolator -> noiseInterpolator.updateForZ(d));
	}

	public void swapSlices() {
		this.interpolators.forEach(NoiseChunk.NoiseInterpolator::swapSlices);
	}

	public Aquifer aquifer() {
		return this.aquifer;
	}

	@Nullable
	protected BlockState updateNoiseAndGenerateBaseState(int i, int j, int k) {
		return this.baseNoise.calculate(i, j, k);
	}

	@Nullable
	protected BlockState oreVeinify(int i, int j, int k) {
		return this.oreVeins.calculate(i, j, k);
	}

	@FunctionalInterface
	public interface BlockStateFiller {
		@Nullable
		BlockState calculate(int i, int j, int k);
	}

	public static final class FlatNoiseData {
		final double shiftedX;
		final double shiftedZ;
		final double continentalness;
		final double weirdness;
		final double erosion;
		@VisibleForDebug
		public final TerrainInfo terrainInfo;

		FlatNoiseData(NoiseSampler noiseSampler, int i, int j) {
			this.shiftedX = (double)i + noiseSampler.getOffset(i, 0, j);
			this.shiftedZ = (double)j + noiseSampler.getOffset(j, i, 0);
			this.continentalness = noiseSampler.getContinentalness(this.shiftedX, 0.0, this.shiftedZ);
			this.weirdness = noiseSampler.getWeirdness(this.shiftedX, 0.0, this.shiftedZ);
			this.erosion = noiseSampler.getErosion(this.shiftedX, 0.0, this.shiftedZ);
			this.terrainInfo = noiseSampler.terrainInfo(
				QuartPos.toBlock(i), QuartPos.toBlock(j), (float)this.continentalness, (float)this.weirdness, (float)this.erosion
			);
		}
	}

	@FunctionalInterface
	public interface InterpolatableNoise {
		NoiseChunk.Sampler instantiate(NoiseChunk noiseChunk);
	}

	@FunctionalInterface
	public interface NoiseFiller {
		double calculateNoise(int i, int j, int k);
	}

	public class NoiseInterpolator implements NoiseChunk.Sampler {
		private double[][] slice0;
		private double[][] slice1;
		private final NoiseChunk.NoiseFiller noiseFiller;
		private double noise000;
		private double noise001;
		private double noise100;
		private double noise101;
		private double noise010;
		private double noise011;
		private double noise110;
		private double noise111;
		private double valueXZ00;
		private double valueXZ10;
		private double valueXZ01;
		private double valueXZ11;
		private double valueZ0;
		private double valueZ1;
		private double value;

		NoiseInterpolator(NoiseChunk.NoiseFiller noiseFiller) {
			this.noiseFiller = noiseFiller;
			this.slice0 = this.allocateSlice(NoiseChunk.this.cellCountY, NoiseChunk.this.cellCountXZ);
			this.slice1 = this.allocateSlice(NoiseChunk.this.cellCountY, NoiseChunk.this.cellCountXZ);
			NoiseChunk.this.interpolators.add(this);
		}

		private double[][] allocateSlice(int i, int j) {
			int k = j + 1;
			int l = i + 1;
			double[][] ds = new double[k][l];

			for (int m = 0; m < k; m++) {
				ds[m] = new double[l];
			}

			return ds;
		}

		void initializeForFirstCellX() {
			this.fillSlice(this.slice0, NoiseChunk.this.firstCellX);
		}

		void advanceCellX(int i) {
			this.fillSlice(this.slice1, NoiseChunk.this.firstCellX + i + 1);
		}

		private void fillSlice(double[][] ds, int i) {
			for (int j = 0; j < NoiseChunk.this.cellCountXZ + 1; j++) {
				int k = NoiseChunk.this.firstCellZ + j;

				for (int l = 0; l < NoiseChunk.this.cellCountY + 1; l++) {
					int m = l + NoiseChunk.this.cellNoiseMinY;
					int n = m * NoiseChunk.this.cellHeight;
					double d = this.noiseFiller.calculateNoise(i * NoiseChunk.this.cellWidth, n, k * NoiseChunk.this.cellWidth);
					ds[j][l] = d;
				}
			}
		}

		void selectCellYZ(int i, int j) {
			this.noise000 = this.slice0[j][i];
			this.noise001 = this.slice0[j + 1][i];
			this.noise100 = this.slice1[j][i];
			this.noise101 = this.slice1[j + 1][i];
			this.noise010 = this.slice0[j][i + 1];
			this.noise011 = this.slice0[j + 1][i + 1];
			this.noise110 = this.slice1[j][i + 1];
			this.noise111 = this.slice1[j + 1][i + 1];
		}

		void updateForY(double d) {
			this.valueXZ00 = Mth.lerp(d, this.noise000, this.noise010);
			this.valueXZ10 = Mth.lerp(d, this.noise100, this.noise110);
			this.valueXZ01 = Mth.lerp(d, this.noise001, this.noise011);
			this.valueXZ11 = Mth.lerp(d, this.noise101, this.noise111);
		}

		void updateForX(double d) {
			this.valueZ0 = Mth.lerp(d, this.valueXZ00, this.valueXZ10);
			this.valueZ1 = Mth.lerp(d, this.valueXZ01, this.valueXZ11);
		}

		void updateForZ(double d) {
			this.value = Mth.lerp(d, this.valueZ0, this.valueZ1);
		}

		@Override
		public double sample() {
			return this.value;
		}

		private void swapSlices() {
			double[][] ds = this.slice0;
			this.slice0 = this.slice1;
			this.slice1 = ds;
		}
	}

	@FunctionalInterface
	public interface Sampler {
		double sample();
	}
}
