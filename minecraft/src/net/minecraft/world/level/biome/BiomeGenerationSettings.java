package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeGenerationSettings {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(
		() -> SurfaceBuilders.NOPE, ImmutableMap.of(), ImmutableList.of(), ImmutableList.of()
	);
	public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ConfiguredSurfaceBuilder.CODEC.fieldOf("surface_builder").forGetter(biomeGenerationSettings -> biomeGenerationSettings.surfaceBuilder),
					Codec.simpleMap(
							GenerationStep.Carving.CODEC,
							ConfiguredWorldCarver.LIST_CODEC.promotePartial(Util.prefix("Carver: ", LOGGER::error)),
							StringRepresentable.keys(GenerationStep.Carving.values())
						)
						.fieldOf("carvers")
						.forGetter(biomeGenerationSettings -> biomeGenerationSettings.carvers),
					ConfiguredFeature.LIST_CODEC
						.promotePartial(Util.prefix("Feature: ", LOGGER::error))
						.listOf()
						.fieldOf("features")
						.forGetter(biomeGenerationSettings -> biomeGenerationSettings.features),
					ConfiguredStructureFeature.LIST_CODEC
						.promotePartial(Util.prefix("Structure start: ", LOGGER::error))
						.fieldOf("starts")
						.forGetter(biomeGenerationSettings -> biomeGenerationSettings.structureStarts)
				)
				.apply(instance, BiomeGenerationSettings::new)
	);
	private final Supplier<ConfiguredSurfaceBuilder<?>> surfaceBuilder;
	private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers;
	private final List<List<Supplier<ConfiguredFeature<?, ?>>>> features;
	private final List<Supplier<ConfiguredStructureFeature<?, ?>>> structureStarts;
	private final List<ConfiguredFeature<?, ?>> flowerFeatures;

	private BiomeGenerationSettings(
		Supplier<ConfiguredSurfaceBuilder<?>> supplier,
		Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> map,
		List<List<Supplier<ConfiguredFeature<?, ?>>>> list,
		List<Supplier<ConfiguredStructureFeature<?, ?>>> list2
	) {
		this.surfaceBuilder = supplier;
		this.carvers = map;
		this.features = list;
		this.structureStarts = list2;
		this.flowerFeatures = (List<ConfiguredFeature<?, ?>>)list.stream()
			.flatMap(Collection::stream)
			.map(Supplier::get)
			.flatMap(ConfiguredFeature::getFeatures)
			.filter(configuredFeature -> configuredFeature.feature == Feature.FLOWER)
			.collect(ImmutableList.toImmutableList());
	}

	public List<Supplier<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving carving) {
		return (List<Supplier<ConfiguredWorldCarver<?>>>)this.carvers.getOrDefault(carving, ImmutableList.of());
	}

	public boolean isValidStart(StructureFeature<?> structureFeature) {
		return this.structureStarts.stream().anyMatch(supplier -> ((ConfiguredStructureFeature)supplier.get()).feature == structureFeature);
	}

	public Collection<Supplier<ConfiguredStructureFeature<?, ?>>> structures() {
		return this.structureStarts;
	}

	public ConfiguredStructureFeature<?, ?> withBiomeConfig(ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
		return DataFixUtils.orElse(
			this.structureStarts
				.stream()
				.map(Supplier::get)
				.filter(configuredStructureFeature2 -> configuredStructureFeature2.feature == configuredStructureFeature.feature)
				.findAny(),
			configuredStructureFeature
		);
	}

	public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
		return this.flowerFeatures;
	}

	public List<List<Supplier<ConfiguredFeature<?, ?>>>> features() {
		return this.features;
	}

	public Supplier<ConfiguredSurfaceBuilder<?>> getSurfaceBuilder() {
		return this.surfaceBuilder;
	}

	public SurfaceBuilderConfiguration getSurfaceBuilderConfig() {
		return ((ConfiguredSurfaceBuilder)this.surfaceBuilder.get()).config();
	}

	public static class Builder {
		private Optional<Supplier<ConfiguredSurfaceBuilder<?>>> surfaceBuilder = Optional.empty();
		private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers = Maps.<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>>newLinkedHashMap();
		private final List<List<Supplier<ConfiguredFeature<?, ?>>>> features = Lists.<List<Supplier<ConfiguredFeature<?, ?>>>>newArrayList();
		private final List<Supplier<ConfiguredStructureFeature<?, ?>>> structureStarts = Lists.<Supplier<ConfiguredStructureFeature<?, ?>>>newArrayList();

		public BiomeGenerationSettings.Builder surfaceBuilder(ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder) {
			return this.surfaceBuilder(() -> configuredSurfaceBuilder);
		}

		public BiomeGenerationSettings.Builder surfaceBuilder(Supplier<ConfiguredSurfaceBuilder<?>> supplier) {
			this.surfaceBuilder = Optional.of(supplier);
			return this;
		}

		public BiomeGenerationSettings.Builder addFeature(GenerationStep.Decoration decoration, ConfiguredFeature<?, ?> configuredFeature) {
			return this.addFeature(decoration.ordinal(), () -> configuredFeature);
		}

		public BiomeGenerationSettings.Builder addFeature(int i, Supplier<ConfiguredFeature<?, ?>> supplier) {
			this.addFeatureStepsUpTo(i);
			((List)this.features.get(i)).add(supplier);
			return this;
		}

		public <C extends CarverConfiguration> BiomeGenerationSettings.Builder addCarver(
			GenerationStep.Carving carving, ConfiguredWorldCarver<C> configuredWorldCarver
		) {
			((List)this.carvers.computeIfAbsent(carving, carvingx -> Lists.newArrayList())).add((Supplier)() -> configuredWorldCarver);
			return this;
		}

		public BiomeGenerationSettings.Builder addStructureStart(ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
			this.structureStarts.add((Supplier)() -> configuredStructureFeature);
			return this;
		}

		private void addFeatureStepsUpTo(int i) {
			while (this.features.size() <= i) {
				this.features.add(Lists.newArrayList());
			}
		}

		public BiomeGenerationSettings build() {
			return new BiomeGenerationSettings(
				(Supplier)this.surfaceBuilder.orElseThrow(() -> new IllegalStateException("Missing surface builder")),
				(Map)this.carvers.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, entry -> ImmutableList.copyOf((Collection)entry.getValue()))),
				(List)this.features.stream().map(ImmutableList::copyOf).collect(ImmutableList.toImmutableList()),
				ImmutableList.copyOf(this.structureStarts)
			);
		}
	}
}
