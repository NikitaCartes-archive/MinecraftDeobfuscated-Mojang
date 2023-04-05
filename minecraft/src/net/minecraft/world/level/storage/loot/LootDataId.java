package net.minecraft.world.level.storage.loot;

import net.minecraft.resources.ResourceLocation;

public record LootDataId<T>(LootDataType<T> type, ResourceLocation location) {
}
