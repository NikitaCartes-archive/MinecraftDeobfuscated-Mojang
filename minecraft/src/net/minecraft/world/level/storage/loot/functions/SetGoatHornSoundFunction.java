package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.GoatHornItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetGoatHornSoundFunction extends LootItemConditionalFunction {
	SetGoatHornSoundFunction(LootItemCondition[] lootItemConditions) {
		super(lootItemConditions);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_GOAT_HORN_SOUND;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		GoatHornItem.setRandomNonScreamingSound(itemStack, lootContext.getRandom());
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setGoatHornSounds() {
		return simpleBuilder(SetGoatHornSoundFunction::new);
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetGoatHornSoundFunction> {
		public void serialize(JsonObject jsonObject, SetGoatHornSoundFunction setGoatHornSoundFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setGoatHornSoundFunction, jsonSerializationContext);
		}

		public SetGoatHornSoundFunction deserialize(
			JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions
		) {
			return new SetGoatHornSoundFunction(lootItemConditions);
		}
	}
}
