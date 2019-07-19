package net.minecraft.world.level.storage.loot.predicates;

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
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class LootItemConditions {
	private static final Map<ResourceLocation, LootItemCondition.Serializer<?>> CONDITIONS_BY_NAME = Maps.<ResourceLocation, LootItemCondition.Serializer<?>>newHashMap();
	private static final Map<Class<? extends LootItemCondition>, LootItemCondition.Serializer<?>> CONDITIONS_BY_CLASS = Maps.<Class<? extends LootItemCondition>, LootItemCondition.Serializer<?>>newHashMap();

	public static <T extends LootItemCondition> void register(LootItemCondition.Serializer<? extends T> serializer) {
		ResourceLocation resourceLocation = serializer.getName();
		Class<T> class_ = (Class<T>)serializer.getPredicateClass();
		if (CONDITIONS_BY_NAME.containsKey(resourceLocation)) {
			throw new IllegalArgumentException("Can't re-register item condition name " + resourceLocation);
		} else if (CONDITIONS_BY_CLASS.containsKey(class_)) {
			throw new IllegalArgumentException("Can't re-register item condition class " + class_.getName());
		} else {
			CONDITIONS_BY_NAME.put(resourceLocation, serializer);
			CONDITIONS_BY_CLASS.put(class_, serializer);
		}
	}

	public static LootItemCondition.Serializer<?> getSerializer(ResourceLocation resourceLocation) {
		LootItemCondition.Serializer<?> serializer = (LootItemCondition.Serializer<?>)CONDITIONS_BY_NAME.get(resourceLocation);
		if (serializer == null) {
			throw new IllegalArgumentException("Unknown loot item condition '" + resourceLocation + "'");
		} else {
			return serializer;
		}
	}

	public static <T extends LootItemCondition> LootItemCondition.Serializer<T> getSerializer(T lootItemCondition) {
		LootItemCondition.Serializer<T> serializer = (LootItemCondition.Serializer<T>)CONDITIONS_BY_CLASS.get(lootItemCondition.getClass());
		if (serializer == null) {
			throw new IllegalArgumentException("Unknown loot item condition " + lootItemCondition);
		} else {
			return serializer;
		}
	}

	public static <T> Predicate<T> andConditions(Predicate<T>[] predicates) {
		switch (predicates.length) {
			case 0:
				return object -> true;
			case 1:
				return predicates[0];
			case 2:
				return predicates[0].and(predicates[1]);
			default:
				return object -> {
					for (Predicate<T> predicate : predicates) {
						if (!predicate.test(object)) {
							return false;
						}
					}

					return true;
				};
		}
	}

	public static <T> Predicate<T> orConditions(Predicate<T>[] predicates) {
		switch (predicates.length) {
			case 0:
				return object -> false;
			case 1:
				return predicates[0];
			case 2:
				return predicates[0].or(predicates[1]);
			default:
				return object -> {
					for (Predicate<T> predicate : predicates) {
						if (predicate.test(object)) {
							return true;
						}
					}

					return false;
				};
		}
	}

	static {
		register(new InvertedLootItemCondition.Serializer());
		register(new AlternativeLootItemCondition.Serializer());
		register(new LootItemRandomChanceCondition.Serializer());
		register(new LootItemRandomChanceWithLootingCondition.Serializer());
		register(new LootItemEntityPropertyCondition.Serializer());
		register(new LootItemKilledByPlayerCondition.Serializer());
		register(new EntityHasScoreCondition.Serializer());
		register(new LootItemBlockStatePropertyCondition.Serializer());
		register(new MatchTool.Serializer());
		register(new BonusLevelTableCondition.Serializer());
		register(new ExplosionCondition.Serializer());
		register(new DamageSourceCondition.Serializer());
		register(new LocationCheck.Serializer());
		register(new WeatherCheck.Serializer());
	}

	public static class Serializer implements JsonDeserializer<LootItemCondition>, JsonSerializer<LootItemCondition> {
		public LootItemCondition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "condition");
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "condition"));

			LootItemCondition.Serializer<?> serializer;
			try {
				serializer = LootItemConditions.getSerializer(resourceLocation);
			} catch (IllegalArgumentException var8) {
				throw new JsonSyntaxException("Unknown condition '" + resourceLocation + "'");
			}

			return serializer.deserialize(jsonObject, jsonDeserializationContext);
		}

		public JsonElement serialize(LootItemCondition lootItemCondition, Type type, JsonSerializationContext jsonSerializationContext) {
			LootItemCondition.Serializer<LootItemCondition> serializer = LootItemConditions.getSerializer(lootItemCondition);
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("condition", serializer.getName().toString());
			serializer.serialize(jsonObject, lootItemCondition, jsonSerializationContext);
			return jsonObject;
		}
	}
}
