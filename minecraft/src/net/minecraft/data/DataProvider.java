package net.minecraft.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;

public interface DataProvider {
	ToIntFunction<String> FIXED_ORDER_FIELDS = Util.make(new Object2IntOpenHashMap<>(), object2IntOpenHashMap -> {
		object2IntOpenHashMap.put("type", 0);
		object2IntOpenHashMap.put("parent", 1);
		object2IntOpenHashMap.defaultReturnValue(2);
	});
	Comparator<String> KEY_COMPARATOR = Comparator.comparingInt(FIXED_ORDER_FIELDS).thenComparing(string -> string);

	void run(CachedOutput cachedOutput) throws IOException;

	String getName();

	static void saveStable(CachedOutput cachedOutput, JsonElement jsonElement, Path path) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
		Writer writer = new OutputStreamWriter(hashingOutputStream, StandardCharsets.UTF_8);
		JsonWriter jsonWriter = new JsonWriter(writer);
		jsonWriter.setSerializeNulls(false);
		jsonWriter.setIndent("  ");
		GsonHelper.writeValue(jsonWriter, jsonElement, KEY_COMPARATOR);
		jsonWriter.close();
		cachedOutput.writeIfNeeded(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
	}
}
