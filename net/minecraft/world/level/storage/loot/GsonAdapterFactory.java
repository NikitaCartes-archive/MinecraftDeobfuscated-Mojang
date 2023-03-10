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
        return new Builder<E, T>(registry, string, string2, function);
    }

    public static class Builder<E, T extends SerializerType<E>> {
        private final Registry<T> registry;
        private final String elementName;
        private final String typeKey;
        private final Function<E, T> typeGetter;
        @Nullable
        private Pair<T, InlineSerializer<? extends E>> inlineType;
        @Nullable
        private T defaultType;

        Builder(Registry<T> registry, String string, String string2, Function<E, T> function) {
            this.registry = registry;
            this.elementName = string;
            this.typeKey = string2;
            this.typeGetter = function;
        }

        public Builder<E, T> withInlineSerializer(T serializerType, InlineSerializer<? extends E> inlineSerializer) {
            this.inlineType = Pair.of(serializerType, inlineSerializer);
            return this;
        }

        public Builder<E, T> withDefaultType(T serializerType) {
            this.defaultType = serializerType;
            return this;
        }

        public Object build() {
            return new JsonAdapter<E, T>(this.registry, this.elementName, this.typeKey, this.typeGetter, this.defaultType, this.inlineType);
        }
    }

    public static interface InlineSerializer<T> {
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
        private final T defaultType;
        @Nullable
        private final Pair<T, InlineSerializer<? extends E>> inlineType;

        JsonAdapter(Registry<T> registry, String string, String string2, Function<E, T> function, @Nullable T serializerType, @Nullable Pair<T, InlineSerializer<? extends E>> pair) {
            this.registry = registry;
            this.elementName = string;
            this.typeKey = string2;
            this.typeGetter = function;
            this.defaultType = serializerType;
            this.inlineType = pair;
        }

        @Override
        public E deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonObject()) {
                Object serializerType;
                JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, this.elementName);
                String string = GsonHelper.getAsString(jsonObject, this.typeKey, "");
                if (string.isEmpty()) {
                    serializerType = this.defaultType;
                } else {
                    ResourceLocation resourceLocation = new ResourceLocation(string);
                    serializerType = (SerializerType)this.registry.get(resourceLocation);
                }
                if (serializerType == null) {
                    throw new JsonSyntaxException("Unknown type '" + string + "'");
                }
                return (E)((SerializerType)serializerType).getSerializer().deserialize(jsonObject, jsonDeserializationContext);
            }
            if (this.inlineType == null) {
                throw new UnsupportedOperationException("Object " + jsonElement + " can't be deserialized");
            }
            return this.inlineType.getSecond().deserialize(jsonElement, jsonDeserializationContext);
        }

        @Override
        public JsonElement serialize(E object, Type type, JsonSerializationContext jsonSerializationContext) {
            SerializerType serializerType = (SerializerType)this.typeGetter.apply(object);
            if (this.inlineType != null && this.inlineType.getFirst() == serializerType) {
                return this.inlineType.getSecond().serialize(object, jsonSerializationContext);
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
}

