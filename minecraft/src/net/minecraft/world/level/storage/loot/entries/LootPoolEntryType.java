package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;

public record LootPoolEntryType(Codec<? extends LootPoolEntryContainer> codec) {
}
