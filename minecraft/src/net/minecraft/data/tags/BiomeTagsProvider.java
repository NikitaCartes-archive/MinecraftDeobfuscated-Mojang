package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

public class BiomeTagsProvider extends TagsProvider<Biome> {
	public BiomeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(packOutput, Registries.BIOME, completableFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(BiomeTags.IS_DEEP_OCEAN).add(Biomes.DEEP_FROZEN_OCEAN).add(Biomes.DEEP_COLD_OCEAN).add(Biomes.DEEP_OCEAN).add(Biomes.DEEP_LUKEWARM_OCEAN);
		this.tag(BiomeTags.IS_OCEAN)
			.addTag(BiomeTags.IS_DEEP_OCEAN)
			.add(Biomes.FROZEN_OCEAN)
			.add(Biomes.OCEAN)
			.add(Biomes.COLD_OCEAN)
			.add(Biomes.LUKEWARM_OCEAN)
			.add(Biomes.WARM_OCEAN);
		this.tag(BiomeTags.IS_BEACH).add(Biomes.BEACH).add(Biomes.SNOWY_BEACH);
		this.tag(BiomeTags.IS_RIVER).add(Biomes.RIVER).add(Biomes.FROZEN_RIVER);
		this.tag(BiomeTags.IS_MOUNTAIN).add(Biomes.MEADOW).add(Biomes.FROZEN_PEAKS).add(Biomes.JAGGED_PEAKS).add(Biomes.STONY_PEAKS).add(Biomes.SNOWY_SLOPES);
		this.tag(BiomeTags.IS_BADLANDS).add(Biomes.BADLANDS).add(Biomes.ERODED_BADLANDS).add(Biomes.WOODED_BADLANDS);
		this.tag(BiomeTags.IS_HILL).add(Biomes.WINDSWEPT_HILLS).add(Biomes.WINDSWEPT_FOREST).add(Biomes.WINDSWEPT_GRAVELLY_HILLS);
		this.tag(BiomeTags.IS_TAIGA).add(Biomes.TAIGA).add(Biomes.SNOWY_TAIGA).add(Biomes.OLD_GROWTH_PINE_TAIGA).add(Biomes.OLD_GROWTH_SPRUCE_TAIGA);
		this.tag(BiomeTags.IS_JUNGLE).add(Biomes.BAMBOO_JUNGLE).add(Biomes.JUNGLE).add(Biomes.SPARSE_JUNGLE);
		this.tag(BiomeTags.IS_FOREST)
			.add(Biomes.FOREST)
			.add(Biomes.FLOWER_FOREST)
			.add(Biomes.BIRCH_FOREST)
			.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
			.add(Biomes.DARK_FOREST)
			.add(Biomes.GROVE);
		this.tag(BiomeTags.IS_SAVANNA).add(Biomes.SAVANNA).add(Biomes.SAVANNA_PLATEAU).add(Biomes.WINDSWEPT_SAVANNA);
		HolderGetter<Biome> holderGetter = provider.lookupOrThrow(Registries.BIOME);
		TagsProvider.TagAppender<Biome> tagAppender = this.tag(BiomeTags.IS_NETHER);
		MultiNoiseBiomeSource.Preset.NETHER.possibleBiomes(holderGetter).forEach(tagAppender::add);
		TagsProvider.TagAppender<Biome> tagAppender2 = this.tag(BiomeTags.IS_OVERWORLD);
		MultiNoiseBiomeSource.Preset.OVERWORLD.possibleBiomes(holderGetter).forEach(tagAppender2::add);
		this.tag(BiomeTags.IS_END).add(Biomes.THE_END).add(Biomes.END_HIGHLANDS).add(Biomes.END_MIDLANDS).add(Biomes.SMALL_END_ISLANDS).add(Biomes.END_BARRENS);
		this.tag(BiomeTags.HAS_BURIED_TREASURE).addTag(BiomeTags.IS_BEACH);
		this.tag(BiomeTags.HAS_DESERT_PYRAMID).add(Biomes.DESERT);
		this.tag(BiomeTags.HAS_IGLOO).add(Biomes.SNOWY_TAIGA).add(Biomes.SNOWY_PLAINS).add(Biomes.SNOWY_SLOPES);
		this.tag(BiomeTags.HAS_JUNGLE_TEMPLE).add(Biomes.BAMBOO_JUNGLE).add(Biomes.JUNGLE);
		this.tag(BiomeTags.HAS_MINESHAFT)
			.addTag(BiomeTags.IS_OCEAN)
			.addTag(BiomeTags.IS_RIVER)
			.addTag(BiomeTags.IS_BEACH)
			.addTag(BiomeTags.IS_MOUNTAIN)
			.addTag(BiomeTags.IS_HILL)
			.addTag(BiomeTags.IS_TAIGA)
			.addTag(BiomeTags.IS_JUNGLE)
			.addTag(BiomeTags.IS_FOREST)
			.add(Biomes.STONY_SHORE)
			.add(Biomes.MUSHROOM_FIELDS)
			.add(Biomes.ICE_SPIKES)
			.add(Biomes.WINDSWEPT_SAVANNA)
			.add(Biomes.DESERT)
			.add(Biomes.SAVANNA)
			.add(Biomes.SNOWY_PLAINS)
			.add(Biomes.PLAINS)
			.add(Biomes.SUNFLOWER_PLAINS)
			.add(Biomes.SWAMP)
			.add(Biomes.MANGROVE_SWAMP)
			.add(Biomes.SAVANNA_PLATEAU)
			.add(Biomes.DRIPSTONE_CAVES)
			.add(Biomes.LUSH_CAVES);
		this.tag(BiomeTags.HAS_MINESHAFT_MESA).addTag(BiomeTags.IS_BADLANDS);
		this.tag(BiomeTags.MINESHAFT_BLOCKING).add(Biomes.DEEP_DARK);
		this.tag(BiomeTags.HAS_OCEAN_MONUMENT).addTag(BiomeTags.IS_DEEP_OCEAN);
		this.tag(BiomeTags.REQUIRED_OCEAN_MONUMENT_SURROUNDING).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER);
		this.tag(BiomeTags.HAS_OCEAN_RUIN_COLD)
			.add(Biomes.FROZEN_OCEAN)
			.add(Biomes.COLD_OCEAN)
			.add(Biomes.OCEAN)
			.add(Biomes.DEEP_FROZEN_OCEAN)
			.add(Biomes.DEEP_COLD_OCEAN)
			.add(Biomes.DEEP_OCEAN);
		this.tag(BiomeTags.HAS_OCEAN_RUIN_WARM).add(Biomes.LUKEWARM_OCEAN).add(Biomes.WARM_OCEAN).add(Biomes.DEEP_LUKEWARM_OCEAN);
		this.tag(BiomeTags.HAS_PILLAGER_OUTPOST)
			.add(Biomes.DESERT)
			.add(Biomes.PLAINS)
			.add(Biomes.SAVANNA)
			.add(Biomes.SNOWY_PLAINS)
			.add(Biomes.TAIGA)
			.addTag(BiomeTags.IS_MOUNTAIN)
			.add(Biomes.GROVE);
		this.tag(BiomeTags.HAS_RUINED_PORTAL_DESERT).add(Biomes.DESERT);
		this.tag(BiomeTags.HAS_RUINED_PORTAL_JUNGLE).addTag(BiomeTags.IS_JUNGLE);
		this.tag(BiomeTags.HAS_RUINED_PORTAL_OCEAN).addTag(BiomeTags.IS_OCEAN);
		this.tag(BiomeTags.HAS_RUINED_PORTAL_SWAMP).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP);
		this.tag(BiomeTags.HAS_RUINED_PORTAL_MOUNTAIN)
			.addTag(BiomeTags.IS_BADLANDS)
			.addTag(BiomeTags.IS_HILL)
			.add(Biomes.SAVANNA_PLATEAU)
			.add(Biomes.WINDSWEPT_SAVANNA)
			.add(Biomes.STONY_SHORE)
			.addTag(BiomeTags.IS_MOUNTAIN);
		this.tag(BiomeTags.HAS_RUINED_PORTAL_STANDARD)
			.addTag(BiomeTags.IS_BEACH)
			.addTag(BiomeTags.IS_RIVER)
			.addTag(BiomeTags.IS_TAIGA)
			.addTag(BiomeTags.IS_FOREST)
			.add(Biomes.MUSHROOM_FIELDS)
			.add(Biomes.ICE_SPIKES)
			.add(Biomes.DRIPSTONE_CAVES)
			.add(Biomes.LUSH_CAVES)
			.add(Biomes.SAVANNA)
			.add(Biomes.SNOWY_PLAINS)
			.add(Biomes.PLAINS)
			.add(Biomes.SUNFLOWER_PLAINS);
		this.tag(BiomeTags.HAS_SHIPWRECK_BEACHED).addTag(BiomeTags.IS_BEACH);
		this.tag(BiomeTags.HAS_SHIPWRECK).addTag(BiomeTags.IS_OCEAN);
		this.tag(BiomeTags.HAS_SWAMP_HUT).add(Biomes.SWAMP);
		this.tag(BiomeTags.HAS_VILLAGE_DESERT).add(Biomes.DESERT);
		this.tag(BiomeTags.HAS_VILLAGE_PLAINS).add(Biomes.PLAINS).add(Biomes.MEADOW);
		this.tag(BiomeTags.HAS_VILLAGE_SAVANNA).add(Biomes.SAVANNA);
		this.tag(BiomeTags.HAS_VILLAGE_SNOWY).add(Biomes.SNOWY_PLAINS);
		this.tag(BiomeTags.HAS_VILLAGE_TAIGA).add(Biomes.TAIGA);
		this.tag(BiomeTags.HAS_WOODLAND_MANSION).add(Biomes.DARK_FOREST);
		this.tag(BiomeTags.STRONGHOLD_BIASED_TO)
			.add(Biomes.PLAINS)
			.add(Biomes.SUNFLOWER_PLAINS)
			.add(Biomes.SNOWY_PLAINS)
			.add(Biomes.ICE_SPIKES)
			.add(Biomes.DESERT)
			.add(Biomes.FOREST)
			.add(Biomes.FLOWER_FOREST)
			.add(Biomes.BIRCH_FOREST)
			.add(Biomes.DARK_FOREST)
			.add(Biomes.OLD_GROWTH_BIRCH_FOREST)
			.add(Biomes.OLD_GROWTH_PINE_TAIGA)
			.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
			.add(Biomes.TAIGA)
			.add(Biomes.SNOWY_TAIGA)
			.add(Biomes.SAVANNA)
			.add(Biomes.SAVANNA_PLATEAU)
			.add(Biomes.WINDSWEPT_HILLS)
			.add(Biomes.WINDSWEPT_GRAVELLY_HILLS)
			.add(Biomes.WINDSWEPT_FOREST)
			.add(Biomes.WINDSWEPT_SAVANNA)
			.add(Biomes.JUNGLE)
			.add(Biomes.SPARSE_JUNGLE)
			.add(Biomes.BAMBOO_JUNGLE)
			.add(Biomes.BADLANDS)
			.add(Biomes.ERODED_BADLANDS)
			.add(Biomes.WOODED_BADLANDS)
			.add(Biomes.MEADOW)
			.add(Biomes.GROVE)
			.add(Biomes.SNOWY_SLOPES)
			.add(Biomes.FROZEN_PEAKS)
			.add(Biomes.JAGGED_PEAKS)
			.add(Biomes.STONY_PEAKS)
			.add(Biomes.MUSHROOM_FIELDS)
			.add(Biomes.DRIPSTONE_CAVES)
			.add(Biomes.LUSH_CAVES);
		this.tag(BiomeTags.HAS_STRONGHOLD).addTag(BiomeTags.IS_OVERWORLD);
		this.tag(BiomeTags.HAS_NETHER_FORTRESS).addTag(BiomeTags.IS_NETHER);
		this.tag(BiomeTags.HAS_NETHER_FOSSIL).add(Biomes.SOUL_SAND_VALLEY);
		this.tag(BiomeTags.HAS_BASTION_REMNANT).add(Biomes.CRIMSON_FOREST).add(Biomes.NETHER_WASTES).add(Biomes.SOUL_SAND_VALLEY).add(Biomes.WARPED_FOREST);
		this.tag(BiomeTags.HAS_ANCIENT_CITY).add(Biomes.DEEP_DARK);
		this.tag(BiomeTags.HAS_RUINED_PORTAL_NETHER).addTag(BiomeTags.IS_NETHER);
		this.tag(BiomeTags.HAS_END_CITY).add(Biomes.END_HIGHLANDS).add(Biomes.END_MIDLANDS);
		this.tag(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL).add(Biomes.WARM_OCEAN);
		this.tag(BiomeTags.PLAYS_UNDERWATER_MUSIC).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER);
		this.tag(BiomeTags.HAS_CLOSER_WATER_FOG).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP);
		this.tag(BiomeTags.WATER_ON_MAP_OUTLINES).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP);
		this.tag(BiomeTags.WITHOUT_ZOMBIE_SIEGES).add(Biomes.MUSHROOM_FIELDS);
		this.tag(BiomeTags.WITHOUT_PATROL_SPAWNS).add(Biomes.MUSHROOM_FIELDS);
		this.tag(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS).add(Biomes.THE_VOID);
		this.tag(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)
			.add(Biomes.SNOWY_PLAINS)
			.add(Biomes.ICE_SPIKES)
			.add(Biomes.FROZEN_PEAKS)
			.add(Biomes.JAGGED_PEAKS)
			.add(Biomes.SNOWY_SLOPES)
			.add(Biomes.FROZEN_OCEAN)
			.add(Biomes.DEEP_FROZEN_OCEAN)
			.add(Biomes.GROVE)
			.add(Biomes.DEEP_DARK)
			.add(Biomes.FROZEN_RIVER)
			.add(Biomes.SNOWY_TAIGA)
			.add(Biomes.SNOWY_BEACH)
			.addTag(BiomeTags.IS_END);
		this.tag(BiomeTags.SPAWNS_WARM_VARIANT_FROGS)
			.add(Biomes.DESERT)
			.add(Biomes.WARM_OCEAN)
			.addTag(BiomeTags.IS_JUNGLE)
			.addTag(BiomeTags.IS_SAVANNA)
			.addTag(BiomeTags.IS_NETHER)
			.addTag(BiomeTags.IS_BADLANDS)
			.add(Biomes.MANGROVE_SWAMP);
		this.tag(BiomeTags.ONLY_ALLOWS_SNOW_AND_GOLD_RABBITS).add(Biomes.DESERT);
		this.tag(BiomeTags.REDUCED_WATER_AMBIENT_SPAWNS).addTag(BiomeTags.IS_RIVER);
		this.tag(BiomeTags.ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT).add(Biomes.LUSH_CAVES);
		this.tag(BiomeTags.POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS).add(Biomes.FROZEN_OCEAN).add(Biomes.DEEP_FROZEN_OCEAN);
		this.tag(BiomeTags.MORE_FREQUENT_DROWNED_SPAWNS).addTag(BiomeTags.IS_RIVER);
		this.tag(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP);
	}
}
