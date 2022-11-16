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
import javax.annotation.Nullable;
import net.minecraft.Util;

public interface JsonEventLogReader<T> extends Closeable {
	static <T> JsonEventLogReader<T> create(Codec<T> codec, Reader reader) {
		final JsonReader jsonReader = new JsonReader(reader);
		jsonReader.setLenient(true);
		return new JsonEventLogReader<T>() {
			@Nullable
			@Override
			public T next() throws IOException {
				try {
					if (!jsonReader.hasNext()) {
						return null;
					} else {
						JsonElement jsonElement = JsonParser.parseReader(jsonReader);
						return Util.getOrThrow(codec.parse(JsonOps.INSTANCE, jsonElement), IOException::new);
					}
				} catch (JsonParseException var2) {
					throw new IOException(var2);
				} catch (EOFException var3) {
					return null;
				}
			}

			public void close() throws IOException {
				jsonReader.close();
			}
		};
	}

	@Nullable
	T next() throws IOException;
}
