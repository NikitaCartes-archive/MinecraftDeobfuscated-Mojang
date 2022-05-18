package net.minecraft.data;

import com.google.common.hash.HashCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public interface CachedOutput {
	CachedOutput NO_CACHE = (path, bs, hashCode) -> {
		Files.createDirectories(path.getParent());
		Files.write(path, bs, new OpenOption[0]);
	};

	void writeIfNeeded(Path path, byte[] bs, HashCode hashCode) throws IOException;
}
