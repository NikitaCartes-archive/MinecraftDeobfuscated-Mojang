package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.serialization.Codec;

public record LootNumberProviderType(Codec<? extends NumberProvider> codec) {
}
