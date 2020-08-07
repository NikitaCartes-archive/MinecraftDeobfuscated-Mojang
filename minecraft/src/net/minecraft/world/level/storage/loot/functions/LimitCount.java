package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntLimiter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount extends LootItemConditionalFunction {
	private final IntLimiter limiter;

	private LimitCount(LootItemCondition[] lootItemConditions, IntLimiter intLimiter) {
		super(lootItemConditions);
		this.limiter = intLimiter;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.LIMIT_COUNT;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		int i = this.limiter.applyAsInt(itemStack.getCount());
		itemStack.setCount(i);
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> limitCount(IntLimiter intLimiter) {
		return simpleBuilder(lootItemConditions -> new LimitCount(lootItemConditions, intLimiter));
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<LimitCount> {
		public void serialize(JsonObject jsonObject, LimitCount limitCount, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, limitCount, jsonSerializationContext);
			jsonObject.add("limit", jsonSerializationContext.serialize(limitCount.limiter));
		}

		public LimitCount deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			IntLimiter intLimiter = GsonHelper.getAsObject(jsonObject, "limit", jsonDeserializationContext, IntLimiter.class);
			return new LimitCount(lootItemConditions, intLimiter);
		}
	}
}
