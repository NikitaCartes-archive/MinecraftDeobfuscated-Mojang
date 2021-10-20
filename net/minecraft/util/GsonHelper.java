/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.math.BigDecimal;
import java.math.BigInteger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class GsonHelper {
    private static final Gson GSON = new GsonBuilder().create();

    public static boolean isStringValue(JsonObject jsonObject, String string) {
        if (!GsonHelper.isValidPrimitive(jsonObject, string)) {
            return false;
        }
        return jsonObject.getAsJsonPrimitive(string).isString();
    }

    public static boolean isStringValue(JsonElement jsonElement) {
        if (!jsonElement.isJsonPrimitive()) {
            return false;
        }
        return jsonElement.getAsJsonPrimitive().isString();
    }

    public static boolean isNumberValue(JsonObject jsonObject, String string) {
        if (!GsonHelper.isValidPrimitive(jsonObject, string)) {
            return false;
        }
        return jsonObject.getAsJsonPrimitive(string).isNumber();
    }

    public static boolean isNumberValue(JsonElement jsonElement) {
        if (!jsonElement.isJsonPrimitive()) {
            return false;
        }
        return jsonElement.getAsJsonPrimitive().isNumber();
    }

    public static boolean isBooleanValue(JsonObject jsonObject, String string) {
        if (!GsonHelper.isValidPrimitive(jsonObject, string)) {
            return false;
        }
        return jsonObject.getAsJsonPrimitive(string).isBoolean();
    }

    public static boolean isBooleanValue(JsonElement jsonElement) {
        if (!jsonElement.isJsonPrimitive()) {
            return false;
        }
        return jsonElement.getAsJsonPrimitive().isBoolean();
    }

    public static boolean isArrayNode(JsonObject jsonObject, String string) {
        if (!GsonHelper.isValidNode(jsonObject, string)) {
            return false;
        }
        return jsonObject.get(string).isJsonArray();
    }

    public static boolean isObjectNode(JsonObject jsonObject, String string) {
        if (!GsonHelper.isValidNode(jsonObject, string)) {
            return false;
        }
        return jsonObject.get(string).isJsonObject();
    }

    public static boolean isValidPrimitive(JsonObject jsonObject, String string) {
        if (!GsonHelper.isValidNode(jsonObject, string)) {
            return false;
        }
        return jsonObject.get(string).isJsonPrimitive();
    }

    public static boolean isValidNode(JsonObject jsonObject, String string) {
        if (jsonObject == null) {
            return false;
        }
        return jsonObject.get(string) != null;
    }

    public static String convertToString(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive()) {
            return jsonElement.getAsString();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a string, was " + GsonHelper.getType(jsonElement));
    }

    public static String getAsString(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToString(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a string");
    }

    @Nullable
    @Contract(value="_,_,!null->!null;_,_,null->_")
    public static String getAsString(JsonObject jsonObject, String string, @Nullable String string2) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToString(jsonObject.get(string), string);
        }
        return string2;
    }

    public static Item convertToItem(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive()) {
            String string2 = jsonElement.getAsString();
            return Registry.ITEM.getOptional(new ResourceLocation(string2)).orElseThrow(() -> new JsonSyntaxException("Expected " + string + " to be an item, was unknown string '" + string2 + "'"));
        }
        throw new JsonSyntaxException("Expected " + string + " to be an item, was " + GsonHelper.getType(jsonElement));
    }

    public static Item getAsItem(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToItem(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find an item");
    }

    @Nullable
    @Contract(value="_,_,!null->!null;_,_,null->_")
    public static Item getAsItem(JsonObject jsonObject, String string, @Nullable Item item) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToItem(jsonObject.get(string), string);
        }
        return item;
    }

    public static boolean convertToBoolean(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive()) {
            return jsonElement.getAsBoolean();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a Boolean, was " + GsonHelper.getType(jsonElement));
    }

    public static boolean getAsBoolean(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToBoolean(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a Boolean");
    }

    public static boolean getAsBoolean(JsonObject jsonObject, String string, boolean bl) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToBoolean(jsonObject.get(string), string);
        }
        return bl;
    }

    public static double convertToDouble(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsDouble();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a Double, was " + GsonHelper.getType(jsonElement));
    }

    public static double getAsDouble(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToDouble(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a Double");
    }

    public static double getAsDouble(JsonObject jsonObject, String string, double d) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToDouble(jsonObject.get(string), string);
        }
        return d;
    }

    public static float convertToFloat(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsFloat();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a Float, was " + GsonHelper.getType(jsonElement));
    }

    public static float getAsFloat(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToFloat(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a Float");
    }

    public static float getAsFloat(JsonObject jsonObject, String string, float f) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToFloat(jsonObject.get(string), string);
        }
        return f;
    }

    public static long convertToLong(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsLong();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a Long, was " + GsonHelper.getType(jsonElement));
    }

    public static long getAsLong(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToLong(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a Long");
    }

    public static long getAsLong(JsonObject jsonObject, String string, long l) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToLong(jsonObject.get(string), string);
        }
        return l;
    }

    public static int convertToInt(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsInt();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a Int, was " + GsonHelper.getType(jsonElement));
    }

    public static int getAsInt(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToInt(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a Int");
    }

    public static int getAsInt(JsonObject jsonObject, String string, int i) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToInt(jsonObject.get(string), string);
        }
        return i;
    }

    public static byte convertToByte(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsByte();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a Byte, was " + GsonHelper.getType(jsonElement));
    }

    public static byte getAsByte(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToByte(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a Byte");
    }

    public static byte getAsByte(JsonObject jsonObject, String string, byte b) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToByte(jsonObject.get(string), string);
        }
        return b;
    }

    public static char convertToCharacter(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsCharacter();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a Character, was " + GsonHelper.getType(jsonElement));
    }

    public static char getAsCharacter(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToCharacter(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a Character");
    }

    public static char getAsCharacter(JsonObject jsonObject, String string, char c) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToCharacter(jsonObject.get(string), string);
        }
        return c;
    }

    public static BigDecimal convertToBigDecimal(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsBigDecimal();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a BigDecimal, was " + GsonHelper.getType(jsonElement));
    }

    public static BigDecimal getAsBigDecimal(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToBigDecimal(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a BigDecimal");
    }

    public static BigDecimal getAsBigDecimal(JsonObject jsonObject, String string, BigDecimal bigDecimal) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToBigDecimal(jsonObject.get(string), string);
        }
        return bigDecimal;
    }

    public static BigInteger convertToBigInteger(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsBigInteger();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a BigInteger, was " + GsonHelper.getType(jsonElement));
    }

    public static BigInteger getAsBigInteger(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToBigInteger(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a BigInteger");
    }

    public static BigInteger getAsBigInteger(JsonObject jsonObject, String string, BigInteger bigInteger) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToBigInteger(jsonObject.get(string), string);
        }
        return bigInteger;
    }

    public static short convertToShort(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            return jsonElement.getAsShort();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a Short, was " + GsonHelper.getType(jsonElement));
    }

    public static short getAsShort(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToShort(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a Short");
    }

    public static short getAsShort(JsonObject jsonObject, String string, short s) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToShort(jsonObject.get(string), string);
        }
        return s;
    }

    public static JsonObject convertToJsonObject(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonObject()) {
            return jsonElement.getAsJsonObject();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a JsonObject, was " + GsonHelper.getType(jsonElement));
    }

    public static JsonObject getAsJsonObject(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToJsonObject(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a JsonObject");
    }

    @Nullable
    @Contract(value="_,_,!null->!null;_,_,null->_")
    public static JsonObject getAsJsonObject(JsonObject jsonObject, String string, @Nullable JsonObject jsonObject2) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToJsonObject(jsonObject.get(string), string);
        }
        return jsonObject2;
    }

    public static JsonArray convertToJsonArray(JsonElement jsonElement, String string) {
        if (jsonElement.isJsonArray()) {
            return jsonElement.getAsJsonArray();
        }
        throw new JsonSyntaxException("Expected " + string + " to be a JsonArray, was " + GsonHelper.getType(jsonElement));
    }

    public static JsonArray getAsJsonArray(JsonObject jsonObject, String string) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToJsonArray(jsonObject.get(string), string);
        }
        throw new JsonSyntaxException("Missing " + string + ", expected to find a JsonArray");
    }

    @Nullable
    @Contract(value="_,_,!null->!null;_,_,null->_")
    public static JsonArray getAsJsonArray(JsonObject jsonObject, String string, @Nullable JsonArray jsonArray) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToJsonArray(jsonObject.get(string), string);
        }
        return jsonArray;
    }

    public static <T> T convertToObject(@Nullable JsonElement jsonElement, String string, JsonDeserializationContext jsonDeserializationContext, Class<? extends T> class_) {
        if (jsonElement != null) {
            return jsonDeserializationContext.deserialize(jsonElement, class_);
        }
        throw new JsonSyntaxException("Missing " + string);
    }

    public static <T> T getAsObject(JsonObject jsonObject, String string, JsonDeserializationContext jsonDeserializationContext, Class<? extends T> class_) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToObject(jsonObject.get(string), string, jsonDeserializationContext, class_);
        }
        throw new JsonSyntaxException("Missing " + string);
    }

    @Nullable
    @Contract(value="_,_,!null,_,_->!null;_,_,null,_,_->_")
    public static <T> T getAsObject(JsonObject jsonObject, String string, @Nullable T object, JsonDeserializationContext jsonDeserializationContext, Class<? extends T> class_) {
        if (jsonObject.has(string)) {
            return GsonHelper.convertToObject(jsonObject.get(string), string, jsonDeserializationContext, class_);
        }
        return object;
    }

    public static String getType(@Nullable JsonElement jsonElement) {
        String string = StringUtils.abbreviateMiddle(String.valueOf(jsonElement), "...", 10);
        if (jsonElement == null) {
            return "null (missing)";
        }
        if (jsonElement.isJsonNull()) {
            return "null (json)";
        }
        if (jsonElement.isJsonArray()) {
            return "an array (" + string + ")";
        }
        if (jsonElement.isJsonObject()) {
            return "an object (" + string + ")";
        }
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

    @Nullable
    public static <T> T fromJson(Gson gson, Reader reader, Class<T> class_, boolean bl) {
        try {
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(bl);
            return gson.getAdapter(class_).read(jsonReader);
        } catch (IOException iOException) {
            throw new JsonParseException(iOException);
        }
    }

    @Nullable
    public static <T> T fromJson(Gson gson, Reader reader, TypeToken<T> typeToken, boolean bl) {
        try {
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(bl);
            return gson.getAdapter(typeToken).read(jsonReader);
        } catch (IOException iOException) {
            throw new JsonParseException(iOException);
        }
    }

    @Nullable
    public static <T> T fromJson(Gson gson, String string, TypeToken<T> typeToken, boolean bl) {
        return GsonHelper.fromJson(gson, (Reader)new StringReader(string), typeToken, bl);
    }

    @Nullable
    public static <T> T fromJson(Gson gson, String string, Class<T> class_, boolean bl) {
        return GsonHelper.fromJson(gson, (Reader)new StringReader(string), class_, bl);
    }

    @Nullable
    public static <T> T fromJson(Gson gson, Reader reader, TypeToken<T> typeToken) {
        return GsonHelper.fromJson(gson, reader, typeToken, false);
    }

    @Nullable
    public static <T> T fromJson(Gson gson, String string, TypeToken<T> typeToken) {
        return GsonHelper.fromJson(gson, string, typeToken, false);
    }

    @Nullable
    public static <T> T fromJson(Gson gson, Reader reader, Class<T> class_) {
        return GsonHelper.fromJson(gson, reader, class_, false);
    }

    @Nullable
    public static <T> T fromJson(Gson gson, String string, Class<T> class_) {
        return GsonHelper.fromJson(gson, string, class_, false);
    }

    public static JsonObject parse(String string, boolean bl) {
        return GsonHelper.parse(new StringReader(string), bl);
    }

    public static JsonObject parse(Reader reader, boolean bl) {
        return GsonHelper.fromJson(GSON, reader, JsonObject.class, bl);
    }

    public static JsonObject parse(String string) {
        return GsonHelper.parse(string, false);
    }

    public static JsonObject parse(Reader reader) {
        return GsonHelper.parse(reader, false);
    }

    public static JsonArray parseArray(Reader reader) {
        return GsonHelper.fromJson(GSON, reader, JsonArray.class, false);
    }
}

