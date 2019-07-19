package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetNbtFunction extends LootItemConditionalFunction {
	private final CompoundTag tag;

	private SetNbtFunction(LootItemCondition[] lootItemConditions, CompoundTag compoundTag) {
		super(lootItemConditions);
		this.tag = compoundTag;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.getOrCreateTag().merge(this.tag);
		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> setTag(CompoundTag compoundTag) {
		return simpleBuilder(lootItemConditions -> new SetNbtFunction(lootItemConditions, compoundTag));
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetNbtFunction> {
		public Serializer() {
			super(new ResourceLocation("set_nbt"), SetNbtFunction.class);
		}

		public void serialize(JsonObject jsonObject, SetNbtFunction setNbtFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setNbtFunction, jsonSerializationContext);
			jsonObject.addProperty("tag", setNbtFunction.tag.toString());
		}

		public SetNbtFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			try {
				CompoundTag compoundTag = TagParser.parseTag(GsonHelper.getAsString(jsonObject, "tag"));
				return new SetNbtFunction(lootItemConditions, compoundTag);
			} catch (CommandSyntaxException var5) {
				throw new JsonSyntaxException(var5.getMessage());
			}
		}
	}
}
