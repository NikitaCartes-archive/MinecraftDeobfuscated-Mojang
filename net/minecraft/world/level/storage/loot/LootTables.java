/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTables
extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = Deserializers.createLootTableSerializer().create();
    private Map<ResourceLocation, LootTable> tables = ImmutableMap.of();
    private final PredicateManager predicateManager;

    public LootTables(PredicateManager predicateManager) {
        super(GSON, "loot_tables");
        this.predicateManager = predicateManager;
    }

    public LootTable get(ResourceLocation resourceLocation) {
        return this.tables.getOrDefault(resourceLocation, LootTable.EMPTY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder<ResourceLocation, LootTable> builder = ImmutableMap.builder();
        JsonElement jsonElement2 = map.remove(BuiltInLootTables.EMPTY);
        if (jsonElement2 != null) {
            LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", (Object)BuiltInLootTables.EMPTY);
        }
        map.forEach((resourceLocation, jsonElement) -> {
            try {
                LootTable lootTable = GSON.fromJson((JsonElement)jsonElement, LootTable.class);
                builder.put((ResourceLocation)resourceLocation, lootTable);
            } catch (Exception exception) {
                LOGGER.error("Couldn't parse loot table {}", resourceLocation, (Object)exception);
            }
        });
        builder.put(BuiltInLootTables.EMPTY, LootTable.EMPTY);
        ImmutableMap<ResourceLocation, LootTable> immutableMap = builder.build();
        ValidationContext validationContext = new ValidationContext(LootContextParamSets.ALL_PARAMS, this.predicateManager::get, immutableMap::get);
        immutableMap.forEach((resourceLocation, lootTable) -> LootTables.validate(validationContext, resourceLocation, lootTable));
        validationContext.getProblems().forEach((string, string2) -> LOGGER.warn("Found validation problem in {}: {}", string, string2));
        this.tables = immutableMap;
    }

    public static void validate(ValidationContext validationContext, ResourceLocation resourceLocation, LootTable lootTable) {
        lootTable.validate(validationContext.setParams(lootTable.getParamSet()).enterTable("{" + resourceLocation + "}", resourceLocation));
    }

    public static JsonElement serialize(LootTable lootTable) {
        return GSON.toJsonTree(lootTable);
    }

    public Set<ResourceLocation> getIds() {
        return this.tables.keySet();
    }
}

