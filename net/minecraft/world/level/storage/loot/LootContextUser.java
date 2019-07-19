/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public interface LootContextUser {
    default public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of();
    }

    default public void validate(LootTableProblemCollector lootTableProblemCollector, Function<ResourceLocation, LootTable> function, Set<ResourceLocation> set, LootContextParamSet lootContextParamSet) {
        lootContextParamSet.validateUser(lootTableProblemCollector, this);
    }
}

