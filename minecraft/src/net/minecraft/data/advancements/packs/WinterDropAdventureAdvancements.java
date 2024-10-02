package net.minecraft.data.advancements.packs;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

public class WinterDropAdventureAdvancements implements AdvancementSubProvider {
	@Override
	public void generate(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer) {
		AdvancementHolder advancementHolder = AdvancementSubProvider.createPlaceholder("adventure/root");
		VanillaAdventureAdvancements.createMonsterHunterAdvancement(
			advancementHolder,
			consumer,
			provider.lookupOrThrow(Registries.ENTITY_TYPE),
			(List<EntityType<?>>)Stream.concat(VanillaAdventureAdvancements.MOBS_TO_KILL.stream(), Stream.of(EntityType.CREAKING_TRANSIENT))
				.collect(Collectors.toList())
		);
		AdvancementHolder advancementHolder2 = AdvancementSubProvider.createPlaceholder("adventure/sleep_in_bed");
		VanillaAdventureAdvancements.createAdventuringTime(provider, consumer, advancementHolder2, MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD_WINTER_DROP);
	}
}
