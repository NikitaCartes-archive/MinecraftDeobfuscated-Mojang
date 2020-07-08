package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LevelStorageSource {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
		.appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
		.appendLiteral('-')
		.appendValue(ChronoField.MONTH_OF_YEAR, 2)
		.appendLiteral('-')
		.appendValue(ChronoField.DAY_OF_MONTH, 2)
		.appendLiteral('_')
		.appendValue(ChronoField.HOUR_OF_DAY, 2)
		.appendLiteral('-')
		.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
		.appendLiteral('-')
		.appendValue(ChronoField.SECOND_OF_MINUTE, 2)
		.toFormatter();
	private static final ImmutableList<String> OLD_SETTINGS_KEYS = ImmutableList.of(
		"RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest"
	);
	private final Path baseDir;
	private final Path backupDir;
	private final DataFixer fixerUpper;

	public LevelStorageSource(Path path, Path path2, DataFixer dataFixer) {
		this.fixerUpper = dataFixer;

		try {
			Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath() : path);
		} catch (IOException var5) {
			throw new RuntimeException(var5);
		}

		this.baseDir = path;
		this.backupDir = path2;
	}

	public static LevelStorageSource createDefault(Path path) {
		return new LevelStorageSource(path, path.resolve("../backups"), DataFixers.getDataFixer());
	}

	private static Pair<WorldGenSettings, Lifecycle> readWorldGenSettings(Dynamic<?> dynamic, DataFixer dataFixer, int i) {
		Dynamic<?> dynamic2 = dynamic.get("WorldGenSettings").orElseEmptyMap();

		for (String string : OLD_SETTINGS_KEYS) {
			Optional<? extends Dynamic<?>> optional = dynamic.get(string).result();
			if (optional.isPresent()) {
				dynamic2 = dynamic2.set(string, (Dynamic<?>)optional.get());
			}
		}

		Dynamic<?> dynamic3 = dataFixer.update(References.WORLD_GEN_SETTINGS, dynamic2, i, SharedConstants.getCurrentVersion().getWorldVersion());
		DataResult<WorldGenSettings> dataResult = WorldGenSettings.CODEC.parse(dynamic3);
		return Pair.of(
			(WorldGenSettings)dataResult.resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error)).orElseGet(WorldGenSettings::makeDefault),
			dataResult.lifecycle()
		);
	}

	private static DataPackConfig readDataPackConfig(Dynamic<?> dynamic) {
		return (DataPackConfig)DataPackConfig.CODEC.parse(dynamic).resultOrPartial(LOGGER::error).orElse(DataPackConfig.DEFAULT);
	}

	@Environment(EnvType.CLIENT)
	public List<LevelSummary> getLevelList() throws LevelStorageException {
		if (!Files.isDirectory(this.baseDir, new LinkOption[0])) {
			throw new LevelStorageException(new TranslatableComponent("selectWorld.load_folder_access").getString());
		} else {
			List<LevelSummary> list = Lists.<LevelSummary>newArrayList();
			File[] files = this.baseDir.toFile().listFiles();

			for (File file : files) {
				if (file.isDirectory()) {
					boolean bl;
					try {
						bl = DirectoryLock.isLocked(file.toPath());
					} catch (Exception var9) {
						LOGGER.warn("Failed to read {} lock", file, var9);
						continue;
					}

					LevelSummary levelSummary = this.readLevelData(file, this.levelSummaryReader(file, bl));
					if (levelSummary != null) {
						list.add(levelSummary);
					}
				}
			}

			return list;
		}
	}

	private int getStorageVersion() {
		return 19133;
	}

	@Nullable
	private <T> T readLevelData(File file, BiFunction<File, DataFixer, T> biFunction) {
		if (!file.exists()) {
			return null;
		} else {
			File file2 = new File(file, "level.dat");
			if (file2.exists()) {
				T object = (T)biFunction.apply(file2, this.fixerUpper);
				if (object != null) {
					return object;
				}
			}

			file2 = new File(file, "level.dat_old");
			return (T)(file2.exists() ? biFunction.apply(file2, this.fixerUpper) : null);
		}
	}

	@Nullable
	private static DataPackConfig getDataPacks(File file, DataFixer dataFixer) {
		try {
			CompoundTag compoundTag = NbtIo.readCompressed(file);
			CompoundTag compoundTag2 = compoundTag.getCompound("Data");
			compoundTag2.remove("Player");
			int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
			Dynamic<Tag> dynamic = dataFixer.update(
				DataFixTypes.LEVEL.getType(), new Dynamic<>(NbtOps.INSTANCE, compoundTag2), i, SharedConstants.getCurrentVersion().getWorldVersion()
			);
			return (DataPackConfig)dynamic.get("DataPacks").result().map(LevelStorageSource::readDataPackConfig).orElse(DataPackConfig.DEFAULT);
		} catch (Exception var6) {
			LOGGER.error("Exception reading {}", file, var6);
			return null;
		}
	}

	private static BiFunction<File, DataFixer, PrimaryLevelData> getLevelData(DynamicOps<Tag> dynamicOps, DataPackConfig dataPackConfig) {
		return (file, dataFixer) -> {
			try {
				CompoundTag compoundTag = NbtIo.readCompressed(file);
				CompoundTag compoundTag2 = compoundTag.getCompound("Data");
				CompoundTag compoundTag3 = compoundTag2.contains("Player", 10) ? compoundTag2.getCompound("Player") : null;
				compoundTag2.remove("Player");
				int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
				Dynamic<Tag> dynamic = dataFixer.update(
					DataFixTypes.LEVEL.getType(), new Dynamic<>(dynamicOps, compoundTag2), i, SharedConstants.getCurrentVersion().getWorldVersion()
				);
				Pair<WorldGenSettings, Lifecycle> pair = readWorldGenSettings(dynamic, dataFixer, i);
				LevelVersion levelVersion = LevelVersion.parse(dynamic);
				LevelSettings levelSettings = LevelSettings.parse(dynamic, dataPackConfig);
				return PrimaryLevelData.parse(dynamic, dataFixer, i, compoundTag3, levelSettings, levelVersion, pair.getFirst(), pair.getSecond());
			} catch (Exception var12) {
				LOGGER.error("Exception reading {}", file, var12);
				return null;
			}
		};
	}

	private BiFunction<File, DataFixer, LevelSummary> levelSummaryReader(File file, boolean bl) {
		return (file2, dataFixer) -> {
			try {
				CompoundTag compoundTag = NbtIo.readCompressed(file2);
				CompoundTag compoundTag2 = compoundTag.getCompound("Data");
				compoundTag2.remove("Player");
				int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
				Dynamic<Tag> dynamic = dataFixer.update(
					DataFixTypes.LEVEL.getType(), new Dynamic<>(NbtOps.INSTANCE, compoundTag2), i, SharedConstants.getCurrentVersion().getWorldVersion()
				);
				LevelVersion levelVersion = LevelVersion.parse(dynamic);
				int j = levelVersion.levelDataVersion();
				if (j != 19132 && j != 19133) {
					return null;
				} else {
					boolean bl2 = j != this.getStorageVersion();
					File file3 = new File(file, "icon.png");
					DataPackConfig dataPackConfig = (DataPackConfig)dynamic.get("DataPacks")
						.result()
						.map(LevelStorageSource::readDataPackConfig)
						.orElse(DataPackConfig.DEFAULT);
					LevelSettings levelSettings = LevelSettings.parse(dynamic, dataPackConfig);
					return new LevelSummary(levelSettings, levelVersion, file.getName(), bl2, bl, file3);
				}
			} catch (Exception var15) {
				LOGGER.error("Exception reading {}", file2, var15);
				return null;
			}
		};
	}

	@Environment(EnvType.CLIENT)
	public boolean isNewLevelIdAcceptable(String string) {
		try {
			Path path = this.baseDir.resolve(string);
			Files.createDirectory(path);
			Files.deleteIfExists(path);
			return true;
		} catch (IOException var3) {
			return false;
		}
	}

	@Environment(EnvType.CLIENT)
	public boolean levelExists(String string) {
		return Files.isDirectory(this.baseDir.resolve(string), new LinkOption[0]);
	}

	@Environment(EnvType.CLIENT)
	public Path getBaseDir() {
		return this.baseDir;
	}

	@Environment(EnvType.CLIENT)
	public Path getBackupPath() {
		return this.backupDir;
	}

	public LevelStorageSource.LevelStorageAccess createAccess(String string) throws IOException {
		return new LevelStorageSource.LevelStorageAccess(string);
	}

	public class LevelStorageAccess implements AutoCloseable {
		private final DirectoryLock lock;
		private final Path levelPath;
		private final String levelId;
		private final Map<LevelResource, Path> resources = Maps.<LevelResource, Path>newHashMap();

		public LevelStorageAccess(String string) throws IOException {
			this.levelId = string;
			this.levelPath = LevelStorageSource.this.baseDir.resolve(string);
			this.lock = DirectoryLock.create(this.levelPath);
		}

		public String getLevelId() {
			return this.levelId;
		}

		public Path getLevelPath(LevelResource levelResource) {
			return (Path)this.resources.computeIfAbsent(levelResource, levelResourcex -> this.levelPath.resolve(levelResourcex.getId()));
		}

		public File getDimensionPath(ResourceKey<Level> resourceKey) {
			return DimensionType.getStorageFolder(resourceKey, this.levelPath.toFile());
		}

		private void checkLock() {
			if (!this.lock.isValid()) {
				throw new IllegalStateException("Lock is no longer valid");
			}
		}

		public PlayerDataStorage createPlayerStorage() {
			this.checkLock();
			return new PlayerDataStorage(this, LevelStorageSource.this.fixerUpper);
		}

		public boolean requiresConversion() {
			LevelSummary levelSummary = this.getSummary();
			return levelSummary != null && levelSummary.levelVersion().levelDataVersion() != LevelStorageSource.this.getStorageVersion();
		}

		public boolean convertLevel(ProgressListener progressListener) {
			this.checkLock();
			return McRegionUpgrader.convertLevel(this, progressListener);
		}

		@Nullable
		public LevelSummary getSummary() {
			this.checkLock();
			return LevelStorageSource.this.readLevelData(this.levelPath.toFile(), LevelStorageSource.this.levelSummaryReader(this.levelPath.toFile(), false));
		}

		@Nullable
		public WorldData getDataTag(DynamicOps<Tag> dynamicOps, DataPackConfig dataPackConfig) {
			this.checkLock();
			return LevelStorageSource.this.readLevelData(this.levelPath.toFile(), LevelStorageSource.getLevelData(dynamicOps, dataPackConfig));
		}

		@Nullable
		public DataPackConfig getDataPacks() {
			this.checkLock();
			return LevelStorageSource.this.readLevelData(this.levelPath.toFile(), (file, dataFixer) -> LevelStorageSource.getDataPacks(file, dataFixer));
		}

		public void saveDataTag(RegistryAccess registryAccess, WorldData worldData) {
			this.saveDataTag(registryAccess, worldData, null);
		}

		public void saveDataTag(RegistryAccess registryAccess, WorldData worldData, @Nullable CompoundTag compoundTag) {
			File file = this.levelPath.toFile();
			CompoundTag compoundTag2 = worldData.createTag(registryAccess, compoundTag);
			CompoundTag compoundTag3 = new CompoundTag();
			compoundTag3.put("Data", compoundTag2);

			try {
				File file2 = File.createTempFile("level", ".dat", file);
				NbtIo.writeCompressed(compoundTag3, file2);
				File file3 = new File(file, "level.dat_old");
				File file4 = new File(file, "level.dat");
				Util.safeReplaceFile(file4, file2, file3);
			} catch (Exception var10) {
				LevelStorageSource.LOGGER.error("Failed to save level {}", file, var10);
			}
		}

		public File getIconFile() {
			this.checkLock();
			return this.levelPath.resolve("icon.png").toFile();
		}

		@Environment(EnvType.CLIENT)
		public void deleteLevel() throws IOException {
			this.checkLock();
			final Path path = this.levelPath.resolve("session.lock");

			for (int i = 1; i <= 5; i++) {
				LevelStorageSource.LOGGER.info("Attempt {}...", i);

				try {
					Files.walkFileTree(this.levelPath, new SimpleFileVisitor<Path>() {
						public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
							if (!path.equals(path)) {
								LevelStorageSource.LOGGER.debug("Deleting {}", path);
								Files.delete(path);
							}

							return FileVisitResult.CONTINUE;
						}

						public FileVisitResult postVisitDirectory(Path path, IOException iOException) throws IOException {
							if (iOException != null) {
								throw iOException;
							} else {
								if (path.equals(LevelStorageAccess.this.levelPath)) {
									LevelStorageAccess.this.lock.close();
									Files.deleteIfExists(path);
								}

								Files.delete(path);
								return FileVisitResult.CONTINUE;
							}
						}
					});
					break;
				} catch (IOException var6) {
					if (i >= 5) {
						throw var6;
					}

					LevelStorageSource.LOGGER.warn("Failed to delete {}", this.levelPath, var6);

					try {
						Thread.sleep(500L);
					} catch (InterruptedException var5) {
					}
				}
			}
		}

		@Environment(EnvType.CLIENT)
		public void renameLevel(String string) throws IOException {
			this.checkLock();
			File file = new File(LevelStorageSource.this.baseDir.toFile(), this.levelId);
			if (file.exists()) {
				File file2 = new File(file, "level.dat");
				if (file2.exists()) {
					CompoundTag compoundTag = NbtIo.readCompressed(file2);
					CompoundTag compoundTag2 = compoundTag.getCompound("Data");
					compoundTag2.putString("LevelName", string);
					NbtIo.writeCompressed(compoundTag, file2);
				}
			}
		}

		@Environment(EnvType.CLIENT)
		public long makeWorldBackup() throws IOException {
			this.checkLock();
			String string = LocalDateTime.now().format(LevelStorageSource.FORMATTER) + "_" + this.levelId;
			Path path = LevelStorageSource.this.getBackupPath();

			try {
				Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath() : path);
			} catch (IOException var16) {
				throw new RuntimeException(var16);
			}

			Path path2 = path.resolve(FileUtil.findAvailableName(path, string, ".zip"));
			final ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path2)));
			Throwable var5 = null;

			try {
				final Path path3 = Paths.get(this.levelId);
				Files.walkFileTree(this.levelPath, new SimpleFileVisitor<Path>() {
					public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
						if (path.endsWith("session.lock")) {
							return FileVisitResult.CONTINUE;
						} else {
							String string = path3.resolve(LevelStorageAccess.this.levelPath.relativize(path)).toString().replace('\\', '/');
							ZipEntry zipEntry = new ZipEntry(string);
							zipOutputStream.putNextEntry(zipEntry);
							com.google.common.io.Files.asByteSource(path.toFile()).copyTo(zipOutputStream);
							zipOutputStream.closeEntry();
							return FileVisitResult.CONTINUE;
						}
					}
				});
			} catch (Throwable var15) {
				var5 = var15;
				throw var15;
			} finally {
				if (zipOutputStream != null) {
					if (var5 != null) {
						try {
							zipOutputStream.close();
						} catch (Throwable var14) {
							var5.addSuppressed(var14);
						}
					} else {
						zipOutputStream.close();
					}
				}
			}

			return Files.size(path2);
		}

		public void close() throws IOException {
			this.lock.close();
		}
	}
}
