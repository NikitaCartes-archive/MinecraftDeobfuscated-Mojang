/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.entries;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;

@FunctionalInterface
interface ComposableEntryContainer {
    public static final ComposableEntryContainer ALWAYS_FALSE = (lootContext, consumer) -> false;
    public static final ComposableEntryContainer ALWAYS_TRUE = (lootContext, consumer) -> true;

    public boolean expand(LootContext var1, Consumer<LootPoolEntry> var2);

    default public ComposableEntryContainer and(ComposableEntryContainer composableEntryContainer) {
        Objects.requireNonNull(composableEntryContainer);
        return (lootContext, consumer) -> this.expand(lootContext, consumer) && composableEntryContainer.expand(lootContext, consumer);
    }

    default public ComposableEntryContainer or(ComposableEntryContainer composableEntryContainer) {
        Objects.requireNonNull(composableEntryContainer);
        return (lootContext, consumer) -> this.expand(lootContext, consumer) || composableEntryContainer.expand(lootContext, consumer);
    }
}

