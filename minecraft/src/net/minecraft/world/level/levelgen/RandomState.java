package net.minecraft.world.level.levelgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class RandomState {
	final PositionalRandomFactory random;
	private final long legacyLevelSeed;
	private final Registry<NormalNoise.NoiseParameters> noises;
	private final NoiseRouter router;
	private final Climate.Sampler sampler;
	private final SurfaceSystem surfaceSystem;
	private final PositionalRandomFactory aquiferRandom;
	private final PositionalRandomFactory oreRandom;
	private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances;
	private final Map<ResourceLocation, PositionalRandomFactory> positionalRandoms;

	public static RandomState create(RegistryAccess registryAccess, ResourceKey<NoiseGeneratorSettings> resourceKey, long l) {
		return create(
			registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).getOrThrow(resourceKey),
			registryAccess.registryOrThrow(Registry.NOISE_REGISTRY),
			l
		);
	}

	public static RandomState create(NoiseGeneratorSettings noiseGeneratorSettings, Registry<NormalNoise.NoiseParameters> registry, long l) {
		return new RandomState(noiseGeneratorSettings, registry, l);
	}

	private RandomState(NoiseGeneratorSettings noiseGeneratorSettings, Registry<NormalNoise.NoiseParameters> registry, long l) {
		this.random = noiseGeneratorSettings.getRandomSource().newInstance(l).forkPositional();
		this.legacyLevelSeed = l;
		this.noises = registry;
		this.aquiferRandom = this.random.fromHashOf(new ResourceLocation("aquifer")).forkPositional();
		this.oreRandom = this.random.fromHashOf(new ResourceLocation("ore")).forkPositional();
		this.noiseIntances = new ConcurrentHashMap();
		this.positionalRandoms = new ConcurrentHashMap();
		this.surfaceSystem = new SurfaceSystem(this, noiseGeneratorSettings.defaultBlock(), noiseGeneratorSettings.seaLevel(), this.random);
		final boolean bl = noiseGeneratorSettings.useLegacyRandomSource();

		class NoiseWiringHelper implements DensityFunction.Visitor {
			private final Map<DensityFunction, DensityFunction> wrapped = new HashMap();

			private RandomSource newLegacyInstance(long l) {
				return new LegacyRandomSource(l + l);
			}

			@Override
			public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
				Holder<NormalNoise.NoiseParameters> holder = noiseHolder.noiseData();
				if (bl) {
					if (Objects.equals(holder.unwrapKey(), Optional.of(Noises.TEMPERATURE))) {
						NormalNoise normalNoise = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(0L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
						return new DensityFunction.NoiseHolder(holder, normalNoise);
					}

					if (Objects.equals(holder.unwrapKey(), Optional.of(Noises.VEGETATION))) {
						NormalNoise normalNoise = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(1L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
						return new DensityFunction.NoiseHolder(holder, normalNoise);
					}

					if (Objects.equals(holder.unwrapKey(), Optional.of(Noises.SHIFT))) {
						NormalNoise normalNoise = NormalNoise.create(RandomState.this.random.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0));
						return new DensityFunction.NoiseHolder(holder, normalNoise);
					}
				}

				NormalNoise normalNoise = RandomState.this.getOrCreateNoise((ResourceKey<NormalNoise.NoiseParameters>)holder.unwrapKey().orElseThrow());
				return new DensityFunction.NoiseHolder(holder, normalNoise);
			}

			private DensityFunction wrapNew(DensityFunction densityFunction) {
				if (densityFunction instanceof BlendedNoise blendedNoise) {
					RandomSource randomSource = bl ? this.newLegacyInstance(0L) : RandomState.this.random.fromHashOf(new ResourceLocation("terrain"));
					return blendedNoise.withNewRandom(randomSource);
				} else {
					return (DensityFunction)(densityFunction instanceof DensityFunctions.EndIslandDensityFunction
						? new DensityFunctions.EndIslandDensityFunction(l)
						: densityFunction);
				}
			}

			@Override
			public DensityFunction apply(DensityFunction densityFunction) {
				return (DensityFunction)this.wrapped.computeIfAbsent(densityFunction, this::wrapNew);
			}
		}

		this.router = noiseGeneratorSettings.noiseRouter().mapAll(new NoiseWiringHelper());
		this.sampler = new Climate.Sampler(
			this.router.temperature(),
			this.router.vegetation(),
			this.router.continents(),
			this.router.erosion(),
			this.router.depth(),
			this.router.ridges(),
			noiseGeneratorSettings.spawnTarget()
		);
	}

	public NormalNoise getOrCreateNoise(ResourceKey<NormalNoise.NoiseParameters> resourceKey) {
		return (NormalNoise)this.noiseIntances.computeIfAbsent(resourceKey, resourceKey2 -> Noises.instantiate(this.noises, this.random, resourceKey));
	}

	public PositionalRandomFactory getOrCreateRandomFactory(ResourceLocation resourceLocation) {
		return (PositionalRandomFactory)this.positionalRandoms
			.computeIfAbsent(resourceLocation, resourceLocation2 -> this.random.fromHashOf(resourceLocation).forkPositional());
	}

	public long legacyLevelSeed() {
		return this.legacyLevelSeed;
	}

	public NoiseRouter router() {
		return this.router;
	}

	public Climate.Sampler sampler() {
		return this.sampler;
	}

	public SurfaceSystem surfaceSystem() {
		return this.surfaceSystem;
	}

	public PositionalRandomFactory aquiferRandom() {
		return this.aquiferRandom;
	}

	public PositionalRandomFactory oreRandom() {
		return this.oreRandom;
	}
}
