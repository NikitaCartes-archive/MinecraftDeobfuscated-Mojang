/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Function;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public interface ConditionUserBuilder<T extends ConditionUserBuilder<T>> {
    public T when(LootItemCondition.Builder var1);

    default public <E> T when(Iterable<E> iterable, Function<E, LootItemCondition.Builder> function) {
        T conditionUserBuilder = this.unwrap();
        for (E object : iterable) {
            conditionUserBuilder = conditionUserBuilder.when(function.apply(object));
        }
        return conditionUserBuilder;
    }

    public T unwrap();
}

