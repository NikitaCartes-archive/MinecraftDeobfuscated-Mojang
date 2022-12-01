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
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;

public class GsonHelper {
	private static final Gson GSON = new GsonBuilder().create();

	public static boolean isStringValue(JsonObject jsonObject, String string) {
		return !isValidPrimitive(jsonObject, string) ? false : jsonObject.getAsJsonPrimitive(string).isString();
	}

	public static boolean isStringValue(JsonElement jsonElement) {
		return !jsonElement.isJsonPrimitive() ? false : jsonElement.getAsJsonPrimitive().isString();
	}

	public static boolean isNumberValue(JsonObject jsonObject, String string) {
		return !isValidPrimitive(jsonObject, string) ? false : jsonObject.getAsJsonPrimitive(string).isNumber();
	}

	public static boolean isNumberValue(JsonElement jsonElement) {
		return !jsonElement.isJsonPrimitive() ? false : jsonElement.getAsJsonPrimitive().isNumber();
	}

	public static boolean isBooleanValue(JsonObject jsonObject, String string) {
		return !isValidPrimitive(jsonObject, string) ? false : jsonObject.getAsJsonPrimitive(string).isBoolean();
	}

	public static boolean isBooleanValue(JsonElement jsonElement) {
		return !jsonElement.isJsonPrimitive() ? false : jsonElement.getAsJsonPrimitive().isBoolean();
	}

	public static boolean isArrayNode(JsonObject jsonObject, String string) {
		return !isValidNode(jsonObject, string) ? false : jsonObject.get(string).isJsonArray();
	}

	public static boolean isObjectNode(JsonObject jsonObject, String string) {
		return !isValidNode(jsonObject, string) ? false : jsonObject.get(string).isJsonObject();
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

	@Nullable
	@Contract("_,_,!null->!null;_,_,null->_")
	public static String getAsString(JsonObject jsonObject, String string, @Nullable String string2) {
		return jsonObject.has(string) ? convertToString(jsonObject.get(string), string) : string2;
	}

	public static Item convertToItem(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive()) {
			String string2 = jsonElement.getAsString();
			return (Item)BuiltInRegistries.ITEM
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

	@Nullable
	@Contract("_,_,!null->!null;_,_,null->_")
	public static Item getAsItem(JsonObject jsonObject, String string, @Nullable Item item) {
		return jsonObject.has(string) ? convertToItem(jsonObject.get(string), string) : item;
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

	public static double convertToDouble(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
			return jsonElement.getAsDouble();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a Double, was " + getType(jsonElement));
		}
	}

	public static double getAsDouble(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToDouble(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a Double");
		}
	}

	public static double getAsDouble(JsonObject jsonObject, String string, double d) {
		return jsonObject.has(string) ? convertToDouble(jsonObject.get(string), string) : d;
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

	public static byte getAsByte(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToByte(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a Byte");
		}
	}

	public static byte getAsByte(JsonObject jsonObject, String string, byte b) {
		return jsonObject.has(string) ? convertToByte(jsonObject.get(string), string) : b;
	}

	public static char convertToCharacter(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
			return jsonElement.getAsCharacter();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a Character, was " + getType(jsonElement));
		}
	}

	public static char getAsCharacter(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToCharacter(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a Character");
		}
	}

	public static char getAsCharacter(JsonObject jsonObject, String string, char c) {
		return jsonObject.has(string) ? convertToCharacter(jsonObject.get(string), string) : c;
	}

	public static BigDecimal convertToBigDecimal(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
			return jsonElement.getAsBigDecimal();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a BigDecimal, was " + getType(jsonElement));
		}
	}

	public static BigDecimal getAsBigDecimal(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToBigDecimal(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a BigDecimal");
		}
	}

	public static BigDecimal getAsBigDecimal(JsonObject jsonObject, String string, BigDecimal bigDecimal) {
		return jsonObject.has(string) ? convertToBigDecimal(jsonObject.get(string), string) : bigDecimal;
	}

	public static BigInteger convertToBigInteger(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
			return jsonElement.getAsBigInteger();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a BigInteger, was " + getType(jsonElement));
		}
	}

	public static BigInteger getAsBigInteger(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToBigInteger(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a BigInteger");
		}
	}

	public static BigInteger getAsBigInteger(JsonObject jsonObject, String string, BigInteger bigInteger) {
		return jsonObject.has(string) ? convertToBigInteger(jsonObject.get(string), string) : bigInteger;
	}

	public static short convertToShort(JsonElement jsonElement, String string) {
		if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
			return jsonElement.getAsShort();
		} else {
			throw new JsonSyntaxException("Expected " + string + " to be a Short, was " + getType(jsonElement));
		}
	}

	public static short getAsShort(JsonObject jsonObject, String string) {
		if (jsonObject.has(string)) {
			return convertToShort(jsonObject.get(string), string);
		} else {
			throw new JsonSyntaxException("Missing " + string + ", expected to find a Short");
		}
	}

	public static short getAsShort(JsonObject jsonObject, String string, short s) {
		return jsonObject.has(string) ? convertToShort(jsonObject.get(string), string) : s;
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

	@Nullable
	@Contract("_,_,!null->!null;_,_,null->_")
	public static JsonObject getAsJsonObject(JsonObject jsonObject, String string, @Nullable JsonObject jsonObject2) {
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
	@Contract("_,_,!null->!null;_,_,null->_")
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

	@Nullable
	@Contract("_,_,!null,_,_->!null;_,_,null,_,_->_")
	public static <T> T getAsObject(
		JsonObject jsonObject, String string, @Nullable T object, JsonDeserializationContext jsonDeserializationContext, Class<? extends T> class_
	) {
		return jsonObject.has(string) ? convertToObject(jsonObject.get(string), string, jsonDeserializationContext, class_) : object;
	}

	public static String getType(@Nullable JsonElement jsonElement) {
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
	public static <T> T fromNullableJson(Gson gson, Reader reader, Class<T> class_, boolean bl) {
		try {
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(bl);
			return gson.<T>getAdapter(class_).read(jsonReader);
		} catch (IOException var5) {
			throw new JsonParseException(var5);
		}
	}

	public static <T> T fromJson(Gson gson, Reader reader, Class<T> class_, boolean bl) {
		T object = fromNullableJson(gson, reader, class_, bl);
		if (object == null) {
			throw new JsonParseException("JSON data was null or empty");
		} else {
			return object;
		}
	}

	@Nullable
	public static <T> T fromNullableJson(Gson gson, Reader reader, TypeToken<T> typeToken, boolean bl) {
		try {
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(bl);
			return gson.getAdapter(typeToken).read(jsonReader);
		} catch (IOException var5) {
			throw new JsonParseException(var5);
		}
	}

	public static <T> T fromJson(Gson gson, Reader reader, TypeToken<T> typeToken, boolean bl) {
		T object = fromNullableJson(gson, reader, typeToken, bl);
		if (object == null) {
			throw new JsonParseException("JSON data was null or empty");
		} else {
			return object;
		}
	}

	@Nullable
	public static <T> T fromNullableJson(Gson gson, String string, TypeToken<T> typeToken, boolean bl) {
		return fromNullableJson(gson, new StringReader(string), typeToken, bl);
	}

	public static <T> T fromJson(Gson gson, String string, Class<T> class_, boolean bl) {
		return fromJson(gson, new StringReader(string), class_, bl);
	}

	@Nullable
	public static <T> T fromNullableJson(Gson gson, String string, Class<T> class_, boolean bl) {
		return fromNullableJson(gson, new StringReader(string), class_, bl);
	}

	public static <T> T fromJson(Gson gson, Reader reader, TypeToken<T> typeToken) {
		return fromJson(gson, reader, typeToken, false);
	}

	@Nullable
	public static <T> T fromNullableJson(Gson gson, String string, TypeToken<T> typeToken) {
		return fromNullableJson(gson, string, typeToken, false);
	}

	public static <T> T fromJson(Gson gson, Reader reader, Class<T> class_) {
		return fromJson(gson, reader, class_, false);
	}

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

	public static JsonArray parseArray(String string) {
		return parseArray(new StringReader(string));
	}

	public static JsonArray parseArray(Reader reader) {
		return fromJson(GSON, reader, JsonArray.class, false);
	}

	public static String toStableString(JsonElement jsonElement) {
		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(stringWriter);

		try {
			writeValue(jsonWriter, jsonElement, Comparator.naturalOrder());
		} catch (IOException var4) {
			throw new AssertionError(var4);
		}

		return stringWriter.toString();
	}

	public static void writeValue(JsonWriter jsonWriter, @Nullable JsonElement jsonElement, @Nullable Comparator<String> comparator) throws IOException {
		if (jsonElement == null || jsonElement.isJsonNull()) {
			jsonWriter.nullValue();
		} else if (jsonElement.isJsonPrimitive()) {
			JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
			if (jsonPrimitive.isNumber()) {
				jsonWriter.value(jsonPrimitive.getAsNumber());
			} else if (jsonPrimitive.isBoolean()) {
				jsonWriter.value(jsonPrimitive.getAsBoolean());
			} else {
				jsonWriter.value(jsonPrimitive.getAsString());
			}
		} else if (jsonElement.isJsonArray()) {
			jsonWriter.beginArray();

			for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
				writeValue(jsonWriter, jsonElement2, comparator);
			}

			jsonWriter.endArray();
		} else {
			if (!jsonElement.isJsonObject()) {
				throw new IllegalArgumentException("Couldn't write " + jsonElement.getClass());
			}

			jsonWriter.beginObject();

			for (Entry<String, JsonElement> entry : sortByKeyIfNeeded(jsonElement.getAsJsonObject().entrySet(), comparator)) {
				jsonWriter.name((String)entry.getKey());
				writeValue(jsonWriter, (JsonElement)entry.getValue(), comparator);
			}

			jsonWriter.endObject();
		}
	}

	private static Collection<Entry<String, JsonElement>> sortByKeyIfNeeded(
		Collection<Entry<String, JsonElement>> collection, @Nullable Comparator<String> comparator
	) {
		if (comparator == null) {
			return collection;
		} else {
			List<Entry<String, JsonElement>> list = new ArrayList(collection);
			list.sort(Entry.comparingByKey(comparator));
			return list;
		}
	}
}
