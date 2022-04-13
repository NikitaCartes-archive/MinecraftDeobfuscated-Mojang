package net.minecraft.data;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.file.Path;

public interface DataProvider {
	HashFunction SHA1 = Hashing.sha1();

	void run(CachedOutput cachedOutput) throws IOException;

	String getName();

	static void save(Gson gson, CachedOutput cachedOutput, JsonElement jsonElement, Path path) throws IOException {
		String string = gson.toJson(jsonElement);
		cachedOutput.writeIfNeeded(path, string);
	}
}
