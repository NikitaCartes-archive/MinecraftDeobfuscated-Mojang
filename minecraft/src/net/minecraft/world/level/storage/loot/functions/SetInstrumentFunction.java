package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetInstrumentFunction extends LootItemConditionalFunction {
	final TagKey<Instrument> options;

	SetInstrumentFunction(LootItemCondition[] lootItemConditions, TagKey<Instrument> tagKey) {
		super(lootItemConditions);
		this.options = tagKey;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_INSTRUMENT;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		InstrumentItem.setRandom(itemStack, this.options, lootContext.getRandom());
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setInstrumentOptions(TagKey<Instrument> tagKey) {
		return simpleBuilder(lootItemConditions -> new SetInstrumentFunction(lootItemConditions, tagKey));
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetInstrumentFunction> {
		public void serialize(JsonObject jsonObject, SetInstrumentFunction setInstrumentFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setInstrumentFunction, jsonSerializationContext);
			jsonObject.addProperty("options", "#" + setInstrumentFunction.options.location());
		}

		public SetInstrumentFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			String string = GsonHelper.getAsString(jsonObject, "options");
			if (!string.startsWith("#")) {
				throw new JsonSyntaxException("Inline tag value not supported: " + string);
			} else {
				return new SetInstrumentFunction(lootItemConditions, TagKey.create(Registry.INSTRUMENT_REGISTRY, new ResourceLocation(string.substring(1))));
			}
		}
	}
}
