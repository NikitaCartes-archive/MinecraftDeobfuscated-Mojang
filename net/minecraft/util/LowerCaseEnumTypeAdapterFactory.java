/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public class LowerCaseEnumTypeAdapterFactory
implements TypeAdapterFactory {
    @Override
    @Nullable
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<T> class_ = typeToken.getRawType();
        if (!class_.isEnum()) {
            return null;
        }
        final HashMap<String, T> map = Maps.newHashMap();
        for (T object : class_.getEnumConstants()) {
            map.put(this.toLowercase(object), object);
        }
        return new TypeAdapter<T>(){

            @Override
            public void write(JsonWriter jsonWriter, T object) throws IOException {
                if (object == null) {
                    jsonWriter.nullValue();
                } else {
                    jsonWriter.value(LowerCaseEnumTypeAdapterFactory.this.toLowercase(object));
                }
            }

            @Override
            @Nullable
            public T read(JsonReader jsonReader) throws IOException {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.nextNull();
                    return null;
                }
                return map.get(jsonReader.nextString());
            }
        };
    }

    private String toLowercase(Object object) {
        if (object instanceof Enum) {
            return ((Enum)object).name().toLowerCase(Locale.ROOT);
        }
        return object.toString().toLowerCase(Locale.ROOT);
    }
}

