package net.minecraft.data.advancements.packs;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

public class UpdateOneTwentyAdventureAdvancements implements AdvancementSubProvider {
	@Override
	public void generate(HolderLookup.Provider provider, Consumer<Advancement> consumer) {
		Advancement advancement = AdvancementSubProvider.createPlaceholder("adventure/sleep_in_bed");
		VanillaAdventureAdvancements.createAdventuringTime(consumer, advancement, MultiNoiseBiomeSource.Preset.OVERWORLD_UPDATE_1_20);
	}
}
