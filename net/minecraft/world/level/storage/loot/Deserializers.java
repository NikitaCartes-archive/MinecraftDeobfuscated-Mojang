/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import net.minecraft.world.level.storage.loot.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.IntLimiter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class Deserializers {
    public static GsonBuilder createConditionSerializer() {
        return new GsonBuilder().registerTypeAdapter((Type)((Object)RandomValueBounds.class), new RandomValueBounds.Serializer()).registerTypeAdapter((Type)((Object)BinomialDistributionGenerator.class), new BinomialDistributionGenerator.Serializer()).registerTypeAdapter((Type)((Object)ConstantIntValue.class), new ConstantIntValue.Serializer()).registerTypeHierarchyAdapter(LootItemCondition.class, new LootItemConditions.Serializer()).registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer());
    }

    public static GsonBuilder createFunctionSerializer() {
        return Deserializers.createConditionSerializer().registerTypeAdapter((Type)((Object)IntLimiter.class), new IntLimiter.Serializer()).registerTypeHierarchyAdapter(LootPoolEntryContainer.class, new LootPoolEntries.Serializer()).registerTypeHierarchyAdapter(LootItemFunction.class, new LootItemFunctions.Serializer());
    }

    public static GsonBuilder createLootTableSerializer() {
        return Deserializers.createFunctionSerializer().registerTypeAdapter((Type)((Object)LootPool.class), new LootPool.Serializer()).registerTypeAdapter((Type)((Object)LootTable.class), new LootTable.Serializer());
    }
}

