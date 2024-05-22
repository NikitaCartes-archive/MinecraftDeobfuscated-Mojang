package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureTemplateManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String STRUCTURE_DIRECTORY_NAME = "structure";
	private static final String STRUCTURE_FILE_EXTENSION = ".nbt";
	private static final String STRUCTURE_TEXT_FILE_EXTENSION = ".snbt";
	private final Map<ResourceLocation, Optional<StructureTemplate>> structureRepository = Maps.<ResourceLocation, Optional<StructureTemplate>>newConcurrentMap();
	private final DataFixer fixerUpper;
	private ResourceManager resourceManager;
	private final Path generatedDir;
	private final List<StructureTemplateManager.Source> sources;
	private final HolderGetter<Block> blockLookup;
	private static final FileToIdConverter LISTER = new FileToIdConverter("structure", ".nbt");

	public StructureTemplateManager(
		ResourceManager resourceManager, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, HolderGetter<Block> holderGetter
	) {
		this.resourceManager = resourceManager;
		this.fixerUpper = dataFixer;
		this.generatedDir = levelStorageAccess.getLevelPath(LevelResource.GENERATED_DIR).normalize();
		this.blockLookup = holderGetter;
		Builder<StructureTemplateManager.Source> builder = ImmutableList.builder();
		builder.add(new StructureTemplateManager.Source(this::loadFromGenerated, this::listGenerated));
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			builder.add(new StructureTemplateManager.Source(this::loadFromTestStructures, this::listTestStructures));
		}

		builder.add(new StructureTemplateManager.Source(this::loadFromResource, this::listResources));
		this.sources = builder.build();
	}

	public StructureTemplate getOrCreate(ResourceLocation resourceLocation) {
		Optional<StructureTemplate> optional = this.get(resourceLocation);
		if (optional.isPresent()) {
			return (StructureTemplate)optional.get();
		} else {
			StructureTemplate structureTemplate = new StructureTemplate();
			this.structureRepository.put(resourceLocation, Optional.of(structureTemplate));
			return structureTemplate;
		}
	}

	public Optional<StructureTemplate> get(ResourceLocation resourceLocation) {
		return (Optional<StructureTemplate>)this.structureRepository.computeIfAbsent(resourceLocation, this::tryLoad);
	}

	public Stream<ResourceLocation> listTemplates() {
		return this.sources.stream().flatMap(source -> (Stream)source.lister().get()).distinct();
	}

	private Optional<StructureTemplate> tryLoad(ResourceLocation resourceLocation) {
		for (StructureTemplateManager.Source source : this.sources) {
			try {
				Optional<StructureTemplate> optional = (Optional<StructureTemplate>)source.loader().apply(resourceLocation);
				if (optional.isPresent()) {
					return optional;
				}
			} catch (Exception var5) {
			}
		}

		return Optional.empty();
	}

	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		this.structureRepository.clear();
	}

	private Optional<StructureTemplate> loadFromResource(ResourceLocation resourceLocation) {
		ResourceLocation resourceLocation2 = LISTER.idToFile(resourceLocation);
		return this.load(() -> this.resourceManager.open(resourceLocation2), throwable -> LOGGER.error("Couldn't load structure {}", resourceLocation, throwable));
	}

	private Stream<ResourceLocation> listResources() {
		return LISTER.listMatchingResources(this.resourceManager).keySet().stream().map(LISTER::fileToId);
	}

	private Optional<StructureTemplate> loadFromTestStructures(ResourceLocation resourceLocation) {
		return this.loadFromSnbt(resourceLocation, Paths.get(StructureUtils.testStructuresDir));
	}

	private Stream<ResourceLocation> listTestStructures() {
		return this.listFolderContents(Paths.get(StructureUtils.testStructuresDir), "minecraft", ".snbt");
	}

	private Optional<StructureTemplate> loadFromGenerated(ResourceLocation resourceLocation) {
		if (!Files.isDirectory(this.generatedDir, new LinkOption[0])) {
			return Optional.empty();
		} else {
			Path path = createAndValidatePathToStructure(this.generatedDir, resourceLocation, ".nbt");
			return this.load(() -> new FileInputStream(path.toFile()), throwable -> LOGGER.error("Couldn't load structure from {}", path, throwable));
		}
	}

	private Stream<ResourceLocation> listGenerated() {
		if (!Files.isDirectory(this.generatedDir, new LinkOption[0])) {
			return Stream.empty();
		} else {
			try {
				return Files.list(this.generatedDir).filter(path -> Files.isDirectory(path, new LinkOption[0])).flatMap(path -> this.listGeneratedInNamespace(path));
			} catch (IOException var2) {
				return Stream.empty();
			}
		}
	}

	private Stream<ResourceLocation> listGeneratedInNamespace(Path path) {
		Path path2 = path.resolve("structure");
		return this.listFolderContents(path2, path.getFileName().toString(), ".nbt");
	}

	private Stream<ResourceLocation> listFolderContents(Path path, String string, String string2) {
		if (!Files.isDirectory(path, new LinkOption[0])) {
			return Stream.empty();
		} else {
			int i = string2.length();
			Function<String, String> function = stringx -> stringx.substring(0, stringx.length() - i);

			try {
				return Files.walk(path).filter(pathx -> pathx.toString().endsWith(string2)).mapMulti((path2, consumer) -> {
					try {
						consumer.accept(ResourceLocation.fromNamespaceAndPath(string, (String)function.apply(this.relativize(path, path2))));
					} catch (ResourceLocationException var7x) {
						LOGGER.error("Invalid location while listing pack contents", (Throwable)var7x);
					}
				});
			} catch (IOException var7) {
				LOGGER.error("Failed to list folder contents", (Throwable)var7);
				return Stream.empty();
			}
		}
	}

	private String relativize(Path path, Path path2) {
		return path.relativize(path2).toString().replace(File.separator, "/");
	}

	private Optional<StructureTemplate> loadFromSnbt(ResourceLocation resourceLocation, Path path) {
		if (!Files.isDirectory(path, new LinkOption[0])) {
			return Optional.empty();
		} else {
			Path path2 = FileUtil.createPathToResource(path, resourceLocation.getPath(), ".snbt");

			try {
				BufferedReader bufferedReader = Files.newBufferedReader(path2);

				Optional var6;
				try {
					String string = IOUtils.toString(bufferedReader);
					var6 = Optional.of(this.readStructure(NbtUtils.snbtToStructure(string)));
				} catch (Throwable var8) {
					if (bufferedReader != null) {
						try {
							bufferedReader.close();
						} catch (Throwable var7) {
							var8.addSuppressed(var7);
						}
					}

					throw var8;
				}

				if (bufferedReader != null) {
					bufferedReader.close();
				}

				return var6;
			} catch (NoSuchFileException var9) {
				return Optional.empty();
			} catch (CommandSyntaxException | IOException var10) {
				LOGGER.error("Couldn't load structure from {}", path2, var10);
				return Optional.empty();
			}
		}
	}

	private Optional<StructureTemplate> load(StructureTemplateManager.InputStreamOpener inputStreamOpener, Consumer<Throwable> consumer) {
		try {
			InputStream inputStream = inputStreamOpener.open();

			Optional var5;
			try {
				InputStream inputStream2 = new FastBufferedInputStream(inputStream);

				try {
					var5 = Optional.of(this.readStructure(inputStream2));
				} catch (Throwable var9) {
					try {
						inputStream2.close();
					} catch (Throwable var8) {
						var9.addSuppressed(var8);
					}

					throw var9;
				}

				inputStream2.close();
			} catch (Throwable var10) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var7) {
						var10.addSuppressed(var7);
					}
				}

				throw var10;
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return var5;
		} catch (FileNotFoundException var11) {
			return Optional.empty();
		} catch (Throwable var12) {
			consumer.accept(var12);
			return Optional.empty();
		}
	}

	private StructureTemplate readStructure(InputStream inputStream) throws IOException {
		CompoundTag compoundTag = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
		return this.readStructure(compoundTag);
	}

	public StructureTemplate readStructure(CompoundTag compoundTag) {
		StructureTemplate structureTemplate = new StructureTemplate();
		int i = NbtUtils.getDataVersion(compoundTag, 500);
		structureTemplate.load(this.blockLookup, DataFixTypes.STRUCTURE.updateToCurrentVersion(this.fixerUpper, compoundTag, i));
		return structureTemplate;
	}

	public boolean save(ResourceLocation resourceLocation) {
		Optional<StructureTemplate> optional = (Optional<StructureTemplate>)this.structureRepository.get(resourceLocation);
		if (optional.isEmpty()) {
			return false;
		} else {
			StructureTemplate structureTemplate = (StructureTemplate)optional.get();
			Path path = createAndValidatePathToStructure(this.generatedDir, resourceLocation, ".nbt");
			Path path2 = path.getParent();
			if (path2 == null) {
				return false;
			} else {
				try {
					Files.createDirectories(Files.exists(path2, new LinkOption[0]) ? path2.toRealPath() : path2);
				} catch (IOException var13) {
					LOGGER.error("Failed to create parent directory: {}", path2);
					return false;
				}

				CompoundTag compoundTag = structureTemplate.save(new CompoundTag());

				try {
					OutputStream outputStream = new FileOutputStream(path.toFile());

					try {
						NbtIo.writeCompressed(compoundTag, outputStream);
					} catch (Throwable var11) {
						try {
							outputStream.close();
						} catch (Throwable var10) {
							var11.addSuppressed(var10);
						}

						throw var11;
					}

					outputStream.close();
					return true;
				} catch (Throwable var12) {
					return false;
				}
			}
		}
	}

	public Path getPathToGeneratedStructure(ResourceLocation resourceLocation, String string) {
		return createPathToStructure(this.generatedDir, resourceLocation, string);
	}

	public static Path createPathToStructure(Path path, ResourceLocation resourceLocation, String string) {
		try {
			Path path2 = path.resolve(resourceLocation.getNamespace());
			Path path3 = path2.resolve("structure");
			return FileUtil.createPathToResource(path3, resourceLocation.getPath(), string);
		} catch (InvalidPathException var5) {
			throw new ResourceLocationException("Invalid resource path: " + resourceLocation, var5);
		}
	}

	private static Path createAndValidatePathToStructure(Path path, ResourceLocation resourceLocation, String string) {
		if (resourceLocation.getPath().contains("//")) {
			throw new ResourceLocationException("Invalid resource path: " + resourceLocation);
		} else {
			Path path2 = createPathToStructure(path, resourceLocation, string);
			if (path2.startsWith(path) && FileUtil.isPathNormalized(path2) && FileUtil.isPathPortable(path2)) {
				return path2;
			} else {
				throw new ResourceLocationException("Invalid resource path: " + path2);
			}
		}
	}

	public void remove(ResourceLocation resourceLocation) {
		this.structureRepository.remove(resourceLocation);
	}

	@FunctionalInterface
	interface InputStreamOpener {
		InputStream open() throws IOException;
	}

	static record Source(Function<ResourceLocation, Optional<StructureTemplate>> loader, Supplier<Stream<ResourceLocation>> lister) {
	}
}
