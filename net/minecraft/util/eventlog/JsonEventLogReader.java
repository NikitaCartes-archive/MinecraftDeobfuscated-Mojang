/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.eventlog;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;

public interface JsonEventLogReader<T>
extends Closeable {
    public static <T> JsonEventLogReader<T> create(final Codec<T> codec, Reader reader) {
        final JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);
        return new JsonEventLogReader<T>(){

            @Override
            @Nullable
            public T next() throws IOException {
                try {
                    if (!jsonReader.hasNext()) {
                        return null;
                    }
                    JsonElement jsonElement = JsonParser.parseReader(jsonReader);
                    return Util.getOrThrow(codec.parse(JsonOps.INSTANCE, jsonElement), IOException::new);
                } catch (JsonParseException jsonParseException) {
                    throw new IOException(jsonParseException);
                } catch (EOFException eOFException) {
                    return null;
                }
            }

            @Override
            public void close() throws IOException {
                jsonReader.close();
            }
        };
    }

    @Nullable
    public T next() throws IOException;
}

