package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

public interface Serializer<T> {
	void serialize(JsonObject jsonObject, T object, JsonSerializationContext jsonSerializationContext);

	T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext);
}
