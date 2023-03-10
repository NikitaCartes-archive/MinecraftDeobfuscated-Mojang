/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.loot.packs;

import java.util.List;
import java.util.Set;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.packs.UpdateOneTwentyArchaeologyLoot;
import net.minecraft.data.loot.packs.UpdateOneTwentyBlockLoot;
import net.minecraft.data.loot.packs.UpdateOneTwentyChestLoot;
import net.minecraft.data.loot.packs.UpdateOneTwentyEntityLoot;
import net.minecraft.data.loot.packs.UpdateOneTwentyFishingLoot;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class UpdateOneTwentyLootTableProvider {
    public static LootTableProvider create(PackOutput packOutput) {
        return new LootTableProvider(packOutput, Set.of(), List.of(new LootTableProvider.SubProviderEntry(UpdateOneTwentyFishingLoot::new, LootContextParamSets.FISHING), new LootTableProvider.SubProviderEntry(UpdateOneTwentyBlockLoot::new, LootContextParamSets.BLOCK), new LootTableProvider.SubProviderEntry(UpdateOneTwentyChestLoot::new, LootContextParamSets.CHEST), new LootTableProvider.SubProviderEntry(UpdateOneTwentyEntityLoot::new, LootContextParamSets.ENTITY), new LootTableProvider.SubProviderEntry(UpdateOneTwentyArchaeologyLoot::new, LootContextParamSets.ARCHAEOLOGY)));
    }
}

