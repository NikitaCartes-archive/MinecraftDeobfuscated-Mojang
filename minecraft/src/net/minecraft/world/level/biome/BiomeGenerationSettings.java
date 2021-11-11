package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeGenerationSettings {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(ImmutableMap.of(), ImmutableList.of());
	public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.simpleMap(
							GenerationStep.Carving.CODEC,
							ConfiguredWorldCarver.LIST_CODEC
								.promotePartial(Util.prefix("Carver: ", LOGGER::error))
								.flatXmap(ExtraCodecs.nonNullSupplierListCheck(), ExtraCodecs.nonNullSupplierListCheck()),
							StringRepresentable.keys(GenerationStep.Carving.values())
						)
						.fieldOf("carvers")
						.forGetter(biomeGenerationSettings -> biomeGenerationSettings.carvers),
					PlacedFeature.LIST_CODEC
						.promotePartial(Util.prefix("Feature: ", LOGGER::error))
						.flatXmap(ExtraCodecs.nonNullSupplierListCheck(), ExtraCodecs.nonNullSupplierListCheck())
						.listOf()
						.fieldOf("features")
						.forGetter(biomeGenerationSettings -> biomeGenerationSettings.features)
				)
				.apply(instance, BiomeGenerationSettings::new)
	);
	private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers;
	private final List<List<Supplier<PlacedFeature>>> features;
	private final List<ConfiguredFeature<?, ?>> flowerFeatures;
	private final Set<PlacedFeature> featureSet;

	BiomeGenerationSettings(Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> map, List<List<Supplier<PlacedFeature>>> list) {
		this.carvers = map;
		this.features = list;
		this.flowerFeatures = (List<ConfiguredFeature<?, ?>>)list.stream()
			.flatMap(Collection::stream)
			.map(Supplier::get)
			.flatMap(PlacedFeature::getFeatures)
			.filter(configuredFeature -> configuredFeature.feature == Feature.FLOWER)
			.collect(ImmutableList.toImmutableList());
		this.featureSet = (Set<PlacedFeature>)list.stream().flatMap(Collection::stream).map(Supplier::get).collect(Collectors.toSet());
	}

	public List<Supplier<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving carving) {
		return (List<Supplier<ConfiguredWorldCarver<?>>>)this.carvers.getOrDefault(carving, ImmutableList.of());
	}

	public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
		return this.flowerFeatures;
	}

	public List<List<Supplier<PlacedFeature>>> features() {
		return this.features;
	}

	public boolean hasFeature(PlacedFeature placedFeature) {
		return this.featureSet.contains(placedFeature);
	}

	public static class Builder {
		private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers = Maps.<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>>newLinkedHashMap();
		private final List<List<Supplier<PlacedFeature>>> features = Lists.<List<Supplier<PlacedFeature>>>newArrayList();

		public BiomeGenerationSettings.Builder addFeature(GenerationStep.Decoration decoration, PlacedFeature placedFeature) {
			return this.addFeature(decoration.ordinal(), () -> placedFeature);
		}

		public BiomeGenerationSettings.Builder addFeature(int i, Supplier<PlacedFeature> supplier) {
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

		private void addFeatureStepsUpTo(int i) {
			while (this.features.size() <= i) {
				this.features.add(Lists.newArrayList());
			}
		}

		public BiomeGenerationSettings build() {
			return new BiomeGenerationSettings(
				(Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>>)this.carvers
					.entrySet()
					.stream()
					.collect(ImmutableMap.toImmutableMap(Entry::getKey, entry -> ImmutableList.copyOf((Collection)entry.getValue()))),
				(List<List<Supplier<PlacedFeature>>>)this.features.stream().map(ImmutableList::copyOf).collect(ImmutableList.toImmutableList())
			);
		}
	}
}
