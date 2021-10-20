package net.minecraft.world.level.levelgen;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NoiseUtils;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class NoiseSampler implements Climate.Sampler {
	private static final float ORE_VEIN_RARITY = 1.0F;
	private static final float ORE_THICKNESS = 0.08F;
	private static final float VEININESS_THRESHOLD = 0.4F;
	private static final double VEININESS_FREQUENCY = 1.5;
	private static final int EDGE_ROUNDOFF_BEGIN = 20;
	private static final double MAX_EDGE_ROUNDOFF = 0.2;
	private static final float VEIN_SOLIDNESS = 0.7F;
	private static final float MIN_RICHNESS = 0.1F;
	private static final float MAX_RICHNESS = 0.3F;
	private static final float MAX_RICHNESS_THRESHOLD = 0.6F;
	private static final float CHANCE_OF_RAW_ORE_BLOCK = 0.02F;
	private static final float SKIP_ORE_IF_GAP_NOISE_IS_BELOW = -0.3F;
	private static final double NOODLE_SPACING_AND_STRAIGHTNESS = 1.5;
	private final int cellHeight;
	private final int cellCountY;
	private final NoiseSettings noiseSettings;
	private final double dimensionDensityFactor;
	private final double dimensionDensityOffset;
	private final int minCellY;
	private final TerrainShaper shaper = new TerrainShaper();
	private final boolean isNoiseCavesEnabled;
	private final NoiseChunk.InterpolatableNoise baseNoise;
	private final BlendedNoise blendedNoise;
	@Nullable
	private final SimplexNoise islandNoise;
	private final NormalNoise jaggedNoise;
	private final NormalNoise barrierNoise;
	private final NormalNoise fluidLevelFloodednessNoise;
	private final NormalNoise fluidLevelSpreadNoise;
	private final NormalNoise lavaNoise;
	private final NormalNoise layerNoiseSource;
	private final NormalNoise pillarNoiseSource;
	private final NormalNoise pillarRarenessModulator;
	private final NormalNoise pillarThicknessModulator;
	private final NormalNoise spaghetti2DNoiseSource;
	private final NormalNoise spaghetti2DElevationModulator;
	private final NormalNoise spaghetti2DRarityModulator;
	private final NormalNoise spaghetti2DThicknessModulator;
	private final NormalNoise spaghetti3DNoiseSource1;
	private final NormalNoise spaghetti3DNoiseSource2;
	private final NormalNoise spaghetti3DRarityModulator;
	private final NormalNoise spaghetti3DThicknessModulator;
	private final NormalNoise spaghettiRoughnessNoise;
	private final NormalNoise spaghettiRoughnessModulator;
	private final NormalNoise bigEntranceNoiseSource;
	private final NormalNoise cheeseNoiseSource;
	private final NormalNoise temperatureNoise;
	private final NormalNoise humidityNoise;
	private final NormalNoise continentalnessNoise;
	private final NormalNoise erosionNoise;
	private final NormalNoise weirdnessNoise;
	private final NormalNoise offsetNoise;
	private final NormalNoise gapNoise;
	private final NoiseChunk.InterpolatableNoise veininess;
	private final NoiseChunk.InterpolatableNoise veinA;
	private final NoiseChunk.InterpolatableNoise veinB;
	private final NoiseChunk.InterpolatableNoise noodleToggle;
	private final NoiseChunk.InterpolatableNoise noodleThickness;
	private final NoiseChunk.InterpolatableNoise noodleRidgeA;
	private final NoiseChunk.InterpolatableNoise noodleRidgeB;
	private final PositionalRandomFactory aquiferPositionalRandomFactory;
	private final PositionalRandomFactory oreVeinsPositionalRandomFactory;
	private final PositionalRandomFactory depthBasedLayerPositionalRandomFactory;
	private final List<Climate.ParameterPoint> spawnTarget = new OverworldBiomeBuilder().spawnTarget();

	public NoiseSampler(
		int i, int j, int k, NoiseSettings noiseSettings, boolean bl, long l, Registry<NormalNoise.NoiseParameters> registry, WorldgenRandom.Algorithm algorithm
	) {
		this.cellHeight = j;
		this.cellCountY = k;
		this.noiseSettings = noiseSettings;
		this.dimensionDensityFactor = noiseSettings.densityFactor();
		this.dimensionDensityOffset = noiseSettings.densityOffset();
		int m = noiseSettings.minY();
		this.minCellY = Mth.intFloorDiv(m, j);
		this.isNoiseCavesEnabled = bl;
		this.baseNoise = noiseChunk -> noiseChunk.createNoiseInterpolator(
				(ix, jx, kx) -> this.calculateBaseNoise(ix, jx, kx, noiseChunk.terrainInfo(QuartPos.fromBlock(ix), QuartPos.fromBlock(kx)))
			);
		if (noiseSettings.islandNoiseOverride()) {
			RandomSource randomSource = algorithm.newInstance(l);
			randomSource.consumeCount(17292);
			this.islandNoise = new SimplexNoise(randomSource);
		} else {
			this.islandNoise = null;
		}

		int n = Stream.of(NoiseSampler.VeinType.values()).mapToInt(veinType -> veinType.minY).min().orElse(m);
		int o = Stream.of(NoiseSampler.VeinType.values()).mapToInt(veinType -> veinType.maxY).max().orElse(m);
		float f = 4.0F;
		double d = 2.6666666666666665;
		int p = m + 4;
		int q = m + noiseSettings.height();
		PositionalRandomFactory positionalRandomFactory = algorithm.newInstance(l).forkPositional();
		if (algorithm != WorldgenRandom.Algorithm.LEGACY) {
			this.blendedNoise = new BlendedNoise(positionalRandomFactory.fromHashOf("terrain"), noiseSettings.noiseSamplingSettings(), i, j);
			this.temperatureNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.TEMPERATURE);
			this.humidityNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.VEGETATION);
			this.offsetNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.SHIFT);
		} else {
			RandomSource randomSource2 = algorithm.newInstance(l);
			this.blendedNoise = new BlendedNoise(algorithm.newInstance(l), noiseSettings.noiseSamplingSettings(), i, j);
			this.temperatureNoise = NormalNoise.createLegacyNetherBiome(algorithm.newInstance(l), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
			this.humidityNoise = NormalNoise.createLegacyNetherBiome(algorithm.newInstance(l + 1L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
			this.offsetNoise = NormalNoise.create(positionalRandomFactory.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0));
		}

		this.aquiferPositionalRandomFactory = positionalRandomFactory.fromHashOf("aquifer").forkPositional();
		this.oreVeinsPositionalRandomFactory = positionalRandomFactory.fromHashOf("ore").forkPositional();
		this.depthBasedLayerPositionalRandomFactory = positionalRandomFactory.fromHashOf("depth_based_layer").forkPositional();
		this.barrierNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.AQUIFER_BARRIER);
		this.fluidLevelFloodednessNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS);
		this.lavaNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.AQUIFER_LAVA);
		this.fluidLevelSpreadNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.AQUIFER_FLUID_LEVEL_SPREAD);
		this.pillarNoiseSource = Noises.instantiate(registry, positionalRandomFactory, Noises.PILLAR);
		this.pillarRarenessModulator = Noises.instantiate(registry, positionalRandomFactory, Noises.PILLAR_RARENESS);
		this.pillarThicknessModulator = Noises.instantiate(registry, positionalRandomFactory, Noises.PILLAR_THICKNESS);
		this.spaghetti2DNoiseSource = Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_2D);
		this.spaghetti2DElevationModulator = Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_2D_ELEVATION);
		this.spaghetti2DRarityModulator = Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_2D_MODULATOR);
		this.spaghetti2DThicknessModulator = Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_2D_THICKNESS);
		this.spaghetti3DNoiseSource1 = Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_3D_1);
		this.spaghetti3DNoiseSource2 = Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_3D_2);
		this.spaghetti3DRarityModulator = Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_3D_RARITY);
		this.spaghetti3DThicknessModulator = Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_3D_THICKNESS);
		this.spaghettiRoughnessNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_ROUGHNESS);
		this.spaghettiRoughnessModulator = Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_ROUGHNESS_MODULATOR);
		this.bigEntranceNoiseSource = Noises.instantiate(registry, positionalRandomFactory, Noises.CAVE_ENTRANCE);
		this.layerNoiseSource = Noises.instantiate(registry, positionalRandomFactory, Noises.CAVE_LAYER);
		this.cheeseNoiseSource = Noises.instantiate(registry, positionalRandomFactory, Noises.CAVE_CHEESE);
		this.continentalnessNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.CONTINENTALNESS);
		this.erosionNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.EROSION);
		this.weirdnessNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.RIDGE);
		this.veininess = yLimitedInterpolatableNoise(Noises.instantiate(registry, positionalRandomFactory, Noises.ORE_VEININESS), n, o, 0, 1.5);
		this.veinA = yLimitedInterpolatableNoise(Noises.instantiate(registry, positionalRandomFactory, Noises.ORE_VEIN_A), n, o, 0, 4.0);
		this.veinB = yLimitedInterpolatableNoise(Noises.instantiate(registry, positionalRandomFactory, Noises.ORE_VEIN_B), n, o, 0, 4.0);
		this.gapNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.ORE_GAP);
		this.noodleToggle = yLimitedInterpolatableNoise(Noises.instantiate(registry, positionalRandomFactory, Noises.NOODLE), p, q, -1, 1.0);
		this.noodleThickness = yLimitedInterpolatableNoise(Noises.instantiate(registry, positionalRandomFactory, Noises.NOODLE_THICKNESS), p, q, 0, 1.0);
		this.noodleRidgeA = yLimitedInterpolatableNoise(Noises.instantiate(registry, positionalRandomFactory, Noises.NOODLE_RIDGE_A), p, q, 0, 2.6666666666666665);
		this.noodleRidgeB = yLimitedInterpolatableNoise(Noises.instantiate(registry, positionalRandomFactory, Noises.NOODLE_RIDGE_B), p, q, 0, 2.6666666666666665);
		this.jaggedNoise = Noises.instantiate(registry, positionalRandomFactory, Noises.JAGGED);
	}

	private static NoiseChunk.InterpolatableNoise yLimitedInterpolatableNoise(NormalNoise normalNoise, int i, int j, int k, double d) {
		NoiseChunk.NoiseFiller noiseFiller = (l, m, n) -> m <= j && m >= i ? normalNoise.getValue((double)l * d, (double)m * d, (double)n * d) : (double)k;
		return noiseChunk -> noiseChunk.createNoiseInterpolator(noiseFiller);
	}

	private double calculateBaseNoise(int i, int j, int k, TerrainInfo terrainInfo) {
		double d = this.blendedNoise.calculateNoise(i, j, k);
		boolean bl = !this.isNoiseCavesEnabled;
		return this.calculateBaseNoise(i, j, k, terrainInfo, d, bl, true);
	}

	private double calculateBaseNoise(int i, int j, int k, TerrainInfo terrainInfo, double d, boolean bl, boolean bl2) {
		double e;
		if (this.dimensionDensityFactor == 0.0 && this.dimensionDensityOffset == -0.030078125) {
			e = 0.0;
		} else {
			double f = bl2 ? this.sampleJaggedNoise(terrainInfo.jaggedness(), (double)i, (double)k) : 0.0;
			double g = this.computeDimensionDensity((double)j);
			double h = (g + terrainInfo.offset() + f) * terrainInfo.factor();
			e = h * (double)(h > 0.0 ? 4 : 1);
		}

		double f = e + d;
		double g = 1.5625;
		double l;
		double m;
		double h;
		if (!bl && !(f < -64.0)) {
			double n = f - 1.5625;
			boolean bl3 = n < 0.0;
			double o = this.getBigEntrances(i, j, k);
			double p = this.spaghettiRoughness(i, j, k);
			double q = this.getSpaghetti3D(i, j, k);
			double r = Math.min(o, q + p);
			if (bl3) {
				h = f;
				l = r * 5.0;
				m = -64.0;
			} else {
				double s = this.getLayerizedCaverns(i, j, k);
				if (s > 64.0) {
					h = 64.0;
				} else {
					double t = this.cheeseNoiseSource.getValue((double)i, (double)j / 1.5, (double)k);
					double u = Mth.clamp(t + 0.27, -1.0, 1.0);
					double v = n * 1.28;
					double w = u + Mth.clampedLerp(0.5, 0.0, v);
					h = w + s;
				}

				double t = this.getSpaghetti2D(i, j, k);
				l = Math.min(r, t + p);
				m = this.getPillars(i, j, k);
			}
		} else {
			h = f;
			l = 64.0;
			m = -64.0;
		}

		double n = Math.max(Math.min(h, l), m);
		n = this.applySlide(n, j / this.cellHeight);
		return Mth.clamp(n, -64.0, 64.0);
	}

	private double sampleJaggedNoise(double d, double e, double f) {
		if (d == 0.0) {
			return 0.0;
		} else {
			float g = 1500.0F;
			double h = this.jaggedNoise.getValue(e * 1500.0, 0.0, f * 1500.0);
			return h > 0.0 ? d * h : d / 2.0 * h;
		}
	}

	private double computeDimensionDensity(double d) {
		double e = 1.0 - d / 128.0;
		return e * this.dimensionDensityFactor + this.dimensionDensityOffset;
	}

	private double applySlide(double d, int i) {
		int j = i - this.minCellY;
		d = this.noiseSettings.topSlideSettings().applySlide(d, this.cellCountY - j);
		return this.noiseSettings.bottomSlideSettings().applySlide(d, j);
	}

	protected NoiseChunk.BlockStateFiller makeBaseNoiseFiller(NoiseChunk noiseChunk, NoiseChunk.NoiseFiller noiseFiller, boolean bl) {
		NoiseChunk.Sampler sampler = this.baseNoise.instantiate(noiseChunk);
		NoiseChunk.Sampler sampler2 = bl ? this.noodleToggle.instantiate(noiseChunk) : () -> -1.0;
		NoiseChunk.Sampler sampler3 = bl ? this.noodleThickness.instantiate(noiseChunk) : () -> 0.0;
		NoiseChunk.Sampler sampler4 = bl ? this.noodleRidgeA.instantiate(noiseChunk) : () -> 0.0;
		NoiseChunk.Sampler sampler5 = bl ? this.noodleRidgeB.instantiate(noiseChunk) : () -> 0.0;
		return (i, j, k) -> {
			double d = sampler.sample();
			double e = Mth.clamp(d * 0.64, -1.0, 1.0);
			e = e / 2.0 - e * e * e / 24.0;
			if (sampler2.sample() >= 0.0) {
				double f = 0.05;
				double g = 0.1;
				double h = Mth.clampedMap(sampler3.sample(), -1.0, 1.0, 0.05, 0.1);
				double l = Math.abs(1.5 * sampler4.sample()) - h;
				double m = Math.abs(1.5 * sampler5.sample()) - h;
				e = Math.min(e, Math.max(l, m));
			}

			e += noiseFiller.calculateNoise(i, j, k);
			return noiseChunk.aquifer().computeSubstance(i, j, k, d, e);
		};
	}

	protected NoiseChunk.BlockStateFiller makeOreVeinifier(NoiseChunk noiseChunk, boolean bl) {
		if (!bl) {
			return (i, j, k) -> null;
		} else {
			NoiseChunk.Sampler sampler = this.veininess.instantiate(noiseChunk);
			NoiseChunk.Sampler sampler2 = this.veinA.instantiate(noiseChunk);
			NoiseChunk.Sampler sampler3 = this.veinB.instantiate(noiseChunk);
			BlockState blockState = null;
			return (i, j, k) -> {
				RandomSource randomSource = this.oreVeinsPositionalRandomFactory.at(i, j, k);
				double d = sampler.sample();
				NoiseSampler.VeinType veinType = this.getVeinType(d, j);
				if (veinType == null) {
					return blockState;
				} else if (randomSource.nextFloat() > 0.7F) {
					return blockState;
				} else if (this.isVein(sampler2.sample(), sampler3.sample())) {
					double e = Mth.clampedMap(Math.abs(d), 0.4F, 0.6F, 0.1F, 0.3F);
					if ((double)randomSource.nextFloat() < e && this.gapNoise.getValue((double)i, (double)j, (double)k) > -0.3F) {
						return randomSource.nextFloat() < 0.02F ? veinType.rawOreBlock : veinType.ore;
					} else {
						return veinType.filler;
					}
				} else {
					return blockState;
				}
			};
		}
	}

	protected int getPreliminarySurfaceLevel(int i, int j, TerrainInfo terrainInfo) {
		for (int k = this.minCellY + this.cellCountY; k >= this.minCellY; k--) {
			int l = k * this.cellHeight;
			double d = -0.703125;
			double e = this.calculateBaseNoise(i, l, j, terrainInfo, -0.703125, true, false);
			if (e > 0.390625) {
				return l;
			}
		}

		return Integer.MAX_VALUE;
	}

	protected Aquifer createAquifer(NoiseChunk noiseChunk, int i, int j, int k, int l, Aquifer.FluidPicker fluidPicker, boolean bl) {
		if (!bl) {
			return Aquifer.createDisabled(fluidPicker);
		} else {
			int m = SectionPos.blockToSectionCoord(i);
			int n = SectionPos.blockToSectionCoord(j);
			return Aquifer.create(
				noiseChunk,
				new ChunkPos(m, n),
				this.barrierNoise,
				this.fluidLevelFloodednessNoise,
				this.fluidLevelSpreadNoise,
				this.lavaNoise,
				this.aquiferPositionalRandomFactory,
				this,
				k * this.cellHeight,
				l * this.cellHeight,
				fluidPicker
			);
		}
	}

	@Override
	public Climate.TargetPoint sample(int i, int j, int k) {
		double d = (double)i + this.getOffset(i, 0, k);
		double e = (double)k + this.getOffset(k, i, 0);
		float f = (float)this.getContinentalness(d, 0.0, e);
		float g = (float)this.getErosion(d, 0.0, e);
		float h = (float)this.getWeirdness(d, 0.0, e);
		double l = (double)this.shaper.offset(this.shaper.makePoint(f, g, h));
		return this.target(i, j, k, d, e, f, g, h, l);
	}

	protected Climate.TargetPoint target(int i, int j, int k, double d, double e, float f, float g, float h, double l) {
		double m = (double)j + this.getOffset(j, k, i);
		double n = this.computeDimensionDensity((double)QuartPos.toBlock(j)) + l;
		return Climate.target((float)this.getTemperature(d, m, e), (float)this.getHumidity(d, m, e), f, g, (float)n, h);
	}

	public TerrainInfo terrainInfo(int i, int j, float f, float g, float h) {
		if (this.islandNoise != null) {
			double d = (double)(TheEndBiomeSource.getHeightValue(this.islandNoise, i / 8, j / 8) - 8.0F);
			double e;
			if (d > 0.0) {
				e = 0.001953125;
			} else {
				e = 0.0078125;
			}

			return new TerrainInfo(d, e, 0.0);
		} else {
			TerrainShaper.Point point = this.shaper.makePoint(f, h, g);
			return new TerrainInfo((double)this.shaper.offset(point), (double)this.shaper.factor(point), (double)this.shaper.jaggedness(point));
		}
	}

	@Override
	public BlockPos findSpawnPosition() {
		return Climate.findSpawnPosition(this.spawnTarget, this);
	}

	public double getOffset(int i, int j, int k) {
		return this.offsetNoise.getValue((double)i, (double)j, (double)k) * 4.0;
	}

	public double getTemperature(double d, double e, double f) {
		return this.temperatureNoise.getValue(d, 0.0, f);
	}

	public double getHumidity(double d, double e, double f) {
		return this.humidityNoise.getValue(d, 0.0, f);
	}

	public double getContinentalness(double d, double e, double f) {
		if (SharedConstants.debugGenerateSquareTerrainWithoutNoise) {
			if (SharedConstants.debugVoidTerrain((int)d * 4, (int)f * 4)) {
				return -1.0;
			} else {
				double g = Mth.frac(d / 2048.0) * 2.0 - 1.0;
				return g * g * (double)(g < 0.0 ? -1 : 1);
			}
		} else if (SharedConstants.debugGenerateStripedTerrainWithoutNoise) {
			double g = d * 0.005;
			return Math.sin(g + 0.5 * Math.sin(g));
		} else {
			return this.continentalnessNoise.getValue(d, e, f);
		}
	}

	public double getErosion(double d, double e, double f) {
		if (SharedConstants.debugGenerateSquareTerrainWithoutNoise) {
			if (SharedConstants.debugVoidTerrain((int)d * 4, (int)f * 4)) {
				return -1.0;
			} else {
				double g = Mth.frac(f / 256.0) * 2.0 - 1.0;
				return g * g * (double)(g < 0.0 ? -1 : 1);
			}
		} else if (SharedConstants.debugGenerateStripedTerrainWithoutNoise) {
			double g = f * 0.005;
			return Math.sin(g + 0.5 * Math.sin(g));
		} else {
			return this.erosionNoise.getValue(d, e, f);
		}
	}

	public double getWeirdness(double d, double e, double f) {
		return this.weirdnessNoise.getValue(d, e, f);
	}

	private double getBigEntrances(int i, int j, int k) {
		double d = 0.75;
		double e = 0.5;
		double f = 0.37;
		double g = this.bigEntranceNoiseSource.getValue((double)i * 0.75, (double)j * 0.5, (double)k * 0.75) + 0.37;
		int l = -10;
		double h = (double)(j - -10) / 40.0;
		double m = 0.3;
		return g + Mth.clampedLerp(0.3, 0.0, h);
	}

	private double getPillars(int i, int j, int k) {
		double d = 0.0;
		double e = 2.0;
		double f = NoiseUtils.sampleNoiseAndMapToRange(this.pillarRarenessModulator, (double)i, (double)j, (double)k, 0.0, 2.0);
		double g = 0.0;
		double h = 1.1;
		double l = NoiseUtils.sampleNoiseAndMapToRange(this.pillarThicknessModulator, (double)i, (double)j, (double)k, 0.0, 1.1);
		l = Math.pow(l, 3.0);
		double m = 25.0;
		double n = 0.3;
		double o = this.pillarNoiseSource.getValue((double)i * 25.0, (double)j * 0.3, (double)k * 25.0);
		o = l * (o * 2.0 - f);
		return o > 0.03 ? o : Double.NEGATIVE_INFINITY;
	}

	private double getLayerizedCaverns(int i, int j, int k) {
		double d = this.layerNoiseSource.getValue((double)i, (double)(j * 8), (double)k);
		return Mth.square(d) * 4.0;
	}

	private double getSpaghetti3D(int i, int j, int k) {
		double d = this.spaghetti3DRarityModulator.getValue((double)(i * 2), (double)j, (double)(k * 2));
		double e = NoiseSampler.QuantizedSpaghettiRarity.getSpaghettiRarity3D(d);
		double f = 0.065;
		double g = 0.088;
		double h = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti3DThicknessModulator, (double)i, (double)j, (double)k, 0.065, 0.088);
		double l = sampleWithRarity(this.spaghetti3DNoiseSource1, (double)i, (double)j, (double)k, e);
		double m = Math.abs(e * l) - h;
		double n = sampleWithRarity(this.spaghetti3DNoiseSource2, (double)i, (double)j, (double)k, e);
		double o = Math.abs(e * n) - h;
		return clampToUnit(Math.max(m, o));
	}

	private double getSpaghetti2D(int i, int j, int k) {
		double d = this.spaghetti2DRarityModulator.getValue((double)(i * 2), (double)j, (double)(k * 2));
		double e = NoiseSampler.QuantizedSpaghettiRarity.getSphaghettiRarity2D(d);
		double f = 0.6;
		double g = 1.3;
		double h = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti2DThicknessModulator, (double)(i * 2), (double)j, (double)(k * 2), 0.6, 1.3);
		double l = sampleWithRarity(this.spaghetti2DNoiseSource, (double)i, (double)j, (double)k, e);
		double m = 0.083;
		double n = Math.abs(e * l) - 0.083 * h;
		int o = this.minCellY;
		int p = 8;
		double q = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti2DElevationModulator, (double)i, 0.0, (double)k, (double)o, 8.0);
		double r = Math.abs(q - (double)j / 8.0) - 1.0 * h;
		r = r * r * r;
		return clampToUnit(Math.max(r, n));
	}

	private double spaghettiRoughness(int i, int j, int k) {
		double d = NoiseUtils.sampleNoiseAndMapToRange(this.spaghettiRoughnessModulator, (double)i, (double)j, (double)k, 0.0, 0.1);
		return (0.4 - Math.abs(this.spaghettiRoughnessNoise.getValue((double)i, (double)j, (double)k))) * d;
	}

	public PositionalRandomFactory getDepthBasedLayerPositionalRandom() {
		return this.depthBasedLayerPositionalRandomFactory;
	}

	private static double clampToUnit(double d) {
		return Mth.clamp(d, -1.0, 1.0);
	}

	private static double sampleWithRarity(NormalNoise normalNoise, double d, double e, double f, double g) {
		return normalNoise.getValue(d / g, e / g, f / g);
	}

	private boolean isVein(double d, double e) {
		double f = Math.abs(1.0 * d) - 0.08F;
		double g = Math.abs(1.0 * e) - 0.08F;
		return Math.max(f, g) < 0.0;
	}

	@Nullable
	private NoiseSampler.VeinType getVeinType(double d, int i) {
		NoiseSampler.VeinType veinType = d > 0.0 ? NoiseSampler.VeinType.COPPER : NoiseSampler.VeinType.IRON;
		int j = veinType.maxY - i;
		int k = i - veinType.minY;
		if (k >= 0 && j >= 0) {
			int l = Math.min(j, k);
			double e = Mth.clampedMap((double)l, 0.0, 20.0, -0.2, 0.0);
			return Math.abs(d) + e < 0.4F ? null : veinType;
		} else {
			return null;
		}
	}

	static final class QuantizedSpaghettiRarity {
		private QuantizedSpaghettiRarity() {
		}

		static double getSphaghettiRarity2D(double d) {
			if (d < -0.75) {
				return 0.5;
			} else if (d < -0.5) {
				return 0.75;
			} else if (d < 0.5) {
				return 1.0;
			} else {
				return d < 0.75 ? 2.0 : 3.0;
			}
		}

		static double getSpaghettiRarity3D(double d) {
			if (d < -0.5) {
				return 0.75;
			} else if (d < 0.0) {
				return 1.0;
			} else {
				return d < 0.5 ? 1.5 : 2.0;
			}
		}
	}

	static enum VeinType {
		COPPER(Blocks.COPPER_ORE.defaultBlockState(), Blocks.RAW_COPPER_BLOCK.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), 0, 50),
		IRON(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.RAW_IRON_BLOCK.defaultBlockState(), Blocks.TUFF.defaultBlockState(), -60, -8);

		final BlockState ore;
		final BlockState rawOreBlock;
		final BlockState filler;
		final int minY;
		final int maxY;

		private VeinType(BlockState blockState, BlockState blockState2, BlockState blockState3, int j, int k) {
			this.ore = blockState;
			this.rawOreBlock = blockState2;
			this.filler = blockState3;
			this.minY = j;
			this.maxY = k;
		}
	}
}
