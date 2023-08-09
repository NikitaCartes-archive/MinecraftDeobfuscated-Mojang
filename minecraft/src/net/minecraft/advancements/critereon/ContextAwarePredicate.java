package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ContextAwarePredicate {
	private final List<LootItemCondition> conditions;
	private final Predicate<LootContext> compositePredicates;

	ContextAwarePredicate(List<LootItemCondition> list) {
		if (list.isEmpty()) {
			throw new IllegalArgumentException("ContextAwarePredicate must have at least one condition");
		} else {
			this.conditions = list;
			this.compositePredicates = LootItemConditions.andConditions(list);
		}
	}

	public static ContextAwarePredicate create(LootItemCondition... lootItemConditions) {
		return new ContextAwarePredicate(List.of(lootItemConditions));
	}

	public static Optional<Optional<ContextAwarePredicate>> fromElement(
		String string, DeserializationContext deserializationContext, @Nullable JsonElement jsonElement, LootContextParamSet lootContextParamSet
	) {
		if (jsonElement != null && jsonElement.isJsonArray()) {
			List<LootItemCondition> list = deserializationContext.deserializeConditions(
				jsonElement.getAsJsonArray(), deserializationContext.getAdvancementId() + "/" + string, lootContextParamSet
			);
			return list.isEmpty() ? Optional.of(Optional.empty()) : Optional.of(Optional.of(new ContextAwarePredicate(list)));
		} else {
			return Optional.empty();
		}
	}

	public boolean matches(LootContext lootContext) {
		return this.compositePredicates.test(lootContext);
	}

	public JsonElement toJson() {
		return Util.getOrThrow(LootItemConditions.CODEC.listOf().encodeStart(JsonOps.INSTANCE, this.conditions), IllegalStateException::new);
	}

	public static JsonElement toJson(List<ContextAwarePredicate> list) {
		if (list.isEmpty()) {
			return JsonNull.INSTANCE;
		} else {
			JsonArray jsonArray = new JsonArray();

			for (ContextAwarePredicate contextAwarePredicate : list) {
				jsonArray.add(contextAwarePredicate.toJson());
			}

			return jsonArray;
		}
	}
}
