package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class LootItemFunctions {
	private static final Map<ResourceLocation, LootItemFunction.Serializer<?>> FUNCTIONS_BY_NAME = Maps.<ResourceLocation, LootItemFunction.Serializer<?>>newHashMap();
	private static final Map<Class<? extends LootItemFunction>, LootItemFunction.Serializer<?>> FUNCTIONS_BY_CLASS = Maps.<Class<? extends LootItemFunction>, LootItemFunction.Serializer<?>>newHashMap();
	public static final BiFunction<ItemStack, LootContext, ItemStack> IDENTITY = (itemStack, lootContext) -> itemStack;

	public static <T extends LootItemFunction> void register(LootItemFunction.Serializer<? extends T> serializer) {
		ResourceLocation resourceLocation = serializer.getName();
		Class<T> class_ = (Class<T>)serializer.getFunctionClass();
		if (FUNCTIONS_BY_NAME.containsKey(resourceLocation)) {
			throw new IllegalArgumentException("Can't re-register item function name " + resourceLocation);
		} else if (FUNCTIONS_BY_CLASS.containsKey(class_)) {
			throw new IllegalArgumentException("Can't re-register item function class " + class_.getName());
		} else {
			FUNCTIONS_BY_NAME.put(resourceLocation, serializer);
			FUNCTIONS_BY_CLASS.put(class_, serializer);
		}
	}

	public static LootItemFunction.Serializer<?> getSerializer(ResourceLocation resourceLocation) {
		LootItemFunction.Serializer<?> serializer = (LootItemFunction.Serializer<?>)FUNCTIONS_BY_NAME.get(resourceLocation);
		if (serializer == null) {
			throw new IllegalArgumentException("Unknown loot item function '" + resourceLocation + "'");
		} else {
			return serializer;
		}
	}

	public static <T extends LootItemFunction> LootItemFunction.Serializer<T> getSerializer(T lootItemFunction) {
		LootItemFunction.Serializer<T> serializer = (LootItemFunction.Serializer<T>)FUNCTIONS_BY_CLASS.get(lootItemFunction.getClass());
		if (serializer == null) {
			throw new IllegalArgumentException("Unknown loot item function " + lootItemFunction);
		} else {
			return serializer;
		}
	}

	public static BiFunction<ItemStack, LootContext, ItemStack> compose(BiFunction<ItemStack, LootContext, ItemStack>[] biFunctions) {
		switch (biFunctions.length) {
			case 0:
				return IDENTITY;
			case 1:
				return biFunctions[0];
			case 2:
				BiFunction<ItemStack, LootContext, ItemStack> biFunction = biFunctions[0];
				BiFunction<ItemStack, LootContext, ItemStack> biFunction2 = biFunctions[1];
				return (itemStack, lootContext) -> (ItemStack)biFunction2.apply(biFunction.apply(itemStack, lootContext), lootContext);
			default:
				return (itemStack, lootContext) -> {
					for (BiFunction<ItemStack, LootContext, ItemStack> biFunctionx : biFunctions) {
						itemStack = (ItemStack)biFunctionx.apply(itemStack, lootContext);
					}

					return itemStack;
				};
		}
	}

	static {
		register(new SetItemCountFunction.Serializer());
		register(new EnchantWithLevelsFunction.Serializer());
		register(new EnchantRandomlyFunction.Serializer());
		register(new SetNbtFunction.Serializer());
		register(new SmeltItemFunction.Serializer());
		register(new LootingEnchantFunction.Serializer());
		register(new SetItemDamageFunction.Serializer());
		register(new SetAttributesFunction.Serializer());
		register(new SetNameFunction.Serializer());
		register(new ExplorationMapFunction.Serializer());
		register(new SetStewEffectFunction.Serializer());
		register(new CopyNameFunction.Serializer());
		register(new SetContainerContents.Serializer());
		register(new LimitCount.Serializer());
		register(new ApplyBonusCount.Serializer());
		register(new SetContainerLootTable.Serializer());
		register(new ApplyExplosionDecay.Serializer());
		register(new SetLoreFunction.Serializer());
		register(new FillPlayerHead.Serializer());
		register(new CopyNbtFunction.Serializer());
	}

	public static class Serializer implements JsonDeserializer<LootItemFunction>, JsonSerializer<LootItemFunction> {
		public LootItemFunction deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "function");
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "function"));

			LootItemFunction.Serializer<?> serializer;
			try {
				serializer = LootItemFunctions.getSerializer(resourceLocation);
			} catch (IllegalArgumentException var8) {
				throw new JsonSyntaxException("Unknown function '" + resourceLocation + "'");
			}

			return serializer.deserialize(jsonObject, jsonDeserializationContext);
		}

		public JsonElement serialize(LootItemFunction lootItemFunction, Type type, JsonSerializationContext jsonSerializationContext) {
			LootItemFunction.Serializer<LootItemFunction> serializer = LootItemFunctions.getSerializer(lootItemFunction);
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("function", serializer.getName().toString());
			serializer.serialize(jsonObject, lootItemFunction, jsonSerializationContext);
			return jsonObject;
		}
	}
}
