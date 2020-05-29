package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Type;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class GsonAdapterFactory {
	public static <E, T extends SerializerType<E>> GsonAdapterFactory.Builder<E, T> builder(
		Registry<T> registry, String string, String string2, Function<E, T> function
	) {
		return new GsonAdapterFactory.Builder<>(registry, string, string2, function);
	}

	public static class Builder<E, T extends SerializerType<E>> {
		private final Registry<T> registry;
		private final String elementName;
		private final String typeKey;
		private final Function<E, T> typeGetter;
		@Nullable
		private Pair<T, GsonAdapterFactory.DefaultSerializer<? extends E>> defaultType;

		private Builder(Registry<T> registry, String string, String string2, Function<E, T> function) {
			this.registry = registry;
			this.elementName = string;
			this.typeKey = string2;
			this.typeGetter = function;
		}

		public Object build() {
			return new GsonAdapterFactory.JsonAdapter(this.registry, this.elementName, this.typeKey, this.typeGetter, this.defaultType);
		}
	}

	public interface DefaultSerializer<T> {
		JsonElement serialize(T object, JsonSerializationContext jsonSerializationContext);

		T deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext);
	}

	static class JsonAdapter<E, T extends SerializerType<E>> implements JsonDeserializer<E>, JsonSerializer<E> {
		private final Registry<T> registry;
		private final String elementName;
		private final String typeKey;
		private final Function<E, T> typeGetter;
		@Nullable
		private final Pair<T, GsonAdapterFactory.DefaultSerializer<? extends E>> defaultType;

		private JsonAdapter(
			Registry<T> registry, String string, String string2, Function<E, T> function, @Nullable Pair<T, GsonAdapterFactory.DefaultSerializer<? extends E>> pair
		) {
			this.registry = registry;
			this.elementName = string;
			this.typeKey = string2;
			this.typeGetter = function;
			this.defaultType = pair;
		}

		@Override
		public E deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			if (jsonElement.isJsonObject()) {
				JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, this.elementName);
				ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, this.typeKey));
				T serializerType = this.registry.get(resourceLocation);
				if (serializerType == null) {
					throw new JsonSyntaxException("Unknown type '" + resourceLocation + "'");
				} else {
					return (E)serializerType.getSerializer().deserialize(jsonObject, jsonDeserializationContext);
				}
			} else if (this.defaultType == null) {
				throw new UnsupportedOperationException("Object " + jsonElement + " can't be deserialized");
			} else {
				return (E)this.defaultType.getSecond().deserialize(jsonElement, jsonDeserializationContext);
			}
		}

		@Override
		public JsonElement serialize(E object, Type type, JsonSerializationContext jsonSerializationContext) {
			T serializerType = (T)this.typeGetter.apply(object);
			if (this.defaultType != null && this.defaultType.getFirst() == serializerType) {
				return this.defaultType.getSecond().serialize(object, jsonSerializationContext);
			} else if (serializerType == null) {
				throw new JsonSyntaxException("Unknown type: " + object);
			} else {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty(this.typeKey, this.registry.getKey(serializerType).toString());
				serializerType.getSerializer().serialize(jsonObject, object, jsonSerializationContext);
				return jsonObject;
			}
		}
	}
}
