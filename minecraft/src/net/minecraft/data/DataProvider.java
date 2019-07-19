package net.minecraft.data;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;

public interface DataProvider {
	HashFunction SHA1 = Hashing.sha1();

	void run(HashCache hashCache) throws IOException;

	String getName();

	static void save(Gson gson, HashCache hashCache, JsonElement jsonElement, Path path) throws IOException {
		String string = gson.toJson(jsonElement);
		String string2 = SHA1.hashUnencodedChars(string).toString();
		if (!Objects.equals(hashCache.getHash(path), string2) || !Files.exists(path, new LinkOption[0])) {
			Files.createDirectories(path.getParent());
			BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
			Throwable var7 = null;

			try {
				bufferedWriter.write(string);
			} catch (Throwable var16) {
				var7 = var16;
				throw var16;
			} finally {
				if (bufferedWriter != null) {
					if (var7 != null) {
						try {
							bufferedWriter.close();
						} catch (Throwable var15) {
							var7.addSuppressed(var15);
						}
					} else {
						bufferedWriter.close();
					}
				}
			}
		}

		hashCache.putNew(path, string2);
	}
}
