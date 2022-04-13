package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;

public interface CachedOutput {
	void writeIfNeeded(Path path, String string) throws IOException;

	void writeIfNeeded(Path path, byte[] bs, String string) throws IOException;
}
