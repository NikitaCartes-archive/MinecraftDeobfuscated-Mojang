package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.serialization.Codec;

public record LootNbtProviderType(Codec<? extends NbtProvider> codec) {
}
