package net.minecraft.world.level.levelgen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record RandomState(
	PositionalRandomFactory random,
	long legacyLevelSeed,
	Registry<NormalNoise.NoiseParameters> noises,
	NoiseRouter router,
	Climate.Sampler sampler,
	SurfaceSystem surfaceSystem,
	PositionalRandomFactory aquiferRandom,
	PositionalRandomFactory oreRandom,
	Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances,
	Map<ResourceLocation, PositionalRandomFactory> positionalRandoms
) {
	@Deprecated
	public RandomState(
		PositionalRandomFactory random,
		long legacyLevelSeed,
		Registry<NormalNoise.NoiseParameters> noises,
		NoiseRouter router,
		Climate.Sampler sampler,
		SurfaceSystem surfaceSystem,
		PositionalRandomFactory aquiferRandom,
		PositionalRandomFactory oreRandom,
		Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances,
		Map<ResourceLocation, PositionalRandomFactory> positionalRandoms
	) {
		this.random = random;
		this.legacyLevelSeed = legacyLevelSeed;
		this.noises = noises;
		this.router = router;
		this.sampler = sampler;
		this.surfaceSystem = surfaceSystem;
		this.aquiferRandom = aquiferRandom;
		this.oreRandom = oreRandom;
		this.noiseIntances = noiseIntances;
		this.positionalRandoms = positionalRandoms;
	}

	public static RandomState create(RegistryAccess registryAccess, ResourceKey<NoiseGeneratorSettings> resourceKey, long l) {
		return create(
			registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).getOrThrow(resourceKey),
			registryAccess.registryOrThrow(Registry.NOISE_REGISTRY),
			l
		);
	}

	public static RandomState create(NoiseGeneratorSettings noiseGeneratorSettings, Registry<NormalNoise.NoiseParameters> registry, long l) {
		PositionalRandomFactory positionalRandomFactory = noiseGeneratorSettings.getRandomSource().newInstance(l).forkPositional();
		NoiseRouter noiseRouter = noiseGeneratorSettings.createNoiseRouter(
			registry, new RandomWithLegacy(positionalRandomFactory, noiseGeneratorSettings.useLegacyRandomSource(), l)
		);
		Climate.Sampler sampler = new Climate.Sampler(
			noiseRouter.temperature(),
			noiseRouter.vegetation(),
			noiseRouter.continents(),
			noiseRouter.erosion(),
			noiseRouter.depth(),
			noiseRouter.ridges(),
			noiseGeneratorSettings.spawnTarget()
		);
		return new RandomState(
			positionalRandomFactory,
			l,
			registry,
			noiseRouter,
			sampler,
			new SurfaceSystem(registry, noiseGeneratorSettings.defaultBlock(), noiseGeneratorSettings.seaLevel(), positionalRandomFactory),
			positionalRandomFactory.fromHashOf(new ResourceLocation("aquifer")).forkPositional(),
			positionalRandomFactory.fromHashOf(new ResourceLocation("ore")).forkPositional(),
			new ConcurrentHashMap(),
			new ConcurrentHashMap()
		);
	}

	public NormalNoise getOrCreateNoise(ResourceKey<NormalNoise.NoiseParameters> resourceKey) {
		return (NormalNoise)this.noiseIntances.computeIfAbsent(resourceKey, resourceKey2 -> Noises.instantiate(this.noises, this.random, resourceKey));
	}

	public PositionalRandomFactory getOrCreateRandomFactory(ResourceLocation resourceLocation) {
		return (PositionalRandomFactory)this.positionalRandoms
			.computeIfAbsent(resourceLocation, resourceLocation2 -> this.random.fromHashOf(resourceLocation).forkPositional());
	}
}
