package net.minecraft.server.packs.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@FunctionalInterface
public interface IoSupplier<T> {
	static IoSupplier<InputStream> create(Path path) {
		return () -> Files.newInputStream(path);
	}

	static IoSupplier<InputStream> create(ZipFile zipFile, ZipEntry zipEntry) {
		return () -> zipFile.getInputStream(zipEntry);
	}

	T get() throws IOException;
}
