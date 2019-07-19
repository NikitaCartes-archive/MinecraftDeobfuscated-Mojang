package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

public class LootPoolEntries {
	private static final Map<ResourceLocation, LootPoolEntryContainer.Serializer<?>> ID_TO_SERIALIZER = Maps.<ResourceLocation, LootPoolEntryContainer.Serializer<?>>newHashMap();
	private static final Map<Class<?>, LootPoolEntryContainer.Serializer<?>> CLASS_TO_SERIALIZER = Maps.<Class<?>, LootPoolEntryContainer.Serializer<?>>newHashMap();

	private static void register(LootPoolEntryContainer.Serializer<?> serializer) {
		ID_TO_SERIALIZER.put(serializer.getName(), serializer);
		CLASS_TO_SERIALIZER.put(serializer.getContainerClass(), serializer);
	}

	static {
		register(CompositeEntryBase.createSerializer(new ResourceLocation("alternatives"), AlternativesEntry.class, AlternativesEntry::new));
		register(CompositeEntryBase.createSerializer(new ResourceLocation("sequence"), SequentialEntry.class, SequentialEntry::new));
		register(CompositeEntryBase.createSerializer(new ResourceLocation("group"), EntryGroup.class, EntryGroup::new));
		register(new EmptyLootItem.Serializer());
		register(new LootItem.Serializer());
		register(new LootTableReference.Serializer());
		register(new DynamicLoot.Serializer());
		register(new TagEntry.Serializer());
	}

	public static class Serializer implements JsonDeserializer<LootPoolEntryContainer>, JsonSerializer<LootPoolEntryContainer> {
		public LootPoolEntryContainer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entry");
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "type"));
			LootPoolEntryContainer.Serializer<?> serializer = (LootPoolEntryContainer.Serializer<?>)LootPoolEntries.ID_TO_SERIALIZER.get(resourceLocation);
			if (serializer == null) {
				throw new JsonParseException("Unknown item type: " + resourceLocation);
			} else {
				LootItemCondition[] lootItemConditions = GsonHelper.getAsObject(
					jsonObject, "conditions", new LootItemCondition[0], jsonDeserializationContext, LootItemCondition[].class
				);
				return serializer.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
			}
		}

		public JsonElement serialize(LootPoolEntryContainer lootPoolEntryContainer, Type type, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			LootPoolEntryContainer.Serializer<LootPoolEntryContainer> serializer = getSerializer(lootPoolEntryContainer.getClass());
			jsonObject.addProperty("type", serializer.getName().toString());
			if (!ArrayUtils.isEmpty((Object[])lootPoolEntryContainer.conditions)) {
				jsonObject.add("conditions", jsonSerializationContext.serialize(lootPoolEntryContainer.conditions));
			}

			serializer.serialize(jsonObject, lootPoolEntryContainer, jsonSerializationContext);
			return jsonObject;
		}

		private static LootPoolEntryContainer.Serializer<LootPoolEntryContainer> getSerializer(Class<?> class_) {
			LootPoolEntryContainer.Serializer<?> serializer = (LootPoolEntryContainer.Serializer<?>)LootPoolEntries.CLASS_TO_SERIALIZER.get(class_);
			if (serializer == null) {
				throw new JsonParseException("Unknown item type: " + class_);
			} else {
				return (LootPoolEntryContainer.Serializer<LootPoolEntryContainer>)serializer;
			}
		}
	}
}
