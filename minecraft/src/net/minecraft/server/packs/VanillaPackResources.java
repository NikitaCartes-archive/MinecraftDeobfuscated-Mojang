package net.minecraft.server.packs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaPackResources implements PackResources {
	public static Path generatedDir;
	private static final Logger LOGGER = LogManager.getLogger();
	public static Class<?> clientObject;
	private static final Map<PackType, FileSystem> JAR_FILESYSTEM_BY_TYPE = Util.make(Maps.<PackType, FileSystem>newHashMap(), hashMap -> {
		synchronized (VanillaPackResources.class) {
			for (PackType packType : PackType.values()) {
				URL uRL = VanillaPackResources.class.getResource("/" + packType.getDirectory() + "/.mcassetsroot");

				try {
					URI uRI = uRL.toURI();
					if ("jar".equals(uRI.getScheme())) {
						FileSystem fileSystem;
						try {
							fileSystem = FileSystems.getFileSystem(uRI);
						} catch (FileSystemNotFoundException var11) {
							fileSystem = FileSystems.newFileSystem(uRI, Collections.emptyMap());
						}

						hashMap.put(packType, fileSystem);
					}
				} catch (IOException | URISyntaxException var12) {
					LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)var12);
				}
			}
		}
	});
	public final Set<String> namespaces;

	public VanillaPackResources(String... strings) {
		this.namespaces = ImmutableSet.copyOf(strings);
	}

	@Override
	public InputStream getRootResource(String string) throws IOException {
		if (!string.contains("/") && !string.contains("\\")) {
			if (generatedDir != null) {
				Path path = generatedDir.resolve(string);
				if (Files.exists(path, new LinkOption[0])) {
					return Files.newInputStream(path);
				}
			}

			return this.getResourceAsStream(string);
		} else {
			throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
		}
	}

	@Override
	public InputStream getResource(PackType packType, ResourceLocation resourceLocation) throws IOException {
		InputStream inputStream = this.getResourceAsStream(packType, resourceLocation);
		if (inputStream != null) {
			return inputStream;
		} else {
			throw new FileNotFoundException(resourceLocation.getPath());
		}
	}

	@Override
	public Collection<ResourceLocation> getResources(PackType packType, String string, String string2, int i, Predicate<String> predicate) {
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();
		if (generatedDir != null) {
			try {
				getResources(set, i, string, generatedDir.resolve(packType.getDirectory()), string2, predicate);
			} catch (IOException var15) {
			}

			if (packType == PackType.CLIENT_RESOURCES) {
				Enumeration<URL> enumeration = null;

				try {
					enumeration = clientObject.getClassLoader().getResources(packType.getDirectory() + "/");
				} catch (IOException var14) {
				}

				while (enumeration != null && enumeration.hasMoreElements()) {
					try {
						URI uRI = ((URL)enumeration.nextElement()).toURI();
						if ("file".equals(uRI.getScheme())) {
							getResources(set, i, string, Paths.get(uRI), string2, predicate);
						}
					} catch (IOException | URISyntaxException var13) {
					}
				}
			}
		}

		try {
			URL uRL = VanillaPackResources.class.getResource("/" + packType.getDirectory() + "/.mcassetsroot");
			if (uRL == null) {
				LOGGER.error("Couldn't find .mcassetsroot, cannot load vanilla resources");
				return set;
			}

			URI uRI = uRL.toURI();
			if ("file".equals(uRI.getScheme())) {
				URL uRL2 = new URL(uRL.toString().substring(0, uRL.toString().length() - ".mcassetsroot".length()));
				Path path = Paths.get(uRL2.toURI());
				getResources(set, i, string, path, string2, predicate);
			} else if ("jar".equals(uRI.getScheme())) {
				Path path2 = ((FileSystem)JAR_FILESYSTEM_BY_TYPE.get(packType)).getPath("/" + packType.getDirectory());
				getResources(set, i, "minecraft", path2, string2, predicate);
			} else {
				LOGGER.error("Unsupported scheme {} trying to list vanilla resources (NYI?)", uRI);
			}
		} catch (NoSuchFileException | FileNotFoundException var11) {
		} catch (IOException | URISyntaxException var12) {
			LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)var12);
		}

		return set;
	}

	private static void getResources(Collection<ResourceLocation> collection, int i, String string, Path path, String string2, Predicate<String> predicate) throws IOException {
		Path path2 = path.resolve(string);
		Stream<Path> stream = Files.walk(path2.resolve(string2), i, new FileVisitOption[0]);
		Throwable var8 = null;

		try {
			stream.filter(pathx -> !pathx.endsWith(".mcmeta") && Files.isRegularFile(pathx, new LinkOption[0]) && predicate.test(pathx.getFileName().toString()))
				.map(path2x -> new ResourceLocation(string, path2.relativize(path2x).toString().replaceAll("\\\\", "/")))
				.forEach(collection::add);
		} catch (Throwable var17) {
			var8 = var17;
			throw var17;
		} finally {
			if (stream != null) {
				if (var8 != null) {
					try {
						stream.close();
					} catch (Throwable var16) {
						var8.addSuppressed(var16);
					}
				} else {
					stream.close();
				}
			}
		}
	}

	@Nullable
	protected InputStream getResourceAsStream(PackType packType, ResourceLocation resourceLocation) {
		String string = createPath(packType, resourceLocation);
		if (generatedDir != null) {
			Path path = generatedDir.resolve(packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath());
			if (Files.exists(path, new LinkOption[0])) {
				try {
					return Files.newInputStream(path);
				} catch (IOException var7) {
				}
			}
		}

		try {
			URL uRL = VanillaPackResources.class.getResource(string);
			return isResourceUrlValid(string, uRL) ? uRL.openStream() : null;
		} catch (IOException var6) {
			return VanillaPackResources.class.getResourceAsStream(string);
		}
	}

	private static String createPath(PackType packType, ResourceLocation resourceLocation) {
		return "/" + packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath();
	}

	private static boolean isResourceUrlValid(String string, @Nullable URL uRL) throws IOException {
		return uRL != null && (uRL.getProtocol().equals("jar") || FolderPackResources.validatePath(new File(uRL.getFile()), string));
	}

	@Nullable
	protected InputStream getResourceAsStream(String string) {
		return VanillaPackResources.class.getResourceAsStream("/" + string);
	}

	@Override
	public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
		String string = createPath(packType, resourceLocation);
		if (generatedDir != null) {
			Path path = generatedDir.resolve(packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath());
			if (Files.exists(path, new LinkOption[0])) {
				return true;
			}
		}

		try {
			URL uRL = VanillaPackResources.class.getResource(string);
			return isResourceUrlValid(string, uRL);
		} catch (IOException var5) {
			return false;
		}
	}

	@Override
	public Set<String> getNamespaces(PackType packType) {
		return this.namespaces;
	}

	@Nullable
	@Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
		try {
			InputStream inputStream = this.getRootResource("pack.mcmeta");
			Throwable var3 = null;

			Object var4;
			try {
				var4 = AbstractPackResources.getMetadataFromStream(metadataSectionSerializer, inputStream);
			} catch (Throwable var14) {
				var3 = var14;
				throw var14;
			} finally {
				if (inputStream != null) {
					if (var3 != null) {
						try {
							inputStream.close();
						} catch (Throwable var13) {
							var3.addSuppressed(var13);
						}
					} else {
						inputStream.close();
					}
				}
			}

			return (T)var4;
		} catch (FileNotFoundException | RuntimeException var16) {
			return null;
		}
	}

	@Override
	public String getName() {
		return "Default";
	}

	@Override
	public void close() {
	}
}
