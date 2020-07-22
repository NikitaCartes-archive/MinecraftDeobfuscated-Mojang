package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MultiNoiseBiomeSource extends BiomeSource {
	private static final MultiNoiseBiomeSource.NoiseParameters DEFAULT_NOISE_PARAMETERS = new MultiNoiseBiomeSource.NoiseParameters(-7, ImmutableList.of(1.0, 1.0));
	public static final MapCodec<MultiNoiseBiomeSource> DIRECT_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.LONG.fieldOf("seed").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.seed),
					RecordCodecBuilder.create(
							instancex -> instancex.group(
										Biome.ClimateParameters.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond)
									)
									.apply(instancex, Pair::of)
						)
						.listOf()
						.fieldOf("biomes")
						.forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.parameters),
					MultiNoiseBiomeSource.NoiseParameters.CODEC.fieldOf("temperature_noise").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.temperatureParams),
					MultiNoiseBiomeSource.NoiseParameters.CODEC.fieldOf("humidity_noise").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.humidityParams),
					MultiNoiseBiomeSource.NoiseParameters.CODEC.fieldOf("altitude_noise").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.altitudeParams),
					MultiNoiseBiomeSource.NoiseParameters.CODEC.fieldOf("weirdness_noise").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.weirdnessParams)
				)
				.apply(instance, MultiNoiseBiomeSource::new)
	);
	public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(MultiNoiseBiomeSource.Preset.CODEC, DIRECT_CODEC)
		.<MultiNoiseBiomeSource>xmap(
			either -> either.map(pair -> ((MultiNoiseBiomeSource.Preset)pair.getFirst()).biomeSource((Long)pair.getSecond()), Function.identity()),
			multiNoiseBiomeSource -> (Either)multiNoiseBiomeSource.preset
					.map(preset -> Either.left(Pair.of(preset, multiNoiseBiomeSource.seed)))
					.orElseGet(() -> Either.right(multiNoiseBiomeSource))
		)
		.codec();
	private final MultiNoiseBiomeSource.NoiseParameters temperatureParams;
	private final MultiNoiseBiomeSource.NoiseParameters humidityParams;
	private final MultiNoiseBiomeSource.NoiseParameters altitudeParams;
	private final MultiNoiseBiomeSource.NoiseParameters weirdnessParams;
	private final NormalNoise temperatureNoise;
	private final NormalNoise humidityNoise;
	private final NormalNoise altitudeNoise;
	private final NormalNoise weirdnessNoise;
	private final List<Pair<Biome.ClimateParameters, Supplier<Biome>>> parameters;
	private final boolean useY;
	private final long seed;
	private final Optional<MultiNoiseBiomeSource.Preset> preset;

	public MultiNoiseBiomeSource(long l, List<Pair<Biome.ClimateParameters, Supplier<Biome>>> list, Optional<MultiNoiseBiomeSource.Preset> optional) {
		this(l, list, DEFAULT_NOISE_PARAMETERS, DEFAULT_NOISE_PARAMETERS, DEFAULT_NOISE_PARAMETERS, DEFAULT_NOISE_PARAMETERS, optional);
	}

	public MultiNoiseBiomeSource(
		long l,
		List<Pair<Biome.ClimateParameters, Supplier<Biome>>> list,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters2,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters3,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters4
	) {
		this(l, list, noiseParameters, noiseParameters2, noiseParameters3, noiseParameters4, Optional.empty());
	}

	public MultiNoiseBiomeSource(
		long l,
		List<Pair<Biome.ClimateParameters, Supplier<Biome>>> list,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters2,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters3,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters4,
		Optional<MultiNoiseBiomeSource.Preset> optional
	) {
		super((List<Biome>)list.stream().map(Pair::getSecond).map(Supplier::get).collect(Collectors.toList()));
		this.seed = l;
		this.preset = optional;
		this.temperatureParams = noiseParameters;
		this.humidityParams = noiseParameters2;
		this.altitudeParams = noiseParameters3;
		this.weirdnessParams = noiseParameters4;
		this.temperatureNoise = NormalNoise.create(new WorldgenRandom(l), noiseParameters.firstOctave(), noiseParameters.amplitudes());
		this.humidityNoise = NormalNoise.create(new WorldgenRandom(l + 1L), noiseParameters2.firstOctave(), noiseParameters2.amplitudes());
		this.altitudeNoise = NormalNoise.create(new WorldgenRandom(l + 2L), noiseParameters3.firstOctave(), noiseParameters3.amplitudes());
		this.weirdnessNoise = NormalNoise.create(new WorldgenRandom(l + 3L), noiseParameters4.firstOctave(), noiseParameters4.amplitudes());
		this.parameters = list;
		this.useY = false;
	}

	private static MultiNoiseBiomeSource defaultNether(long l) {
		return new MultiNoiseBiomeSource(
			l,
			ImmutableList.of(
				Pair.of(new Biome.ClimateParameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F), () -> Biomes.NETHER_WASTES),
				Pair.of(new Biome.ClimateParameters(0.0F, -0.5F, 0.0F, 0.0F, 0.0F), () -> Biomes.SOUL_SAND_VALLEY),
				Pair.of(new Biome.ClimateParameters(0.4F, 0.0F, 0.0F, 0.0F, 0.0F), () -> Biomes.CRIMSON_FOREST),
				Pair.of(new Biome.ClimateParameters(0.0F, 0.5F, 0.0F, 0.0F, 0.375F), () -> Biomes.WARPED_FOREST),
				Pair.of(new Biome.ClimateParameters(-0.5F, 0.0F, 0.0F, 0.0F, 0.175F), () -> Biomes.BASALT_DELTAS)
			),
			Optional.of(MultiNoiseBiomeSource.Preset.NETHER)
		);
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public BiomeSource withSeed(long l) {
		return new MultiNoiseBiomeSource(l, this.parameters, this.temperatureParams, this.humidityParams, this.altitudeParams, this.weirdnessParams, this.preset);
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		int l = this.useY ? j : 0;
		Biome.ClimateParameters climateParameters = new Biome.ClimateParameters(
			(float)this.temperatureNoise.getValue((double)i, (double)l, (double)k),
			(float)this.humidityNoise.getValue((double)i, (double)l, (double)k),
			(float)this.altitudeNoise.getValue((double)i, (double)l, (double)k),
			(float)this.weirdnessNoise.getValue((double)i, (double)l, (double)k),
			0.0F
		);
		return (Biome)this.parameters
			.stream()
			.min(Comparator.comparing(pair -> ((Biome.ClimateParameters)pair.getFirst()).fitness(climateParameters)))
			.map(Pair::getSecond)
			.map(Supplier::get)
			.orElse(Biomes.THE_VOID);
	}

	public boolean stable(long l) {
		return this.seed == l && Objects.equals(this.preset, Optional.of(MultiNoiseBiomeSource.Preset.NETHER));
	}

	static class NoiseParameters {
		private final int firstOctave;
		private final DoubleList amplitudes;
		public static final Codec<MultiNoiseBiomeSource.NoiseParameters> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.INT.fieldOf("firstOctave").forGetter(MultiNoiseBiomeSource.NoiseParameters::firstOctave),
						Codec.DOUBLE.listOf().fieldOf("amplitudes").forGetter(MultiNoiseBiomeSource.NoiseParameters::amplitudes)
					)
					.apply(instance, MultiNoiseBiomeSource.NoiseParameters::new)
		);

		public NoiseParameters(int i, List<Double> list) {
			this.firstOctave = i;
			this.amplitudes = new DoubleArrayList(list);
		}

		public int firstOctave() {
			return this.firstOctave;
		}

		public DoubleList amplitudes() {
			return this.amplitudes;
		}
	}

	public static class Preset {
		private static final Map<ResourceLocation, MultiNoiseBiomeSource.Preset> BY_NAME = Maps.<ResourceLocation, MultiNoiseBiomeSource.Preset>newHashMap();
		public static final MapCodec<Pair<MultiNoiseBiomeSource.Preset, Long>> CODEC = Codec.mapPair(
				ResourceLocation.CODEC
					.<MultiNoiseBiomeSource.Preset>flatXmap(
						resourceLocation -> (DataResult)Optional.ofNullable(BY_NAME.get(resourceLocation))
								.map(DataResult::success)
								.orElseGet(() -> DataResult.error("Unknown preset: " + resourceLocation)),
						preset -> DataResult.success(preset.name)
					)
					.fieldOf("preset"),
				Codec.LONG.fieldOf("seed")
			)
			.stable();
		public static final MultiNoiseBiomeSource.Preset NETHER = new MultiNoiseBiomeSource.Preset(
			new ResourceLocation("nether"), l -> MultiNoiseBiomeSource.defaultNether(l)
		);
		private final ResourceLocation name;
		private final LongFunction<MultiNoiseBiomeSource> biomeSource;

		public Preset(ResourceLocation resourceLocation, LongFunction<MultiNoiseBiomeSource> longFunction) {
			this.name = resourceLocation;
			this.biomeSource = longFunction;
			BY_NAME.put(resourceLocation, this);
		}

		public MultiNoiseBiomeSource biomeSource(long l) {
			return (MultiNoiseBiomeSource)this.biomeSource.apply(l);
		}
	}
}
