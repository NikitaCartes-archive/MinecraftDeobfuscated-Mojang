package com.mojang.realmsclient.dto;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Ops extends ValueObject {
	public Set<String> ops = Sets.<String>newHashSet();

	public static Ops parse(String string) {
		Ops ops = new Ops();
		JsonParser jsonParser = new JsonParser();

		try {
			JsonElement jsonElement = jsonParser.parse(string);
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			JsonElement jsonElement2 = jsonObject.get("ops");
			if (jsonElement2.isJsonArray()) {
				for (JsonElement jsonElement3 : jsonElement2.getAsJsonArray()) {
					ops.ops.add(jsonElement3.getAsString());
				}
			}
		} catch (Exception var8) {
		}

		return ops;
	}
}
