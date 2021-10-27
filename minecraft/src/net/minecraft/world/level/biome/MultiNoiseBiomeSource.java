package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.TerrainInfo;
import net.minecraft.world.level.levelgen.blending.Blender;

public class MultiNoiseBiomeSource extends BiomeSource {
	public static final MapCodec<MultiNoiseBiomeSource> DIRECT_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					RecordCodecBuilder.create(
							instancex -> instancex.group(
										Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond)
									)
									.apply(instancex, Pair::of)
						)
						.listOf()
						.xmap(Climate.ParameterList::new, Climate.ParameterList::biomes)
						.fieldOf("biomes")
						.forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.parameters)
				)
				.apply(instance, MultiNoiseBiomeSource::new)
	);
	public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(MultiNoiseBiomeSource.PresetInstance.CODEC, DIRECT_CODEC)
		.<MultiNoiseBiomeSource>xmap(
			either -> either.map(MultiNoiseBiomeSource.PresetInstance::biomeSource, Function.identity()),
			multiNoiseBiomeSource -> (Either)multiNoiseBiomeSource.preset().map(Either::left).orElseGet(() -> Either.right(multiNoiseBiomeSource))
		)
		.codec();
	private final Climate.ParameterList<Biome> parameters;
	private final Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> preset;

	private MultiNoiseBiomeSource(Climate.ParameterList<Biome> parameterList) {
		this(parameterList, Optional.empty());
	}

	MultiNoiseBiomeSource(Climate.ParameterList<Biome> parameterList, Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> optional) {
		super(parameterList.biomes().stream().map(Pair::getSecond));
		this.preset = optional;
		this.parameters = parameterList;
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	public BiomeSource withSeed(long l) {
		return this;
	}

	private Optional<MultiNoiseBiomeSource.PresetInstance> preset() {
		return this.preset.map(pair -> new MultiNoiseBiomeSource.PresetInstance((MultiNoiseBiomeSource.Preset)pair.getSecond(), (Registry<Biome>)pair.getFirst()));
	}

	public boolean stable(MultiNoiseBiomeSource.Preset preset) {
		return this.preset.isPresent() && Objects.equals(((Pair)this.preset.get()).getSecond(), preset);
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
		return this.getNoiseBiome(sampler.sample(i, j, k));
	}

	@VisibleForDebug
	public Biome getNoiseBiome(Climate.TargetPoint targetPoint) {
		return this.parameters.findBiome(targetPoint, () -> net.minecraft.data.worldgen.biome.Biomes.THE_VOID);
	}

	@Override
	public void addMultinoiseDebugInfo(List<String> list, BlockPos blockPos, Climate.Sampler sampler) {
		int i = QuartPos.fromBlock(blockPos.getX());
		int j = QuartPos.fromBlock(blockPos.getY());
		int k = QuartPos.fromBlock(blockPos.getZ());
		Climate.TargetPoint targetPoint = sampler.sample(i, j, k);
		float f = Climate.unquantizeCoord(targetPoint.continentalness());
		float g = Climate.unquantizeCoord(targetPoint.erosion());
		float h = Climate.unquantizeCoord(targetPoint.temperature());
		float l = Climate.unquantizeCoord(targetPoint.humidity());
		float m = Climate.unquantizeCoord(targetPoint.weirdness());
		double d = (double)TerrainShaper.peaksAndValleys(m);
		DecimalFormat decimalFormat = new DecimalFormat("0.000");
		list.add(
			"Multinoise C: "
				+ decimalFormat.format((double)f)
				+ " E: "
				+ decimalFormat.format((double)g)
				+ " T: "
				+ decimalFormat.format((double)h)
				+ " H: "
				+ decimalFormat.format((double)l)
				+ " W: "
				+ decimalFormat.format((double)m)
		);
		OverworldBiomeBuilder overworldBiomeBuilder = new OverworldBiomeBuilder();
		list.add(
			"Biome builder PV: "
				+ OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(d)
				+ " C: "
				+ overworldBiomeBuilder.getDebugStringForContinentalness((double)f)
				+ " E: "
				+ overworldBiomeBuilder.getDebugStringForErosion((double)g)
				+ " T: "
				+ overworldBiomeBuilder.getDebugStringForTemperature((double)h)
				+ " H: "
				+ overworldBiomeBuilder.getDebugStringForHumidity((double)l)
		);
		if (sampler instanceof NoiseSampler noiseSampler) {
			TerrainInfo terrainInfo = noiseSampler.terrainInfo(blockPos.getX(), blockPos.getZ(), f, m, g, Blender.empty());
			list.add(
				"Terrain PV: "
					+ decimalFormat.format(d)
					+ " O: "
					+ decimalFormat.format(terrainInfo.offset())
					+ " F: "
					+ decimalFormat.format(terrainInfo.factor())
					+ " JA: "
					+ decimalFormat.format(terrainInfo.jaggedness())
			);
		}
	}

	public static class Preset {
		static final Map<ResourceLocation, MultiNoiseBiomeSource.Preset> BY_NAME = Maps.<ResourceLocation, MultiNoiseBiomeSource.Preset>newHashMap();
		public static final MultiNoiseBiomeSource.Preset NETHER = new MultiNoiseBiomeSource.Preset(
			new ResourceLocation("nether"),
			(preset, registry) -> new MultiNoiseBiomeSource(
					new Climate.ParameterList<>(
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
		public static final MultiNoiseBiomeSource.Preset OVERWORLD = new MultiNoiseBiomeSource.Preset(new ResourceLocation("overworld"), (preset, registry) -> {
			Builder<Pair<Climate.ParameterPoint, Supplier<Biome>>> builder = ImmutableList.builder();
			new OverworldBiomeBuilder().addBiomes(pair -> builder.add(pair.mapSecond(resourceKey -> () -> (Biome)registry.getOrThrow(resourceKey))));
			return new MultiNoiseBiomeSource(new Climate.ParameterList<>(builder.build()), Optional.of(Pair.of(registry, preset)));
		});
		final ResourceLocation name;
		private final BiFunction<MultiNoiseBiomeSource.Preset, Registry<Biome>, MultiNoiseBiomeSource> biomeSource;

		public Preset(ResourceLocation resourceLocation, BiFunction<MultiNoiseBiomeSource.Preset, Registry<Biome>, MultiNoiseBiomeSource> biFunction) {
			this.name = resourceLocation;
			this.biomeSource = biFunction;
			BY_NAME.put(resourceLocation, this);
		}

		public MultiNoiseBiomeSource biomeSource(Registry<Biome> registry) {
			return (MultiNoiseBiomeSource)this.biomeSource.apply(this, registry);
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
						RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(MultiNoiseBiomeSource.PresetInstance::biomes)
					)
					.apply(instance, instance.stable(MultiNoiseBiomeSource.PresetInstance::new))
		);
		private final MultiNoiseBiomeSource.Preset preset;
		private final Registry<Biome> biomes;

		PresetInstance(MultiNoiseBiomeSource.Preset preset, Registry<Biome> registry) {
			this.preset = preset;
			this.biomes = registry;
		}

		public MultiNoiseBiomeSource.Preset preset() {
			return this.preset;
		}

		public Registry<Biome> biomes() {
			return this.biomes;
		}

		public MultiNoiseBiomeSource biomeSource() {
			return this.preset.biomeSource(this.biomes);
		}
	}
}
