package net.minecraft.world.level.storage.loot.providers.number;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

public interface NumberProvider extends LootContextUser {
	float getFloat(LootContext lootContext);

	default int getInt(LootContext lootContext) {
		return Math.round(this.getFloat(lootContext));
	}

	LootNumberProviderType getType();
}
