package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

public class StructureTagsProvider extends TagsProvider<Structure> {
	public StructureTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(packOutput, Registries.STRUCTURE, completableFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(StructureTags.VILLAGE)
			.add(BuiltinStructures.VILLAGE_PLAINS)
			.add(BuiltinStructures.VILLAGE_DESERT)
			.add(BuiltinStructures.VILLAGE_SAVANNA)
			.add(BuiltinStructures.VILLAGE_SNOWY)
			.add(BuiltinStructures.VILLAGE_TAIGA);
		this.tag(StructureTags.MINESHAFT).add(BuiltinStructures.MINESHAFT).add(BuiltinStructures.MINESHAFT_MESA);
		this.tag(StructureTags.OCEAN_RUIN).add(BuiltinStructures.OCEAN_RUIN_COLD).add(BuiltinStructures.OCEAN_RUIN_WARM);
		this.tag(StructureTags.SHIPWRECK).add(BuiltinStructures.SHIPWRECK).add(BuiltinStructures.SHIPWRECK_BEACHED);
		this.tag(StructureTags.RUINED_PORTAL)
			.add(BuiltinStructures.RUINED_PORTAL_DESERT)
			.add(BuiltinStructures.RUINED_PORTAL_JUNGLE)
			.add(BuiltinStructures.RUINED_PORTAL_MOUNTAIN)
			.add(BuiltinStructures.RUINED_PORTAL_NETHER)
			.add(BuiltinStructures.RUINED_PORTAL_OCEAN)
			.add(BuiltinStructures.RUINED_PORTAL_STANDARD)
			.add(BuiltinStructures.RUINED_PORTAL_SWAMP);
		this.tag(StructureTags.CATS_SPAWN_IN).add(BuiltinStructures.SWAMP_HUT);
		this.tag(StructureTags.CATS_SPAWN_AS_BLACK).add(BuiltinStructures.SWAMP_HUT);
		this.tag(StructureTags.EYE_OF_ENDER_LOCATED).add(BuiltinStructures.STRONGHOLD);
		this.tag(StructureTags.DOLPHIN_LOCATED).addTag(StructureTags.OCEAN_RUIN).addTag(StructureTags.SHIPWRECK);
		this.tag(StructureTags.ON_WOODLAND_EXPLORER_MAPS).add(BuiltinStructures.WOODLAND_MANSION);
		this.tag(StructureTags.ON_OCEAN_EXPLORER_MAPS).add(BuiltinStructures.OCEAN_MONUMENT);
		this.tag(StructureTags.ON_SAVANNA_VILLAGE_MAPS).add(BuiltinStructures.VILLAGE_SAVANNA);
		this.tag(StructureTags.ON_DESERT_VILLAGE_MAPS).add(BuiltinStructures.VILLAGE_DESERT);
		this.tag(StructureTags.ON_PLAINS_VILLAGE_MAPS).add(BuiltinStructures.VILLAGE_PLAINS);
		this.tag(StructureTags.ON_TAIGA_VILLAGE_MAPS).add(BuiltinStructures.VILLAGE_TAIGA);
		this.tag(StructureTags.ON_SNOWY_VILLAGE_MAPS).add(BuiltinStructures.VILLAGE_SNOWY);
		this.tag(StructureTags.ON_SWAMP_EXPLORER_MAPS).add(BuiltinStructures.SWAMP_HUT);
		this.tag(StructureTags.ON_JUNGLE_EXPLORER_MAPS).add(BuiltinStructures.JUNGLE_TEMPLE);
		this.tag(StructureTags.ON_TREASURE_MAPS).add(BuiltinStructures.BURIED_TREASURE);
	}
}
