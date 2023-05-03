package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ContextAwarePredicate {
	public static final ContextAwarePredicate ANY = new ContextAwarePredicate(new LootItemCondition[0]);
	private final LootItemCondition[] conditions;
	private final Predicate<LootContext> compositePredicates;

	ContextAwarePredicate(LootItemCondition[] lootItemConditions) {
		this.conditions = lootItemConditions;
		this.compositePredicates = LootItemConditions.andConditions(lootItemConditions);
	}

	public static ContextAwarePredicate create(LootItemCondition... lootItemConditions) {
		return new ContextAwarePredicate(lootItemConditions);
	}

	@Nullable
	public static ContextAwarePredicate fromElement(
		String string, DeserializationContext deserializationContext, @Nullable JsonElement jsonElement, LootContextParamSet lootContextParamSet
	) {
		if (jsonElement != null && jsonElement.isJsonArray()) {
			LootItemCondition[] lootItemConditions = deserializationContext.deserializeConditions(
				jsonElement.getAsJsonArray(), deserializationContext.getAdvancementId() + "/" + string, lootContextParamSet
			);
			return new ContextAwarePredicate(lootItemConditions);
		} else {
			return null;
		}
	}

	public boolean matches(LootContext lootContext) {
		return this.compositePredicates.test(lootContext);
	}

	public JsonElement toJson(SerializationContext serializationContext) {
		return (JsonElement)(this.conditions.length == 0 ? JsonNull.INSTANCE : serializationContext.serializeConditions(this.conditions));
	}

	public static JsonElement toJson(ContextAwarePredicate[] contextAwarePredicates, SerializationContext serializationContext) {
		if (contextAwarePredicates.length == 0) {
			return JsonNull.INSTANCE;
		} else {
			JsonArray jsonArray = new JsonArray();

			for (ContextAwarePredicate contextAwarePredicate : contextAwarePredicates) {
				jsonArray.add(contextAwarePredicate.toJson(serializationContext));
			}

			return jsonArray;
		}
	}
}
