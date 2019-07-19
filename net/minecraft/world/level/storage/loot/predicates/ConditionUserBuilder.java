/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public interface ConditionUserBuilder<T> {
    public T when(LootItemCondition.Builder var1);

    public T unwrap();
}

