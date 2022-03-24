package net.minecraft.world.level.levelgen;

import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseRouterData {
	public static final float GLOBAL_OFFSET = -0.50375F;
	private static final float ORE_THICKNESS = 0.08F;
	private static final double VEININESS_FREQUENCY = 1.5;
	private static final double NOODLE_SPACING_AND_STRAIGHTNESS = 1.5;
	private static final double SURFACE_DENSITY_THRESHOLD = 1.5625;
	private static final double CHEESE_NOISE_TARGET = -0.703125;
	public static final int ISLAND_CHUNK_DISTANCE = 64;
	public static final long ISLAND_CHUNK_DISTANCE_SQR = 4096L;
	private static final DensityFunction BLENDING_FACTOR = DensityFunctions.constant(10.0);
	private static final DensityFunction BLENDING_JAGGEDNESS = DensityFunctions.zero();
	private static final ResourceKey<DensityFunction> ZERO = createKey("zero");
	private static final ResourceKey<DensityFunction> Y = createKey("y");
	private static final ResourceKey<DensityFunction> SHIFT_X = createKey("shift_x");
	private static final ResourceKey<DensityFunction> SHIFT_Z = createKey("shift_z");
	private static final ResourceKey<DensityFunction> BASE_3D_NOISE_OVERWORLD = createKey("overworld/base_3d_noise");
	private static final ResourceKey<DensityFunction> BASE_3D_NOISE_NETHER = createKey("nether/base_3d_noise");
	private static final ResourceKey<DensityFunction> BASE_3D_NOISE_END = createKey("end/base_3d_noise");
	public static final ResourceKey<DensityFunction> CONTINENTS = createKey("overworld/continents");
	public static final ResourceKey<DensityFunction> EROSION = createKey("overworld/erosion");
	public static final ResourceKey<DensityFunction> RIDGES = createKey("overworld/ridges");
	public static final ResourceKey<DensityFunction> RIDGES_FOLDED = createKey("overworld/ridges_folded");
	public static final ResourceKey<DensityFunction> OFFSET = createKey("overworld/offset");
	public static final ResourceKey<DensityFunction> FACTOR = createKey("overworld/factor");
	public static final ResourceKey<DensityFunction> JAGGEDNESS = createKey("overworld/jaggedness");
	public static final ResourceKey<DensityFunction> DEPTH = createKey("overworld/depth");
	private static final ResourceKey<DensityFunction> SLOPED_CHEESE = createKey("overworld/sloped_cheese");
	public static final ResourceKey<DensityFunction> CONTINENTS_LARGE = createKey("overworld_large_biomes/continents");
	public static final ResourceKey<DensityFunction> EROSION_LARGE = createKey("overworld_large_biomes/erosion");
	private static final ResourceKey<DensityFunction> OFFSET_LARGE = createKey("overworld_large_biomes/offset");
	private static final ResourceKey<DensityFunction> FACTOR_LARGE = createKey("overworld_large_biomes/factor");
	private static final ResourceKey<DensityFunction> JAGGEDNESS_LARGE = createKey("overworld_large_biomes/jaggedness");
	private static final ResourceKey<DensityFunction> DEPTH_LARGE = createKey("overworld_large_biomes/depth");
	private static final ResourceKey<DensityFunction> SLOPED_CHEESE_LARGE = createKey("overworld_large_biomes/sloped_cheese");
	private static final ResourceKey<DensityFunction> OFFSET_AMPLIFIED = createKey("overworld_amplified/offset");
	private static final ResourceKey<DensityFunction> FACTOR_AMPLIFIED = createKey("overworld_amplified/factor");
	private static final ResourceKey<DensityFunction> JAGGEDNESS_AMPLIFIED = createKey("overworld_amplified/jaggedness");
	private static final ResourceKey<DensityFunction> DEPTH_AMPLIFIED = createKey("overworld_amplified/depth");
	private static final ResourceKey<DensityFunction> SLOPED_CHEESE_AMPLIFIED = createKey("overworld_amplified/sloped_cheese");
	private static final ResourceKey<DensityFunction> SLOPED_CHEESE_END = createKey("end/sloped_cheese");
	private static final ResourceKey<DensityFunction> SPAGHETTI_ROUGHNESS_FUNCTION = createKey("overworld/caves/spaghetti_roughness_function");
	private static final ResourceKey<DensityFunction> ENTRANCES = createKey("overworld/caves/entrances");
	private static final ResourceKey<DensityFunction> NOODLE = createKey("overworld/caves/noodle");
	private static final ResourceKey<DensityFunction> PILLARS = createKey("overworld/caves/pillars");
	private static final ResourceKey<DensityFunction> SPAGHETTI_2D_THICKNESS_MODULATOR = createKey("overworld/caves/spaghetti_2d_thickness_modulator");
	private static final ResourceKey<DensityFunction> SPAGHETTI_2D = createKey("overworld/caves/spaghetti_2d");

	private static ResourceKey<DensityFunction> createKey(String string) {
		return ResourceKey.create(Registry.DENSITY_FUNCTION_REGISTRY, new ResourceLocation(string));
	}

	public static Holder<? extends DensityFunction> bootstrap() {
		register(ZERO, DensityFunctions.zero());
		int i = DimensionType.MIN_Y * 2;
		int j = DimensionType.MAX_Y * 2;
		register(Y, DensityFunctions.yClampedGradient(i, j, (double)i, (double)j));
		DensityFunction densityFunction = registerAndWrap(
			SHIFT_X, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(getNoise(Noises.SHIFT))))
		);
		DensityFunction densityFunction2 = registerAndWrap(
			SHIFT_Z, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(getNoise(Noises.SHIFT))))
		);
		register(BASE_3D_NOISE_OVERWORLD, BlendedNoise.createUnseeded(0.25, 0.125, 80.0, 160.0, 8.0));
		register(BASE_3D_NOISE_NETHER, BlendedNoise.createUnseeded(0.25, 0.375, 80.0, 60.0, 8.0));
		register(BASE_3D_NOISE_END, BlendedNoise.createUnseeded(0.25, 0.25, 80.0, 160.0, 4.0));
		Holder<DensityFunction> holder = register(
			CONTINENTS, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.CONTINENTALNESS)))
		);
		Holder<DensityFunction> holder2 = register(
			EROSION, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.EROSION)))
		);
		DensityFunction densityFunction3 = registerAndWrap(
			RIDGES, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.RIDGE)))
		);
		register(RIDGES_FOLDED, peaksAndValleys(densityFunction3));
		DensityFunction densityFunction4 = DensityFunctions.noise(getNoise(Noises.JAGGED), 1500.0, 0.0);
		registerTerrainNoises(densityFunction4, holder, holder2, OFFSET, FACTOR, JAGGEDNESS, DEPTH, SLOPED_CHEESE, false);
		Holder<DensityFunction> holder3 = register(
			CONTINENTS_LARGE,
			DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.CONTINENTALNESS_LARGE)))
		);
		Holder<DensityFunction> holder4 = register(
			EROSION_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.EROSION_LARGE)))
		);
		registerTerrainNoises(densityFunction4, holder3, holder4, OFFSET_LARGE, FACTOR_LARGE, JAGGEDNESS_LARGE, DEPTH_LARGE, SLOPED_CHEESE_LARGE, false);
		registerTerrainNoises(
			densityFunction4, holder, holder2, OFFSET_AMPLIFIED, FACTOR_AMPLIFIED, JAGGEDNESS_AMPLIFIED, DEPTH_AMPLIFIED, SLOPED_CHEESE_AMPLIFIED, true
		);
		register(SLOPED_CHEESE_END, DensityFunctions.add(DensityFunctions.endIslands(0L), getFunction(BASE_3D_NOISE_END)));
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

	private static void registerTerrainNoises(
		DensityFunction densityFunction,
		Holder<DensityFunction> holder,
		Holder<DensityFunction> holder2,
		ResourceKey<DensityFunction> resourceKey,
		ResourceKey<DensityFunction> resourceKey2,
		ResourceKey<DensityFunction> resourceKey3,
		ResourceKey<DensityFunction> resourceKey4,
		ResourceKey<DensityFunction> resourceKey5,
		boolean bl
	) {
		DensityFunctions.Spline.Coordinate coordinate = new DensityFunctions.Spline.Coordinate(holder);
		DensityFunctions.Spline.Coordinate coordinate2 = new DensityFunctions.Spline.Coordinate(holder2);
		DensityFunctions.Spline.Coordinate coordinate3 = new DensityFunctions.Spline.Coordinate(BuiltinRegistries.DENSITY_FUNCTION.getHolderOrThrow(RIDGES));
		DensityFunctions.Spline.Coordinate coordinate4 = new DensityFunctions.Spline.Coordinate(BuiltinRegistries.DENSITY_FUNCTION.getHolderOrThrow(RIDGES_FOLDED));
		DensityFunction densityFunction2 = registerAndWrap(
			resourceKey,
			splineWithBlending(
				DensityFunctions.add(
					DensityFunctions.constant(-0.50375F), DensityFunctions.spline(TerrainProvider.overworldOffset(coordinate, coordinate2, coordinate4, bl))
				),
				DensityFunctions.blendOffset()
			)
		);
		DensityFunction densityFunction3 = registerAndWrap(
			resourceKey2,
			splineWithBlending(DensityFunctions.spline(TerrainProvider.overworldFactor(coordinate, coordinate2, coordinate3, coordinate4, bl)), BLENDING_FACTOR)
		);
		DensityFunction densityFunction4 = registerAndWrap(
			resourceKey4, DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5), densityFunction2)
		);
		DensityFunction densityFunction5 = registerAndWrap(
			resourceKey3,
			splineWithBlending(DensityFunctions.spline(TerrainProvider.overworldJaggedness(coordinate, coordinate2, coordinate3, coordinate4, bl)), BLENDING_JAGGEDNESS)
		);
		DensityFunction densityFunction6 = DensityFunctions.mul(densityFunction5, densityFunction.halfNegative());
		DensityFunction densityFunction7 = noiseGradientDensity(densityFunction3, DensityFunctions.add(densityFunction4, densityFunction6));
		register(resourceKey5, DensityFunctions.add(densityFunction7, getFunction(BASE_3D_NOISE_OVERWORLD)));
	}

	private static DensityFunction registerAndWrap(ResourceKey<DensityFunction> resourceKey, DensityFunction densityFunction) {
		return new DensityFunctions.HolderHolder(BuiltinRegistries.register(BuiltinRegistries.DENSITY_FUNCTION, resourceKey, densityFunction));
	}

	private static Holder<DensityFunction> register(ResourceKey<DensityFunction> resourceKey, DensityFunction densityFunction) {
		return BuiltinRegistries.register(BuiltinRegistries.DENSITY_FUNCTION, resourceKey, densityFunction);
	}

	private static Holder<NormalNoise.NoiseParameters> getNoise(ResourceKey<NormalNoise.NoiseParameters> resourceKey) {
		return BuiltinRegistries.NOISE.getHolderOrThrow(resourceKey);
	}

	private static DensityFunction getFunction(ResourceKey<DensityFunction> resourceKey) {
		return new DensityFunctions.HolderHolder(BuiltinRegistries.DENSITY_FUNCTION.getHolderOrThrow(resourceKey));
	}

	private static DensityFunction peaksAndValleys(DensityFunction densityFunction) {
		return DensityFunctions.mul(
			DensityFunctions.add(
				DensityFunctions.add(densityFunction.abs(), DensityFunctions.constant(-0.6666666666666666)).abs(), DensityFunctions.constant(-0.3333333333333333)
			),
			DensityFunctions.constant(-3.0)
		);
	}

	public static float peaksAndValleys(float f) {
		return -(Math.abs(Math.abs(f) - 0.6666667F) - 0.33333334F) * 3.0F;
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

	private static DensityFunction postProcess(DensityFunction densityFunction) {
		DensityFunction densityFunction2 = DensityFunctions.blendDensity(densityFunction);
		return DensityFunctions.mul(DensityFunctions.interpolated(densityFunction2), DensityFunctions.constant(0.64)).squeeze();
	}

	protected static NoiseRouter overworld(boolean bl, boolean bl2) {
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
		DensityFunction densityFunction9 = getFunction(bl ? FACTOR_LARGE : (bl2 ? FACTOR_AMPLIFIED : FACTOR));
		DensityFunction densityFunction10 = getFunction(bl ? DEPTH_LARGE : (bl2 ? DEPTH_AMPLIFIED : DEPTH));
		DensityFunction densityFunction11 = noiseGradientDensity(DensityFunctions.cache2d(densityFunction9), densityFunction10);
		DensityFunction densityFunction12 = getFunction(bl ? SLOPED_CHEESE_LARGE : (bl2 ? SLOPED_CHEESE_AMPLIFIED : SLOPED_CHEESE));
		DensityFunction densityFunction13 = DensityFunctions.min(densityFunction12, DensityFunctions.mul(DensityFunctions.constant(5.0), getFunction(ENTRANCES)));
		DensityFunction densityFunction14 = DensityFunctions.rangeChoice(densityFunction12, -1000000.0, 1.5625, densityFunction13, underground(densityFunction12));
		DensityFunction densityFunction15 = DensityFunctions.min(postProcess(slideOverworld(bl2, densityFunction14)), getFunction(NOODLE));
		DensityFunction densityFunction16 = getFunction(Y);
		int i = Stream.of(OreVeinifier.VeinType.values()).mapToInt(veinType -> veinType.minY).min().orElse(-DimensionType.MIN_Y * 2);
		int j = Stream.of(OreVeinifier.VeinType.values()).mapToInt(veinType -> veinType.maxY).max().orElse(-DimensionType.MIN_Y * 2);
		DensityFunction densityFunction17 = yLimitedInterpolatable(densityFunction16, DensityFunctions.noise(getNoise(Noises.ORE_VEININESS), 1.5, 1.5), i, j, 0);
		float f = 4.0F;
		DensityFunction densityFunction18 = yLimitedInterpolatable(densityFunction16, DensityFunctions.noise(getNoise(Noises.ORE_VEIN_A), 4.0, 4.0), i, j, 0).abs();
		DensityFunction densityFunction19 = yLimitedInterpolatable(densityFunction16, DensityFunctions.noise(getNoise(Noises.ORE_VEIN_B), 4.0, 4.0), i, j, 0).abs();
		DensityFunction densityFunction20 = DensityFunctions.add(DensityFunctions.constant(-0.08F), DensityFunctions.max(densityFunction18, densityFunction19));
		DensityFunction densityFunction21 = DensityFunctions.noise(getNoise(Noises.ORE_GAP));
		return new NoiseRouter(
			densityFunction,
			densityFunction2,
			densityFunction3,
			densityFunction4,
			densityFunction7,
			densityFunction8,
			getFunction(bl ? CONTINENTS_LARGE : CONTINENTS),
			getFunction(bl ? EROSION_LARGE : EROSION),
			densityFunction10,
			getFunction(RIDGES),
			slideOverworld(bl2, DensityFunctions.add(densityFunction11, DensityFunctions.constant(-0.703125)).clamp(-64.0, 64.0)),
			densityFunction15,
			densityFunction17,
			densityFunction20,
			densityFunction21
		);
	}

	private static NoiseRouter noNewCaves(DensityFunction densityFunction) {
		DensityFunction densityFunction2 = getFunction(SHIFT_X);
		DensityFunction densityFunction3 = getFunction(SHIFT_Z);
		DensityFunction densityFunction4 = DensityFunctions.shiftedNoise2d(densityFunction2, densityFunction3, 0.25, getNoise(Noises.TEMPERATURE));
		DensityFunction densityFunction5 = DensityFunctions.shiftedNoise2d(densityFunction2, densityFunction3, 0.25, getNoise(Noises.VEGETATION));
		DensityFunction densityFunction6 = postProcess(densityFunction);
		return new NoiseRouter(
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			densityFunction4,
			densityFunction5,
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			densityFunction6,
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero()
		);
	}

	private static DensityFunction slideOverworld(boolean bl, DensityFunction densityFunction) {
		return slide(densityFunction, -64, 384, bl ? 16 : 80, bl ? 0 : 64, -0.078125, 0, 24, bl ? 0.4 : 0.1171875);
	}

	private static DensityFunction slideNetherLike(int i, int j) {
		return slide(getFunction(BASE_3D_NOISE_NETHER), i, j, 24, 0, 0.9375, -8, 24, 2.5);
	}

	private static DensityFunction slideEndLike(DensityFunction densityFunction, int i, int j) {
		return slide(densityFunction, i, j, 72, -184, -23.4375, 4, 32, -0.234375);
	}

	protected static NoiseRouter nether() {
		return noNewCaves(slideNetherLike(0, 128));
	}

	protected static NoiseRouter caves() {
		return noNewCaves(slideNetherLike(-64, 192));
	}

	protected static NoiseRouter floatingIslands() {
		return noNewCaves(slideEndLike(getFunction(BASE_3D_NOISE_END), 0, 256));
	}

	private static DensityFunction slideEnd(DensityFunction densityFunction) {
		return slideEndLike(densityFunction, 0, 128);
	}

	protected static NoiseRouter end() {
		DensityFunction densityFunction = DensityFunctions.cache2d(DensityFunctions.endIslands(0L));
		DensityFunction densityFunction2 = postProcess(slideEnd(getFunction(SLOPED_CHEESE_END)));
		return new NoiseRouter(
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			densityFunction,
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			slideEnd(DensityFunctions.add(densityFunction, DensityFunctions.constant(-0.703125))),
			densityFunction2,
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero()
		);
	}

	private static DensityFunction splineWithBlending(DensityFunction densityFunction, DensityFunction densityFunction2) {
		DensityFunction densityFunction3 = DensityFunctions.lerp(DensityFunctions.blendAlpha(), densityFunction2, densityFunction);
		return DensityFunctions.flatCache(DensityFunctions.cache2d(densityFunction3));
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

	private static DensityFunction slide(DensityFunction densityFunction, int i, int j, int k, int l, double d, int m, int n, double e) {
		DensityFunction densityFunction3 = DensityFunctions.yClampedGradient(i + j - k, i + j - l, 1.0, 0.0);
		DensityFunction densityFunction2 = DensityFunctions.lerp(densityFunction3, d, densityFunction);
		DensityFunction densityFunction4 = DensityFunctions.yClampedGradient(i + m, i + n, 0.0, 1.0);
		return DensityFunctions.lerp(densityFunction4, e, densityFunction2);
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
