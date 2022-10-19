package net.minecraft.server.packs.metadata;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

public interface MetadataSectionType<T> extends MetadataSectionSerializer<T> {
	JsonObject toJson(T object);

	static <T> MetadataSectionType<T> fromCodec(String string, Codec<T> codec) {
		return new MetadataSectionType<T>() {
			@Override
			public String getMetadataSectionName() {
				return string;
			}

			@Override
			public T fromJson(JsonObject jsonObject) {
				return codec.parse(JsonOps.INSTANCE, jsonObject).getOrThrow(false, stringx -> {
				});
			}

			@Override
			public JsonObject toJson(T object) {
				return codec.encodeStart(JsonOps.INSTANCE, object).getOrThrow(false, stringx -> {
				}).getAsJsonObject();
			}
		};
	}
}
