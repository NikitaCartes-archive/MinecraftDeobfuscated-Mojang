/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SequentialEntry
extends CompositeEntryBase {
    SequentialEntry(LootPoolEntryContainer[] lootPoolEntryContainers, LootItemCondition[] lootItemConditions) {
        super(lootPoolEntryContainers, lootItemConditions);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.SEQUENCE;
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
                return composableEntryContainers[0].and(composableEntryContainers[1]);
            }
        }
        return (lootContext, consumer) -> {
            for (ComposableEntryContainer composableEntryContainer : composableEntryContainers) {
                if (composableEntryContainer.expand(lootContext, consumer)) continue;
                return false;
            }
            return true;
        };
    }

    public static Builder sequential(LootPoolEntryContainer.Builder<?> ... builders) {
        return new Builder(builders);
    }

    public static class Builder
    extends LootPoolEntryContainer.Builder<Builder> {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

        public Builder(LootPoolEntryContainer.Builder<?> ... builders) {
            for (LootPoolEntryContainer.Builder<?> builder : builders) {
                this.entries.add(builder.build());
            }
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public Builder then(LootPoolEntryContainer.Builder<?> builder) {
            this.entries.add(builder.build());
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return new SequentialEntry(this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
        }

        @Override
        protected /* synthetic */ LootPoolEntryContainer.Builder getThis() {
            return this.getThis();
        }
    }
}

