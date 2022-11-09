/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.entries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.entries.SequentialEntry;
import net.minecraft.world.level.storage.loot.entries.TagEntry;

public class LootPoolEntries {
    public static final LootPoolEntryType EMPTY = LootPoolEntries.register("empty", new EmptyLootItem.Serializer());
    public static final LootPoolEntryType ITEM = LootPoolEntries.register("item", new LootItem.Serializer());
    public static final LootPoolEntryType REFERENCE = LootPoolEntries.register("loot_table", new LootTableReference.Serializer());
    public static final LootPoolEntryType DYNAMIC = LootPoolEntries.register("dynamic", new DynamicLoot.Serializer());
    public static final LootPoolEntryType TAG = LootPoolEntries.register("tag", new TagEntry.Serializer());
    public static final LootPoolEntryType ALTERNATIVES = LootPoolEntries.register("alternatives", CompositeEntryBase.createSerializer(AlternativesEntry::new));
    public static final LootPoolEntryType SEQUENCE = LootPoolEntries.register("sequence", CompositeEntryBase.createSerializer(SequentialEntry::new));
    public static final LootPoolEntryType GROUP = LootPoolEntries.register("group", CompositeEntryBase.createSerializer(EntryGroup::new));

    private static LootPoolEntryType register(String string, Serializer<? extends LootPoolEntryContainer> serializer) {
        return Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(string), new LootPoolEntryType(serializer));
    }

    public static Object createGsonAdapter() {
        return GsonAdapterFactory.builder(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, "entry", "type", LootPoolEntryContainer::getType).build();
    }
}

