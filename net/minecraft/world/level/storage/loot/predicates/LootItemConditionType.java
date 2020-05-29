/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.SerializerType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemConditionType
extends SerializerType<LootItemCondition> {
    public LootItemConditionType(Serializer<? extends LootItemCondition> serializer) {
        super(serializer);
    }
}

