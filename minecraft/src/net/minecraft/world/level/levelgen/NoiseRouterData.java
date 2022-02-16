package net.minecraft.world.level.levelgen;

import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseRouterData {
	private static final float ORE_THICKNESS = 0.08F;
	private static final double VEININESS_FREQUENCY = 1.5;
	private static final double NOODLE_SPACING_AND_STRAIGHTNESS = 1.5;
	private static final double SURFACE_DENSITY_THRESHOLD = 1.5625;
	private static final DensityFunction BLENDING_FACTOR = DensityFunctions.constant(10.0);
	private static final DensityFunction BLENDING_JAGGEDNESS = DensityFunctions.zero();

	public static NoiseRouter createNoiseRouter(
		NoiseSettings noiseSettings, boolean bl, boolean bl2, long l, Registry<NormalNoise.NoiseParameters> registry, WorldgenRandom.Algorithm algorithm
	) {
		PositionalRandomFactory positionalRandomFactory = algorithm.newInstance(l).forkPositional();
		DensityFunction densityFunction = DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.AQUIFER_BARRIER), 0.5);
		DensityFunction densityFunction2 = DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67);
		DensityFunction densityFunction3 = DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.AQUIFER_LAVA));
		DensityFunction densityFunction4 = DensityFunctions.noise(
			Noises.instantiate(registry, positionalRandomFactory, Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143
		);
		PositionalRandomFactory positionalRandomFactory2 = positionalRandomFactory.fromHashOf(new ResourceLocation("aquifer")).forkPositional();
		PositionalRandomFactory positionalRandomFactory3 = positionalRandomFactory.fromHashOf(new ResourceLocation("ore")).forkPositional();
		double d = 25.0;
		double e = 0.3;
		DensityFunction densityFunction5 = DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.PILLAR), 25.0, 0.3);
		DensityFunction densityFunction6 = DensityFunctions.mappedNoise(Noises.instantiate(registry, positionalRandomFactory, Noises.PILLAR_RARENESS), 0.0, -2.0);
		DensityFunction densityFunction7 = DensityFunctions.mappedNoise(Noises.instantiate(registry, positionalRandomFactory, Noises.PILLAR_THICKNESS), 0.0, 1.1);
		DensityFunction densityFunction8 = DensityFunctions.add(DensityFunctions.mul(densityFunction5, DensityFunctions.constant(2.0)), densityFunction6);
		DensityFunction densityFunction9 = DensityFunctions.cacheOnce(DensityFunctions.mul(densityFunction8, densityFunction7.cube()));
		DensityFunction densityFunction10 = DensityFunctions.rangeChoice(
			densityFunction9, Double.NEGATIVE_INFINITY, 0.03, DensityFunctions.constant(Double.NEGATIVE_INFINITY), densityFunction9
		);
		DensityFunction densityFunction11 = DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_2D_MODULATOR), 2.0, 1.0);
		DensityFunction densityFunction12 = DensityFunctions.weirdScaledSampler(
			densityFunction11,
			Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_2D),
			NoiseRouterData.QuantizedSpaghettiRarity::getSphaghettiRarity2D,
			3.0
		);
		DensityFunction densityFunction13 = DensityFunctions.mappedNoise(
			Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_2D_ELEVATION), 0.0, (double)noiseSettings.getMinCellY(), 8.0
		);
		DensityFunction densityFunction14 = DensityFunctions.cacheOnce(
			DensityFunctions.mappedNoise(Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_2D_THICKNESS), 2.0, 1.0, -0.6, -1.3)
		);
		DensityFunction densityFunction15 = DensityFunctions.add(densityFunction13, DensityFunctions.yClampedGradient(-64, 320, 8.0, -40.0)).abs();
		DensityFunction densityFunction16 = DensityFunctions.add(densityFunction15, densityFunction14).cube();
		double f = 0.083;
		DensityFunction densityFunction17 = DensityFunctions.add(densityFunction12, DensityFunctions.mul(DensityFunctions.constant(0.083), densityFunction14));
		DensityFunction densityFunction18 = DensityFunctions.max(densityFunction17, densityFunction16).clamp(-1.0, 1.0);
		DensityFunction densityFunction19 = DensityFunctions.cacheOnce(
			DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_3D_RARITY), 2.0, 1.0)
		);
		DensityFunction densityFunction20 = DensityFunctions.mappedNoise(
			Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_3D_THICKNESS), -0.065, -0.088
		);
		DensityFunction densityFunction21 = DensityFunctions.weirdScaledSampler(
			densityFunction19,
			Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_3D_1),
			NoiseRouterData.QuantizedSpaghettiRarity::getSpaghettiRarity3D,
			2.0
		);
		DensityFunction densityFunction22 = DensityFunctions.weirdScaledSampler(
			densityFunction19,
			Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_3D_2),
			NoiseRouterData.QuantizedSpaghettiRarity::getSpaghettiRarity3D,
			2.0
		);
		DensityFunction densityFunction23 = DensityFunctions.add(DensityFunctions.max(densityFunction21, densityFunction22), densityFunction20).clamp(-1.0, 1.0);
		DensityFunction densityFunction24 = DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_ROUGHNESS));
		DensityFunction densityFunction25 = DensityFunctions.mappedNoise(
			Noises.instantiate(registry, positionalRandomFactory, Noises.SPAGHETTI_ROUGHNESS_MODULATOR), 0.0, -0.1
		);
		DensityFunction densityFunction26 = DensityFunctions.cacheOnce(
			DensityFunctions.mul(densityFunction25, DensityFunctions.add(densityFunction24.abs(), DensityFunctions.constant(-0.4)))
		);
		DensityFunction densityFunction27 = DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.CAVE_ENTRANCE), 0.75, 0.5);
		DensityFunction densityFunction28 = DensityFunctions.add(
			DensityFunctions.add(densityFunction27, DensityFunctions.constant(0.37)), DensityFunctions.yClampedGradient(-10, 30, 0.3, 0.0)
		);
		DensityFunction densityFunction29 = DensityFunctions.cacheOnce(
			DensityFunctions.min(densityFunction28, DensityFunctions.add(densityFunction26, densityFunction23))
		);
		DensityFunction densityFunction30 = DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.CAVE_LAYER), 8.0);
		DensityFunction densityFunction31 = DensityFunctions.mul(DensityFunctions.constant(4.0), densityFunction30.square());
		DensityFunction densityFunction32 = DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.CAVE_CHEESE), 0.6666666666666666);
		int i = DimensionType.MIN_Y * 2;
		int j = DimensionType.MAX_Y * 2;
		DensityFunction densityFunction33 = DensityFunctions.yClampedGradient(i, j, (double)i, (double)j);
		int k = noiseSettings.minY();
		int m = Stream.of(OreVeinifier.VeinType.values()).mapToInt(veinType -> veinType.minY).min().orElse(k);
		int n = Stream.of(OreVeinifier.VeinType.values()).mapToInt(veinType -> veinType.maxY).max().orElse(k);
		DensityFunction densityFunction34 = yLimitedInterpolatable(
			densityFunction33, DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.ORE_VEININESS), 1.5, 1.5), m, n, 0
		);
		float g = 4.0F;
		DensityFunction densityFunction35 = yLimitedInterpolatable(
				densityFunction33, DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.ORE_VEIN_A), 4.0, 4.0), m, n, 0
			)
			.abs();
		DensityFunction densityFunction36 = yLimitedInterpolatable(
				densityFunction33, DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.ORE_VEIN_B), 4.0, 4.0), m, n, 0
			)
			.abs();
		DensityFunction densityFunction37 = DensityFunctions.add(DensityFunctions.constant(-0.08F), DensityFunctions.max(densityFunction35, densityFunction36));
		DensityFunction densityFunction38 = DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.ORE_GAP));
		int o = k + 4;
		int p = k + noiseSettings.height();
		DensityFunction densityFunction44;
		if (bl2) {
			DensityFunction densityFunction39 = yLimitedInterpolatable(
				densityFunction33, DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.NOODLE), 1.0, 1.0), o, p, -1
			);
			DensityFunction densityFunction40 = yLimitedInterpolatable(
				densityFunction33,
				DensityFunctions.mappedNoise(Noises.instantiate(registry, positionalRandomFactory, Noises.NOODLE_THICKNESS), 1.0, 1.0, -0.05, -0.1),
				o,
				p,
				0
			);
			double h = 2.6666666666666665;
			DensityFunction densityFunction41 = yLimitedInterpolatable(
				densityFunction33,
				DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.NOODLE_RIDGE_A), 2.6666666666666665, 2.6666666666666665),
				o,
				p,
				0
			);
			DensityFunction densityFunction42 = yLimitedInterpolatable(
				densityFunction33,
				DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.NOODLE_RIDGE_B), 2.6666666666666665, 2.6666666666666665),
				o,
				p,
				0
			);
			DensityFunction densityFunction43 = DensityFunctions.mul(
				DensityFunctions.constant(1.5), DensityFunctions.max(densityFunction41.abs(), densityFunction42.abs())
			);
			densityFunction44 = DensityFunctions.rangeChoice(
				densityFunction39, Double.NEGATIVE_INFINITY, 0.0, DensityFunctions.constant(64.0), DensityFunctions.add(densityFunction40, densityFunction43)
			);
		} else {
			densityFunction44 = DensityFunctions.constant(64.0);
		}

		boolean bl3 = noiseSettings.largeBiomes();
		NormalNoise normalNoise3;
		DensityFunction densityFunction39;
		NormalNoise normalNoise;
		NormalNoise normalNoise2;
		if (algorithm != WorldgenRandom.Algorithm.LEGACY) {
			densityFunction39 = new BlendedNoise(
				positionalRandomFactory.fromHashOf(new ResourceLocation("terrain")),
				noiseSettings.noiseSamplingSettings(),
				noiseSettings.getCellWidth(),
				noiseSettings.getCellHeight()
			);
			normalNoise = Noises.instantiate(registry, positionalRandomFactory, bl3 ? Noises.TEMPERATURE_LARGE : Noises.TEMPERATURE);
			normalNoise2 = Noises.instantiate(registry, positionalRandomFactory, bl3 ? Noises.VEGETATION_LARGE : Noises.VEGETATION);
			normalNoise3 = Noises.instantiate(registry, positionalRandomFactory, Noises.SHIFT);
		} else {
			densityFunction39 = new BlendedNoise(
				algorithm.newInstance(l), noiseSettings.noiseSamplingSettings(), noiseSettings.getCellWidth(), noiseSettings.getCellHeight()
			);
			normalNoise = NormalNoise.createLegacyNetherBiome(algorithm.newInstance(l), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
			normalNoise2 = NormalNoise.createLegacyNetherBiome(algorithm.newInstance(l + 1L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
			normalNoise3 = NormalNoise.create(positionalRandomFactory.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0));
		}

		DensityFunction densityFunction42 = DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(normalNoise3)));
		DensityFunction densityFunction43 = DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(normalNoise3)));
		DensityFunction densityFunction45 = DensityFunctions.shiftedNoise2d(densityFunction42, densityFunction43, 0.25, normalNoise);
		DensityFunction densityFunction46 = DensityFunctions.shiftedNoise2d(densityFunction42, densityFunction43, 0.25, normalNoise2);
		DensityFunction densityFunction47 = DensityFunctions.flatCache(
			DensityFunctions.shiftedNoise2d(
				densityFunction42,
				densityFunction43,
				0.25,
				Noises.instantiate(registry, positionalRandomFactory, bl3 ? Noises.CONTINENTALNESS_LARGE : Noises.CONTINENTALNESS)
			)
		);
		DensityFunction densityFunction48 = DensityFunctions.flatCache(
			DensityFunctions.shiftedNoise2d(
				densityFunction42, densityFunction43, 0.25, Noises.instantiate(registry, positionalRandomFactory, bl3 ? Noises.EROSION_LARGE : Noises.EROSION)
			)
		);
		DensityFunction densityFunction49 = DensityFunctions.flatCache(
			DensityFunctions.shiftedNoise2d(densityFunction42, densityFunction43, 0.25, Noises.instantiate(registry, positionalRandomFactory, Noises.RIDGE))
		);
		TerrainShaper terrainShaper = noiseSettings.terrainShaper();
		DensityFunction densityFunction50 = splineWithBlending(
			densityFunction47, densityFunction48, densityFunction49, terrainShaper::offset, -0.81, 2.5, DensityFunctions.blendOffset()
		);
		DensityFunction densityFunction51 = splineWithBlending(
			densityFunction47, densityFunction48, densityFunction49, terrainShaper::factor, 0.0, 8.0, BLENDING_FACTOR
		);
		DensityFunction densityFunction52 = splineWithBlending(
			densityFunction47, densityFunction48, densityFunction49, terrainShaper::jaggedness, 0.0, 1.28, BLENDING_JAGGEDNESS
		);
		DensityFunction densityFunction53 = DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5), densityFunction50);
		DensityFunction densityFunction54 = DensityFunctions.noise(Noises.instantiate(registry, positionalRandomFactory, Noises.JAGGED), 1500.0, 0.0);
		DensityFunction densityFunction55 = DensityFunctions.mul(densityFunction52, densityFunction54.halfNegative());
		DensityFunction densityFunction56;
		DensityFunction densityFunction57;
		if (noiseSettings.islandNoiseOverride()) {
			densityFunction56 = DensityFunctions.endIslands(l);
			densityFunction57 = DensityFunctions.cache2d(densityFunction56);
		} else {
			densityFunction56 = noiseGradientDensity(densityFunction53, densityFunction51, densityFunction55);
			densityFunction57 = noiseGradientDensity(densityFunction53, DensityFunctions.cache2d(densityFunction51), DensityFunctions.zero());
		}

		boolean bl4 = !bl;
		DensityFunction densityFunction58 = DensityFunctions.cacheOnce(DensityFunctions.add(densityFunction56, densityFunction39));
		DensityFunction densityFunction59 = DensityFunctions.add(
			DensityFunctions.add(DensityFunctions.constant(0.27), densityFunction32).clamp(-1.0, 1.0),
			DensityFunctions.add(DensityFunctions.constant(1.5), DensityFunctions.mul(DensityFunctions.constant(-0.64), densityFunction58)).clamp(0.0, 0.5)
		);
		DensityFunction densityFunction60 = DensityFunctions.add(densityFunction31, densityFunction59);
		DensityFunction densityFunction61 = DensityFunctions.min(
			DensityFunctions.min(densityFunction60, densityFunction29), DensityFunctions.add(densityFunction18, densityFunction26)
		);
		DensityFunction densityFunction62 = DensityFunctions.max(densityFunction61, densityFunction10);
		DensityFunction densityFunction63;
		if (bl4) {
			densityFunction63 = densityFunction58;
		} else {
			DensityFunction densityFunction64 = DensityFunctions.min(densityFunction58, DensityFunctions.mul(DensityFunctions.constant(5.0), densityFunction29));
			densityFunction63 = DensityFunctions.rangeChoice(densityFunction58, Double.NEGATIVE_INFINITY, 1.5625, densityFunction64, densityFunction62);
		}

		DensityFunction densityFunction64 = DensityFunctions.slide(noiseSettings, densityFunction63);
		DensityFunction densityFunction65 = DensityFunctions.interpolated(DensityFunctions.blendDensity(densityFunction64));
		DensityFunction densityFunction66 = DensityFunctions.mul(densityFunction65, DensityFunctions.constant(0.64));
		DensityFunction densityFunction67 = DensityFunctions.min(densityFunction66.squeeze(), densityFunction44);
		return new NoiseRouter(
			densityFunction,
			densityFunction2,
			densityFunction4,
			densityFunction3,
			positionalRandomFactory2,
			positionalRandomFactory3,
			densityFunction45,
			densityFunction46,
			densityFunction47,
			densityFunction48,
			densityFunction53,
			densityFunction49,
			densityFunction57,
			densityFunction67,
			densityFunction34,
			densityFunction37,
			densityFunction38,
			new OverworldBiomeBuilder().spawnTarget()
		);
	}

	private static DensityFunction splineWithBlending(
		DensityFunction densityFunction,
		DensityFunction densityFunction2,
		DensityFunction densityFunction3,
		ToFloatFunction<TerrainShaper.Point> toFloatFunction,
		double d,
		double e,
		DensityFunction densityFunction4
	) {
		DensityFunction densityFunction5 = DensityFunctions.terrainShaperSpline(densityFunction, densityFunction2, densityFunction3, toFloatFunction, d, e);
		DensityFunction densityFunction6 = DensityFunctions.lerp(DensityFunctions.blendAlpha(), densityFunction4, densityFunction5);
		return DensityFunctions.flatCache(DensityFunctions.cache2d(densityFunction6));
	}

	private static DensityFunction noiseGradientDensity(DensityFunction densityFunction, DensityFunction densityFunction2, DensityFunction densityFunction3) {
		DensityFunction densityFunction4 = DensityFunctions.mul(DensityFunctions.add(densityFunction, densityFunction3), densityFunction2);
		return DensityFunctions.mul(DensityFunctions.constant(4.0), densityFunction4.quarterNegative());
	}

	private static DensityFunction yLimitedInterpolatable(DensityFunction densityFunction, DensityFunction densityFunction2, int i, int j, int k) {
		return DensityFunctions.interpolated(
			DensityFunctions.rangeChoice(densityFunction, (double)i, (double)(j + 1), densityFunction2, DensityFunctions.constant((double)k))
		);
	}

	protected static double applySlide(NoiseSettings noiseSettings, double d, double e) {
		double f = (double)((int)e / noiseSettings.getCellHeight() - noiseSettings.getMinCellY());
		d = noiseSettings.topSlideSettings().applySlide(d, (double)noiseSettings.getCellCountY() - f);
		return noiseSettings.bottomSlideSettings().applySlide(d, f);
	}

	protected static double computePreliminarySurfaceLevelScanning(NoiseSettings noiseSettings, DensityFunction densityFunction, int i, int j) {
		for (int k = noiseSettings.getMinCellY() + noiseSettings.getCellCountY(); k >= noiseSettings.getMinCellY(); k--) {
			int l = k * noiseSettings.getCellHeight();
			double d = -0.703125;
			double e = densityFunction.compute(new DensityFunction.SinglePointContext(i, l, j)) + -0.703125;
			double f = Mth.clamp(e, -64.0, 64.0);
			f = applySlide(noiseSettings, f, (double)l);
			if (f > 0.390625) {
				return (double)l;
			}
		}

		return 2.147483647E9;
	}

	static final class QuantizedSpaghettiRarity {
		private QuantizedSpaghettiRarity() {
		}

		private static double getSphaghettiRarity2D(double d) {
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

		private static double getSpaghettiRarity3D(double d) {
			if (d < -0.5) {
				return 0.75;
			} else if (d < 0.0) {
				return 1.0;
			} else {
				return d < 0.5 ? 1.5 : 2.0;
			}
		}
	}
}
