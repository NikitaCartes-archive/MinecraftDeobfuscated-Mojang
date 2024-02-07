package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.Util;
import org.slf4j.Logger;

public class VanillaPackResourcesBuilder {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static Consumer<VanillaPackResourcesBuilder> developmentConfig = vanillaPackResourcesBuilder -> {
	};
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
	private final Set<Path> rootPaths = new LinkedHashSet();
	private final Map<PackType, Set<Path>> pathsForType = new EnumMap(PackType.class);
	private BuiltInMetadata metadata = BuiltInMetadata.of();
	private final Set<String> namespaces = new HashSet();

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

	private boolean validateDirPath(Path path) {
		if (!Files.exists(path, new LinkOption[0])) {
			return false;
		} else if (!Files.isDirectory(path, new LinkOption[0])) {
			throw new IllegalArgumentException("Path " + path.toAbsolutePath() + " is not directory");
		} else {
			return true;
		}
	}

	private void pushRootPath(Path path) {
		if (this.validateDirPath(path)) {
			this.rootPaths.add(path);
		}
	}

	private void pushPathForType(PackType packType, Path path) {
		if (this.validateDirPath(path)) {
			((Set)this.pathsForType.computeIfAbsent(packType, packTypex -> new LinkedHashSet())).add(path);
		}
	}

	public VanillaPackResourcesBuilder pushJarResources() {
		ROOT_DIR_BY_TYPE.forEach((packType, path) -> {
			this.pushRootPath(path.getParent());
			this.pushPathForType(packType, path);
		});
		return this;
	}

	public VanillaPackResourcesBuilder pushClasspathResources(PackType packType, Class<?> class_) {
		Enumeration<URL> enumeration = null;

		try {
			enumeration = class_.getClassLoader().getResources(packType.getDirectory() + "/");
		} catch (IOException var8) {
		}

		while (enumeration != null && enumeration.hasMoreElements()) {
			URL uRL = (URL)enumeration.nextElement();

			try {
				URI uRI = uRL.toURI();
				if ("file".equals(uRI.getScheme())) {
					Path path = Paths.get(uRI);
					this.pushRootPath(path.getParent());
					this.pushPathForType(packType, path);
				}
			} catch (Exception var7) {
				LOGGER.error("Failed to extract path from {}", uRL, var7);
			}
		}

		return this;
	}

	public VanillaPackResourcesBuilder applyDevelopmentConfig() {
		developmentConfig.accept(this);
		return this;
	}

	public VanillaPackResourcesBuilder pushUniversalPath(Path path) {
		this.pushRootPath(path);

		for (PackType packType : PackType.values()) {
			this.pushPathForType(packType, path.resolve(packType.getDirectory()));
		}

		return this;
	}

	public VanillaPackResourcesBuilder pushAssetPath(PackType packType, Path path) {
		this.pushRootPath(path);
		this.pushPathForType(packType, path);
		return this;
	}

	public VanillaPackResourcesBuilder setMetadata(BuiltInMetadata builtInMetadata) {
		this.metadata = builtInMetadata;
		return this;
	}

	public VanillaPackResourcesBuilder exposeNamespace(String... strings) {
		this.namespaces.addAll(Arrays.asList(strings));
		return this;
	}

	public VanillaPackResources build(PackLocationInfo packLocationInfo) {
		Map<PackType, List<Path>> map = new EnumMap(PackType.class);

		for (PackType packType : PackType.values()) {
			List<Path> list = copyAndReverse((Collection<Path>)this.pathsForType.getOrDefault(packType, Set.of()));
			map.put(packType, list);
		}

		return new VanillaPackResources(packLocationInfo, this.metadata, Set.copyOf(this.namespaces), copyAndReverse(this.rootPaths), map);
	}

	private static List<Path> copyAndReverse(Collection<Path> collection) {
		List<Path> list = new ArrayList(collection);
		Collections.reverse(list);
		return List.copyOf(list);
	}
}
