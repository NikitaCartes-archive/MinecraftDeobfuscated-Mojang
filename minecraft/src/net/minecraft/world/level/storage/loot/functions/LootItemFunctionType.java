package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;

public record LootItemFunctionType<T extends LootItemFunction>(MapCodec<T> codec) {
}
