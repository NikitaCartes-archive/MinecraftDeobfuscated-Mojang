package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.commons.lang3.StringUtils;
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

	@Environment(EnvType.CLIENT)
	public List<LevelSummary> getLevelList() throws LevelStorageException {
		if (!Files.isDirectory(this.baseDir, new LinkOption[0])) {
			throw new LevelStorageException(new TranslatableComponent("selectWorld.load_folder_access").getString());
		} else {
			List<LevelSummary> list = Lists.<LevelSummary>newArrayList();
			File[] files = this.baseDir.toFile().listFiles();

			for (File file : files) {
				if (file.isDirectory()) {
					String string = file.getName();

					boolean bl;
					try {
						bl = DirectoryLock.isLocked(file.toPath());
					} catch (Exception var15) {
						LOGGER.warn("Failed to read {} lock", file, var15);
						continue;
					}

					WorldData worldData = this.getLevelData(file);
					if (worldData != null && (worldData.getVersion() == 19132 || worldData.getVersion() == 19133)) {
						boolean bl2 = worldData.getVersion() != this.getStorageVersion();
						String string2 = worldData.getLevelName();
						if (StringUtils.isEmpty(string2)) {
							string2 = string;
						}

						long l = 0L;
						File file2 = new File(file, "icon.png");
						list.add(new LevelSummary(worldData, string, string2, 0L, bl2, bl, file2));
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
	private WorldData getLevelData(File file) {
		if (!file.exists()) {
			return null;
		} else {
			File file2 = new File(file, "level.dat");
			if (file2.exists()) {
				WorldData worldData = getLevelData(file2, this.fixerUpper);
				if (worldData != null) {
					return worldData;
				}
			}

			file2 = new File(file, "level.dat_old");
			return file2.exists() ? getLevelData(file2, this.fixerUpper) : null;
		}
	}

	@Nullable
	public static WorldData getLevelData(File file, DataFixer dataFixer) {
		try {
			CompoundTag compoundTag = NbtIo.readCompressed(new FileInputStream(file));
			CompoundTag compoundTag2 = compoundTag.getCompound("Data");
			CompoundTag compoundTag3 = compoundTag2.contains("Player", 10) ? compoundTag2.getCompound("Player") : null;
			compoundTag2.remove("Player");
			int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
			return new PrimaryLevelData(NbtUtils.update(dataFixer, DataFixTypes.LEVEL, compoundTag2, i), dataFixer, i, compoundTag3);
		} catch (Exception var6) {
			LOGGER.error("Exception reading {}", file, var6);
			return null;
		}
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

		public File getDimensionPath(DimensionType dimensionType) {
			return dimensionType.getStorageFolder(this.levelPath.toFile());
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
			WorldData worldData = this.getDataTag();
			return worldData != null && worldData.getVersion() != LevelStorageSource.this.getStorageVersion();
		}

		public boolean convertLevel(ProgressListener progressListener) {
			this.checkLock();
			return McRegionUpgrader.convertLevel(this, progressListener);
		}

		@Nullable
		public WorldData getDataTag() {
			this.checkLock();
			return LevelStorageSource.this.getLevelData(this.levelPath.toFile());
		}

		public void saveDataTag(WorldData worldData) {
			this.saveDataTag(worldData, null);
		}

		public void saveDataTag(WorldData worldData, @Nullable CompoundTag compoundTag) {
			File file = this.levelPath.toFile();
			CompoundTag compoundTag2 = worldData.createTag(compoundTag);
			CompoundTag compoundTag3 = new CompoundTag();
			compoundTag3.put("Data", compoundTag2);

			try {
				File file2 = File.createTempFile("level", ".dat", file);
				NbtIo.writeCompressed(compoundTag3, new FileOutputStream(file2));
				File file3 = new File(file, "level.dat_old");
				File file4 = new File(file, "level.dat");
				Util.safeReplaceFile(file4, file2, file3);
			} catch (Exception var9) {
				LevelStorageSource.LOGGER.error("Failed to save level {}", file, var9);
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
					CompoundTag compoundTag = NbtIo.readCompressed(new FileInputStream(file2));
					CompoundTag compoundTag2 = compoundTag.getCompound("Data");
					compoundTag2.putString("LevelName", string);
					NbtIo.writeCompressed(compoundTag, new FileOutputStream(file2));
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
