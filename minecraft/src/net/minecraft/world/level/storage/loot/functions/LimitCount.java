package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount extends LootItemConditionalFunction {
	final IntRange limiter;

	LimitCount(LootItemCondition[] lootItemConditions, IntRange intRange) {
		super(lootItemConditions);
		this.limiter = intRange;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.LIMIT_COUNT;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.limiter.getReferencedContextParams();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		int i = this.limiter.clamp(lootContext, itemStack.getCount());
		itemStack.setCount(i);
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> limitCount(IntRange intRange) {
		return simpleBuilder(lootItemConditions -> new LimitCount(lootItemConditions, intRange));
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<LimitCount> {
		public void serialize(JsonObject jsonObject, LimitCount limitCount, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, limitCount, jsonSerializationContext);
			jsonObject.add("limit", jsonSerializationContext.serialize(limitCount.limiter));
		}

		public LimitCount deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			IntRange intRange = GsonHelper.getAsObject(jsonObject, "limit", jsonDeserializationContext, IntRange.class);
			return new LimitCount(lootItemConditions, intRange);
		}
	}
}
