package net.minecraft.server.packs;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import org.slf4j.Logger;

public class PathPackResources extends AbstractPackResources {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Joiner PATH_JOINER = Joiner.on("/");
	private final Path root;

	public PathPackResources(PackLocationInfo packLocationInfo, Path path) {
		super(packLocationInfo);
		this.root = path;
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getRootResource(String... strings) {
		FileUtil.validatePath(strings);
		Path path = FileUtil.resolvePath(this.root, List.of(strings));
		return Files.exists(path, new LinkOption[0]) ? IoSupplier.create(path) : null;
	}

	public static boolean validatePath(Path path) {
		return true;
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
		Path path = this.root.resolve(packType.getDirectory()).resolve(resourceLocation.getNamespace());
		return getResource(resourceLocation, path);
	}

	@Nullable
	public static IoSupplier<InputStream> getResource(ResourceLocation resourceLocation, Path path) {
		return FileUtil.decomposePath(resourceLocation.getPath()).mapOrElse(list -> {
			Path path2 = FileUtil.resolvePath(path, list);
			return returnFileIfExists(path2);
		}, error -> {
			LOGGER.error("Invalid path {}: {}", resourceLocation, error.message());
			return null;
		});
	}

	@Nullable
	private static IoSupplier<InputStream> returnFileIfExists(Path path) {
		return Files.exists(path, new LinkOption[0]) && validatePath(path) ? IoSupplier.create(path) : null;
	}

	@Override
	public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
		FileUtil.decomposePath(string2).ifSuccess(list -> {
			Path path = this.root.resolve(packType.getDirectory()).resolve(string);
			listPath(string, path, list, resourceOutput);
		}).ifError(error -> LOGGER.error("Invalid path {}: {}", string2, error.message()));
	}

	public static void listPath(String string, Path path, List<String> list, PackResources.ResourceOutput resourceOutput) {
		Path path2 = FileUtil.resolvePath(path, list);

		try {
			Stream<Path> stream = Files.find(path2, Integer.MAX_VALUE, (pathx, basicFileAttributes) -> basicFileAttributes.isRegularFile(), new FileVisitOption[0]);

			try {
				stream.forEach(path2x -> {
					String string2 = PATH_JOINER.join(path.relativize(path2x));
					ResourceLocation resourceLocation = ResourceLocation.tryBuild(string, string2);
					if (resourceLocation == null) {
						Util.logAndPauseIfInIde(String.format(Locale.ROOT, "Invalid path in pack: %s:%s, ignoring", string, string2));
					} else {
						resourceOutput.accept(resourceLocation, IoSupplier.create(path2x));
					}
				});
			} catch (Throwable var9) {
				if (stream != null) {
					try {
						stream.close();
					} catch (Throwable var8) {
						var9.addSuppressed(var8);
					}
				}

				throw var9;
			}

			if (stream != null) {
				stream.close();
			}
		} catch (NotDirectoryException | NoSuchFileException var10) {
		} catch (IOException var11) {
			LOGGER.error("Failed to list path {}", path2, var11);
		}
	}

	@Override
	public Set<String> getNamespaces(PackType packType) {
		Set<String> set = Sets.<String>newHashSet();
		Path path = this.root.resolve(packType.getDirectory());

		try {
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);

			try {
				for (Path path2 : directoryStream) {
					String string = path2.getFileName().toString();
					if (ResourceLocation.isValidNamespace(string)) {
						set.add(string);
					} else {
						LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", string, this.root);
					}
				}
			} catch (Throwable var9) {
				if (directoryStream != null) {
					try {
						directoryStream.close();
					} catch (Throwable var8) {
						var9.addSuppressed(var8);
					}
				}

				throw var9;
			}

			if (directoryStream != null) {
				directoryStream.close();
			}
		} catch (NotDirectoryException | NoSuchFileException var10) {
		} catch (IOException var11) {
			LOGGER.error("Failed to list path {}", path, var11);
		}

		return set;
	}

	@Override
	public void close() {
	}

	public static class PathResourcesSupplier implements Pack.ResourcesSupplier {
		private final Path content;

		public PathResourcesSupplier(Path path) {
			this.content = path;
		}

		@Override
		public PackResources openPrimary(PackLocationInfo packLocationInfo) {
			return new PathPackResources(packLocationInfo, this.content);
		}

		@Override
		public PackResources openFull(PackLocationInfo packLocationInfo, Pack.Metadata metadata) {
			PackResources packResources = this.openPrimary(packLocationInfo);
			List<String> list = metadata.overlays();
			if (list.isEmpty()) {
				return packResources;
			} else {
				List<PackResources> list2 = new ArrayList(list.size());

				for (String string : list) {
					Path path = this.content.resolve(string);
					list2.add(new PathPackResources(packLocationInfo, path));
				}

				return new CompositePackResources(packResources, list2);
			}
		}
	}
}
