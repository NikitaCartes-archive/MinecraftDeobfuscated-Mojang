package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.slf4j.Logger;

public class VanillaPackResources implements PackResources {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final BuiltInMetadata metadata;
	private final Set<String> namespaces;
	private final List<Path> rootPaths;
	private final Map<PackType, List<Path>> pathsForType;

	VanillaPackResources(BuiltInMetadata builtInMetadata, Set<String> set, List<Path> list, Map<PackType, List<Path>> map) {
		this.metadata = builtInMetadata;
		this.namespaces = set;
		this.rootPaths = list;
		this.pathsForType = map;
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getRootResource(String... strings) {
		FileUtil.validatePath(strings);
		List<String> list = List.of(strings);

		for (Path path : this.rootPaths) {
			Path path2 = FileUtil.resolvePath(path, list);
			if (Files.exists(path2, new LinkOption[0]) && PathPackResources.validatePath(path2)) {
				return IoSupplier.create(path2);
			}
		}

		return null;
	}

	public void listRawPaths(PackType packType, ResourceLocation resourceLocation, Consumer<Path> consumer) {
		FileUtil.decomposePath(resourceLocation.getPath()).get().ifLeft(list -> {
			String string = resourceLocation.getNamespace();

			for (Path path : (List)this.pathsForType.get(packType)) {
				Path path2 = path.resolve(string);
				consumer.accept(FileUtil.resolvePath(path2, list));
			}
		}).ifRight(partialResult -> LOGGER.error("Invalid path {}: {}", resourceLocation, partialResult.message()));
	}

	@Override
	public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
		FileUtil.decomposePath(string2).get().ifLeft(list -> {
			List<Path> list2 = (List<Path>)this.pathsForType.get(packType);
			int i = list2.size();
			if (i == 1) {
				getResources(resourceOutput, string, (Path)list2.get(0), list);
			} else if (i > 1) {
				Map<ResourceLocation, IoSupplier<InputStream>> map = new HashMap();

				for (int j = 0; j < i - 1; j++) {
					getResources(map::putIfAbsent, string, (Path)list2.get(j), list);
				}

				Path path = (Path)list2.get(i - 1);
				if (map.isEmpty()) {
					getResources(resourceOutput, string, path, list);
				} else {
					getResources(map::putIfAbsent, string, path, list);
					map.forEach(resourceOutput);
				}
			}
		}).ifRight(partialResult -> LOGGER.error("Invalid path {}: {}", string2, partialResult.message()));
	}

	private static void getResources(PackResources.ResourceOutput resourceOutput, String string, Path path, List<String> list) {
		Path path2 = path.resolve(string);
		PathPackResources.listPath(string, path2, list, resourceOutput);
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
		return FileUtil.decomposePath(resourceLocation.getPath()).get().map(list -> {
			String string = resourceLocation.getNamespace();

			for (Path path : (List)this.pathsForType.get(packType)) {
				Path path2 = FileUtil.resolvePath(path.resolve(string), list);
				if (Files.exists(path2, new LinkOption[0]) && PathPackResources.validatePath(path2)) {
					return IoSupplier.create(path2);
				}
			}

			return null;
		}, partialResult -> {
			LOGGER.error("Invalid path {}: {}", resourceLocation, partialResult.message());
			return null;
		});
	}

	@Override
	public Set<String> getNamespaces(PackType packType) {
		return this.namespaces;
	}

	@Nullable
	@Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
		IoSupplier<InputStream> ioSupplier = this.getRootResource("pack.mcmeta");
		if (ioSupplier != null) {
			try {
				InputStream inputStream = ioSupplier.get();

				Object var5;
				label53: {
					try {
						T object = AbstractPackResources.getMetadataFromStream(metadataSectionSerializer, inputStream);
						if (object != null) {
							var5 = object;
							break label53;
						}
					} catch (Throwable var7) {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (Throwable var6) {
								var7.addSuppressed(var6);
							}
						}

						throw var7;
					}

					if (inputStream != null) {
						inputStream.close();
					}

					return this.metadata.get(metadataSectionSerializer);
				}

				if (inputStream != null) {
					inputStream.close();
				}

				return (T)var5;
			} catch (IOException var8) {
			}
		}

		return this.metadata.get(metadataSectionSerializer);
	}

	@Override
	public String packId() {
		return "vanilla";
	}

	@Override
	public boolean isBuiltin() {
		return true;
	}

	@Override
	public void close() {
	}

	public ResourceProvider asProvider() {
		return resourceLocation -> Optional.ofNullable(this.getResource(PackType.CLIENT_RESOURCES, resourceLocation))
				.map(ioSupplier -> new Resource(this, ioSupplier));
	}
}
