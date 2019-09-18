package net.minecraft.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.StringUtils;

public class GsonHelper {
	private static final Gson GSON = new GsonBuilder().create();

	public static boolean isStringValue(JsonObject jsonObject, String string) {
		return !isValidPrimitive(jsonObject, string) ? false : jsonObject.getAsJsonPrimitive(string).isString();
	}

	@Environment(EnvType.CLIENT)
	public static boolean isStringValue(JsonElement jsonElement) {
		return !jsonElement.isJsonPrimitive() ? false : jsonElement.getAsJsonPrimitive().isString();
	}

	public static boolean isNumberValue(JsonElement jsonElement) {
		return !jsonElement.isJsonPrimitive() ? false : jsonElement.getAsJsonPrimitive().isNumber();
	}

	@Environment(EnvType.CLIENT)
	public static boolean isBooleanValue(JsonObject jsonObject, String string) {
		return !isValidPrimitive(jsonObject, string) ? false : jsonObject.getAsJsonPrimitive(string).isBoolean();
	}

	public static boolean isArrayNode(JsonObject jsonObject, String string) {
		return !isValidNode(jsonObject, string) ? false : jsonObject.get(string).isJsonArray();
	}

	public static boolean isValidPrimitive(JsonObject jsonObject, String string) {
		return !isValidNode(jsonObject, string) ? false : jsonObject.get(string).isJsonPrimitive();
	}

	public static boolean isValidNode(JsonObject jsonObject, String string) {
		return jsonObject == null ? false : jsonObject.get(string) != null;
	}

	public static String convertToString(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive()) {
			return jsonElement.getAsString();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a string, was " + getType(jsonElement));
		}
	}

	public static String getAsString(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToString(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a string");
		}
	}

	public static String getAsString(JsonObject jsonObject, String string, String string2) {
		return jsonObject.has(string) ? convertToString(jsonObject.get(string), string) : string2;
	}

	public static Item convertToItem(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive()) {
			String string2 = jsonElement.getAsString();
			return (Item)Registry.ITEM
				.getOptional(new ResourceLocation(string2))
				.orElseThrow(() -> new JsonSyntaxException("Expected " + string + " to be an item, was unknown string '" + string2 + "'"));
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be an item, was " + getType(jsonElement));
		}
	}

	public static Item getAsItem(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToItem(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find an item");
		}
	}

	public static boolean convertToBoolean(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive()) {
			return jsonElement.getAsBoolean();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a Boolean, was " + getType(jsonElement));
		}
	}

	public static boolean getAsBoolean(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToBoolean(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a Boolean");
		}
	}

	public static boolean getAsBoolean(JsonObject jsonObject, String string, boolean bl) {
		return jsonObject.has(string) ? convertToBoolean(jsonObject.get(string), string) : bl;
	}

	public static float convertToFloat(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
			return jsonElement.getAsFloat();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a Float, was " + getType(jsonElement));
		}
	}

	public static float getAsFloat(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToFloat(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a Float");
		}
	}

	public static float getAsFloat(JsonObject jsonObject, String string, float f) {
		return jsonObject.has(string) ? convertToFloat(jsonObject.get(string), string) : f;
	}

	public static long convertToLong(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
			return jsonElement.getAsLong();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a Long, was " + getType(jsonElement));
		}
	}

	public static long getAsLong(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToLong(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a Long");
		}
	}

	public static long getAsLong(JsonObject jsonObject, String string, long l) {
		return jsonObject.has(string) ? convertToLong(jsonObject.get(string), string) : l;
	}

	public static int convertToInt(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
			return jsonElement.getAsInt();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a Int, was " + getType(jsonElement));
		}
	}

	public static int getAsInt(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToInt(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a Int");
		}
	}

	public static int getAsInt(JsonObject jsonObject, String string, int i) {
		return jsonObject.has(string) ? convertToInt(jsonObject.get(string), string) : i;
	}

	public static byte convertToByte(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
			return jsonElement.getAsByte();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a Byte, was " + getType(jsonElement));
		}
	}

	public static byte getAsByte(JsonObject jsonObject, String string, byte b) {
		return jsonObject.has(string) ? convertToByte(jsonObject.get(string), string) : b;
	}

	public static JsonObject convertToJsonObject(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonObject()) {
			return jsonElement.getAsJsonObject();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a JsonObject, was " + getType(jsonElement));
		}
	}

	public static JsonObject getAsJsonObject(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToJsonObject(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a JsonObject");
		}
	}

	public static JsonObject getAsJsonObject(JsonObject jsonObject, String string, JsonObject jsonObject2) {
		return jsonObject.has(string) ? convertToJsonObject(jsonObject.get(string), string) : jsonObject2;
	}

	public static JsonArray convertToJsonArray(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonArray()) {
			return jsonElement.getAsJsonArray();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a JsonArray, was " + getType(jsonElement));
		}
	}

	public static JsonArray getAsJsonArray(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToJsonArray(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a JsonArray");
		}
	}

	@Nullable
	public static JsonArray getAsJsonArray(JsonObject jsonObject, String string, @Nullable JsonArray jsonArray) {
		return jsonObject.has(string) ? convertToJsonArray(jsonObject.get(string), string) : jsonArray;
	}

	public static <T> T convertToObject(
		@Nullable JsonElement jsonElement, String string, JsonDeserializationContext jsonDeserializationContext, Class<? extends T> class_
	) {
		if (jsonElement != null) {
			return jsonDeserializationContext.deserialize(jsonElement, class_);
		} else {
			throw new JsonSyntaxException("Missing " + string);
		}
	}

	public static <T> T getAsObject(JsonObject jsonObject, String string, JsonDeserializationContext jsonDeserializationContext, Class<? extends T> class_) {
		if (jsonObject.has(string)) {
			return convertToObject(jsonObject.get(string), string, jsonDeserializationContext, class_);
		} else {
			throw new JsonSyntaxException("Missing " + string);
		}
	}

	public static <T> T getAsObject(
		JsonObject jsonObject, String string, T object, JsonDeserializationContext jsonDeserializationContext, Class<? extends T> class_
	) {
		return jsonObject.has(string) ? convertToObject(jsonObject.get(string), string, jsonDeserializationContext, class_) : object;
	}

	public static String getType(JsonElement jsonElement) {
		String string = StringUtils.abbreviateMiddle(String.valueOf(jsonElement), "...", 10);
		if (jsonElement == null) {
			return "null (missing)";
		} else if (jsonElement.isJsonNull()) {
			return "null (json)";
		} else if (jsonElement.isJsonArray()) {
			return "an array (" + string + ")";
		} else if (jsonElement.isJsonObject()) {
			return "an object (" + string + ")";
		} else {
			if (jsonElement.isJsonPrimitive()) {
				JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
				if (jsonPrimitive.isNumber()) {
					return "a number (" + string + ")";
				}

				if (jsonPrimitive.isBoolean()) {
					return "a boolean (" + string + ")";
				}
			}

			return string;
		}
	}

	@Nullable
	public static <T> T fromJson(Gson gson, Reader reader, Class<T> class_, boolean bl) {
		try {
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(bl);
			return gson.<T>getAdapter(class_).read(jsonReader);
		} catch (IOException var5) {
			throw new JsonParseException(var5);
		}
	}

	@Nullable
	public static <T> T fromJson(Gson gson, Reader reader, Type type, boolean bl) {
		try {
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(bl);
			return (T)gson.getAdapter(TypeToken.get(type)).read(jsonReader);
		} catch (IOException var5) {
			throw new JsonParseException(var5);
		}
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public static <T> T fromJson(Gson gson, String string, Type type, boolean bl) {
		return fromJson(gson, new StringReader(string), type, bl);
	}

	@Nullable
	public static <T> T fromJson(Gson gson, String string, Class<T> class_, boolean bl) {
		return fromJson(gson, new StringReader(string), class_, bl);
	}

	@Nullable
	public static <T> T fromJson(Gson gson, Reader reader, Type type) {
		return fromJson(gson, reader, type, false);
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public static <T> T fromJson(Gson gson, String string, Type type) {
		return fromJson(gson, string, type, false);
	}

	@Nullable
	public static <T> T fromJson(Gson gson, Reader reader, Class<T> class_) {
		return fromJson(gson, reader, class_, false);
	}

	@Nullable
	public static <T> T fromJson(Gson gson, String string, Class<T> class_) {
		return fromJson(gson, string, class_, false);
	}

	public static JsonObject parse(String string, boolean bl) {
		return parse(new StringReader(string), bl);
	}

	public static JsonObject parse(Reader reader, boolean bl) {
		return fromJson(GSON, reader, JsonObject.class, bl);
	}

	public static JsonObject parse(String string) {
		return parse(string, false);
	}

	public static JsonObject parse(Reader reader) {
		return parse(reader, false);
	}
}
