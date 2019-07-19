package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
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
import java.nio.file.Path;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.datafix.DataFixTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureManager implements ResourceManagerReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Map<ResourceLocation, StructureTemplate> structureRepository = Maps.<ResourceLocation, StructureTemplate>newHashMap();
	private final DataFixer fixerUpper;
	private final MinecraftServer server;
	private final Path generatedDir;

	public StructureManager(MinecraftServer minecraftServer, File file, DataFixer dataFixer) {
		this.server = minecraftServer;
		this.fixerUpper = dataFixer;
		this.generatedDir = file.toPath().resolve("generated").normalize();
		minecraftServer.getResources().registerReloadListener(this);
	}

	public StructureTemplate getOrCreate(ResourceLocation resourceLocation) {
		StructureTemplate structureTemplate = this.get(resourceLocation);
		if (structureTemplate == null) {
			structureTemplate = new StructureTemplate();
			this.structureRepository.put(resourceLocation, structureTemplate);
		}

		return structureTemplate;
	}

	@Nullable
	public StructureTemplate get(ResourceLocation resourceLocation) {
		return (StructureTemplate)this.structureRepository.computeIfAbsent(resourceLocation, resourceLocationx -> {
			StructureTemplate structureTemplate = this.loadFromGenerated(resourceLocationx);
			return structureTemplate != null ? structureTemplate : this.loadFromResource(resourceLocationx);
		});
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.structureRepository.clear();
	}

	@Nullable
	private StructureTemplate loadFromResource(ResourceLocation resourceLocation) {
		ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), "structures/" + resourceLocation.getPath() + ".nbt");

		try {
			Resource resource = this.server.getResources().getResource(resourceLocation2);
			Throwable var4 = null;

			StructureTemplate var5;
			try {
				var5 = this.readStructure(resource.getInputStream());
			} catch (Throwable var16) {
				var4 = var16;
				throw var16;
			} finally {
				if (resource != null) {
					if (var4 != null) {
						try {
							resource.close();
						} catch (Throwable var15) {
							var4.addSuppressed(var15);
						}
					} else {
						resource.close();
					}
				}
			}

			return var5;
		} catch (FileNotFoundException var18) {
			return null;
		} catch (Throwable var19) {
			LOGGER.error("Couldn't load structure {}: {}", resourceLocation, var19.toString());
			return null;
		}
	}

	@Nullable
	private StructureTemplate loadFromGenerated(ResourceLocation resourceLocation) {
		if (!this.generatedDir.toFile().isDirectory()) {
			return null;
		} else {
			Path path = this.createAndValidatePathToStructure(resourceLocation, ".nbt");

			try {
				InputStream inputStream = new FileInputStream(path.toFile());
				Throwable var4 = null;

				StructureTemplate var5;
				try {
					var5 = this.readStructure(inputStream);
				} catch (Throwable var16) {
					var4 = var16;
					throw var16;
				} finally {
					if (inputStream != null) {
						if (var4 != null) {
							try {
								inputStream.close();
							} catch (Throwable var15) {
								var4.addSuppressed(var15);
							}
						} else {
							inputStream.close();
						}
					}
				}

				return var5;
			} catch (FileNotFoundException var18) {
				return null;
			} catch (IOException var19) {
				LOGGER.error("Couldn't load structure from {}", path, var19);
				return null;
			}
		}
	}

	private StructureTemplate readStructure(InputStream inputStream) throws IOException {
		CompoundTag compoundTag = NbtIo.readCompressed(inputStream);
		if (!compoundTag.contains("DataVersion", 99)) {
			compoundTag.putInt("DataVersion", 500);
		}

		StructureTemplate structureTemplate = new StructureTemplate();
		structureTemplate.load(NbtUtils.update(this.fixerUpper, DataFixTypes.STRUCTURE, compoundTag, compoundTag.getInt("DataVersion")));
		return structureTemplate;
	}

	public boolean save(ResourceLocation resourceLocation) {
		StructureTemplate structureTemplate = (StructureTemplate)this.structureRepository.get(resourceLocation);
		if (structureTemplate == null) {
			return false;
		} else {
			Path path = this.createAndValidatePathToStructure(resourceLocation, ".nbt");
			Path path2 = path.getParent();
			if (path2 == null) {
				return false;
			} else {
				try {
					Files.createDirectories(Files.exists(path2, new LinkOption[0]) ? path2.toRealPath() : path2);
				} catch (IOException var19) {
					LOGGER.error("Failed to create parent directory: {}", path2);
					return false;
				}

				CompoundTag compoundTag = structureTemplate.save(new CompoundTag());

				try {
					OutputStream outputStream = new FileOutputStream(path.toFile());
					Throwable var7 = null;

					try {
						NbtIo.writeCompressed(compoundTag, outputStream);
					} catch (Throwable var18) {
						var7 = var18;
						throw var18;
					} finally {
						if (outputStream != null) {
							if (var7 != null) {
								try {
									outputStream.close();
								} catch (Throwable var17) {
									var7.addSuppressed(var17);
								}
							} else {
								outputStream.close();
							}
						}
					}

					return true;
				} catch (Throwable var21) {
					return false;
				}
			}
		}
	}

	private Path createPathToStructure(ResourceLocation resourceLocation, String string) {
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
