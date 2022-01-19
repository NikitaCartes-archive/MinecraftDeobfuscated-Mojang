package net.minecraft.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import org.slf4j.Logger;

public class FileZipper implements Closeable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Path outputFile;
	private final Path tempFile;
	private final FileSystem fs;

	public FileZipper(Path path) {
		this.outputFile = path;
		this.tempFile = path.resolveSibling(path.getFileName().toString() + "_tmp");

		try {
			this.fs = Util.ZIP_FILE_SYSTEM_PROVIDER.newFileSystem(this.tempFile, ImmutableMap.of("create", "true"));
		} catch (IOException var3) {
			throw new UncheckedIOException(var3);
		}
	}

	public void add(Path path, String string) {
		try {
			Path path2 = this.fs.getPath(File.separator);
			Path path3 = path2.resolve(path.toString());
			Files.createDirectories(path3.getParent());
			Files.write(path3, string.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
		} catch (IOException var5) {
			throw new UncheckedIOException(var5);
		}
	}

	public void add(Path path, File file) {
		try {
			Path path2 = this.fs.getPath(File.separator);
			Path path3 = path2.resolve(path.toString());
			Files.createDirectories(path3.getParent());
			Files.copy(file.toPath(), path3);
		} catch (IOException var5) {
			throw new UncheckedIOException(var5);
		}
	}

	public void add(Path path) {
		try {
			Path path2 = this.fs.getPath(File.separator);
			if (Files.isRegularFile(path, new LinkOption[0])) {
				Path path3 = path2.resolve(path.getParent().relativize(path).toString());
				Files.copy(path3, path);
			} else {
				Stream<Path> stream = Files.find(path, Integer.MAX_VALUE, (pathx, basicFileAttributes) -> basicFileAttributes.isRegularFile(), new FileVisitOption[0]);

				try {
					for (Path path4 : (List)stream.collect(Collectors.toList())) {
						Path path5 = path2.resolve(path.relativize(path4).toString());
						Files.createDirectories(path5.getParent());
						Files.copy(path4, path5);
					}
				} catch (Throwable var8) {
					if (stream != null) {
						try {
							stream.close();
						} catch (Throwable var7) {
							var8.addSuppressed(var7);
						}
					}

					throw var8;
				}

				if (stream != null) {
					stream.close();
				}
			}
		} catch (IOException var9) {
			throw new UncheckedIOException(var9);
		}
	}

	public void close() {
		try {
			this.fs.close();
			Files.move(this.tempFile, this.outputFile);
			LOGGER.info("Compressed to {}", this.outputFile);
		} catch (IOException var2) {
			throw new UncheckedIOException(var2);
		}
	}
}
