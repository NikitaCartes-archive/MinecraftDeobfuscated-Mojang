package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;

public class BiomeGenerationSettings {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(ImmutableMap.of(), ImmutableList.of());
	public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.simpleMap(
							GenerationStep.Carving.CODEC,
							ConfiguredWorldCarver.LIST_CODEC.promotePartial(Util.prefix("Carver: ", LOGGER::error)),
							StringRepresentable.keys(GenerationStep.Carving.values())
						)
						.fieldOf("carvers")
						.forGetter(biomeGenerationSettings -> biomeGenerationSettings.carvers),
					PlacedFeature.LIST_OF_LISTS_CODEC
						.promotePartial(Util.prefix("Features: ", LOGGER::error))
						.fieldOf("features")
						.forGetter(biomeGenerationSettings -> biomeGenerationSettings.features)
				)
				.apply(instance, BiomeGenerationSettings::new)
	);
	private final Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> carvers;
	private final List<HolderSet<PlacedFeature>> features;
	private final Supplier<List<ConfiguredFeature<?, ?>>> flowerFeatures;
	private final Supplier<Set<PlacedFeature>> featureSet;

	BiomeGenerationSettings(Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> map, List<HolderSet<PlacedFeature>> list) {
		this.carvers = map;
		this.features = list;
		this.flowerFeatures = Suppliers.memoize(
			() -> (List<ConfiguredFeature<?, ?>>)list.stream()
					.flatMap(HolderSet::stream)
					.map(Holder::value)
					.flatMap(PlacedFeature::getFeatures)
					.filter(configuredFeature -> configuredFeature.feature() == Feature.FLOWER)
					.collect(ImmutableList.toImmutableList())
		);
		this.featureSet = Suppliers.memoize(() -> (Set<PlacedFeature>)list.stream().flatMap(HolderSet::stream).map(Holder::value).collect(Collectors.toSet()));
	}

	public Iterable<Holder<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving carving) {
		return (Iterable<Holder<ConfiguredWorldCarver<?>>>)Objects.requireNonNullElseGet((Iterable)this.carvers.get(carving), List::of);
	}

	public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
		return (List<ConfiguredFeature<?, ?>>)this.flowerFeatures.get();
	}

	public List<HolderSet<PlacedFeature>> features() {
		return this.features;
	}

	public boolean hasFeature(PlacedFeature placedFeature) {
		return ((Set)this.featureSet.get()).contains(placedFeature);
	}

	public static class Builder {
		private final Map<GenerationStep.Carving, List<Holder<ConfiguredWorldCarver<?>>>> carvers = Maps.<GenerationStep.Carving, List<Holder<ConfiguredWorldCarver<?>>>>newLinkedHashMap();
		private final List<List<Holder<PlacedFeature>>> features = Lists.<List<Holder<PlacedFeature>>>newArrayList();

		public BiomeGenerationSettings.Builder addFeature(GenerationStep.Decoration decoration, Holder<PlacedFeature> holder) {
			return this.addFeature(decoration.ordinal(), holder);
		}

		public BiomeGenerationSettings.Builder addFeature(int i, Holder<PlacedFeature> holder) {
			this.addFeatureStepsUpTo(i);
			((List)this.features.get(i)).add(holder);
			return this;
		}

		public BiomeGenerationSettings.Builder addCarver(GenerationStep.Carving carving, Holder<? extends ConfiguredWorldCarver<?>> holder) {
			((List)this.carvers.computeIfAbsent(carving, carvingx -> Lists.newArrayList())).add(Holder.hackyErase(holder));
			return this;
		}

		private void addFeatureStepsUpTo(int i) {
			while (this.features.size() <= i) {
				this.features.add(Lists.newArrayList());
			}
		}

		public BiomeGenerationSettings build() {
			return new BiomeGenerationSettings(
				(Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>>)this.carvers
					.entrySet()
					.stream()
					.collect(ImmutableMap.toImmutableMap(Entry::getKey, entry -> HolderSet.direct((List)entry.getValue()))),
				(List<HolderSet<PlacedFeature>>)this.features.stream().map(HolderSet::direct).collect(ImmutableList.toImmutableList())
			);
		}
	}
}
