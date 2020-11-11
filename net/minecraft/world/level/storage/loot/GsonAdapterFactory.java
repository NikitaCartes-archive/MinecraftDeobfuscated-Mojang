/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.SerializerType;
import org.jetbrains.annotations.Nullable;

public class GsonAdapterFactory {
    public static <E, T extends SerializerType<E>> Builder<E, T> builder(Registry<T> registry, String string, String string2, Function<E, T> function) {
        return new Builder(registry, string, string2, function);
    }

    public static interface DefaultSerializer<T> {
        public JsonElement serialize(T var1, JsonSerializationContext var2);

        public T deserialize(JsonElement var1, JsonDeserializationContext var2);
    }

    static class JsonAdapter<E, T extends SerializerType<E>>
    implements JsonDeserializer<E>,
    JsonSerializer<E> {
        private final Registry<T> registry;
        private final String elementName;
        private final String typeKey;
        private final Function<E, T> typeGetter;
        @Nullable
        private final Pair<T, DefaultSerializer<? extends E>> defaultType;

        private JsonAdapter(Registry<T> registry, String string, String string2, Function<E, T> function, @Nullable Pair<T, DefaultSerializer<? extends E>> pair) {
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
                SerializerType serializerType = (SerializerType)this.registry.get(resourceLocation);
                if (serializerType == null) {
                    throw new JsonSyntaxException("Unknown type '" + resourceLocation + "'");
                }
                return (E)serializerType.getSerializer().deserialize(jsonObject, jsonDeserializationContext);
            }
            if (this.defaultType == null) {
                throw new UnsupportedOperationException("Object " + jsonElement + " can't be deserialized");
            }
            return this.defaultType.getSecond().deserialize(jsonElement, jsonDeserializationContext);
        }

        @Override
        public JsonElement serialize(E object, Type type, JsonSerializationContext jsonSerializationContext) {
            SerializerType serializerType = (SerializerType)this.typeGetter.apply(object);
            if (this.defaultType != null && this.defaultType.getFirst() == serializerType) {
                return this.defaultType.getSecond().serialize(object, jsonSerializationContext);
            }
            if (serializerType == null) {
                throw new JsonSyntaxException("Unknown type: " + object);
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(this.typeKey, this.registry.getKey(serializerType).toString());
            serializerType.getSerializer().serialize(jsonObject, object, jsonSerializationContext);
            return jsonObject;
        }
    }

    public static class Builder<E, T extends SerializerType<E>> {
        private final Registry<T> registry;
        private final String elementName;
        private final String typeKey;
        private final Function<E, T> typeGetter;
        @Nullable
        private Pair<T, DefaultSerializer<? extends E>> defaultType;

        private Builder(Registry<T> registry, String string, String string2, Function<E, T> function) {
            this.registry = registry;
            this.elementName = string;
            this.typeKey = string2;
            this.typeGetter = function;
        }

        public Builder<E, T> withDefaultSerializer(T serializerType, DefaultSerializer<? extends E> defaultSerializer) {
            this.defaultType = Pair.of(serializerType, defaultSerializer);
            return this;
        }

        public Object build() {
            return new JsonAdapter(this.registry, this.elementName, this.typeKey, this.typeGetter, this.defaultType);
        }
    }
}

