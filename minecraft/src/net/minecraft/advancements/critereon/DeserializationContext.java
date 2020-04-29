package net.minecraft.advancements.critereon;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeserializationContext {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ResourceLocation id;
	private final PredicateManager predicateManager;
	private final Gson predicateGson = Deserializers.createConditionSerializer().create();

	public DeserializationContext(ResourceLocation resourceLocation, PredicateManager predicateManager) {
		this.id = resourceLocation;
		this.predicateManager = predicateManager;
	}

	public final LootItemCondition[] deserializeConditions(JsonArray jsonArray, String string, LootContextParamSet lootContextParamSet) {
		LootItemCondition[] lootItemConditions = this.predicateGson.fromJson(jsonArray, LootItemCondition[].class);
		ValidationContext validationContext = new ValidationContext(lootContextParamSet, this.predicateManager::get, resourceLocation -> null);

		for (LootItemCondition lootItemCondition : lootItemConditions) {
			lootItemCondition.validate(validationContext);
			validationContext.getProblems()
				.forEach((string2, string3) -> LOGGER.warn("Found validation problem in advancement trigger {}/{}: {}", string, string2, string3));
		}

		return lootItemConditions;
	}

	public ResourceLocation getAdvancementId() {
		return this.id;
	}
}
