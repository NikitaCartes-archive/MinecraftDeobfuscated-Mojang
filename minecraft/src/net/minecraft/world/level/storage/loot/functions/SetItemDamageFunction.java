package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SetItemDamageFunction extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogManager.getLogger();
	private final RandomValueBounds damage;

	private SetItemDamageFunction(LootItemCondition[] lootItemConditions, RandomValueBounds randomValueBounds) {
		super(lootItemConditions);
		this.damage = randomValueBounds;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_DAMAGE;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.isDamageableItem()) {
			float f = 1.0F - this.damage.getFloat(lootContext.getRandom());
			itemStack.setDamageValue(Mth.floor(f * (float)itemStack.getMaxDamage()));
		} else {
			LOGGER.warn("Couldn't set damage of loot item {}", itemStack);
		}

		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setDamage(RandomValueBounds randomValueBounds) {
		return simpleBuilder(lootItemConditions -> new SetItemDamageFunction(lootItemConditions, randomValueBounds));
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemDamageFunction> {
		public void serialize(JsonObject jsonObject, SetItemDamageFunction setItemDamageFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setItemDamageFunction, jsonSerializationContext);
			jsonObject.add("damage", jsonSerializationContext.serialize(setItemDamageFunction.damage));
		}

		public SetItemDamageFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			return new SetItemDamageFunction(lootItemConditions, GsonHelper.getAsObject(jsonObject, "damage", jsonDeserializationContext, RandomValueBounds.class));
		}
	}
}
