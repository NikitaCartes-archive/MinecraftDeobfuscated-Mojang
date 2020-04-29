package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PredicateManager extends SimpleJsonResourceReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = Deserializers.createConditionSerializer().create();
	private Map<ResourceLocation, LootItemCondition> conditions = ImmutableMap.of();

	public PredicateManager() {
		super(GSON, "predicates");
	}

	@Nullable
	public LootItemCondition get(ResourceLocation resourceLocation) {
		return (LootItemCondition)this.conditions.get(resourceLocation);
	}

	protected void apply(Map<ResourceLocation, JsonObject> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		Builder<ResourceLocation, LootItemCondition> builder = ImmutableMap.builder();
		map.forEach((resourceLocation, jsonObject) -> {
			try {
				LootItemCondition lootItemCondition = GSON.fromJson(jsonObject, LootItemCondition.class);
				builder.put(resourceLocation, lootItemCondition);
			} catch (Exception var4x) {
				LOGGER.error("Couldn't parse loot table {}", resourceLocation, var4x);
			}
		});
		Map<ResourceLocation, LootItemCondition> map2 = builder.build();
		ValidationContext validationContext = new ValidationContext(LootContextParamSets.ALL_PARAMS, map2::get, resourceLocation -> null);
		map2.forEach(
			(resourceLocation, lootItemCondition) -> lootItemCondition.validate(validationContext.enterCondition("{" + resourceLocation + "}", resourceLocation))
		);
		validationContext.getProblems().forEach((string, string2) -> LOGGER.warn("Found validation problem in " + string + ": " + string2));
		this.conditions = map2;
	}

	public Set<ResourceLocation> getKeys() {
		return Collections.unmodifiableSet(this.conditions.keySet());
	}
}
