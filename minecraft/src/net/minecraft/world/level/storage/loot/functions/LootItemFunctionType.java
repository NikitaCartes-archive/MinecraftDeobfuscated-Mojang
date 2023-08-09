package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;

public record LootItemFunctionType(Codec<? extends LootItemFunction> codec) {
}
