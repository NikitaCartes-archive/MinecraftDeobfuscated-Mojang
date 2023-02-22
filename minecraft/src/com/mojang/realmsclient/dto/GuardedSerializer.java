package com.mojang.realmsclient.dto;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GuardedSerializer {
	private final Gson gson = new Gson();

	public String toJson(ReflectionBasedSerialization reflectionBasedSerialization) {
		return this.gson.toJson(reflectionBasedSerialization);
	}

	public String toJson(JsonElement jsonElement) {
		return this.gson.toJson(jsonElement);
	}

	@Nullable
	public <T extends ReflectionBasedSerialization> T fromJson(String string, Class<T> class_) {
		return this.gson.fromJson(string, class_);
	}
}
