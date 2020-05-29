/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Predicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public interface LootItemCondition
extends LootContextUser,
Predicate<LootContext> {
    public LootItemConditionType getType();

    @FunctionalInterface
    public static interface Builder {
        public LootItemCondition build();

        default public Builder invert() {
            return InvertedLootItemCondition.invert(this);
        }

        default public AlternativeLootItemCondition.Builder or(Builder builder) {
            return AlternativeLootItemCondition.alternative(this, builder);
        }
    }
}

