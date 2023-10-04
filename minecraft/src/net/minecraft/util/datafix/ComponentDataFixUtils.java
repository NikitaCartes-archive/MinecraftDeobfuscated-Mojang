package net.minecraft.util.datafix;

import com.google.gson.JsonObject;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
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
}
