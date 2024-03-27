package net.minecraft.util.datafix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.util.GsonHelper;

public class ComponentDataFixUtils {
	private static final String EMPTY_CONTENTS = createTextComponentJson("");

	public static <T> Dynamic<T> createPlainTextComponent(DynamicOps<T> dynamicOps, String string) {
		String string2 = createTextComponentJson(string);
		return new Dynamic<>(dynamicOps, dynamicOps.createString(string2));
	}

	public static <T> Dynamic<T> createEmptyComponent(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createString(EMPTY_CONTENTS));
	}

	private static String createTextComponentJson(String string) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("text", string);
		return GsonHelper.toStableString(jsonObject);
	}

	public static <T> Dynamic<T> createTranslatableComponent(DynamicOps<T> dynamicOps, String string) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("translate", string);
		return new Dynamic<>(dynamicOps, dynamicOps.createString(GsonHelper.toStableString(jsonObject)));
	}

	public static <T> Dynamic<T> wrapLiteralStringAsComponent(Dynamic<T> dynamic) {
		return DataFixUtils.orElse(dynamic.asString().map(string -> createPlainTextComponent(dynamic.getOps(), string)).result(), dynamic);
	}

	public static Dynamic<?> rewriteFromLenient(Dynamic<?> dynamic) {
		Optional<String> optional = dynamic.asString().result();
		if (optional.isEmpty()) {
			return dynamic;
		} else {
			String string = (String)optional.get();
			if (!string.isEmpty() && !string.equals("null")) {
				char c = string.charAt(0);
				char d = string.charAt(string.length() - 1);
				if (c == '"' && d == '"' || c == '{' && d == '}' || c == '[' && d == ']') {
					try {
						JsonElement jsonElement = JsonParser.parseString(string);
						if (jsonElement.isJsonPrimitive()) {
							return createPlainTextComponent(dynamic.getOps(), jsonElement.getAsString());
						}

						return dynamic.createString(GsonHelper.toStableString(jsonElement));
					} catch (JsonParseException var6) {
					}
				}

				return createPlainTextComponent(dynamic.getOps(), string);
			} else {
				return createEmptyComponent(dynamic.getOps());
			}
		}
	}

	public static Optional<String> extractTranslationString(String string) {
		try {
			JsonElement jsonElement = JsonParser.parseString(string);
			if (jsonElement.isJsonObject()) {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				JsonElement jsonElement2 = jsonObject.get("translate");
				if (jsonElement2 != null && jsonElement2.isJsonPrimitive()) {
					return Optional.of(jsonElement2.getAsString());
				}
			}
		} catch (JsonParseException var4) {
		}

		return Optional.empty();
	}
}
