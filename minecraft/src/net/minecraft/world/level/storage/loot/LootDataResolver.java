package net.minecraft.world.level.storage.loot;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface LootDataResolver {
	@Nullable
	<T> T getElement(LootDataId<T> lootDataId);

	@Nullable
	default <T> T getElement(LootDataType<T> lootDataType, ResourceLocation resourceLocation) {
		return this.getElement(new LootDataId<>(lootDataType, resourceLocation));
	}

	default <T> Optional<T> getElementOptional(LootDataId<T> lootDataId) {
		return Optional.ofNullable(this.getElement(lootDataId));
	}

	default <T> Optional<T> getElementOptional(LootDataType<T> lootDataType, ResourceLocation resourceLocation) {
		return this.getElementOptional(new LootDataId<>(lootDataType, resourceLocation));
	}

	default LootTable getLootTable(ResourceLocation resourceLocation) {
		return (LootTable)this.getElementOptional(LootDataType.TABLE, resourceLocation).orElse(LootTable.EMPTY);
	}
}
