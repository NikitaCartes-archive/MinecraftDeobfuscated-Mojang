package net.minecraft.data.advancements.packs;

import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.world.entity.EntityType;

public class UpdateOneTwentyHusbandryAdvancements implements AdvancementSubProvider {
	@Override
	public void generate(HolderLookup.Provider provider, Consumer<Advancement> consumer) {
		Advancement advancement = AdvancementSubProvider.createPlaceholder("husbandry/breed_an_animal");
		Stream<EntityType<?>> stream = Stream.concat(VanillaHusbandryAdvancements.BREEDABLE_ANIMALS.stream(), Stream.of(EntityType.CAMEL, EntityType.SNIFFER));
		VanillaHusbandryAdvancements.createBreedAllAnimalsAdvancement(
			advancement, consumer, stream, VanillaHusbandryAdvancements.INDIRECTLY_BREEDABLE_ANIMALS.stream()
		);
	}
}
