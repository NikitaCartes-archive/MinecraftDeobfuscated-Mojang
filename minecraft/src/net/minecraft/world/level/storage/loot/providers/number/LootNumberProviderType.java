package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.serialization.MapCodec;

public record LootNumberProviderType(MapCodec<? extends NumberProvider> codec) {
}
