/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.functions;

import java.util.Arrays;
import java.util.function.Function;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public interface FunctionUserBuilder<T extends FunctionUserBuilder<T>> {
    public T apply(LootItemFunction.Builder var1);

    default public <E> T apply(Iterable<E> iterable, Function<E, LootItemFunction.Builder> function) {
        T functionUserBuilder = this.unwrap();
        for (E object : iterable) {
            functionUserBuilder = functionUserBuilder.apply(function.apply(object));
        }
        return functionUserBuilder;
    }

    default public <E> T apply(E[] objects, Function<E, LootItemFunction.Builder> function) {
        return this.apply(Arrays.asList(objects), function);
    }

    public T unwrap();
}

