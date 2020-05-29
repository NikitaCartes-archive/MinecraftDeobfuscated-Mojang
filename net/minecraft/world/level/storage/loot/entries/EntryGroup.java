/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.entries;

import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntryGroup
extends CompositeEntryBase {
    EntryGroup(LootPoolEntryContainer[] lootPoolEntryContainers, LootItemCondition[] lootItemConditions) {
        super(lootPoolEntryContainers, lootItemConditions);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.GROUP;
    }

    @Override
    protected ComposableEntryContainer compose(ComposableEntryContainer[] composableEntryContainers) {
        switch (composableEntryContainers.length) {
            case 0: {
                return ALWAYS_TRUE;
            }
            case 1: {
                return composableEntryContainers[0];
            }
            case 2: {
                ComposableEntryContainer composableEntryContainer = composableEntryContainers[0];
                ComposableEntryContainer composableEntryContainer2 = composableEntryContainers[1];
                return (lootContext, consumer) -> {
                    composableEntryContainer.expand(lootContext, consumer);
                    composableEntryContainer2.expand(lootContext, consumer);
                    return true;
                };
            }
        }
        return (lootContext, consumer) -> {
            for (ComposableEntryContainer composableEntryContainer : composableEntryContainers) {
                composableEntryContainer.expand(lootContext, consumer);
            }
            return true;
        };
    }
}

