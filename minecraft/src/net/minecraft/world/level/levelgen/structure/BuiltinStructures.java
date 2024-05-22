package net.minecraft.world.level.levelgen.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface BuiltinStructures {
	ResourceKey<Structure> PILLAGER_OUTPOST = createKey("pillager_outpost");
	ResourceKey<Structure> MINESHAFT = createKey("mineshaft");
	ResourceKey<Structure> MINESHAFT_MESA = createKey("mineshaft_mesa");
	ResourceKey<Structure> WOODLAND_MANSION = createKey("mansion");
	ResourceKey<Structure> JUNGLE_TEMPLE = createKey("jungle_pyramid");
	ResourceKey<Structure> DESERT_PYRAMID = createKey("desert_pyramid");
	ResourceKey<Structure> IGLOO = createKey("igloo");
	ResourceKey<Structure> SHIPWRECK = createKey("shipwreck");
	ResourceKey<Structure> SHIPWRECK_BEACHED = createKey("shipwreck_beached");
	ResourceKey<Structure> SWAMP_HUT = createKey("swamp_hut");
	ResourceKey<Structure> STRONGHOLD = createKey("stronghold");
	ResourceKey<Structure> OCEAN_MONUMENT = createKey("monument");
	ResourceKey<Structure> OCEAN_RUIN_COLD = createKey("ocean_ruin_cold");
	ResourceKey<Structure> OCEAN_RUIN_WARM = createKey("ocean_ruin_warm");
	ResourceKey<Structure> FORTRESS = createKey("fortress");
	ResourceKey<Structure> NETHER_FOSSIL = createKey("nether_fossil");
	ResourceKey<Structure> END_CITY = createKey("end_city");
	ResourceKey<Structure> BURIED_TREASURE = createKey("buried_treasure");
	ResourceKey<Structure> BASTION_REMNANT = createKey("bastion_remnant");
	ResourceKey<Structure> VILLAGE_PLAINS = createKey("village_plains");
	ResourceKey<Structure> VILLAGE_DESERT = createKey("village_desert");
	ResourceKey<Structure> VILLAGE_SAVANNA = createKey("village_savanna");
	ResourceKey<Structure> VILLAGE_SNOWY = createKey("village_snowy");
	ResourceKey<Structure> VILLAGE_TAIGA = createKey("village_taiga");
	ResourceKey<Structure> RUINED_PORTAL_STANDARD = createKey("ruined_portal");
	ResourceKey<Structure> RUINED_PORTAL_DESERT = createKey("ruined_portal_desert");
	ResourceKey<Structure> RUINED_PORTAL_JUNGLE = createKey("ruined_portal_jungle");
	ResourceKey<Structure> RUINED_PORTAL_SWAMP = createKey("ruined_portal_swamp");
	ResourceKey<Structure> RUINED_PORTAL_MOUNTAIN = createKey("ruined_portal_mountain");
	ResourceKey<Structure> RUINED_PORTAL_OCEAN = createKey("ruined_portal_ocean");
	ResourceKey<Structure> RUINED_PORTAL_NETHER = createKey("ruined_portal_nether");
	ResourceKey<Structure> ANCIENT_CITY = createKey("ancient_city");
	ResourceKey<Structure> TRAIL_RUINS = createKey("trail_ruins");
	ResourceKey<Structure> TRIAL_CHAMBERS = createKey("trial_chambers");

	private static ResourceKey<Structure> createKey(String string) {
		return ResourceKey.create(Registries.STRUCTURE, ResourceLocation.withDefaultNamespace(string));
	}
}
