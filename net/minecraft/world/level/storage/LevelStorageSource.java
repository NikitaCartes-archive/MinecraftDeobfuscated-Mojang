/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.LevelVersion;
import net.minecraft.world.level.storage.McRegionUpgrader;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class LevelStorageSource {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('_').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral('-').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral('-').appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();
    private static final ImmutableList<String> OLD_SETTINGS_KEYS = ImmutableList.of("RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest");
    private static final Gson WORLD_GEN_SETTINGS_GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
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

    public static LevelStorageSource createDefault(Path path) {
        return new LevelStorageSource(path, path.resolve("../backups"), DataFixers.getDataFixer());
    }

    private static Pair<WorldGenSettings, Lifecycle> readWorldGenSettings(Dynamic<?> dynamic, DataFixer dataFixer, int i) {
        Dynamic<?> dynamic2 = dynamic.get("WorldGenSettings").orElseEmptyMap();
        for (String string : OLD_SETTINGS_KEYS) {
            Optional<Dynamic<?>> optional = dynamic.get(string).result();
            if (!optional.isPresent()) continue;
            dynamic2 = dynamic2.set(string, optional.get());
        }
        Dynamic<?> dynamic3 = dataFixer.update(References.WORLD_GEN_SETTINGS, dynamic2, i, SharedConstants.getCurrentVersion().getWorldVersion());
        DataResult dataResult = WorldGenSettings.CODEC.parse(dynamic3);
        return Pair.of(dataResult.resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error)).orElseGet(WorldGenSettings::makeDefault), dataResult.lifecycle());
    }

    @Environment(value=EnvType.CLIENT)
    public List<LevelSummary> getLevelList() throws LevelStorageException {
        File[] files;
        if (!Files.isDirectory(this.baseDir, new LinkOption[0])) {
            throw new LevelStorageException(new TranslatableComponent("selectWorld.load_folder_access").getString());
        }
        ArrayList<LevelSummary> list = Lists.newArrayList();
        for (File file : files = this.baseDir.toFile().listFiles()) {
            boolean bl;
            if (!file.isDirectory()) continue;
            try {
                bl = DirectoryLock.isLocked(file.toPath());
            } catch (Exception exception) {
                LOGGER.warn("Failed to read {} lock", (Object)file, (Object)exception);
                continue;
            }
            LevelSummary levelSummary = this.readLevelData(file, this.levelSummaryReader(file, bl));
            if (levelSummary == null) continue;
            list.add(levelSummary);
        }
        return list;
    }

    private int getStorageVersion() {
        return 19133;
    }

    @Nullable
    private <T> T readLevelData(File file, BiFunction<File, DataFixer, T> biFunction) {
        T object;
        if (!file.exists()) {
            return null;
        }
        File file2 = new File(file, "level.dat");
        if (file2.exists() && (object = biFunction.apply(file2, this.fixerUpper)) != null) {
            return object;
        }
        file2 = new File(file, "level.dat_old");
        if (file2.exists()) {
            return biFunction.apply(file2, this.fixerUpper);
        }
        return null;
    }

    @Nullable
    private static WorldData getLevelData(File file, DataFixer dataFixer) {
        try {
            CompoundTag compoundTag = NbtIo.readCompressed(new FileInputStream(file));
            CompoundTag compoundTag2 = compoundTag.getCompound("Data");
            CompoundTag compoundTag3 = compoundTag2.contains("Player", 10) ? compoundTag2.getCompound("Player") : null;
            compoundTag2.remove("Player");
            int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
            Dynamic<Tag> dynamic = dataFixer.update(DataFixTypes.LEVEL.getType(), new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag2), i, SharedConstants.getCurrentVersion().getWorldVersion());
            Pair<WorldGenSettings, Lifecycle> pair = LevelStorageSource.readWorldGenSettings(dynamic, dataFixer, i);
            LevelVersion levelVersion = LevelVersion.parse(dynamic);
            LevelSettings levelSettings = LevelSettings.parse(dynamic, pair.getFirst());
            return PrimaryLevelData.parse(dynamic, dataFixer, i, compoundTag3, levelSettings, levelVersion);
        } catch (Exception exception) {
            LOGGER.error("Exception reading {}", (Object)file, (Object)exception);
            return null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    private BiFunction<File, DataFixer, LevelSummary> levelSummaryReader(File file, boolean bl) {
        return (file2, dataFixer) -> {
            try {
                CompoundTag compoundTag = NbtIo.readCompressed(new FileInputStream((File)file2));
                CompoundTag compoundTag2 = compoundTag.getCompound("Data");
                compoundTag2.remove("Player");
                int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
                Dynamic<CompoundTag> dynamic = dataFixer.update(DataFixTypes.LEVEL.getType(), new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag2), i, SharedConstants.getCurrentVersion().getWorldVersion());
                LevelVersion levelVersion = LevelVersion.parse(dynamic);
                int j = levelVersion.levelDataVersion();
                if (j == 19132 || j == 19133) {
                    boolean bl2 = j != this.getStorageVersion();
                    File file3 = new File(file, "icon.png");
                    Pair<WorldGenSettings, Lifecycle> pair = LevelStorageSource.readWorldGenSettings(dynamic, dataFixer, i);
                    LevelSettings levelSettings = LevelSettings.parse(dynamic, pair.getFirst());
                    return new LevelSummary(levelSettings, levelVersion, file.getName(), bl2, bl, file3, pair.getSecond());
                }
                return null;
            } catch (Exception exception) {
                LOGGER.error("Exception reading {}", file2, (Object)exception);
                return null;
            }
        };
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
    public boolean levelExists(String string) {
        return Files.isDirectory(this.baseDir.resolve(string), new LinkOption[0]);
    }

    @Environment(value=EnvType.CLIENT)
    public Path getBaseDir() {
        return this.baseDir;
    }

    @Environment(value=EnvType.CLIENT)
    public Path getBackupPath() {
        return this.backupDir;
    }

    public LevelStorageAccess createAccess(String string) throws IOException {
        return new LevelStorageAccess(string);
    }

    public class LevelStorageAccess
    implements AutoCloseable {
        private final DirectoryLock lock;
        private final Path levelPath;
        private final String levelId;
        private final Map<LevelResource, Path> resources = Maps.newHashMap();

        public LevelStorageAccess(String string) throws IOException {
            this.levelId = string;
            this.levelPath = LevelStorageSource.this.baseDir.resolve(string);
            this.lock = DirectoryLock.create(this.levelPath);
        }

        public String getLevelId() {
            return this.levelId;
        }

        public Path getLevelPath(LevelResource levelResource2) {
            return this.resources.computeIfAbsent(levelResource2, levelResource -> this.levelPath.resolve(levelResource.getId()));
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
            return (WorldData)LevelStorageSource.this.readLevelData(this.levelPath.toFile(), (file, dataFixer) -> LevelStorageSource.getLevelData(file, dataFixer));
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
            } catch (Exception exception) {
                LOGGER.error("Failed to save level {}", (Object)file, (Object)exception);
            }
        }

        public File getIconFile() {
            this.checkLock();
            return this.levelPath.resolve("icon.png").toFile();
        }

        @Environment(value=EnvType.CLIENT)
        public void deleteLevel() throws IOException {
            this.checkLock();
            final Path path = this.levelPath.resolve("session.lock");
            for (int i = 1; i <= 5; ++i) {
                LOGGER.info("Attempt {}...", (Object)i);
                try {
                    Files.walkFileTree(this.levelPath, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                        @Override
                        public FileVisitResult visitFile(Path path2, BasicFileAttributes basicFileAttributes) throws IOException {
                            if (!path2.equals(path)) {
                                LOGGER.debug("Deleting {}", (Object)path2);
                                Files.delete(path2);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path path2, IOException iOException) throws IOException {
                            if (iOException != null) {
                                throw iOException;
                            }
                            if (path2.equals(LevelStorageAccess.this.levelPath)) {
                                LevelStorageAccess.this.lock.close();
                                Files.deleteIfExists(path);
                            }
                            Files.delete(path2);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public /* synthetic */ FileVisitResult postVisitDirectory(Object object, IOException iOException) throws IOException {
                            return this.postVisitDirectory((Path)object, iOException);
                        }

                        @Override
                        public /* synthetic */ FileVisitResult visitFile(Object object, BasicFileAttributes basicFileAttributes) throws IOException {
                            return this.visitFile((Path)object, basicFileAttributes);
                        }
                    });
                    break;
                } catch (IOException iOException) {
                    if (i < 5) {
                        LOGGER.warn("Failed to delete {}", (Object)this.levelPath, (Object)iOException);
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException interruptedException) {}
                        continue;
                    }
                    throw iOException;
                }
            }
        }

        @Environment(value=EnvType.CLIENT)
        public void renameLevel(String string) throws IOException {
            this.checkLock();
            File file = new File(LevelStorageSource.this.baseDir.toFile(), this.levelId);
            if (!file.exists()) {
                return;
            }
            File file2 = new File(file, "level.dat");
            if (file2.exists()) {
                CompoundTag compoundTag = NbtIo.readCompressed(new FileInputStream(file2));
                CompoundTag compoundTag2 = compoundTag.getCompound("Data");
                compoundTag2.putString("LevelName", string);
                NbtIo.writeCompressed(compoundTag, new FileOutputStream(file2));
            }
        }

        @Environment(value=EnvType.CLIENT)
        public long makeWorldBackup() throws IOException {
            this.checkLock();
            String string = LocalDateTime.now().format(FORMATTER) + "_" + this.levelId;
            Path path = LevelStorageSource.this.getBackupPath();
            try {
                Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath(new LinkOption[0]) : path, new FileAttribute[0]);
            } catch (IOException iOException) {
                throw new RuntimeException(iOException);
            }
            Path path2 = path.resolve(FileUtil.findAvailableName(path, string, ".zip"));
            try (final ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path2, new OpenOption[0])));){
                final Path path3 = Paths.get(this.levelId, new String[0]);
                Files.walkFileTree(this.levelPath, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                        if (path.endsWith("session.lock")) {
                            return FileVisitResult.CONTINUE;
                        }
                        String string = path3.resolve(LevelStorageAccess.this.levelPath.relativize(path)).toString().replace('\\', '/');
                        ZipEntry zipEntry = new ZipEntry(string);
                        zipOutputStream.putNextEntry(zipEntry);
                        com.google.common.io.Files.asByteSource(path.toFile()).copyTo(zipOutputStream);
                        zipOutputStream.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public /* synthetic */ FileVisitResult visitFile(Object object, BasicFileAttributes basicFileAttributes) throws IOException {
                        return this.visitFile((Path)object, basicFileAttributes);
                    }
                });
            }
            return Files.size(path2);
        }

        @Environment(value=EnvType.CLIENT)
        public DataResult<String> exportWorldGenSettings() {
            this.checkLock();
            File file = this.levelPath.toFile();
            LevelSummary levelSummary = (LevelSummary)LevelStorageSource.this.readLevelData(file, LevelStorageSource.this.levelSummaryReader(file, false));
            if (levelSummary == null) {
                return DataResult.error("Could not parse level data!");
            }
            DataResult<JsonElement> dataResult = WorldGenSettings.CODEC.encodeStart(JsonOps.INSTANCE, levelSummary.worldGenSettings());
            return dataResult.flatMap(jsonElement -> {
                Path path = this.levelPath.resolve("worldgen_settings_export.json");
                try (JsonWriter jsonWriter = WORLD_GEN_SETTINGS_GSON.newJsonWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8, new OpenOption[0]));){
                    WORLD_GEN_SETTINGS_GSON.toJson((JsonElement)jsonElement, jsonWriter);
                } catch (JsonIOException | IOException exception) {
                    return DataResult.error("Error writing file: " + exception.getMessage());
                }
                return DataResult.success(path.toString());
            });
        }

        @Override
        public void close() throws IOException {
            this.lock.close();
        }
    }
}

