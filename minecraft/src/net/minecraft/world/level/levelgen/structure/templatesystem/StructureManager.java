package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureManager {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String STRUCTURE_DIRECTORY_NAME = "structures";
	private static final String STRUCTURE_FILE_EXTENSION = ".nbt";
	private static final String STRUCTURE_TEXT_FILE_EXTENSION = ".snbt";
	private final Map<ResourceLocation, Optional<StructureTemplate>> structureRepository = Maps.<ResourceLocation, Optional<StructureTemplate>>newConcurrentMap();
	private final DataFixer fixerUpper;
	private ResourceManager resourceManager;
	private final Path generatedDir;

	public StructureManager(ResourceManager resourceManager, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer) {
		this.resourceManager = resourceManager;
		this.fixerUpper = dataFixer;
		this.generatedDir = levelStorageAccess.getLevelPath(LevelResource.GENERATED_DIR).normalize();
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
		return (Optional<StructureTemplate>)this.structureRepository.computeIfAbsent(resourceLocation, resourceLocationx -> {
			Optional<StructureTemplate> optional = this.loadFromGenerated(resourceLocationx);
			return optional.isPresent() ? optional : this.loadFromResource(resourceLocationx);
		});
	}

	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		this.structureRepository.clear();
	}

	private Optional<StructureTemplate> loadFromResource(ResourceLocation resourceLocation) {
		ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), "structures/" + resourceLocation.getPath() + ".nbt");

		try {
			Resource resource = this.resourceManager.getResource(resourceLocation2);

			Optional var4;
			try {
				var4 = Optional.of(this.readStructure(resource.getInputStream()));
			} catch (Throwable var7) {
				if (resource != null) {
					try {
						resource.close();
					} catch (Throwable var6) {
						var7.addSuppressed(var6);
					}
				}

				throw var7;
			}

			if (resource != null) {
				resource.close();
			}

			return var4;
		} catch (FileNotFoundException var8) {
			return Optional.empty();
		} catch (Throwable var9) {
			LOGGER.error("Couldn't load structure {}: {}", resourceLocation, var9.toString());
			return Optional.empty();
		}
	}

	private Optional<StructureTemplate> loadFromGenerated(ResourceLocation resourceLocation) {
		if (!this.generatedDir.toFile().isDirectory()) {
			return Optional.empty();
		} else {
			Path path = this.createAndValidatePathToStructure(resourceLocation, ".nbt");

			try {
				InputStream inputStream = new FileInputStream(path.toFile());

				Optional var4;
				try {
					var4 = Optional.of(this.readStructure(inputStream));
				} catch (Throwable var7) {
					try {
						inputStream.close();
					} catch (Throwable var6) {
						var7.addSuppressed(var6);
					}

					throw var7;
				}

				inputStream.close();
				return var4;
			} catch (FileNotFoundException var8) {
				return Optional.empty();
			} catch (IOException var9) {
				LOGGER.error("Couldn't load structure from {}", path, var9);
				return Optional.empty();
			}
		}
	}

	private StructureTemplate readStructure(InputStream inputStream) throws IOException {
		CompoundTag compoundTag = NbtIo.readCompressed(inputStream);
		return this.readStructure(compoundTag);
	}

	public StructureTemplate readStructure(CompoundTag compoundTag) {
		if (!compoundTag.contains("DataVersion", 99)) {
			compoundTag.putInt("DataVersion", 500);
		}

		StructureTemplate structureTemplate = new StructureTemplate();
		structureTemplate.load(NbtUtils.update(this.fixerUpper, DataFixTypes.STRUCTURE, compoundTag, compoundTag.getInt("DataVersion")));
		return structureTemplate;
	}

	public boolean save(ResourceLocation resourceLocation) {
		Optional<StructureTemplate> optional = (Optional<StructureTemplate>)this.structureRepository.get(resourceLocation);
		if (!optional.isPresent()) {
			return false;
		} else {
			StructureTemplate structureTemplate = (StructureTemplate)optional.get();
			Path path = this.createAndValidatePathToStructure(resourceLocation, ".nbt");
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

	public Path createPathToStructure(ResourceLocation resourceLocation, String string) {
		try {
			Path path = this.generatedDir.resolve(resourceLocation.getNamespace());
			Path path2 = path.resolve("structures");
			return FileUtil.createPathToResource(path2, resourceLocation.getPath(), string);
		} catch (InvalidPathException var5) {
			throw new ResourceLocationException("Invalid resource path: " + resourceLocation, var5);
		}
	}

	private Path createAndValidatePathToStructure(ResourceLocation resourceLocation, String string) {
		if (resourceLocation.getPath().contains("//")) {
			throw new ResourceLocationException("Invalid resource path: " + resourceLocation);
		} else {
			Path path = this.createPathToStructure(resourceLocation, string);
			if (path.startsWith(this.generatedDir) && FileUtil.isPathNormalized(path) && FileUtil.isPathPortable(path)) {
				return path;
			} else {
				throw new ResourceLocationException("Invalid resource path: " + path);
			}
		}
	}

	public void remove(ResourceLocation resourceLocation) {
		this.structureRepository.remove(resourceLocation);
	}
}
