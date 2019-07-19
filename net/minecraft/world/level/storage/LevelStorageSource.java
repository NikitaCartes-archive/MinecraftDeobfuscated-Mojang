/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.McRegionUpgrader;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class LevelStorageSource {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('_').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral('-').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral('-').appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();
    private final Path baseDir;
    private final Path backupDir;
    private final DataFixer fixerUpper;

    public LevelStorageSource(Path path, Path path2, DataFixer dataFixer) {
        this.fixerUpper = dataFixer;
        try {
            Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath(new LinkOption[0]) : path, new FileAttribute[0]);
        } catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        this.baseDir = path;
        this.backupDir = path2;
    }

    @Environment(value=EnvType.CLIENT)
    public String getName() {
        return "Anvil";
    }

    @Environment(value=EnvType.CLIENT)
    public List<LevelSummary> getLevelList() throws LevelStorageException {
        File[] files;
        if (!Files.isDirectory(this.baseDir, new LinkOption[0])) {
            throw new LevelStorageException(new TranslatableComponent("selectWorld.load_folder_access", new Object[0]).getString());
        }
        ArrayList<LevelSummary> list = Lists.newArrayList();
        for (File file : files = this.baseDir.toFile().listFiles()) {
            String string;
            LevelData levelData;
            if (!file.isDirectory() || (levelData = this.getDataTagFor(string = file.getName())) == null || levelData.getVersion() != 19132 && levelData.getVersion() != 19133) continue;
            boolean bl = levelData.getVersion() != this.getStorageVersion();
            String string2 = levelData.getLevelName();
            if (StringUtils.isEmpty(string2)) {
                string2 = string;
            }
            long l = 0L;
            list.add(new LevelSummary(levelData, string, string2, 0L, bl));
        }
        return list;
    }

    private int getStorageVersion() {
        return 19133;
    }

    public LevelStorage selectLevel(String string, @Nullable MinecraftServer minecraftServer) {
        return LevelStorageSource.selectLevel(this.baseDir, this.fixerUpper, string, minecraftServer);
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
        return LevelStorageSource.getDataTagFor(this.baseDir, this.fixerUpper, string);
    }

    @Nullable
    protected static LevelData getDataTagFor(Path path, DataFixer dataFixer, String string) {
        LevelData levelData;
        File file = new File(path.toFile(), string);
        if (!file.exists()) {
            return null;
        }
        File file2 = new File(file, "level.dat");
        if (file2.exists() && (levelData = LevelStorageSource.getLevelData(file2, dataFixer)) != null) {
            return levelData;
        }
        file2 = new File(file, "level.dat_old");
        if (file2.exists()) {
            return LevelStorageSource.getLevelData(file2, dataFixer);
        }
        return null;
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
        } catch (Exception exception) {
            LOGGER.error("Exception reading {}", (Object)file, (Object)exception);
            return null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public void renameLevel(String string, String string2) {
        File file = new File(this.baseDir.toFile(), string);
        if (!file.exists()) {
            return;
        }
        File file2 = new File(file, "level.dat");
        if (file2.exists()) {
            try {
                CompoundTag compoundTag = NbtIo.readCompressed(new FileInputStream(file2));
                CompoundTag compoundTag2 = compoundTag.getCompound("Data");
                compoundTag2.putString("LevelName", string2);
                NbtIo.writeCompressed(compoundTag, new FileOutputStream(file2));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isNewLevelIdAcceptable(String string) {
        try {
            Path path = this.baseDir.resolve(string);
            Files.createDirectory(path, new FileAttribute[0]);
            Files.deleteIfExists(path);
            return true;
        } catch (IOException iOException) {
            return false;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public boolean deleteLevel(String string) {
        File file = new File(this.baseDir.toFile(), string);
        if (!file.exists()) {
            return true;
        }
        LOGGER.info("Deleting level {}", (Object)string);
        for (int i = 1; i <= 5; ++i) {
            LOGGER.info("Attempt {}...", (Object)i);
            if (LevelStorageSource.deleteRecursive(file.listFiles())) break;
            LOGGER.warn("Unsuccessful in deleting contents.");
            if (i >= 5) continue;
            try {
                Thread.sleep(500L);
                continue;
            } catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        return file.delete();
    }

    @Environment(value=EnvType.CLIENT)
    private static boolean deleteRecursive(File[] files) {
        for (File file : files) {
            LOGGER.debug("Deleting {}", (Object)file);
            if (file.isDirectory() && !LevelStorageSource.deleteRecursive(file.listFiles())) {
                LOGGER.warn("Couldn't delete directory {}", (Object)file);
                return false;
            }
            if (file.delete()) continue;
            LOGGER.warn("Couldn't delete file {}", (Object)file);
            return false;
        }
        return true;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean levelExists(String string) {
        return Files.isDirectory(this.baseDir.resolve(string), new LinkOption[0]);
    }

    @Environment(value=EnvType.CLIENT)
    public Path getBaseDir() {
        return this.baseDir;
    }

    public File getFile(String string, String string2) {
        return this.baseDir.resolve(string).resolve(string2).toFile();
    }

    @Environment(value=EnvType.CLIENT)
    private Path getLevelPath(String string) {
        return this.baseDir.resolve(string);
    }

    @Environment(value=EnvType.CLIENT)
    public Path getBackupPath() {
        return this.backupDir;
    }

    @Environment(value=EnvType.CLIENT)
    public long makeWorldBackup(String string) throws IOException {
        final Path path = this.getLevelPath(string);
        String string2 = LocalDateTime.now().format(FORMATTER) + "_" + string;
        Path path2 = this.getBackupPath();
        try {
            Files.createDirectories(Files.exists(path2, new LinkOption[0]) ? path2.toRealPath(new LinkOption[0]) : path2, new FileAttribute[0]);
        } catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        Path path3 = path2.resolve(FileUtil.findAvailableName(path2, string2, ".zip"));
        try (final ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path3, new OpenOption[0])));){
            final Path path4 = Paths.get(string, new String[0]);
            Files.walkFileTree(path, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                @Override
                public FileVisitResult visitFile(Path path2, BasicFileAttributes basicFileAttributes) throws IOException {
                    String string = path4.resolve(path.relativize(path2)).toString().replace('\\', '/');
                    ZipEntry zipEntry = new ZipEntry(string);
                    zipOutputStream.putNextEntry(zipEntry);
                    com.google.common.io.Files.asByteSource(path2.toFile()).copyTo(zipOutputStream);
                    zipOutputStream.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public /* synthetic */ FileVisitResult visitFile(Object object, BasicFileAttributes basicFileAttributes) throws IOException {
                    return this.visitFile((Path)object, basicFileAttributes);
                }
            });
        }
        return Files.size(path3);
    }
}

