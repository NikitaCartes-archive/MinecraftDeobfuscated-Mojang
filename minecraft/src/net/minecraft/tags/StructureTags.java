package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;

public interface StructureTags {
	TagKey<Structure> EYE_OF_ENDER_LOCATED = create("eye_of_ender_located");
	TagKey<Structure> DOLPHIN_LOCATED = create("dolphin_located");
	TagKey<Structure> ON_WOODLAND_EXPLORER_MAPS = create("on_woodland_explorer_maps");
	TagKey<Structure> ON_OCEAN_EXPLORER_MAPS = create("on_ocean_explorer_maps");
	TagKey<Structure> ON_SAVANNA_VILLAGE_MAPS = create("on_savanna_village_maps");
	TagKey<Structure> ON_DESERT_VILLAGE_MAPS = create("on_desert_village_maps");
	TagKey<Structure> ON_PLAINS_VILLAGE_MAPS = create("on_plains_village_maps");
	TagKey<Structure> ON_TAIGA_VILLAGE_MAPS = create("on_taiga_village_maps");
	TagKey<Structure> ON_SNOWY_VILLAGE_MAPS = create("on_snowy_village_maps");
	TagKey<Structure> ON_JUNGLE_EXPLORER_MAPS = create("on_jungle_explorer_maps");
	TagKey<Structure> ON_SWAMP_EXPLORER_MAPS = create("on_swamp_explorer_maps");
	TagKey<Structure> ON_TREASURE_MAPS = create("on_treasure_maps");
	TagKey<Structure> ON_TRIAL_CHAMBERS_MAPS = create("on_trial_chambers_maps");
	TagKey<Structure> CATS_SPAWN_IN = create("cats_spawn_in");
	TagKey<Structure> CATS_SPAWN_AS_BLACK = create("cats_spawn_as_black");
	TagKey<Structure> VILLAGE = create("village");
	TagKey<Structure> MINESHAFT = create("mineshaft");
	TagKey<Structure> SHIPWRECK = create("shipwreck");
	TagKey<Structure> RUINED_PORTAL = create("ruined_portal");
	TagKey<Structure> OCEAN_RUIN = create("ocean_ruin");

	private static TagKey<Structure> create(String string) {
		return TagKey.create(Registries.STRUCTURE, ResourceLocation.withDefaultNamespace(string));
	}
}
