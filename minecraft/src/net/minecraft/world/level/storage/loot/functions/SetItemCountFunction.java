package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomIntGenerator;
import net.minecraft.world.level.storage.loot.RandomIntGenerators;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetItemCountFunction extends LootItemConditionalFunction {
	private final RandomIntGenerator value;

	private SetItemCountFunction(LootItemCondition[] lootItemConditions, RandomIntGenerator randomIntGenerator) {
		super(lootItemConditions);
		this.value = randomIntGenerator;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.setCount(this.value.getInt(lootContext.getRandom()));
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setCount(RandomIntGenerator randomIntGenerator) {
		return simpleBuilder(lootItemConditions -> new SetItemCountFunction(lootItemConditions, randomIntGenerator));
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemCountFunction> {
		protected Serializer() {
			super(new ResourceLocation("set_count"), SetItemCountFunction.class);
		}

		public void serialize(JsonObject jsonObject, SetItemCountFunction setItemCountFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setItemCountFunction, jsonSerializationContext);
			jsonObject.add("count", RandomIntGenerators.serialize(setItemCountFunction.value, jsonSerializationContext));
		}

		public SetItemCountFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			RandomIntGenerator randomIntGenerator = RandomIntGenerators.deserialize(jsonObject.get("count"), jsonDeserializationContext);
			return new SetItemCountFunction(lootItemConditions, randomIntGenerator);
		}
	}
}
