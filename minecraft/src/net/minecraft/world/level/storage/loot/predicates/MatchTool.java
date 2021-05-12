package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class MatchTool implements LootItemCondition {
	final ItemPredicate predicate;

	public MatchTool(ItemPredicate itemPredicate) {
		this.predicate = itemPredicate;
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.MATCH_TOOL;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.TOOL);
	}

	public boolean test(LootContext lootContext) {
		ItemStack itemStack = lootContext.getParamOrNull(LootContextParams.TOOL);
		return itemStack != null && this.predicate.matches(itemStack);
	}

	public static LootItemCondition.Builder toolMatches(ItemPredicate.Builder builder) {
		return () -> new MatchTool(builder.build());
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<MatchTool> {
		public void serialize(JsonObject jsonObject, MatchTool matchTool, JsonSerializationContext jsonSerializationContext) {
			jsonObject.add("predicate", matchTool.predicate.serializeToJson());
		}

		public MatchTool deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("predicate"));
			return new MatchTool(itemPredicate);
		}
	}
}
