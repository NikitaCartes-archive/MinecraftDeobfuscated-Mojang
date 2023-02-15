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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class MultiNoiseBiomeSource extends BiomeSource {
	public static final MapCodec<MultiNoiseBiomeSource> DIRECT_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ExtraCodecs.nonEmptyList(
							RecordCodecBuilder.create(
									instancex -> instancex.group(
												Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond)
											)
											.apply(instancex, Pair::of)
								)
								.listOf()
						)
						.xmap(Climate.ParameterList::new, Climate.ParameterList::values)
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
	private final Climate.ParameterList<Holder<Biome>> parameters;
	private final Optional<MultiNoiseBiomeSource.PresetInstance> preset;

	private MultiNoiseBiomeSource(Climate.ParameterList<Holder<Biome>> parameterList) {
		this(parameterList, Optional.empty());
	}

	MultiNoiseBiomeSource(Climate.ParameterList<Holder<Biome>> parameterList, Optional<MultiNoiseBiomeSource.PresetInstance> optional) {
		super(parameterList.values().stream().map(Pair::getSecond));
		this.preset = optional;
		this.parameters = parameterList;
	}

	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}

	private Optional<MultiNoiseBiomeSource.PresetInstance> preset() {
		return this.preset;
	}

	public boolean stable(MultiNoiseBiomeSource.Preset preset) {
		return this.preset.isPresent() && Objects.equals(((MultiNoiseBiomeSource.PresetInstance)this.preset.get()).preset(), preset);
	}

	@Override
	public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
		return this.getNoiseBiome(sampler.sample(i, j, k));
	}

	@VisibleForDebug
	public Holder<Biome> getNoiseBiome(Climate.TargetPoint targetPoint) {
		return this.parameters.findValue(targetPoint);
	}

	@Override
	public void addDebugInfo(List<String> list, BlockPos blockPos, Climate.Sampler sampler) {
		int i = QuartPos.fromBlock(blockPos.getX());
		int j = QuartPos.fromBlock(blockPos.getY());
		int k = QuartPos.fromBlock(blockPos.getZ());
		Climate.TargetPoint targetPoint = sampler.sample(i, j, k);
		float f = Climate.unquantizeCoord(targetPoint.continentalness());
		float g = Climate.unquantizeCoord(targetPoint.erosion());
		float h = Climate.unquantizeCoord(targetPoint.temperature());
		float l = Climate.unquantizeCoord(targetPoint.humidity());
		float m = Climate.unquantizeCoord(targetPoint.weirdness());
		double d = (double)NoiseRouterData.peaksAndValleys(m);
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
	}

	public static class Preset {
		static final Map<ResourceLocation, MultiNoiseBiomeSource.Preset> BY_NAME = Maps.<ResourceLocation, MultiNoiseBiomeSource.Preset>newHashMap();
		public static final MultiNoiseBiomeSource.Preset NETHER = new MultiNoiseBiomeSource.Preset(
			new ResourceLocation("nether"),
			new MultiNoiseBiomeSource.Preset.SourceProvider() {
				@Override
				public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function) {
					return new Climate.ParameterList<>(
						List.of(
							Pair.of(Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), function.apply(Biomes.NETHER_WASTES)),
							Pair.of(Climate.parameters(0.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), function.apply(Biomes.SOUL_SAND_VALLEY)),
							Pair.of(Climate.parameters(0.4F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), function.apply(Biomes.CRIMSON_FOREST)),
							Pair.of(Climate.parameters(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.375F), function.apply(Biomes.WARPED_FOREST)),
							Pair.of(Climate.parameters(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.175F), function.apply(Biomes.BASALT_DELTAS))
						)
					);
				}
			}
		);
		public static final MultiNoiseBiomeSource.Preset OVERWORLD = new MultiNoiseBiomeSource.Preset(
			new ResourceLocation("overworld"), new MultiNoiseBiomeSource.Preset.SourceProvider() {
				@Override
				public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function) {
					return MultiNoiseBiomeSource.Preset.generateOverworldBiomes(function, OverworldBiomeBuilder.Modifier.NONE);
				}
			}
		);
		public static final MultiNoiseBiomeSource.Preset OVERWORLD_UPDATE_1_20 = new MultiNoiseBiomeSource.Preset(
			new ResourceLocation("overworld_update_1_20"), new MultiNoiseBiomeSource.Preset.SourceProvider() {
				@Override
				public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function) {
					return MultiNoiseBiomeSource.Preset.generateOverworldBiomes(function, OverworldBiomeBuilder.Modifier.UPDATE_1_20);
				}
			}
		);
		final ResourceLocation name;
		private final MultiNoiseBiomeSource.Preset.SourceProvider parameterSource;

		public Preset(ResourceLocation resourceLocation, MultiNoiseBiomeSource.Preset.SourceProvider sourceProvider) {
			this.name = resourceLocation;
			this.parameterSource = sourceProvider;
			BY_NAME.put(resourceLocation, this);
		}

		@VisibleForDebug
		public static Stream<Pair<ResourceLocation, MultiNoiseBiomeSource.Preset>> getPresets() {
			return BY_NAME.entrySet().stream().map(entry -> Pair.of((ResourceLocation)entry.getKey(), (MultiNoiseBiomeSource.Preset)entry.getValue()));
		}

		static <T> Climate.ParameterList<T> generateOverworldBiomes(Function<ResourceKey<Biome>, T> function, OverworldBiomeBuilder.Modifier modifier) {
			Builder<Pair<Climate.ParameterPoint, T>> builder = ImmutableList.builder();
			new OverworldBiomeBuilder(modifier).addBiomes(pair -> builder.add(pair.mapSecond(function)));
			return new Climate.ParameterList<>(builder.build());
		}

		MultiNoiseBiomeSource biomeSource(MultiNoiseBiomeSource.PresetInstance presetInstance, boolean bl) {
			Climate.ParameterList<Holder<Biome>> parameterList = this.parameterSource.apply(resourceKey -> presetInstance.biomes().getOrThrow(resourceKey));
			return new MultiNoiseBiomeSource(parameterList, bl ? Optional.of(presetInstance) : Optional.empty());
		}

		public MultiNoiseBiomeSource biomeSource(HolderGetter<Biome> holderGetter, boolean bl) {
			return this.biomeSource(new MultiNoiseBiomeSource.PresetInstance(this, holderGetter), bl);
		}

		public MultiNoiseBiomeSource biomeSource(HolderGetter<Biome> holderGetter) {
			return this.biomeSource(holderGetter, true);
		}

		public Stream<ResourceKey<Biome>> possibleBiomes() {
			return this.parameterSource.apply(resourceKey -> resourceKey).values().stream().map(Pair::getSecond).distinct();
		}

		@FunctionalInterface
		public interface SourceProvider {
			<T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function);
		}
	}

	static record PresetInstance(MultiNoiseBiomeSource.Preset preset, HolderGetter<Biome> biomes) {
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
						RegistryOps.retrieveGetter(Registries.BIOME)
					)
					.apply(instance, instance.stable(MultiNoiseBiomeSource.PresetInstance::new))
		);

		public MultiNoiseBiomeSource biomeSource() {
			return this.preset.biomeSource(this, true);
		}
	}
}
