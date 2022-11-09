package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetPotionFunction extends LootItemConditionalFunction {
	final Potion potion;

	SetPotionFunction(LootItemCondition[] lootItemConditions, Potion potion) {
		super(lootItemConditions);
		this.potion = potion;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_POTION;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		PotionUtils.setPotion(itemStack, this.potion);
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setPotion(Potion potion) {
		return simpleBuilder(lootItemConditions -> new SetPotionFunction(lootItemConditions, potion));
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetPotionFunction> {
		public void serialize(JsonObject jsonObject, SetPotionFunction setPotionFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setPotionFunction, jsonSerializationContext);
			jsonObject.addProperty("id", BuiltInRegistries.POTION.getKey(setPotionFunction.potion).toString());
		}

		public SetPotionFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			String string = GsonHelper.getAsString(jsonObject, "id");
			Potion potion = (Potion)BuiltInRegistries.POTION
				.getOptional(ResourceLocation.tryParse(string))
				.orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + string + "'"));
			return new SetPotionFunction(lootItemConditions, potion);
		}
	}
}
