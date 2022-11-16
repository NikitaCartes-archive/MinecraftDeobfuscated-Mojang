package net.minecraft.data.advancements.packs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityType;

public class UpdateOneTwentyAdvancements extends VanillaHusbandryAdvancements {
	@Override
	public void generate(HolderLookup.Provider provider, Consumer<Advancement> consumer) {
		Advancement advancement = this.createRoot(consumer);
		Advancement advancement2 = this.createBreedAnAnimalAdvancement(advancement, consumer);
		this.createBreedAllAnimalsAdvancement(advancement2, consumer);
	}

	@Override
	public EntityType<?>[] getBreedableAnimals() {
		EntityType<?>[] entityTypes = super.getBreedableAnimals();
		List<EntityType<?>> list = (List<EntityType<?>>)Arrays.stream(entityTypes).collect(Collectors.toList());
		list.add(EntityType.CAMEL);
		return (EntityType<?>[])list.toArray(entityTypes);
	}
}
