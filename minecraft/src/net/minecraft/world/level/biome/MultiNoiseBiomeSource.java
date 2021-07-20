package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MultiNoiseBiomeSource extends BiomeSource {
	private static final MultiNoiseBiomeSource.NoiseParameters DEFAULT_NOISE_PARAMETERS = new MultiNoiseBiomeSource.NoiseParameters(-7, ImmutableList.of(1.0, 1.0));
	public static final MapCodec<MultiNoiseBiomeSource> DIRECT_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.LONG.fieldOf("seed").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.seed),
					RecordCodecBuilder.create(
							instancex -> instancex.group(
										Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond)
									)
									.apply(instancex, Pair::of)
						)
						.listOf()
						.xmap(Climate.ParameterList::new, Climate.ParameterList::biomes)
						.fieldOf("biomes")
						.forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.parameters),
					MultiNoiseBiomeSource.NoiseParameters.CODEC.fieldOf("temperature_noise").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.temperatureParams),
					MultiNoiseBiomeSource.NoiseParameters.CODEC.fieldOf("humidity_noise").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.humidityParams),
					MultiNoiseBiomeSource.NoiseParameters.CODEC
						.fieldOf("continentalness_noise")
						.forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.continentalnessParams),
					MultiNoiseBiomeSource.NoiseParameters.CODEC.fieldOf("erosion_noise").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.erosionParams),
					MultiNoiseBiomeSource.NoiseParameters.CODEC.fieldOf("weirdness_noise").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.weirdnessParams),
					Codec.INT.fieldOf("min_quart_y").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.minQuartY),
					Codec.INT.fieldOf("max_quart_y").forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.maxQuartY)
				)
				.apply(instance, MultiNoiseBiomeSource::new)
	);
	public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(MultiNoiseBiomeSource.PresetInstance.CODEC, DIRECT_CODEC)
		.<MultiNoiseBiomeSource>xmap(
			either -> either.map(MultiNoiseBiomeSource.PresetInstance::biomeSource, Function.identity()),
			multiNoiseBiomeSource -> (Either)multiNoiseBiomeSource.preset().map(Either::left).orElseGet(() -> Either.right(multiNoiseBiomeSource))
		)
		.codec();
	private final MultiNoiseBiomeSource.NoiseParameters temperatureParams;
	private final MultiNoiseBiomeSource.NoiseParameters humidityParams;
	private final MultiNoiseBiomeSource.NoiseParameters continentalnessParams;
	private final MultiNoiseBiomeSource.NoiseParameters erosionParams;
	private final MultiNoiseBiomeSource.NoiseParameters weirdnessParams;
	private final NormalNoise temperatureNoise;
	private final NormalNoise humidityNoise;
	private final NormalNoise continentalnessNoise;
	private final NormalNoise erosionNoise;
	private final NormalNoise weirdnessNoise;
	private final NormalNoise offsetNoise;
	private final TerrainShaper shaper = new TerrainShaper();
	private final Climate.ParameterList<Biome> parameters;
	private final boolean useY;
	private final int minQuartY;
	private final int maxQuartY;
	private final long seed;
	private final Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> preset;

	public MultiNoiseBiomeSource(long l, Climate.ParameterList parameterList, Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> optional) {
		this(
			l,
			parameterList,
			DEFAULT_NOISE_PARAMETERS,
			DEFAULT_NOISE_PARAMETERS,
			DEFAULT_NOISE_PARAMETERS,
			DEFAULT_NOISE_PARAMETERS,
			DEFAULT_NOISE_PARAMETERS,
			0,
			32,
			false,
			optional
		);
	}

	private MultiNoiseBiomeSource(
		long l,
		Climate.ParameterList parameterList,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters2,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters3,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters4,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters5,
		int i,
		int j
	) {
		this(l, parameterList, noiseParameters, noiseParameters2, noiseParameters3, noiseParameters4, noiseParameters5, i, j, false, Optional.empty());
	}

	public MultiNoiseBiomeSource(
		long l,
		Climate.ParameterList<Biome> parameterList,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters2,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters3,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters4,
		MultiNoiseBiomeSource.NoiseParameters noiseParameters5,
		int i,
		int j,
		boolean bl,
		Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> optional
	) {
		super(parameterList.biomes().stream().map(Pair::getSecond));
		this.seed = l;
		this.erosionParams = noiseParameters4;
		this.preset = optional;
		this.temperatureParams = noiseParameters;
		this.humidityParams = noiseParameters2;
		this.continentalnessParams = noiseParameters3;
		this.weirdnessParams = noiseParameters5;
		this.temperatureNoise = NormalNoise.create(new WorldgenRandom(l), noiseParameters.firstOctave(), noiseParameters.amplitudes());
		this.humidityNoise = NormalNoise.create(new WorldgenRandom(l + 1L), noiseParameters2.firstOctave(), noiseParameters2.amplitudes());
		this.continentalnessNoise = NormalNoise.create(new WorldgenRandom(l + 2L), noiseParameters3.firstOctave(), noiseParameters3.amplitudes());
		this.erosionNoise = NormalNoise.create(new WorldgenRandom(l + 3L), noiseParameters4.firstOctave(), noiseParameters4.amplitudes());
		this.weirdnessNoise = NormalNoise.create(new WorldgenRandom(l + 4L), noiseParameters5.firstOctave(), noiseParameters5.amplitudes());
		this.offsetNoise = NormalNoise.create(new WorldgenRandom(l + 5L), -3, 1.0, 1.0, 1.0, 0.0);
		this.parameters = parameterList;
		this.useY = bl;
		this.minQuartY = i;
		this.maxQuartY = j;
	}

	public static MultiNoiseBiomeSource overworld(Registry<Biome> registry, long l) {
		ImmutableList<Pair<Climate.ParameterPoint, Supplier<Biome>>> immutableList = parameters(registry);
		MultiNoiseBiomeSource.NoiseParameters noiseParameters = new MultiNoiseBiomeSource.NoiseParameters(-9, 1.0, 1.5, 0.0, 0.0, 0.0, 0.0);
		MultiNoiseBiomeSource.NoiseParameters noiseParameters2 = new MultiNoiseBiomeSource.NoiseParameters(-7, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		MultiNoiseBiomeSource.NoiseParameters noiseParameters3 = new MultiNoiseBiomeSource.NoiseParameters(-9, 1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0);
		MultiNoiseBiomeSource.NoiseParameters noiseParameters4 = new MultiNoiseBiomeSource.NoiseParameters(-9, 1.0, 1.2, 0.0, 1.0, 0.0, 0.0);
		MultiNoiseBiomeSource.NoiseParameters noiseParameters5 = new MultiNoiseBiomeSource.NoiseParameters(-7, 1.0, 2.0, 1.0, 0.0, 0.0, 0.0);
		return new MultiNoiseBiomeSource(
			l,
			new Climate.ParameterList<>(immutableList),
			noiseParameters,
			noiseParameters2,
			noiseParameters3,
			noiseParameters4,
			noiseParameters5,
			-16,
			48,
			false,
			Optional.empty()
		);
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	public BiomeSource withSeed(long l) {
		return new MultiNoiseBiomeSource(
			l,
			this.parameters,
			this.temperatureParams,
			this.humidityParams,
			this.continentalnessParams,
			this.erosionParams,
			this.weirdnessParams,
			this.minQuartY,
			this.maxQuartY,
			this.useY,
			this.preset
		);
	}

	private Optional<MultiNoiseBiomeSource.PresetInstance> preset() {
		return this.preset
			.map(pair -> new MultiNoiseBiomeSource.PresetInstance((MultiNoiseBiomeSource.Preset)pair.getSecond(), (Registry<Biome>)pair.getFirst(), this.seed));
	}

	public boolean stable(long l) {
		return this.seed == l && this.preset.isPresent() && Objects.equals(((Pair)this.preset.get()).getSecond(), MultiNoiseBiomeSource.Preset.NETHER);
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		double d = (double)i + this.getOffset(i, 0, k);
		double e = (double)j + this.getOffset(j, k, i);
		double f = (double)k + this.getOffset(k, i, 0);
		float g = (float)this.getContinentalness(d, 0.0, f);
		float h = (float)this.getErosion(d, 0.0, f);
		float l = (float)this.getWeirdness(d, 0.0, f);
		double m = (double)this.shaper.offset(this.shaper.makePoint(g, h, l));
		double n = 1.0;
		double o = -0.51875;
		double p = NoiseSampler.computeDimensionDensity(1.0, -0.51875, (double)(j * 4)) + m;
		Climate.TargetPoint targetPoint = Climate.target((float)this.getTemperature(d, e, f), (float)this.getHumidity(d, e, f), g, h, (float)p, l);
		return this.parameters.findBiome(targetPoint, () -> net.minecraft.data.worldgen.biome.Biomes.THE_VOID);
	}

	@Override
	public double[] getOffsetAndFactor(int i, int j) {
		double d = (double)i + this.getOffset(i, 0, j);
		double e = (double)j + this.getOffset(j, i, 0);
		float f = (float)this.getContinentalness(d, 0.0, e);
		float g = (float)this.getWeirdness(d, 0.0, e);
		float h = (float)this.getErosion(d, 0.0, e);
		TerrainShaper.Point point = this.shaper.makePoint(f, h, g);
		return new double[]{(double)this.shaper.offset(point), (double)this.shaper.factor(point)};
	}

	public double getOffset(int i, int j, int k) {
		return this.offsetNoise.getValue((double)i, (double)j, (double)k) * 4.0;
	}

	public double getTemperature(double d, double e, double f) {
		return this.temperatureNoise.getValue(d, e, f);
	}

	public double getHumidity(double d, double e, double f) {
		return this.humidityNoise.getValue(d, e, f);
	}

	public double getContinentalness(double d, double e, double f) {
		if (SharedConstants.DEBUG_GENERATE_SQUARE_TERRAIN_WITHOUT_NOISE) {
			return SharedConstants.debugVoidTerrain((int)d * 4, (int)f * 4) ? -1.0 : Mth.frac(d / 2048.0) * 2.0 - 1.0;
		} else {
			return this.continentalnessNoise.getValue(d, e, f);
		}
	}

	public double getErosion(double d, double e, double f) {
		if (SharedConstants.DEBUG_GENERATE_SQUARE_TERRAIN_WITHOUT_NOISE) {
			return SharedConstants.debugVoidTerrain((int)d * 4, (int)f * 4) ? -1.0 : Mth.frac(f / 256.0) * 2.0 - 1.0;
		} else {
			return this.erosionNoise.getValue(d, e, f);
		}
	}

	public double getWeirdness(double d, double e, double f) {
		return this.weirdnessNoise.getValue(d, e, f);
	}

	public static ImmutableList<Pair<Climate.ParameterPoint, Supplier<Biome>>> parameters(Registry<Biome> registry) {
		Builder<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> builder = ImmutableList.builder();
		new OverworldBiomeBuilder().addBiomes(builder);
		return (ImmutableList<Pair<Climate.ParameterPoint, Supplier<Biome>>>)builder.build()
			.stream()
			.map(pair -> pair.mapSecond(resourceKey -> () -> registry.getOrThrow(resourceKey)))
			.collect(ImmutableList.toImmutableList());
	}

	@Override
	public void addMultinoiseDebugInfo(List<String> list, BlockPos blockPos) {
		int i = QuartPos.fromBlock(blockPos.getX());
		int j = QuartPos.fromBlock(blockPos.getY());
		int k = QuartPos.fromBlock(blockPos.getZ());
		double d = this.getContinentalness((double)i, (double)j, (double)k);
		double e = this.getErosion((double)i, (double)j, (double)k);
		double f = this.getTemperature((double)i, (double)j, (double)k);
		double g = this.getHumidity((double)i, (double)j, (double)k);
		double h = this.getWeirdness((double)i, (double)j, (double)k);
		double l = (double)TerrainShaper.peaksAndValleys((float)h);
		double[] ds = this.getOffsetAndFactor(i, k);
		double m = ds[0];
		double n = ds[1];
		DecimalFormat decimalFormat = new DecimalFormat("0.000");
		list.add(
			"Multinoise C: "
				+ decimalFormat.format(d)
				+ " E: "
				+ decimalFormat.format(e)
				+ " T: "
				+ decimalFormat.format(f)
				+ " H: "
				+ decimalFormat.format(g)
				+ " W: "
				+ decimalFormat.format(h)
		);
		list.add("Terrain PV: " + decimalFormat.format(l) + " O: " + decimalFormat.format(m) + " F: " + decimalFormat.format(n));
	}

	public static class NoiseParameters {
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

		public NoiseParameters(int i, double... ds) {
			this.firstOctave = i;
			this.amplitudes = new DoubleArrayList(ds);
		}

		public int firstOctave() {
			return this.firstOctave;
		}

		public DoubleList amplitudes() {
			return this.amplitudes;
		}
	}

	public static class Preset {
		static final Map<ResourceLocation, MultiNoiseBiomeSource.Preset> BY_NAME = Maps.<ResourceLocation, MultiNoiseBiomeSource.Preset>newHashMap();
		public static final MultiNoiseBiomeSource.Preset NETHER = new MultiNoiseBiomeSource.Preset(
			new ResourceLocation("nether"),
			(preset, registry, long_) -> new MultiNoiseBiomeSource(
					long_,
					new Climate.ParameterList(
						ImmutableList.of(
							Pair.of(Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), () -> registry.getOrThrow(Biomes.NETHER_WASTES)),
							Pair.of(Climate.parameters(0.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), () -> registry.getOrThrow(Biomes.SOUL_SAND_VALLEY)),
							Pair.of(Climate.parameters(0.4F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), () -> registry.getOrThrow(Biomes.CRIMSON_FOREST)),
							Pair.of(Climate.parameters(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.375F), () -> registry.getOrThrow(Biomes.WARPED_FOREST)),
							Pair.of(Climate.parameters(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.175F), () -> registry.getOrThrow(Biomes.BASALT_DELTAS))
						)
					),
					Optional.of(Pair.of(registry, preset))
				)
		);
		public static final MultiNoiseBiomeSource.Preset OVERWORLD = new MultiNoiseBiomeSource.Preset(
			new ResourceLocation("overworld"), (preset, registry, long_) -> MultiNoiseBiomeSource.overworld(registry, long_)
		);
		final ResourceLocation name;
		private final Function3<MultiNoiseBiomeSource.Preset, Registry<Biome>, Long, MultiNoiseBiomeSource> biomeSource;

		public Preset(ResourceLocation resourceLocation, Function3<MultiNoiseBiomeSource.Preset, Registry<Biome>, Long, MultiNoiseBiomeSource> function3) {
			this.name = resourceLocation;
			this.biomeSource = function3;
			BY_NAME.put(resourceLocation, this);
		}

		public MultiNoiseBiomeSource biomeSource(Registry<Biome> registry, long l) {
			return this.biomeSource.apply(this, registry, l);
		}
	}

	static final class PresetInstance {
		public static final MapCodec<MultiNoiseBiomeSource.PresetInstance> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						ResourceLocation.CODEC
							.flatXmap(
								resourceLocation -> (DataResult)Optional.ofNullable((MultiNoiseBiomeSource.Preset)MultiNoiseBiomeSource.Preset.BY_NAME.get(resourceLocation))
										.map(DataResult::success)
										.orElseGet(() -> DataResult.error("Unknown preset: " + resourceLocation)),
								preset -> DataResult.success(preset.name)
							)
							.fieldOf("preset")
							.stable()
							.forGetter(MultiNoiseBiomeSource.PresetInstance::preset),
						RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(MultiNoiseBiomeSource.PresetInstance::biomes),
						Codec.LONG.fieldOf("seed").stable().forGetter(MultiNoiseBiomeSource.PresetInstance::seed)
					)
					.apply(instance, instance.stable(MultiNoiseBiomeSource.PresetInstance::new))
		);
		private final MultiNoiseBiomeSource.Preset preset;
		private final Registry<Biome> biomes;
		private final long seed;

		PresetInstance(MultiNoiseBiomeSource.Preset preset, Registry<Biome> registry, long l) {
			this.preset = preset;
			this.biomes = registry;
			this.seed = l;
		}

		public MultiNoiseBiomeSource.Preset preset() {
			return this.preset;
		}

		public Registry<Biome> biomes() {
			return this.biomes;
		}

		public long seed() {
			return this.seed;
		}

		public MultiNoiseBiomeSource biomeSource() {
			return this.preset.biomeSource(this.biomes, this.seed);
		}
	}
}
