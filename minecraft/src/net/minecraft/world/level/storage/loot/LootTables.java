package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootTables extends SimpleJsonResourceReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(RandomValueBounds.class, new RandomValueBounds.Serializer())
		.registerTypeAdapter(BinomialDistributionGenerator.class, new BinomialDistributionGenerator.Serializer())
		.registerTypeAdapter(ConstantIntValue.class, new ConstantIntValue.Serializer())
		.registerTypeAdapter(IntLimiter.class, new IntLimiter.Serializer())
		.registerTypeAdapter(LootPool.class, new LootPool.Serializer())
		.registerTypeAdapter(LootTable.class, new LootTable.Serializer())
		.registerTypeHierarchyAdapter(LootPoolEntryContainer.class, new LootPoolEntries.Serializer())
		.registerTypeHierarchyAdapter(LootItemFunction.class, new LootItemFunctions.Serializer())
		.registerTypeHierarchyAdapter(LootItemCondition.class, new LootItemConditions.Serializer())
		.registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer())
		.create();
	private Map<ResourceLocation, LootTable> tables = ImmutableMap.of();

	public LootTables() {
		super(GSON, "loot_tables");
	}

	public LootTable get(ResourceLocation resourceLocation) {
		return (LootTable)this.tables.getOrDefault(resourceLocation, LootTable.EMPTY);
	}

	protected void apply(Map<ResourceLocation, JsonObject> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		Builder<ResourceLocation, LootTable> builder = ImmutableMap.builder();
		JsonObject jsonObject = (JsonObject)map.remove(BuiltInLootTables.EMPTY);
		if (jsonObject != null) {
			LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", BuiltInLootTables.EMPTY);
		}

		map.forEach((resourceLocation, jsonObjectx) -> {
			try {
				LootTable lootTable = GSON.fromJson(jsonObjectx, LootTable.class);
				builder.put(resourceLocation, lootTable);
			} catch (Exception var4x) {
				LOGGER.error("Couldn't parse loot table {}", resourceLocation, var4x);
			}
		});
		builder.put(BuiltInLootTables.EMPTY, LootTable.EMPTY);
		ImmutableMap<ResourceLocation, LootTable> immutableMap = builder.build();
		LootTableProblemCollector lootTableProblemCollector = new LootTableProblemCollector();
		immutableMap.forEach((resourceLocation, lootTable) -> validate(lootTableProblemCollector, resourceLocation, lootTable, immutableMap::get));
		lootTableProblemCollector.getProblems().forEach((string, string2) -> LOGGER.warn("Found validation problem in " + string + ": " + string2));
		this.tables = immutableMap;
	}

	public static void validate(
		LootTableProblemCollector lootTableProblemCollector, ResourceLocation resourceLocation, LootTable lootTable, Function<ResourceLocation, LootTable> function
	) {
		Set<ResourceLocation> set = ImmutableSet.of(resourceLocation);
		lootTable.validate(lootTableProblemCollector.forChild("{" + resourceLocation.toString() + "}"), function, set, lootTable.getParamSet());
	}

	public static JsonElement serialize(LootTable lootTable) {
		return GSON.toJsonTree(lootTable);
	}

	public Set<ResourceLocation> getIds() {
		return this.tables.keySet();
	}
}
