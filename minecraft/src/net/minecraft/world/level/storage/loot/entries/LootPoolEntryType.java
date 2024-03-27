package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;

public record LootPoolEntryType(MapCodec<? extends LootPoolEntryContainer> codec) {
}
