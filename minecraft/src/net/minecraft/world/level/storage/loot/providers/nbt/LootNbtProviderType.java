package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.serialization.MapCodec;

public record LootNbtProviderType(MapCodec<? extends NbtProvider> codec) {
}
