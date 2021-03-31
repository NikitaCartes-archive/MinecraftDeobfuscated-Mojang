package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetItemCountFunction extends LootItemConditionalFunction {
	private final NumberProvider value;
	private final boolean add;

	private SetItemCountFunction(LootItemCondition[] lootItemConditions, NumberProvider numberProvider, boolean bl) {
		super(lootItemConditions);
		this.value = numberProvider;
		this.add = bl;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_COUNT;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.value.getReferencedContextParams();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		int i = this.add ? itemStack.getCount() : 0;
		itemStack.setCount(Mth.clamp(i + this.value.getInt(lootContext), 0, itemStack.getMaxStackSize()));
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider numberProvider) {
		return simpleBuilder(lootItemConditions -> new SetItemCountFunction(lootItemConditions, numberProvider, false));
	}

	public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider numberProvider, boolean bl) {
		return simpleBuilder(lootItemConditions -> new SetItemCountFunction(lootItemConditions, numberProvider, bl));
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemCountFunction> {
		public void serialize(JsonObject jsonObject, SetItemCountFunction setItemCountFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setItemCountFunction, jsonSerializationContext);
			jsonObject.add("count", jsonSerializationContext.serialize(setItemCountFunction.value));
			jsonObject.addProperty("add", setItemCountFunction.add);
		}

		public SetItemCountFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			NumberProvider numberProvider = GsonHelper.getAsObject(jsonObject, "count", jsonDeserializationContext, NumberProvider.class);
			boolean bl = GsonHelper.getAsBoolean(jsonObject, "add", false);
			return new SetItemCountFunction(lootItemConditions, numberProvider, bl);
		}
	}
}
