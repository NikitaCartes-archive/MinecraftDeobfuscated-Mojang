package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import org.slf4j.Logger;

public class FolderRepositorySource implements RepositorySource {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Path folder;
	private final PackType packType;
	private final PackSource packSource;

	public FolderRepositorySource(Path path, PackType packType, PackSource packSource) {
		this.folder = path;
		this.packType = packType;
		this.packSource = packSource;
	}

	private static String nameFromPath(Path path) {
		return path.getFileName().toString();
	}

	@Override
	public void loadPacks(Consumer<Pack> consumer) {
		try {
			Files.createDirectories(this.folder);
			discoverPacks(
				this.folder,
				false,
				(path, resourcesSupplier) -> {
					String string = nameFromPath(path);
					Pack pack = Pack.readMetaAndCreate(
						"file/" + string, Component.literal(string), false, resourcesSupplier, this.packType, Pack.Position.TOP, this.packSource
					);
					if (pack != null) {
						consumer.accept(pack);
					}
				}
			);
		} catch (IOException var3) {
			LOGGER.warn("Failed to list packs in {}", this.folder, var3);
		}
	}

	public static void discoverPacks(Path path, boolean bl, BiConsumer<Path, Pack.ResourcesSupplier> biConsumer) throws IOException {
		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);

		try {
			for (Path path2 : directoryStream) {
				Pack.ResourcesSupplier resourcesSupplier = detectPackResources(path2, bl);
				if (resourcesSupplier != null) {
					biConsumer.accept(path2, resourcesSupplier);
				}
			}
		} catch (Throwable var8) {
			if (directoryStream != null) {
				try {
					directoryStream.close();
				} catch (Throwable var7) {
					var8.addSuppressed(var7);
				}
			}

			throw var8;
		}

		if (directoryStream != null) {
			directoryStream.close();
		}
	}

	@Nullable
	public static Pack.ResourcesSupplier detectPackResources(Path path, boolean bl) {
		BasicFileAttributes basicFileAttributes;
		try {
			basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
		} catch (NoSuchFileException var5) {
			return null;
		} catch (IOException var6) {
			LOGGER.warn("Failed to read properties of '{}', ignoring", path, var6);
			return null;
		}

		if (basicFileAttributes.isDirectory() && Files.isRegularFile(path.resolve("pack.mcmeta"), new LinkOption[0])) {
			return string -> new PathPackResources(string, path, bl);
		} else {
			if (basicFileAttributes.isRegularFile() && path.getFileName().toString().endsWith(".zip")) {
				FileSystem fileSystem = path.getFileSystem();
				if (fileSystem == FileSystems.getDefault() || fileSystem instanceof LinkFileSystem) {
					File file = path.toFile();
					return string -> new FilePackResources(string, file, bl);
				}
			}

			LOGGER.info("Found non-pack entry '{}', ignoring", path);
			return null;
		}
	}
}
