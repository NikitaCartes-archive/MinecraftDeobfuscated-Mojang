package net.minecraft.server.packs.metadata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;

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
				return Util.getOrThrow(codec.parse(JsonOps.INSTANCE, jsonObject), JsonParseException::new);
			}

			@Override
			public JsonObject toJson(T object) {
				return Util.getOrThrow(codec.encodeStart(JsonOps.INSTANCE, object), IllegalArgumentException::new).getAsJsonObject();
			}
		};
	}
}
