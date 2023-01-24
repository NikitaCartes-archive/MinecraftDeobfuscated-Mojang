package net.minecraft.data.registries;

import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class VanillaRegistries {
	private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
		.add(Registries.DIMENSION_TYPE, DimensionTypes::bootstrap)
		.add(Registries.CONFIGURED_CARVER, Carvers::bootstrap)
		.add(Registries.CONFIGURED_FEATURE, FeatureUtils::bootstrap)
		.add(Registries.PLACED_FEATURE, PlacementUtils::bootstrap)
		.add(Registries.STRUCTURE, Structures::bootstrap)
		.add(Registries.STRUCTURE_SET, StructureSets::bootstrap)
		.add(Registries.PROCESSOR_LIST, ProcessorLists::bootstrap)
		.add(Registries.TEMPLATE_POOL, Pools::bootstrap)
		.add(Registries.BIOME, Biomes::bootstrap)
		.add(Registries.NOISE, NoiseData::bootstrap)
		.add(Registries.DENSITY_FUNCTION, NoiseRouterData::bootstrap)
		.add(Registries.NOISE_SETTINGS, NoiseGeneratorSettings::bootstrap)
		.add(Registries.WORLD_PRESET, WorldPresets::bootstrap)
		.add(Registries.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPresets::bootstrap)
		.add(Registries.CHAT_TYPE, ChatType::bootstrap)
		.add(Registries.TRIM_PATTERN, TrimPatterns::bootstrap)
		.add(Registries.TRIM_MATERIAL, TrimMaterials::bootstrap);

	private static void validateThatAllBiomeFeaturesHaveBiomeFilter(HolderLookup.Provider provider) {
		HolderGetter<PlacedFeature> holderGetter = provider.lookupOrThrow(Registries.PLACED_FEATURE);
		provider.lookupOrThrow(Registries.BIOME).listElements().forEach(reference -> {
			ResourceLocation resourceLocation = reference.key().location();
			List<HolderSet<PlacedFeature>> list = ((Biome)reference.value()).getGenerationSettings().features();
			list.stream().flatMap(HolderSet::stream).forEach(holder -> holder.unwrap().ifLeft(resourceKey -> {
					Holder.Reference<PlacedFeature> referencexx = holderGetter.getOrThrow(resourceKey);
					if (!validatePlacedFeature(referencexx.value())) {
						Util.logAndPauseIfInIde("Placed feature " + resourceKey.location() + " in biome " + resourceLocation + " is missing BiomeFilter.biome()");
					}
				}).ifRight(placedFeature -> {
					if (!validatePlacedFeature(placedFeature)) {
						Util.logAndPauseIfInIde("Placed inline feature in biome " + reference + " is missing BiomeFilter.biome()");
					}
				}));
		});
	}

	private static boolean validatePlacedFeature(PlacedFeature placedFeature) {
		return placedFeature.placement().contains(BiomeFilter.biome());
	}

	public static HolderLookup.Provider createLookup() {
		RegistryAccess.Frozen frozen = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		HolderLookup.Provider provider = BUILDER.build(frozen);
		validateThatAllBiomeFeaturesHaveBiomeFilter(provider);
		return provider;
	}
}
