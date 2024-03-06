package net.minecraft.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Field;
import net.minecraft.CharPredicate;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;

public class ParserUtils {
	private static final Field JSON_READER_POS = Util.make(() -> {
		try {
			Field field = JsonReader.class.getDeclaredField("pos");
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException var1) {
			throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", var1);
		}
	});
	private static final Field JSON_READER_LINESTART = Util.make(() -> {
		try {
			Field field = JsonReader.class.getDeclaredField("lineStart");
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException var1) {
			throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", var1);
		}
	});

	private static int getPos(JsonReader jsonReader) {
		try {
			return JSON_READER_POS.getInt(jsonReader) - JSON_READER_LINESTART.getInt(jsonReader);
		} catch (IllegalAccessException var2) {
			throw new IllegalStateException("Couldn't read position of JsonReader", var2);
		}
	}

	public static <T> T parseJson(HolderLookup.Provider provider, StringReader stringReader, Codec<T> codec) {
		JsonReader jsonReader = new JsonReader(new java.io.StringReader(stringReader.getRemaining()));
		jsonReader.setLenient(false);

		Object var5;
		try {
			JsonElement jsonElement = Streams.parse(jsonReader);
			var5 = Util.getOrThrow(codec.parse(provider.createSerializationContext(JsonOps.INSTANCE), jsonElement), JsonParseException::new);
		} catch (StackOverflowError var9) {
			throw new JsonParseException(var9);
		} finally {
			stringReader.setCursor(stringReader.getCursor() + getPos(jsonReader));
		}

		return (T)var5;
	}

	public static String readWhile(StringReader stringReader, CharPredicate charPredicate) {
		int i = stringReader.getCursor();

		while (stringReader.canRead() && charPredicate.test(stringReader.peek())) {
			stringReader.skip();
		}

		return stringReader.getString().substring(i, stringReader.getCursor());
	}
}
