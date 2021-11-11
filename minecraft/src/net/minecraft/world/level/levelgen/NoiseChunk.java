package net.minecraft.world.level.levelgen;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.blending.Blender;

public class NoiseChunk {
	final NoiseSettings noiseSettings;
	final int cellCountXZ;
	final int cellCountY;
	final int cellNoiseMinY;
	final int firstCellX;
	final int firstCellZ;
	private final int firstNoiseX;
	private final int firstNoiseZ;
	final List<NoiseChunk.NoiseInterpolator> interpolators;
	private final NoiseSampler.FlatNoiseData[][] noiseData;
	private final Long2ObjectMap<TerrainInfo> terrainInfo = new Long2ObjectOpenHashMap<>();
	private final Aquifer aquifer;
	private final NoiseChunk.BlockStateFiller baseNoise;
	private final NoiseChunk.BlockStateFiller oreVeins;
	private final Blender blender;

	public static NoiseChunk forChunk(
		ChunkAccess chunkAccess,
		NoiseSampler noiseSampler,
		Supplier<NoiseChunk.NoiseFiller> supplier,
		NoiseGeneratorSettings noiseGeneratorSettings,
		Aquifer.FluidPicker fluidPicker,
		Blender blender
	) {
		ChunkPos chunkPos = chunkAccess.getPos();
		NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
		int i = Math.max(noiseSettings.minY(), chunkAccess.getMinBuildHeight());
		int j = Math.min(noiseSettings.minY() + noiseSettings.height(), chunkAccess.getMaxBuildHeight());
		int k = Mth.intFloorDiv(i, noiseSettings.getCellHeight());
		int l = Mth.intFloorDiv(j - i, noiseSettings.getCellHeight());
		return new NoiseChunk(
			16 / noiseSettings.getCellWidth(),
			l,
			k,
			noiseSampler,
			chunkPos.getMinBlockX(),
			chunkPos.getMinBlockZ(),
			(NoiseChunk.NoiseFiller)supplier.get(),
			noiseGeneratorSettings,
			fluidPicker,
			blender
		);
	}

	public static NoiseChunk forColumn(
		int i, int j, int k, int l, NoiseSampler noiseSampler, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker
	) {
		return new NoiseChunk(1, l, k, noiseSampler, i, j, (ix, jx, kx) -> 0.0, noiseGeneratorSettings, fluidPicker, Blender.empty());
	}

	private NoiseChunk(
		int i,
		int j,
		int k,
		NoiseSampler noiseSampler,
		int l,
		int m,
		NoiseChunk.NoiseFiller noiseFiller,
		NoiseGeneratorSettings noiseGeneratorSettings,
		Aquifer.FluidPicker fluidPicker,
		Blender blender
	) {
		this.noiseSettings = noiseGeneratorSettings.noiseSettings();
		this.cellCountXZ = i;
		this.cellCountY = j;
		this.cellNoiseMinY = k;
		int n = this.noiseSettings.getCellWidth();
		this.firstCellX = Math.floorDiv(l, n);
		this.firstCellZ = Math.floorDiv(m, n);
		this.interpolators = Lists.<NoiseChunk.NoiseInterpolator>newArrayList();
		this.firstNoiseX = QuartPos.fromBlock(l);
		this.firstNoiseZ = QuartPos.fromBlock(m);
		int o = QuartPos.fromBlock(i * n);
		this.noiseData = new NoiseSampler.FlatNoiseData[o + 1][];
		this.blender = blender;

		for (int p = 0; p <= o; p++) {
			int q = this.firstNoiseX + p;
			this.noiseData[p] = new NoiseSampler.FlatNoiseData[o + 1];

			for (int r = 0; r <= o; r++) {
				int s = this.firstNoiseZ + r;
				this.noiseData[p][r] = noiseSampler.noiseData(q, s, blender);
			}
		}

		this.aquifer = noiseSampler.createAquifer(this, l, m, k, j, fluidPicker, noiseGeneratorSettings.isAquifersEnabled());
		this.baseNoise = noiseSampler.makeBaseNoiseFiller(this, noiseFiller, noiseGeneratorSettings.isNoodleCavesEnabled());
		this.oreVeins = noiseSampler.makeOreVeinifier(this, noiseGeneratorSettings.isOreVeinsEnabled());
	}

	public NoiseSampler.FlatNoiseData noiseData(int i, int j) {
		return this.noiseData[i - this.firstNoiseX][j - this.firstNoiseZ];
	}

	public TerrainInfo terrainInfoWide(NoiseSampler noiseSampler, int i, int j) {
		int k = i - this.firstNoiseX;
		int l = j - this.firstNoiseZ;
		int m = this.noiseData.length;
		return k >= 0 && l >= 0 && k < m && l < m
			? this.noiseData[k][l].terrainInfo()
			: this.terrainInfo
				.computeIfAbsent(
					ChunkPos.asLong(i, j),
					(Long2ObjectFunction<? extends TerrainInfo>)(lx -> noiseSampler.noiseData(ChunkPos.getX(lx), ChunkPos.getZ(lx), this.blender).terrainInfo())
				);
	}

	public TerrainInfo terrainInfoInterpolated(int i, int j) {
		int k = QuartPos.fromBlock(i) - this.firstNoiseX;
		int l = QuartPos.fromBlock(j) - this.firstNoiseZ;
		TerrainInfo terrainInfo = this.noiseData[k][l].terrainInfo();
		TerrainInfo terrainInfo2 = this.noiseData[k][l + 1].terrainInfo();
		TerrainInfo terrainInfo3 = this.noiseData[k + 1][l].terrainInfo();
		TerrainInfo terrainInfo4 = this.noiseData[k + 1][l + 1].terrainInfo();
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

	public Blender getBlender() {
		return this.blender;
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
			int j = NoiseChunk.this.noiseSettings.getCellWidth();
			int k = NoiseChunk.this.noiseSettings.getCellHeight();

			for (int l = 0; l < NoiseChunk.this.cellCountXZ + 1; l++) {
				int m = NoiseChunk.this.firstCellZ + l;

				for (int n = 0; n < NoiseChunk.this.cellCountY + 1; n++) {
					int o = n + NoiseChunk.this.cellNoiseMinY;
					int p = o * k;
					double d = this.noiseFiller.calculateNoise(i * j, p, m * j);
					ds[l][n] = d;
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
