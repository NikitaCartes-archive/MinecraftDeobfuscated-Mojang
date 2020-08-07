package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootTables extends SimpleJsonResourceReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = Deserializers.createLootTableSerializer().create();
	private Map<ResourceLocation, LootTable> tables = ImmutableMap.of();
	private final PredicateManager predicateManager;

	public LootTables(PredicateManager predicateManager) {
		super(GSON, "loot_tables");
		this.predicateManager = predicateManager;
	}

	public LootTable get(ResourceLocation resourceLocation) {
		return (LootTable)this.tables.getOrDefault(resourceLocation, LootTable.EMPTY);
	}

	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		Builder<ResourceLocation, LootTable> builder = ImmutableMap.builder();
		JsonElement jsonElement = (JsonElement)map.remove(BuiltInLootTables.EMPTY);
		if (jsonElement != null) {
			LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", BuiltInLootTables.EMPTY);
		}

		map.forEach((resourceLocation, jsonElementx) -> {
			try {
				LootTable lootTable = GSON.fromJson(jsonElementx, LootTable.class);
				builder.put(resourceLocation, lootTable);
			} catch (Exception var4x) {
				LOGGER.error("Couldn't parse loot table {}", resourceLocation, var4x);
			}
		});
		builder.put(BuiltInLootTables.EMPTY, LootTable.EMPTY);
		ImmutableMap<ResourceLocation, LootTable> immutableMap = builder.build();
		ValidationContext validationContext = new ValidationContext(LootContextParamSets.ALL_PARAMS, this.predicateManager::get, immutableMap::get);
		immutableMap.forEach((resourceLocation, lootTable) -> validate(validationContext, resourceLocation, lootTable));
		validationContext.getProblems().forEach((string, string2) -> LOGGER.warn("Found validation problem in " + string + ": " + string2));
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
