package net.minecraft.world.level.levelgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
	private static final ResourceKey<DensityFunction> ZERO = createKey("zero");
	private static final ResourceKey<DensityFunction> Y = createKey("y");
	private static final ResourceKey<DensityFunction> SHIFT_X = createKey("shift_x");
	private static final ResourceKey<DensityFunction> SHIFT_Z = createKey("shift_z");
	private static final ResourceKey<DensityFunction> BASE_3D_NOISE = createKey("overworld/base_3d_noise");
	private static final ResourceKey<DensityFunction> CONTINENTS = createKey("overworld/continents");
	private static final ResourceKey<DensityFunction> EROSION = createKey("overworld/erosion");
	private static final ResourceKey<DensityFunction> RIDGES = createKey("overworld/ridges");
	private static final ResourceKey<DensityFunction> FACTOR = createKey("overworld/factor");
	private static final ResourceKey<DensityFunction> DEPTH = createKey("overworld/depth");
	private static final ResourceKey<DensityFunction> SLOPED_CHEESE = createKey("overworld/sloped_cheese");
	private static final ResourceKey<DensityFunction> CONTINENTS_LARGE = createKey("overworld_large_biomes/continents");
	private static final ResourceKey<DensityFunction> EROSION_LARGE = createKey("overworld_large_biomes/erosion");
	private static final ResourceKey<DensityFunction> FACTOR_LARGE = createKey("overworld_large_biomes/factor");
	private static final ResourceKey<DensityFunction> DEPTH_LARGE = createKey("overworld_large_biomes/depth");
	private static final ResourceKey<DensityFunction> SLOPED_CHEESE_LARGE = createKey("overworld_large_biomes/sloped_cheese");
	private static final ResourceKey<DensityFunction> SLOPED_CHEESE_END = createKey("end/sloped_cheese");
	private static final ResourceKey<DensityFunction> SPAGHETTI_ROUGHNESS_FUNCTION = createKey("overworld/caves/spaghetti_roughness_function");
	private static final ResourceKey<DensityFunction> ENTRANCES = createKey("overworld/caves/entrances");
	private static final ResourceKey<DensityFunction> NOODLE = createKey("overworld/caves/noodle");
	private static final ResourceKey<DensityFunction> PILLARS = createKey("overworld/caves/pillars");
	private static final ResourceKey<DensityFunction> SPAGHETTI_2D_THICKNESS_MODULATOR = createKey("overworld/caves/spaghetti_2d_thickness_modulator");
	private static final ResourceKey<DensityFunction> SPAGHETTI_2D = createKey("overworld/caves/spaghetti_2d");

	protected static NoiseRouterWithOnlyNoises overworld(NoiseSettings noiseSettings, boolean bl) {
		return overworldWithNewCaves(noiseSettings, bl);
	}

	private static ResourceKey<DensityFunction> createKey(String string) {
		return ResourceKey.create(Registry.DENSITY_FUNCTION_REGISTRY, new ResourceLocation(string));
	}

	public static Holder<? extends DensityFunction> bootstrap() {
		register(ZERO, DensityFunctions.zero());
		int i = DimensionType.MIN_Y * 2;
		int j = DimensionType.MAX_Y * 2;
		register(Y, DensityFunctions.yClampedGradient(i, j, (double)i, (double)j));
		DensityFunction densityFunction = register(SHIFT_X, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(getNoise(Noises.SHIFT)))));
		DensityFunction densityFunction2 = register(SHIFT_Z, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(getNoise(Noises.SHIFT)))));
		register(BASE_3D_NOISE, BlendedNoise.UNSEEDED);
		DensityFunction densityFunction3 = register(
			CONTINENTS, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.CONTINENTALNESS)))
		);
		DensityFunction densityFunction4 = register(
			EROSION, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.EROSION)))
		);
		DensityFunction densityFunction5 = register(
			RIDGES, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.RIDGE)))
		);
		DensityFunction densityFunction6 = DensityFunctions.noise(getNoise(Noises.JAGGED), 1500.0, 0.0);
		DensityFunction densityFunction7 = splineWithBlending(
			densityFunction3, densityFunction4, densityFunction5, DensityFunctions.TerrainShaperSpline.SplineType.OFFSET, -0.81, 2.5, DensityFunctions.blendOffset()
		);
		DensityFunction densityFunction8 = register(
			FACTOR,
			splineWithBlending(densityFunction3, densityFunction4, densityFunction5, DensityFunctions.TerrainShaperSpline.SplineType.FACTOR, 0.0, 8.0, BLENDING_FACTOR)
		);
		DensityFunction densityFunction9 = register(DEPTH, DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5), densityFunction7));
		register(SLOPED_CHEESE, slopedCheese(densityFunction3, densityFunction4, densityFunction5, densityFunction8, densityFunction9, densityFunction6));
		DensityFunction densityFunction10 = register(
			CONTINENTS_LARGE,
			DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.CONTINENTALNESS_LARGE)))
		);
		DensityFunction densityFunction11 = register(
			EROSION_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.EROSION_LARGE)))
		);
		DensityFunction densityFunction12 = splineWithBlending(
			densityFunction10, densityFunction11, densityFunction5, DensityFunctions.TerrainShaperSpline.SplineType.OFFSET, -0.81, 2.5, DensityFunctions.blendOffset()
		);
		DensityFunction densityFunction13 = register(
			FACTOR_LARGE,
			splineWithBlending(densityFunction10, densityFunction11, densityFunction5, DensityFunctions.TerrainShaperSpline.SplineType.FACTOR, 0.0, 8.0, BLENDING_FACTOR)
		);
		DensityFunction densityFunction14 = register(DEPTH_LARGE, DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5), densityFunction12));
		register(SLOPED_CHEESE_LARGE, slopedCheese(densityFunction10, densityFunction11, densityFunction5, densityFunction13, densityFunction14, densityFunction6));
		register(SLOPED_CHEESE_END, DensityFunctions.add(DensityFunctions.endIslands(0L), getFunction(BASE_3D_NOISE)));
		register(SPAGHETTI_ROUGHNESS_FUNCTION, spaghettiRoughnessFunction());
		register(
			SPAGHETTI_2D_THICKNESS_MODULATOR, DensityFunctions.cacheOnce(DensityFunctions.mappedNoise(getNoise(Noises.SPAGHETTI_2D_THICKNESS), 2.0, 1.0, -0.6, -1.3))
		);
		register(SPAGHETTI_2D, spaghetti2D());
		register(ENTRANCES, entrances());
		register(NOODLE, noodle());
		register(PILLARS, pillars());
		return (Holder<? extends DensityFunction>)BuiltinRegistries.DENSITY_FUNCTION.holders().iterator().next();
	}

	private static DensityFunction register(ResourceKey<DensityFunction> resourceKey, DensityFunction densityFunction) {
		return new DensityFunctions.HolderHolder(BuiltinRegistries.register(BuiltinRegistries.DENSITY_FUNCTION, resourceKey, densityFunction));
	}

	private static Holder<NormalNoise.NoiseParameters> getNoise(ResourceKey<NormalNoise.NoiseParameters> resourceKey) {
		return BuiltinRegistries.NOISE.getHolderOrThrow(resourceKey);
	}

	private static DensityFunction getFunction(ResourceKey<DensityFunction> resourceKey) {
		return new DensityFunctions.HolderHolder(BuiltinRegistries.DENSITY_FUNCTION.getHolderOrThrow(resourceKey));
	}

	private static DensityFunction slopedCheese(
		DensityFunction densityFunction,
		DensityFunction densityFunction2,
		DensityFunction densityFunction3,
		DensityFunction densityFunction4,
		DensityFunction densityFunction5,
		DensityFunction densityFunction6
	) {
		DensityFunction densityFunction7 = splineWithBlending(
			densityFunction, densityFunction2, densityFunction3, DensityFunctions.TerrainShaperSpline.SplineType.JAGGEDNESS, 0.0, 1.28, BLENDING_JAGGEDNESS
		);
		DensityFunction densityFunction8 = DensityFunctions.mul(densityFunction7, densityFunction6.halfNegative());
		DensityFunction densityFunction9 = noiseGradientDensity(densityFunction4, DensityFunctions.add(densityFunction5, densityFunction8));
		return DensityFunctions.add(densityFunction9, getFunction(BASE_3D_NOISE));
	}

	private static DensityFunction spaghettiRoughnessFunction() {
		DensityFunction densityFunction = DensityFunctions.noise(getNoise(Noises.SPAGHETTI_ROUGHNESS));
		DensityFunction densityFunction2 = DensityFunctions.mappedNoise(getNoise(Noises.SPAGHETTI_ROUGHNESS_MODULATOR), 0.0, -0.1);
		return DensityFunctions.cacheOnce(DensityFunctions.mul(densityFunction2, DensityFunctions.add(densityFunction.abs(), DensityFunctions.constant(-0.4))));
	}

	private static DensityFunction entrances() {
		DensityFunction densityFunction = DensityFunctions.cacheOnce(DensityFunctions.noise(getNoise(Noises.SPAGHETTI_3D_RARITY), 2.0, 1.0));
		DensityFunction densityFunction2 = DensityFunctions.mappedNoise(getNoise(Noises.SPAGHETTI_3D_THICKNESS), -0.065, -0.088);
		DensityFunction densityFunction3 = DensityFunctions.weirdScaledSampler(
			densityFunction, getNoise(Noises.SPAGHETTI_3D_1), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1
		);
		DensityFunction densityFunction4 = DensityFunctions.weirdScaledSampler(
			densityFunction, getNoise(Noises.SPAGHETTI_3D_2), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1
		);
		DensityFunction densityFunction5 = DensityFunctions.add(DensityFunctions.max(densityFunction3, densityFunction4), densityFunction2).clamp(-1.0, 1.0);
		DensityFunction densityFunction6 = getFunction(SPAGHETTI_ROUGHNESS_FUNCTION);
		DensityFunction densityFunction7 = DensityFunctions.noise(getNoise(Noises.CAVE_ENTRANCE), 0.75, 0.5);
		DensityFunction densityFunction8 = DensityFunctions.add(
			DensityFunctions.add(densityFunction7, DensityFunctions.constant(0.37)), DensityFunctions.yClampedGradient(-10, 30, 0.3, 0.0)
		);
		return DensityFunctions.cacheOnce(DensityFunctions.min(densityFunction8, DensityFunctions.add(densityFunction6, densityFunction5)));
	}

	private static DensityFunction noodle() {
		DensityFunction densityFunction = getFunction(Y);
		int i = -64;
		int j = -60;
		int k = 320;
		DensityFunction densityFunction2 = yLimitedInterpolatable(densityFunction, DensityFunctions.noise(getNoise(Noises.NOODLE), 1.0, 1.0), -60, 320, -1);
		DensityFunction densityFunction3 = yLimitedInterpolatable(
			densityFunction, DensityFunctions.mappedNoise(getNoise(Noises.NOODLE_THICKNESS), 1.0, 1.0, -0.05, -0.1), -60, 320, 0
		);
		double d = 2.6666666666666665;
		DensityFunction densityFunction4 = yLimitedInterpolatable(
			densityFunction, DensityFunctions.noise(getNoise(Noises.NOODLE_RIDGE_A), 2.6666666666666665, 2.6666666666666665), -60, 320, 0
		);
		DensityFunction densityFunction5 = yLimitedInterpolatable(
			densityFunction, DensityFunctions.noise(getNoise(Noises.NOODLE_RIDGE_B), 2.6666666666666665, 2.6666666666666665), -60, 320, 0
		);
		DensityFunction densityFunction6 = DensityFunctions.mul(DensityFunctions.constant(1.5), DensityFunctions.max(densityFunction4.abs(), densityFunction5.abs()));
		return DensityFunctions.rangeChoice(
			densityFunction2, -1000000.0, 0.0, DensityFunctions.constant(64.0), DensityFunctions.add(densityFunction3, densityFunction6)
		);
	}

	private static DensityFunction pillars() {
		double d = 25.0;
		double e = 0.3;
		DensityFunction densityFunction = DensityFunctions.noise(getNoise(Noises.PILLAR), 25.0, 0.3);
		DensityFunction densityFunction2 = DensityFunctions.mappedNoise(getNoise(Noises.PILLAR_RARENESS), 0.0, -2.0);
		DensityFunction densityFunction3 = DensityFunctions.mappedNoise(getNoise(Noises.PILLAR_THICKNESS), 0.0, 1.1);
		DensityFunction densityFunction4 = DensityFunctions.add(DensityFunctions.mul(densityFunction, DensityFunctions.constant(2.0)), densityFunction2);
		return DensityFunctions.cacheOnce(DensityFunctions.mul(densityFunction4, densityFunction3.cube()));
	}

	private static DensityFunction spaghetti2D() {
		DensityFunction densityFunction = DensityFunctions.noise(getNoise(Noises.SPAGHETTI_2D_MODULATOR), 2.0, 1.0);
		DensityFunction densityFunction2 = DensityFunctions.weirdScaledSampler(
			densityFunction, getNoise(Noises.SPAGHETTI_2D), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE2
		);
		DensityFunction densityFunction3 = DensityFunctions.mappedNoise(getNoise(Noises.SPAGHETTI_2D_ELEVATION), 0.0, (double)Math.floorDiv(-64, 8), 8.0);
		DensityFunction densityFunction4 = getFunction(SPAGHETTI_2D_THICKNESS_MODULATOR);
		DensityFunction densityFunction5 = DensityFunctions.add(densityFunction3, DensityFunctions.yClampedGradient(-64, 320, 8.0, -40.0)).abs();
		DensityFunction densityFunction6 = DensityFunctions.add(densityFunction5, densityFunction4).cube();
		double d = 0.083;
		DensityFunction densityFunction7 = DensityFunctions.add(densityFunction2, DensityFunctions.mul(DensityFunctions.constant(0.083), densityFunction4));
		return DensityFunctions.max(densityFunction7, densityFunction6).clamp(-1.0, 1.0);
	}

	private static DensityFunction underground(DensityFunction densityFunction) {
		DensityFunction densityFunction2 = getFunction(SPAGHETTI_2D);
		DensityFunction densityFunction3 = getFunction(SPAGHETTI_ROUGHNESS_FUNCTION);
		DensityFunction densityFunction4 = DensityFunctions.noise(getNoise(Noises.CAVE_LAYER), 8.0);
		DensityFunction densityFunction5 = DensityFunctions.mul(DensityFunctions.constant(4.0), densityFunction4.square());
		DensityFunction densityFunction6 = DensityFunctions.noise(getNoise(Noises.CAVE_CHEESE), 0.6666666666666666);
		DensityFunction densityFunction7 = DensityFunctions.add(
			DensityFunctions.add(DensityFunctions.constant(0.27), densityFunction6).clamp(-1.0, 1.0),
			DensityFunctions.add(DensityFunctions.constant(1.5), DensityFunctions.mul(DensityFunctions.constant(-0.64), densityFunction)).clamp(0.0, 0.5)
		);
		DensityFunction densityFunction8 = DensityFunctions.add(densityFunction5, densityFunction7);
		DensityFunction densityFunction9 = DensityFunctions.min(
			DensityFunctions.min(densityFunction8, getFunction(ENTRANCES)), DensityFunctions.add(densityFunction2, densityFunction3)
		);
		DensityFunction densityFunction10 = getFunction(PILLARS);
		DensityFunction densityFunction11 = DensityFunctions.rangeChoice(
			densityFunction10, -1000000.0, 0.03, DensityFunctions.constant(-1000000.0), densityFunction10
		);
		return DensityFunctions.max(densityFunction9, densityFunction11);
	}

	private static DensityFunction postProcess(NoiseSettings noiseSettings, DensityFunction densityFunction) {
		DensityFunction densityFunction2 = DensityFunctions.slide(noiseSettings, densityFunction);
		DensityFunction densityFunction3 = DensityFunctions.blendDensity(densityFunction2);
		return DensityFunctions.mul(DensityFunctions.interpolated(densityFunction3), DensityFunctions.constant(0.64)).squeeze();
	}

	private static NoiseRouterWithOnlyNoises overworldWithNewCaves(NoiseSettings noiseSettings, boolean bl) {
		DensityFunction densityFunction = DensityFunctions.noise(getNoise(Noises.AQUIFER_BARRIER), 0.5);
		DensityFunction densityFunction2 = DensityFunctions.noise(getNoise(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67);
		DensityFunction densityFunction3 = DensityFunctions.noise(getNoise(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143);
		DensityFunction densityFunction4 = DensityFunctions.noise(getNoise(Noises.AQUIFER_LAVA));
		DensityFunction densityFunction5 = getFunction(SHIFT_X);
		DensityFunction densityFunction6 = getFunction(SHIFT_Z);
		DensityFunction densityFunction7 = DensityFunctions.shiftedNoise2d(
			densityFunction5, densityFunction6, 0.25, getNoise(bl ? Noises.TEMPERATURE_LARGE : Noises.TEMPERATURE)
		);
		DensityFunction densityFunction8 = DensityFunctions.shiftedNoise2d(
			densityFunction5, densityFunction6, 0.25, getNoise(bl ? Noises.VEGETATION_LARGE : Noises.VEGETATION)
		);
		DensityFunction densityFunction9 = getFunction(bl ? FACTOR_LARGE : FACTOR);
		DensityFunction densityFunction10 = getFunction(bl ? DEPTH_LARGE : DEPTH);
		DensityFunction densityFunction11 = noiseGradientDensity(DensityFunctions.cache2d(densityFunction9), densityFunction10);
		DensityFunction densityFunction12 = getFunction(bl ? SLOPED_CHEESE_LARGE : SLOPED_CHEESE);
		DensityFunction densityFunction13 = DensityFunctions.min(densityFunction12, DensityFunctions.mul(DensityFunctions.constant(5.0), getFunction(ENTRANCES)));
		DensityFunction densityFunction14 = DensityFunctions.rangeChoice(densityFunction12, -1000000.0, 1.5625, densityFunction13, underground(densityFunction12));
		DensityFunction densityFunction15 = DensityFunctions.min(postProcess(noiseSettings, densityFunction14), getFunction(NOODLE));
		DensityFunction densityFunction16 = getFunction(Y);
		int i = noiseSettings.minY();
		int j = Stream.of(OreVeinifier.VeinType.values()).mapToInt(veinType -> veinType.minY).min().orElse(i);
		int k = Stream.of(OreVeinifier.VeinType.values()).mapToInt(veinType -> veinType.maxY).max().orElse(i);
		DensityFunction densityFunction17 = yLimitedInterpolatable(densityFunction16, DensityFunctions.noise(getNoise(Noises.ORE_VEININESS), 1.5, 1.5), j, k, 0);
		float f = 4.0F;
		DensityFunction densityFunction18 = yLimitedInterpolatable(densityFunction16, DensityFunctions.noise(getNoise(Noises.ORE_VEIN_A), 4.0, 4.0), j, k, 0).abs();
		DensityFunction densityFunction19 = yLimitedInterpolatable(densityFunction16, DensityFunctions.noise(getNoise(Noises.ORE_VEIN_B), 4.0, 4.0), j, k, 0).abs();
		DensityFunction densityFunction20 = DensityFunctions.add(DensityFunctions.constant(-0.08F), DensityFunctions.max(densityFunction18, densityFunction19));
		DensityFunction densityFunction21 = DensityFunctions.noise(getNoise(Noises.ORE_GAP));
		return new NoiseRouterWithOnlyNoises(
			densityFunction,
			densityFunction2,
			densityFunction3,
			densityFunction4,
			densityFunction7,
			densityFunction8,
			getFunction(bl ? CONTINENTS_LARGE : CONTINENTS),
			getFunction(bl ? EROSION_LARGE : EROSION),
			getFunction(bl ? DEPTH_LARGE : DEPTH),
			getFunction(RIDGES),
			densityFunction11,
			densityFunction15,
			densityFunction17,
			densityFunction20,
			densityFunction21
		);
	}

	private static NoiseRouterWithOnlyNoises noNewCaves(NoiseSettings noiseSettings) {
		DensityFunction densityFunction = getFunction(SHIFT_X);
		DensityFunction densityFunction2 = getFunction(SHIFT_Z);
		DensityFunction densityFunction3 = DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.TEMPERATURE));
		DensityFunction densityFunction4 = DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.VEGETATION));
		DensityFunction densityFunction5 = noiseGradientDensity(DensityFunctions.cache2d(getFunction(FACTOR)), getFunction(DEPTH));
		DensityFunction densityFunction6 = postProcess(noiseSettings, getFunction(SLOPED_CHEESE));
		return new NoiseRouterWithOnlyNoises(
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			densityFunction3,
			densityFunction4,
			getFunction(CONTINENTS),
			getFunction(EROSION),
			getFunction(DEPTH),
			getFunction(RIDGES),
			densityFunction5,
			densityFunction6,
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero()
		);
	}

	protected static NoiseRouterWithOnlyNoises overworldWithoutCaves(NoiseSettings noiseSettings) {
		return noNewCaves(noiseSettings);
	}

	protected static NoiseRouterWithOnlyNoises nether(NoiseSettings noiseSettings) {
		return noNewCaves(noiseSettings);
	}

	protected static NoiseRouterWithOnlyNoises end(NoiseSettings noiseSettings) {
		DensityFunction densityFunction = DensityFunctions.cache2d(DensityFunctions.endIslands(0L));
		DensityFunction densityFunction2 = postProcess(noiseSettings, getFunction(SLOPED_CHEESE_END));
		return new NoiseRouterWithOnlyNoises(
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			densityFunction,
			densityFunction2,
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero()
		);
	}

	private static NormalNoise seedNoise(
		PositionalRandomFactory positionalRandomFactory, Registry<NormalNoise.NoiseParameters> registry, Holder<NormalNoise.NoiseParameters> holder
	) {
		return Noises.instantiate(positionalRandomFactory, (Holder<NormalNoise.NoiseParameters>)holder.unwrapKey().flatMap(registry::getHolder).orElse(holder));
	}

	public static NoiseRouter createNoiseRouter(
		NoiseSettings noiseSettings,
		long l,
		Registry<NormalNoise.NoiseParameters> registry,
		WorldgenRandom.Algorithm algorithm,
		NoiseRouterWithOnlyNoises noiseRouterWithOnlyNoises
	) {
		boolean bl = algorithm == WorldgenRandom.Algorithm.LEGACY;
		PositionalRandomFactory positionalRandomFactory = algorithm.newInstance(l).forkPositional();
		Map<DensityFunction, DensityFunction> map = new HashMap();
		DensityFunction.Visitor visitor = densityFunction -> {
			if (densityFunction instanceof DensityFunctions.Noise noise) {
				Holder<NormalNoise.NoiseParameters> holder = noise.noiseData();
				return new DensityFunctions.Noise(holder, seedNoise(positionalRandomFactory, registry, holder), noise.xzScale(), noise.yScale());
			} else if (densityFunction instanceof DensityFunctions.ShiftNoise shiftNoise) {
				Holder<NormalNoise.NoiseParameters> holder2 = shiftNoise.noiseData();
				NormalNoise normalNoise;
				if (bl) {
					normalNoise = NormalNoise.create(positionalRandomFactory.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0));
				} else {
					normalNoise = seedNoise(positionalRandomFactory, registry, holder2);
				}

				return shiftNoise.withNewNoise(normalNoise);
			} else if (densityFunction instanceof DensityFunctions.ShiftedNoise shiftedNoise) {
				if (bl) {
					Holder<NormalNoise.NoiseParameters> holder = shiftedNoise.noiseData();
					if (Objects.equals(holder.unwrapKey(), Optional.of(Noises.TEMPERATURE))) {
						NormalNoise normalNoise2 = NormalNoise.createLegacyNetherBiome(algorithm.newInstance(l), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
						return new DensityFunctions.ShiftedNoise(
							shiftedNoise.shiftX(), shiftedNoise.shiftY(), shiftedNoise.shiftZ(), shiftedNoise.xzScale(), shiftedNoise.yScale(), holder, normalNoise2
						);
					}

					if (Objects.equals(holder.unwrapKey(), Optional.of(Noises.VEGETATION))) {
						NormalNoise normalNoise2 = NormalNoise.createLegacyNetherBiome(algorithm.newInstance(l + 1L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
						return new DensityFunctions.ShiftedNoise(
							shiftedNoise.shiftX(), shiftedNoise.shiftY(), shiftedNoise.shiftZ(), shiftedNoise.xzScale(), shiftedNoise.yScale(), holder, normalNoise2
						);
					}
				}

				Holder<NormalNoise.NoiseParameters> holderx = shiftedNoise.noiseData();
				return new DensityFunctions.ShiftedNoise(
					shiftedNoise.shiftX(),
					shiftedNoise.shiftY(),
					shiftedNoise.shiftZ(),
					shiftedNoise.xzScale(),
					shiftedNoise.yScale(),
					holderx,
					seedNoise(positionalRandomFactory, registry, holderx)
				);
			} else if (densityFunction instanceof DensityFunctions.WeirdScaledSampler weirdScaledSampler) {
				return new DensityFunctions.WeirdScaledSampler(
					weirdScaledSampler.input(),
					weirdScaledSampler.noiseData(),
					seedNoise(positionalRandomFactory, registry, weirdScaledSampler.noiseData()),
					weirdScaledSampler.rarityValueMapper()
				);
			} else if (densityFunction instanceof BlendedNoise) {
				return bl
					? new BlendedNoise(algorithm.newInstance(l), noiseSettings.noiseSamplingSettings(), noiseSettings.getCellWidth(), noiseSettings.getCellHeight())
					: new BlendedNoise(
						positionalRandomFactory.fromHashOf(new ResourceLocation("terrain")),
						noiseSettings.noiseSamplingSettings(),
						noiseSettings.getCellWidth(),
						noiseSettings.getCellHeight()
					);
			} else if (densityFunction instanceof DensityFunctions.EndIslandDensityFunction) {
				return new DensityFunctions.EndIslandDensityFunction(l);
			} else if (densityFunction instanceof DensityFunctions.TerrainShaperSpline terrainShaperSpline) {
				TerrainShaper terrainShaper = noiseSettings.terrainShaper();
				return new DensityFunctions.TerrainShaperSpline(
					terrainShaperSpline.continentalness(),
					terrainShaperSpline.erosion(),
					terrainShaperSpline.weirdness(),
					terrainShaper,
					terrainShaperSpline.spline(),
					terrainShaperSpline.minValue(),
					terrainShaperSpline.maxValue()
				);
			} else {
				return (DensityFunction)(densityFunction instanceof DensityFunctions.Slide slide
					? new DensityFunctions.Slide(noiseSettings, slide.input())
					: densityFunction);
			}
		};
		DensityFunction.Visitor visitor2 = densityFunction -> (DensityFunction)map.computeIfAbsent(densityFunction, visitor);
		NoiseRouterWithOnlyNoises noiseRouterWithOnlyNoises2 = noiseRouterWithOnlyNoises.mapAll(visitor2);
		PositionalRandomFactory positionalRandomFactory2 = positionalRandomFactory.fromHashOf(new ResourceLocation("aquifer")).forkPositional();
		PositionalRandomFactory positionalRandomFactory3 = positionalRandomFactory.fromHashOf(new ResourceLocation("ore")).forkPositional();
		return new NoiseRouter(
			noiseRouterWithOnlyNoises2.barrierNoise(),
			noiseRouterWithOnlyNoises2.fluidLevelFloodednessNoise(),
			noiseRouterWithOnlyNoises2.fluidLevelSpreadNoise(),
			noiseRouterWithOnlyNoises2.lavaNoise(),
			positionalRandomFactory2,
			positionalRandomFactory3,
			noiseRouterWithOnlyNoises2.temperature(),
			noiseRouterWithOnlyNoises2.vegetation(),
			noiseRouterWithOnlyNoises2.continents(),
			noiseRouterWithOnlyNoises2.erosion(),
			noiseRouterWithOnlyNoises2.depth(),
			noiseRouterWithOnlyNoises2.ridges(),
			noiseRouterWithOnlyNoises2.initialDensityWithoutJaggedness(),
			noiseRouterWithOnlyNoises2.finalDensity(),
			noiseRouterWithOnlyNoises2.veinToggle(),
			noiseRouterWithOnlyNoises2.veinRidged(),
			noiseRouterWithOnlyNoises2.veinGap(),
			new OverworldBiomeBuilder().spawnTarget()
		);
	}

	private static DensityFunction splineWithBlending(
		DensityFunction densityFunction,
		DensityFunction densityFunction2,
		DensityFunction densityFunction3,
		DensityFunctions.TerrainShaperSpline.SplineType splineType,
		double d,
		double e,
		DensityFunction densityFunction4
	) {
		DensityFunction densityFunction5 = DensityFunctions.terrainShaperSpline(densityFunction, densityFunction2, densityFunction3, splineType, d, e);
		DensityFunction densityFunction6 = DensityFunctions.lerp(DensityFunctions.blendAlpha(), densityFunction4, densityFunction5);
		return DensityFunctions.flatCache(DensityFunctions.cache2d(densityFunction6));
	}

	private static DensityFunction noiseGradientDensity(DensityFunction densityFunction, DensityFunction densityFunction2) {
		DensityFunction densityFunction3 = DensityFunctions.mul(densityFunction2, densityFunction);
		return DensityFunctions.mul(DensityFunctions.constant(4.0), densityFunction3.quarterNegative());
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

	protected static final class QuantizedSpaghettiRarity {
		protected static double getSphaghettiRarity2D(double d) {
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

		protected static double getSpaghettiRarity3D(double d) {
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
