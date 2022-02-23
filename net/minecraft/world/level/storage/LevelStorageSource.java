/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.File;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.SkipFields;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.LevelVersion;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LevelStorageSource {
    static final Logger LOGGER = LogUtils.getLogger();
    static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('_').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral('-').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral('-').appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();
    private static final String ICON_FILENAME = "icon.png";
    private static final ImmutableList<String> OLD_SETTINGS_KEYS = ImmutableList.of("RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest");
    private static final String TAG_DATA = "Data";
    final Path baseDir;
    private final Path backupDir;
    final DataFixer fixerUpper;

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

    private static <T> Pair<WorldGenSettings, Lifecycle> readWorldGenSettings(Dynamic<T> dynamic, DataFixer dataFixer, int i) {
        Dynamic<T> dynamic2 = dynamic.get("WorldGenSettings").orElseEmptyMap();
        for (String string : OLD_SETTINGS_KEYS) {
            Optional<Dynamic<T>> optional = dynamic.get(string).result();
            if (!optional.isPresent()) continue;
            dynamic2 = dynamic2.set(string, optional.get());
        }
        Dynamic dynamic3 = dataFixer.update(References.WORLD_GEN_SETTINGS, dynamic2, i, SharedConstants.getCurrentVersion().getWorldVersion());
        DataResult dataResult = WorldGenSettings.CODEC.parse(dynamic3);
        return Pair.of(dataResult.resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error)).orElseGet(() -> {
            RegistryAccess registryAccess = RegistryAccess.readFromDisk(dynamic3);
            return WorldGenSettings.makeDefault(registryAccess);
        }), dataResult.lifecycle());
    }

    private static DataPackConfig readDataPackConfig(Dynamic<?> dynamic) {
        return DataPackConfig.CODEC.parse(dynamic).resultOrPartial(LOGGER::error).orElse(DataPackConfig.DEFAULT);
    }

    public String getName() {
        return "Anvil";
    }

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
            try {
                LevelSummary levelSummary = this.readLevelData(file, this.levelSummaryReader(file, bl));
                if (levelSummary == null) continue;
                list.add(levelSummary);
            } catch (OutOfMemoryError outOfMemoryError) {
                MemoryReserve.release();
                System.gc();
                LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of memory trying to read summary of {}", (Object)file);
                throw outOfMemoryError;
            } catch (StackOverflowError stackOverflowError) {
                LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of stack trying to read summary of {}. Assuming corruption; attempting to restore from from level.dat_old.", (Object)file);
                File file2 = new File(file, "level.dat");
                File file3 = new File(file, "level.dat_old");
                File file4 = new File(file, "level.dat_corrupted_" + LocalDateTime.now().format(FORMATTER));
                Util.safeReplaceOrMoveFile(file2, file3, file4, true);
                throw stackOverflowError;
            }
        }
        return list;
    }

    private int getStorageVersion() {
        return 19133;
    }

    @Nullable
    <T> T readLevelData(File file, BiFunction<File, DataFixer, T> biFunction) {
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
    private static DataPackConfig getDataPacks(File file, DataFixer dataFixer) {
        try {
            Tag tag = LevelStorageSource.readLightweightData(file);
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag = (CompoundTag)tag;
                CompoundTag compoundTag2 = compoundTag.getCompound(TAG_DATA);
                int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
                Dynamic<CompoundTag> dynamic = dataFixer.update(DataFixTypes.LEVEL.getType(), new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag2), i, SharedConstants.getCurrentVersion().getWorldVersion());
                return dynamic.get("DataPacks").result().map(LevelStorageSource::readDataPackConfig).orElse(DataPackConfig.DEFAULT);
            }
        } catch (Exception exception) {
            LOGGER.error("Exception reading {}", (Object)file, (Object)exception);
        }
        return null;
    }

    static BiFunction<File, DataFixer, PrimaryLevelData> getLevelData(DynamicOps<Tag> dynamicOps, DataPackConfig dataPackConfig, Lifecycle lifecycle) {
        return (file, dataFixer) -> {
            try {
                CompoundTag compoundTag = NbtIo.readCompressed(file);
                CompoundTag compoundTag2 = compoundTag.getCompound(TAG_DATA);
                CompoundTag compoundTag3 = compoundTag2.contains("Player", 10) ? compoundTag2.getCompound("Player") : null;
                compoundTag2.remove("Player");
                int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
                Dynamic<Tag> dynamic = dataFixer.update(DataFixTypes.LEVEL.getType(), new Dynamic<CompoundTag>(dynamicOps, compoundTag2), i, SharedConstants.getCurrentVersion().getWorldVersion());
                Pair<WorldGenSettings, Lifecycle> pair = LevelStorageSource.readWorldGenSettings(dynamic, dataFixer, i);
                LevelVersion levelVersion = LevelVersion.parse(dynamic);
                LevelSettings levelSettings = LevelSettings.parse(dynamic, dataPackConfig);
                Lifecycle lifecycle2 = pair.getSecond().add(lifecycle);
                return PrimaryLevelData.parse(dynamic, dataFixer, i, compoundTag3, levelSettings, levelVersion, pair.getFirst(), lifecycle2);
            } catch (Exception exception) {
                LOGGER.error("Exception reading {}", file, (Object)exception);
                return null;
            }
        };
    }

    BiFunction<File, DataFixer, LevelSummary> levelSummaryReader(File file, boolean bl) {
        return (file2, dataFixer) -> {
            try {
                Tag tag = LevelStorageSource.readLightweightData(file2);
                if (tag instanceof CompoundTag) {
                    CompoundTag compoundTag = (CompoundTag)tag;
                    CompoundTag compoundTag2 = compoundTag.getCompound(TAG_DATA);
                    int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
                    Dynamic<CompoundTag> dynamic = dataFixer.update(DataFixTypes.LEVEL.getType(), new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag2), i, SharedConstants.getCurrentVersion().getWorldVersion());
                    LevelVersion levelVersion = LevelVersion.parse(dynamic);
                    int j = levelVersion.levelDataVersion();
                    if (j == 19132 || j == 19133) {
                        boolean bl2 = j != this.getStorageVersion();
                        File file3 = new File(file, ICON_FILENAME);
                        DataPackConfig dataPackConfig = dynamic.get("DataPacks").result().map(LevelStorageSource::readDataPackConfig).orElse(DataPackConfig.DEFAULT);
                        LevelSettings levelSettings = LevelSettings.parse(dynamic, dataPackConfig);
                        return new LevelSummary(levelSettings, levelVersion, file.getName(), bl2, bl, file3);
                    }
                } else {
                    LOGGER.warn("Invalid root tag in {}", file2);
                }
                return null;
            } catch (Exception exception) {
                LOGGER.error("Exception reading {}", file2, (Object)exception);
                return null;
            }
        };
    }

    @Nullable
    private static Tag readLightweightData(File file) throws IOException {
        SkipFields skipFields = new SkipFields(new FieldSelector(TAG_DATA, CompoundTag.TYPE, "Player"), new FieldSelector(TAG_DATA, CompoundTag.TYPE, "WorldGenSettings"));
        NbtIo.parseCompressed(file, (StreamTagVisitor)skipFields);
        return skipFields.getResult();
    }

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

    public boolean levelExists(String string) {
        return Files.isDirectory(this.baseDir.resolve(string), new LinkOption[0]);
    }

    public Path getBaseDir() {
        return this.baseDir;
    }

    public Path getBackupPath() {
        return this.backupDir;
    }

    public LevelStorageAccess createAccess(String string) throws IOException {
        return new LevelStorageAccess(string);
    }

    public class LevelStorageAccess
    implements AutoCloseable {
        final DirectoryLock lock;
        final Path levelPath;
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

        public Path getDimensionPath(ResourceKey<Level> resourceKey) {
            return DimensionType.getStorageFolder(resourceKey, this.levelPath);
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

        @Nullable
        public LevelSummary getSummary() {
            this.checkLock();
            return LevelStorageSource.this.readLevelData(this.levelPath.toFile(), LevelStorageSource.this.levelSummaryReader(this.levelPath.toFile(), false));
        }

        @Nullable
        public WorldData getDataTag(DynamicOps<Tag> dynamicOps, DataPackConfig dataPackConfig, Lifecycle lifecycle) {
            this.checkLock();
            return LevelStorageSource.this.readLevelData(this.levelPath.toFile(), LevelStorageSource.getLevelData(dynamicOps, dataPackConfig, lifecycle));
        }

        @Nullable
        public DataPackConfig getDataPacks() {
            this.checkLock();
            return LevelStorageSource.this.readLevelData(this.levelPath.toFile(), LevelStorageSource::getDataPacks);
        }

        public void saveDataTag(RegistryAccess registryAccess, WorldData worldData) {
            this.saveDataTag(registryAccess, worldData, null);
        }

        public void saveDataTag(RegistryAccess registryAccess, WorldData worldData, @Nullable CompoundTag compoundTag) {
            File file = this.levelPath.toFile();
            CompoundTag compoundTag2 = worldData.createTag(registryAccess, compoundTag);
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.put(LevelStorageSource.TAG_DATA, compoundTag2);
            try {
                File file2 = File.createTempFile("level", ".dat", file);
                NbtIo.writeCompressed(compoundTag3, file2);
                File file3 = new File(file, "level.dat_old");
                File file4 = new File(file, "level.dat");
                Util.safeReplaceFile(file4, file2, file3);
            } catch (Exception exception) {
                LOGGER.error("Failed to save level {}", (Object)file, (Object)exception);
            }
        }

        public Optional<Path> getIconFile() {
            if (!this.lock.isValid()) {
                return Optional.empty();
            }
            return Optional.of(this.levelPath.resolve(LevelStorageSource.ICON_FILENAME));
        }

        public void deleteLevel() throws IOException {
            this.checkLock();
            final Path path = this.levelPath.resolve("session.lock");
            LOGGER.info("Deleting level {}", (Object)this.levelId);
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

        public void renameLevel(String string) throws IOException {
            this.checkLock();
            File file = new File(LevelStorageSource.this.baseDir.toFile(), this.levelId);
            if (!file.exists()) {
                return;
            }
            File file2 = new File(file, "level.dat");
            if (file2.exists()) {
                CompoundTag compoundTag = NbtIo.readCompressed(file2);
                CompoundTag compoundTag2 = compoundTag.getCompound(LevelStorageSource.TAG_DATA);
                compoundTag2.putString("LevelName", string);
                NbtIo.writeCompressed(compoundTag, file2);
            }
        }

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

        @Override
        public void close() throws IOException {
            this.lock.close();
        }
    }
}

