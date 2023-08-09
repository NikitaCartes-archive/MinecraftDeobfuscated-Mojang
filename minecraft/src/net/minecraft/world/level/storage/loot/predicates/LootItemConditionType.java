package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;

public record LootItemConditionType(Codec<? extends LootItemCondition> codec) {
}
