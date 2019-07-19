/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.IntLimiter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootTables
extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter((Type)((Object)RandomValueBounds.class), new RandomValueBounds.Serializer()).registerTypeAdapter((Type)((Object)BinomialDistributionGenerator.class), new BinomialDistributionGenerator.Serializer()).registerTypeAdapter((Type)((Object)ConstantIntValue.class), new ConstantIntValue.Serializer()).registerTypeAdapter((Type)((Object)IntLimiter.class), new IntLimiter.Serializer()).registerTypeAdapter((Type)((Object)LootPool.class), new LootPool.Serializer()).registerTypeAdapter((Type)((Object)LootTable.class), new LootTable.Serializer()).registerTypeHierarchyAdapter(LootPoolEntryContainer.class, new LootPoolEntries.Serializer()).registerTypeHierarchyAdapter(LootItemFunction.class, new LootItemFunctions.Serializer()).registerTypeHierarchyAdapter(LootItemCondition.class, new LootItemConditions.Serializer()).registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer()).create();
    private Map<ResourceLocation, LootTable> tables = ImmutableMap.of();

    public LootTables() {
        super(GSON, "loot_tables");
    }

    public LootTable get(ResourceLocation resourceLocation) {
        return this.tables.getOrDefault(resourceLocation, LootTable.EMPTY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder<ResourceLocation, LootTable> builder = ImmutableMap.builder();
        JsonObject jsonObject2 = map.remove(BuiltInLootTables.EMPTY);
        if (jsonObject2 != null) {
            LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", (Object)BuiltInLootTables.EMPTY);
        }
        map.forEach((resourceLocation, jsonObject) -> {
            try {
                LootTable lootTable = GSON.fromJson((JsonElement)jsonObject, LootTable.class);
                builder.put((ResourceLocation)resourceLocation, lootTable);
            } catch (Exception exception) {
                LOGGER.error("Couldn't parse loot table {}", resourceLocation, (Object)exception);
            }
        });
        builder.put(BuiltInLootTables.EMPTY, LootTable.EMPTY);
        ImmutableMap<ResourceLocation, LootTable> immutableMap = builder.build();
        LootTableProblemCollector lootTableProblemCollector = new LootTableProblemCollector();
        immutableMap.forEach((resourceLocation, lootTable) -> LootTables.validate(lootTableProblemCollector, resourceLocation, lootTable, immutableMap::get));
        lootTableProblemCollector.getProblems().forEach((string, string2) -> LOGGER.warn("Found validation problem in " + string + ": " + string2));
        this.tables = immutableMap;
    }

    public static void validate(LootTableProblemCollector lootTableProblemCollector, ResourceLocation resourceLocation, LootTable lootTable, Function<ResourceLocation, LootTable> function) {
        ImmutableSet<ResourceLocation> set = ImmutableSet.of(resourceLocation);
        lootTable.validate(lootTableProblemCollector.forChild("{" + resourceLocation.toString() + "}"), function, set, lootTable.getParamSet());
    }

    public static JsonElement serialize(LootTable lootTable) {
        return GSON.toJsonTree(lootTable);
    }

    public Set<ResourceLocation> getIds() {
        return this.tables.keySet();
    }
}

