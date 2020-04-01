package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.datafix.DataFixTypes;
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
					LevelData levelData = this.getDataTagFor(string);
					if (levelData != null && (levelData.getVersion() == 19132 || levelData.getVersion() == 19133)) {
						boolean bl = levelData.getVersion() != this.getStorageVersion();
						String string2 = levelData.getLevelName();
						if (StringUtils.isEmpty(string2)) {
							string2 = string;
						}

						long l = 0L;
						list.add(new LevelSummary(levelData, string, string2, 0L, bl));
					}
				}
			}

			return list;
		}
	}

	private int getStorageVersion() {
		return 19133;
	}

	public LevelStorage selectLevel(String string, @Nullable MinecraftServer minecraftServer) {
		return selectLevel(this.baseDir, this.fixerUpper, string, minecraftServer);
	}

	protected static LevelStorage selectLevel(Path path, DataFixer dataFixer, String string, @Nullable MinecraftServer minecraftServer) {
		return new LevelStorage(path.toFile(), string, minecraftServer, dataFixer);
	}

	public boolean requiresConversion(String string) {
		LevelData levelData = this.getDataTagFor(string);
		return levelData != null && levelData.getVersion() != this.getStorageVersion();
	}

	public boolean convertLevel(String string, ProgressListener progressListener) {
		return McRegionUpgrader.convertLevel(this.baseDir, this.fixerUpper, string, progressListener);
	}

	@Nullable
	public LevelData getDataTagFor(String string) {
		return getDataTagFor(this.baseDir, this.fixerUpper, string);
	}

	@Nullable
	protected static LevelData getDataTagFor(Path path, DataFixer dataFixer, String string) {
		File file = new File(path.toFile(), string);
		if (!file.exists()) {
			return null;
		} else {
			File file2 = new File(file, "special_level.dat");
			if (file2.exists()) {
				LevelData levelData = getLevelData(file2, dataFixer);
				if (levelData != null) {
					return levelData;
				}
			}

			file2 = new File(file, "special_level.dat_old");
			return file2.exists() ? getLevelData(file2, dataFixer) : null;
		}
	}

	@Nullable
	public static LevelData getLevelData(File file, DataFixer dataFixer) {
		try {
			CompoundTag compoundTag = NbtIo.readCompressed(new FileInputStream(file));
			CompoundTag compoundTag2 = compoundTag.getCompound("Data");
			CompoundTag compoundTag3 = compoundTag2.contains("Player", 10) ? compoundTag2.getCompound("Player") : null;
			compoundTag2.remove("Player");
			int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
			return new LevelData(NbtUtils.update(dataFixer, DataFixTypes.LEVEL, compoundTag2, i), dataFixer, i, compoundTag3);
		} catch (Exception var6) {
			LOGGER.error("Exception reading {}", file, var6);
			return null;
		}
	}

	@Environment(EnvType.CLIENT)
	public void renameLevel(String string, String string2) {
		File file = new File(this.baseDir.toFile(), string);
		if (file.exists()) {
			File file2 = new File(file, "special_level.dat");
			if (file2.exists()) {
				try {
					CompoundTag compoundTag = NbtIo.readCompressed(new FileInputStream(file2));
					CompoundTag compoundTag2 = compoundTag.getCompound("Data");
					compoundTag2.putString("LevelName", string2);
					NbtIo.writeCompressed(compoundTag, new FileOutputStream(file2));
				} catch (Exception var7) {
					var7.printStackTrace();
				}
			}
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
	public boolean deleteLevel(String string) {
		File file = new File(this.baseDir.toFile(), string);
		if (!file.exists()) {
			return true;
		} else {
			LOGGER.info("Deleting level {}", string);

			for (int i = 1; i <= 5; i++) {
				LOGGER.info("Attempt {}...", i);
				if (deleteRecursive(file.listFiles())) {
					break;
				}

				LOGGER.warn("Unsuccessful in deleting contents.");
				if (i < 5) {
					try {
						Thread.sleep(500L);
					} catch (InterruptedException var5) {
					}
				}
			}

			return file.delete();
		}
	}

	@Environment(EnvType.CLIENT)
	private static boolean deleteRecursive(File[] files) {
		for (File file : files) {
			LOGGER.debug("Deleting {}", file);
			if (file.isDirectory() && !deleteRecursive(file.listFiles())) {
				LOGGER.warn("Couldn't delete directory {}", file);
				return false;
			}

			if (!file.delete()) {
				LOGGER.warn("Couldn't delete file {}", file);
				return false;
			}
		}

		return true;
	}

	@Environment(EnvType.CLIENT)
	public boolean levelExists(String string) {
		return Files.isDirectory(this.baseDir.resolve(string), new LinkOption[0]);
	}

	@Environment(EnvType.CLIENT)
	public Path getBaseDir() {
		return this.baseDir;
	}

	public File getFile(String string, String string2) {
		return this.baseDir.resolve(string).resolve(string2).toFile();
	}

	@Environment(EnvType.CLIENT)
	private Path getLevelPath(String string) {
		return this.baseDir.resolve(string);
	}

	@Environment(EnvType.CLIENT)
	public Path getBackupPath() {
		return this.backupDir;
	}

	@Environment(EnvType.CLIENT)
	public long makeWorldBackup(String string) throws IOException {
		final Path path = this.getLevelPath(string);
		String string2 = LocalDateTime.now().format(FORMATTER) + "_" + string;
		Path path2 = this.getBackupPath();

		try {
			Files.createDirectories(Files.exists(path2, new LinkOption[0]) ? path2.toRealPath() : path2);
		} catch (IOException var18) {
			throw new RuntimeException(var18);
		}

		Path path3 = path2.resolve(FileUtil.findAvailableName(path2, string2, ".zip"));
		final ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path3)));
		Throwable var7 = null;

		try {
			final Path path4 = Paths.get(string);
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
					String string = path4.resolve(path.relativize(path)).toString().replace('\\', '/');
					ZipEntry zipEntry = new ZipEntry(string);
					zipOutputStream.putNextEntry(zipEntry);
					com.google.common.io.Files.asByteSource(path.toFile()).copyTo(zipOutputStream);
					zipOutputStream.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (Throwable var17) {
			var7 = var17;
			throw var17;
		} finally {
			if (zipOutputStream != null) {
				if (var7 != null) {
					try {
						zipOutputStream.close();
					} catch (Throwable var16) {
						var7.addSuppressed(var16);
					}
				} else {
					zipOutputStream.close();
				}
			}
		}

		return Files.size(path3);
	}
}
