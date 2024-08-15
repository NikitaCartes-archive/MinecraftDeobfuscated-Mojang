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
import net.minecraft.data.worldgen.biome.BiomeData;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.item.Instruments;
import net.minecraft.world.item.JukeboxSongs;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.block.entity.BannerPatterns;
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
		.add(Registries.BIOME, BiomeData::bootstrap)
		.add(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterLists::bootstrap)
		.add(Registries.NOISE, NoiseData::bootstrap)
		.add(Registries.DENSITY_FUNCTION, NoiseRouterData::bootstrap)
		.add(Registries.NOISE_SETTINGS, NoiseGeneratorSettings::bootstrap)
		.add(Registries.WORLD_PRESET, WorldPresets::bootstrap)
		.add(Registries.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPresets::bootstrap)
		.add(Registries.CHAT_TYPE, ChatType::bootstrap)
		.add(Registries.TRIM_PATTERN, TrimPatterns::bootstrap)
		.add(Registries.TRIM_MATERIAL, TrimMaterials::bootstrap)
		.add(Registries.WOLF_VARIANT, WolfVariants::bootstrap)
		.add(Registries.PAINTING_VARIANT, PaintingVariants::bootstrap)
		.add(Registries.DAMAGE_TYPE, DamageTypes::bootstrap)
		.add(Registries.BANNER_PATTERN, BannerPatterns::bootstrap)
		.add(Registries.ENCHANTMENT, Enchantments::bootstrap)
		.add(Registries.ENCHANTMENT_PROVIDER, VanillaEnchantmentProviders::bootstrap)
		.add(Registries.JUKEBOX_SONG, JukeboxSongs::bootstrap)
		.add(Registries.INSTRUMENT, Instruments::bootstrap);

	private static void validateThatAllBiomeFeaturesHaveBiomeFilter(HolderLookup.Provider provider) {
		validateThatAllBiomeFeaturesHaveBiomeFilter(provider.lookupOrThrow(Registries.PLACED_FEATURE), provider.lookupOrThrow(Registries.BIOME));
	}

	public static void validateThatAllBiomeFeaturesHaveBiomeFilter(HolderGetter<PlacedFeature> holderGetter, HolderLookup<Biome> holderLookup) {
		holderLookup.listElements().forEach(reference -> {
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
