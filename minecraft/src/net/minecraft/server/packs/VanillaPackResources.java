package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
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
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaPackResources implements PackResources, ResourceProvider {
	public static Path generatedDir;
	private static final Logger LOGGER = LogManager.getLogger();
	public static Class<?> clientObject;
	private static final Map<PackType, Path> ROOT_DIR_BY_TYPE = Util.make(() -> {
		synchronized (VanillaPackResources.class) {
			Builder<PackType, Path> builder = ImmutableMap.builder();

			for (PackType packType : PackType.values()) {
				String string = "/" + packType.getDirectory() + "/.mcassetsroot";
				URL uRL = VanillaPackResources.class.getResource(string);
				if (uRL == null) {
					LOGGER.error("File {} does not exist in classpath", string);
				} else {
					try {
						URI uRI = uRL.toURI();
						String string2 = uRI.getScheme();
						if (!"jar".equals(string2) && !"file".equals(string2)) {
							LOGGER.warn("Assets URL '{}' uses unexpected schema", uRI);
						}

						Path path = safeGetPath(uRI);
						builder.put(packType, path.getParent());
					} catch (Exception var12) {
						LOGGER.error("Couldn't resolve path to vanilla assets", (Throwable)var12);
					}
				}
			}

			return builder.build();
		}
	});
	public final PackMetadataSection packMetadata;
	public final Set<String> namespaces;

	private static Path safeGetPath(URI uRI) throws IOException {
		try {
			return Paths.get(uRI);
		} catch (FileSystemNotFoundException var3) {
		} catch (Throwable var4) {
			LOGGER.warn("Unable to get path for: {}", uRI, var4);
		}

		try {
			FileSystems.newFileSystem(uRI, Collections.emptyMap());
		} catch (FileSystemAlreadyExistsException var2) {
		}

		return Paths.get(uRI);
	}

	public VanillaPackResources(PackMetadataSection packMetadataSection, String... strings) {
		this.packMetadata = packMetadataSection;
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
			} catch (IOException var13) {
			}

			if (packType == PackType.CLIENT_RESOURCES) {
				Enumeration<URL> enumeration = null;

				try {
					enumeration = clientObject.getClassLoader().getResources(packType.getDirectory() + "/");
				} catch (IOException var12) {
				}

				while (enumeration != null && enumeration.hasMoreElements()) {
					try {
						URI uRI = ((URL)enumeration.nextElement()).toURI();
						if ("file".equals(uRI.getScheme())) {
							getResources(set, i, string, Paths.get(uRI), string2, predicate);
						}
					} catch (IOException | URISyntaxException var11) {
					}
				}
			}
		}

		try {
			Path path = (Path)ROOT_DIR_BY_TYPE.get(packType);
			if (path != null) {
				getResources(set, i, string, path, string2, predicate);
			} else {
				LOGGER.error("Can't access assets root for type: {}", packType);
			}
		} catch (NoSuchFileException | FileNotFoundException var9) {
		} catch (IOException var10) {
			LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)var10);
		}

		return set;
	}

	private static void getResources(Collection<ResourceLocation> collection, int i, String string, Path path, String string2, Predicate<String> predicate) throws IOException {
		Path path2 = path.resolve(string);
		Stream<Path> stream = Files.walk(path2.resolve(string2), i, new FileVisitOption[0]);

		try {
			stream.filter(pathx -> !pathx.endsWith(".mcmeta") && Files.isRegularFile(pathx, new LinkOption[0]) && predicate.test(pathx.getFileName().toString()))
				.map(path2x -> new ResourceLocation(string, path2.relativize(path2x).toString().replaceAll("\\\\", "/")))
				.forEach(collection::add);
		} catch (Throwable var11) {
			if (stream != null) {
				try {
					stream.close();
				} catch (Throwable var10) {
					var11.addSuppressed(var10);
				}
			}

			throw var11;
		}

		if (stream != null) {
			stream.close();
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

			Object var4;
			label57: {
				try {
					if (inputStream != null) {
						T object = AbstractPackResources.getMetadataFromStream(metadataSectionSerializer, inputStream);
						if (object != null) {
							var4 = object;
							break label57;
						}
					}
				} catch (Throwable var6) {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (Throwable var5) {
							var6.addSuppressed(var5);
						}
					}

					throw var6;
				}

				if (inputStream != null) {
					inputStream.close();
				}

				return (T)(metadataSectionSerializer == PackMetadataSection.SERIALIZER ? this.packMetadata : null);
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return (T)var4;
		} catch (FileNotFoundException | RuntimeException var7) {
			return (T)(metadataSectionSerializer == PackMetadataSection.SERIALIZER ? this.packMetadata : null);
		}
	}

	@Override
	public String getName() {
		return "Default";
	}

	@Override
	public void close() {
	}

	@Override
	public Resource getResource(ResourceLocation resourceLocation) throws IOException {
		return new Resource() {
			@Nullable
			InputStream inputStream;

			public void close() throws IOException {
				if (this.inputStream != null) {
					this.inputStream.close();
				}
			}

			@Override
			public ResourceLocation getLocation() {
				return resourceLocation;
			}

			@Override
			public InputStream getInputStream() {
				try {
					this.inputStream = VanillaPackResources.this.getResource(PackType.CLIENT_RESOURCES, resourceLocation);
				} catch (IOException var2) {
					throw new UncheckedIOException("Could not get client resource from vanilla pack", var2);
				}

				return this.inputStream;
			}

			@Override
			public boolean hasMetadata() {
				return false;
			}

			@Nullable
			@Override
			public <T> T getMetadata(MetadataSectionSerializer<T> metadataSectionSerializer) {
				return null;
			}

			@Override
			public String getSourceName() {
				return resourceLocation.toString();
			}
		};
	}
}
