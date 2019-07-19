package net.minecraft.server.packs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaPack implements Pack {
	public static Path generatedDir;
	private static final Logger LOGGER = LogManager.getLogger();
	public static Class<?> clientObject;
	private static final Map<PackType, FileSystem> JAR_FILESYSTEM_BY_TYPE = Util.make(Maps.<PackType, FileSystem>newHashMap(), hashMap -> {
		synchronized (VanillaPack.class) {
			for (PackType packType : PackType.values()) {
				URL uRL = VanillaPack.class.getResource("/" + packType.getDirectory() + "/.mcassetsroot");

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

	public VanillaPack(String... strings) {
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
	public Collection<ResourceLocation> getResources(PackType packType, String string, int i, Predicate<String> predicate) {
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();
		if (generatedDir != null) {
			try {
				set.addAll(this.getResources(i, "minecraft", generatedDir.resolve(packType.getDirectory()).resolve("minecraft"), string, predicate));
			} catch (IOException var14) {
			}

			if (packType == PackType.CLIENT_RESOURCES) {
				Enumeration<URL> enumeration = null;

				try {
					enumeration = clientObject.getClassLoader().getResources(packType.getDirectory() + "/minecraft");
				} catch (IOException var13) {
				}

				while (enumeration != null && enumeration.hasMoreElements()) {
					try {
						URI uRI = ((URL)enumeration.nextElement()).toURI();
						if ("file".equals(uRI.getScheme())) {
							set.addAll(this.getResources(i, "minecraft", Paths.get(uRI), string, predicate));
						}
					} catch (IOException | URISyntaxException var12) {
					}
				}
			}
		}

		try {
			URL uRL = VanillaPack.class.getResource("/" + packType.getDirectory() + "/.mcassetsroot");
			if (uRL == null) {
				LOGGER.error("Couldn't find .mcassetsroot, cannot load vanilla resources");
				return set;
			}

			URI uRI = uRL.toURI();
			if ("file".equals(uRI.getScheme())) {
				URL uRL2 = new URL(uRL.toString().substring(0, uRL.toString().length() - ".mcassetsroot".length()) + "minecraft");
				if (uRL2 == null) {
					return set;
				}

				Path path = Paths.get(uRL2.toURI());
				set.addAll(this.getResources(i, "minecraft", path, string, predicate));
			} else if ("jar".equals(uRI.getScheme())) {
				Path path2 = ((FileSystem)JAR_FILESYSTEM_BY_TYPE.get(packType)).getPath("/" + packType.getDirectory() + "/minecraft");
				set.addAll(this.getResources(i, "minecraft", path2, string, predicate));
			} else {
				LOGGER.error("Unsupported scheme {} trying to list vanilla resources (NYI?)", uRI);
			}
		} catch (NoSuchFileException | FileNotFoundException var10) {
		} catch (IOException | URISyntaxException var11) {
			LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)var11);
		}

		return set;
	}

	private Collection<ResourceLocation> getResources(int i, String string, Path path, String string2, Predicate<String> predicate) throws IOException {
		List<ResourceLocation> list = Lists.<ResourceLocation>newArrayList();
		Iterator<Path> iterator = Files.walk(path.resolve(string2), i, new FileVisitOption[0]).iterator();

		while (iterator.hasNext()) {
			Path path2 = (Path)iterator.next();
			if (!path2.endsWith(".mcmeta") && Files.isRegularFile(path2, new LinkOption[0]) && predicate.test(path2.getFileName().toString())) {
				list.add(new ResourceLocation(string, path.relativize(path2).toString().replaceAll("\\\\", "/")));
			}
		}

		return list;
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
			URL uRL = VanillaPack.class.getResource(string);
			return isResourceUrlValid(string, uRL) ? uRL.openStream() : null;
		} catch (IOException var6) {
			return VanillaPack.class.getResourceAsStream(string);
		}
	}

	private static String createPath(PackType packType, ResourceLocation resourceLocation) {
		return "/" + packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath();
	}

	private static boolean isResourceUrlValid(String string, @Nullable URL uRL) throws IOException {
		return uRL != null && (uRL.getProtocol().equals("jar") || FolderResourcePack.validatePath(new File(uRL.getFile()), string));
	}

	@Nullable
	protected InputStream getResourceAsStream(String string) {
		return VanillaPack.class.getResourceAsStream("/" + string);
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
			URL uRL = VanillaPack.class.getResource(string);
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
				var4 = AbstractResourcePack.getMetadataFromStream(metadataSectionSerializer, inputStream);
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

	public void close() {
	}
}
