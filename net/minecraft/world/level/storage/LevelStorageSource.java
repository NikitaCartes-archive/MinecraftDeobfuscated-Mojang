/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableList;
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
import java.io.UncheckedIOException;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.SkipFields;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
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

    private static <T> DataResult<WorldGenSettings> readWorldGenSettings(Dynamic<T> dynamic, DataFixer dataFixer, int i) {
        Dynamic<T> dynamic2 = dynamic.get("WorldGenSettings").orElseEmptyMap();
        for (String string : OLD_SETTINGS_KEYS) {
            Optional<Dynamic<T>> optional = dynamic.get(string).result();
            if (!optional.isPresent()) continue;
            dynamic2 = dynamic2.set(string, optional.get());
        }
        Dynamic<T> dynamic3 = dataFixer.update(References.WORLD_GEN_SETTINGS, dynamic2, i, SharedConstants.getCurrentVersion().getWorldVersion());
        return WorldGenSettings.CODEC.parse(dynamic3);
    }

    private static WorldDataConfiguration readDataConfig(Dynamic<?> dynamic) {
        return WorldDataConfiguration.CODEC.parse(dynamic).resultOrPartial(LOGGER::error).orElse(WorldDataConfiguration.DEFAULT);
    }

    public String getName() {
        return "Anvil";
    }

    public LevelCandidates findLevelCandidates() throws LevelStorageException {
        if (!Files.isDirectory(this.baseDir, new LinkOption[0])) {
            throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
        }
        try {
            List<LevelDirectory> list = Files.list(this.baseDir).filter(path -> Files.isDirectory(path, new LinkOption[0])).map(LevelDirectory::new).filter(levelDirectory -> Files.isRegularFile(levelDirectory.dataFile(), new LinkOption[0]) || Files.isRegularFile(levelDirectory.oldDataFile(), new LinkOption[0])).toList();
            return new LevelCandidates(list);
        } catch (IOException iOException) {
            throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
        }
    }

    public CompletableFuture<List<LevelSummary>> loadLevelSummaries(LevelCandidates levelCandidates) {
        ArrayList<CompletableFuture<LevelSummary>> list2 = new ArrayList<CompletableFuture<LevelSummary>>(levelCandidates.levels.size());
        for (LevelDirectory levelDirectory : levelCandidates.levels) {
            list2.add(CompletableFuture.supplyAsync(() -> {
                boolean bl;
                try {
                    bl = DirectoryLock.isLocked(levelDirectory.path());
                } catch (Exception exception) {
                    LOGGER.warn("Failed to read {} lock", (Object)levelDirectory.path(), (Object)exception);
                    return null;
                }
                try {
                    LevelSummary levelSummary = this.readLevelData(levelDirectory, this.levelSummaryReader(levelDirectory, bl));
                    if (levelSummary != null) {
                        return levelSummary;
                    }
                } catch (OutOfMemoryError outOfMemoryError) {
                    MemoryReserve.release();
                    System.gc();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of memory trying to read summary of {}", (Object)levelDirectory.directoryName());
                    throw outOfMemoryError;
                } catch (StackOverflowError stackOverflowError) {
                    LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of stack trying to read summary of {}. Assuming corruption; attempting to restore from from level.dat_old.", (Object)levelDirectory.directoryName());
                    Util.safeReplaceOrMoveFile(levelDirectory.dataFile(), levelDirectory.oldDataFile(), levelDirectory.corruptedDataFile(LocalDateTime.now()), true);
                    throw stackOverflowError;
                }
                return null;
            }, Util.backgroundExecutor()));
        }
        return Util.sequenceFailFastAndCancel(list2).thenApply(list -> list.stream().filter(Objects::nonNull).sorted().toList());
    }

    private int getStorageVersion() {
        return 19133;
    }

    @Nullable
    <T> T readLevelData(LevelDirectory levelDirectory, BiFunction<Path, DataFixer, T> biFunction) {
        T object;
        if (!Files.exists(levelDirectory.path(), new LinkOption[0])) {
            return null;
        }
        Path path = levelDirectory.dataFile();
        if (Files.exists(path, new LinkOption[0]) && (object = biFunction.apply(path, this.fixerUpper)) != null) {
            return object;
        }
        path = levelDirectory.oldDataFile();
        if (Files.exists(path, new LinkOption[0])) {
            return biFunction.apply(path, this.fixerUpper);
        }
        return null;
    }

    @Nullable
    private static WorldDataConfiguration getDataConfiguration(Path path, DataFixer dataFixer) {
        try {
            Tag tag = LevelStorageSource.readLightweightData(path);
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag = (CompoundTag)tag;
                CompoundTag compoundTag2 = compoundTag.getCompound(TAG_DATA);
                int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
                Dynamic<CompoundTag> dynamic = dataFixer.update(DataFixTypes.LEVEL.getType(), new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag2), i, SharedConstants.getCurrentVersion().getWorldVersion());
                return LevelStorageSource.readDataConfig(dynamic);
            }
        } catch (Exception exception) {
            LOGGER.error("Exception reading {}", (Object)path, (Object)exception);
        }
        return null;
    }

    static BiFunction<Path, DataFixer, Pair<WorldData, WorldDimensions.Complete>> getLevelData(DynamicOps<Tag> dynamicOps, WorldDataConfiguration worldDataConfiguration, Registry<LevelStem> registry, Lifecycle lifecycle) {
        return (path, dataFixer) -> {
            CompoundTag compoundTag;
            try {
                compoundTag = NbtIo.readCompressed(path.toFile());
            } catch (IOException iOException) {
                throw new UncheckedIOException(iOException);
            }
            CompoundTag compoundTag2 = compoundTag.getCompound(TAG_DATA);
            CompoundTag compoundTag3 = compoundTag2.contains("Player", 10) ? compoundTag2.getCompound("Player") : null;
            compoundTag2.remove("Player");
            int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
            Dynamic<Tag> dynamic = dataFixer.update(DataFixTypes.LEVEL.getType(), new Dynamic<CompoundTag>(dynamicOps, compoundTag2), i, SharedConstants.getCurrentVersion().getWorldVersion());
            WorldGenSettings worldGenSettings = LevelStorageSource.readWorldGenSettings(dynamic, dataFixer, i).getOrThrow(false, Util.prefix("WorldGenSettings: ", LOGGER::error));
            LevelVersion levelVersion = LevelVersion.parse(dynamic);
            LevelSettings levelSettings = LevelSettings.parse(dynamic, worldDataConfiguration);
            WorldDimensions.Complete complete = worldGenSettings.dimensions().bake(registry);
            Lifecycle lifecycle2 = complete.lifecycle().add(lifecycle);
            PrimaryLevelData primaryLevelData = PrimaryLevelData.parse(dynamic, dataFixer, i, compoundTag3, levelSettings, levelVersion, complete.specialWorldProperty(), worldGenSettings.options(), lifecycle2);
            return Pair.of(primaryLevelData, complete);
        };
    }

    BiFunction<Path, DataFixer, LevelSummary> levelSummaryReader(LevelDirectory levelDirectory, boolean bl) {
        return (path, dataFixer) -> {
            try {
                Tag tag = LevelStorageSource.readLightweightData(path);
                if (tag instanceof CompoundTag) {
                    CompoundTag compoundTag = (CompoundTag)tag;
                    CompoundTag compoundTag2 = compoundTag.getCompound(TAG_DATA);
                    int i = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : -1;
                    Dynamic<Tag> dynamic = dataFixer.update(DataFixTypes.LEVEL.getType(), new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag2), i, SharedConstants.getCurrentVersion().getWorldVersion());
                    LevelVersion levelVersion = LevelVersion.parse(dynamic);
                    int j = levelVersion.levelDataVersion();
                    if (j == 19132 || j == 19133) {
                        boolean bl2 = j != this.getStorageVersion();
                        Path path2 = levelDirectory.iconFile();
                        WorldDataConfiguration worldDataConfiguration = LevelStorageSource.readDataConfig(dynamic);
                        LevelSettings levelSettings = LevelSettings.parse(dynamic, worldDataConfiguration);
                        FeatureFlagSet featureFlagSet = LevelStorageSource.parseFeatureFlagsFromSummary(dynamic);
                        boolean bl3 = FeatureFlags.isExperimental(featureFlagSet);
                        return new LevelSummary(levelSettings, levelVersion, levelDirectory.directoryName(), bl2, bl, bl3, path2);
                    }
                } else {
                    LOGGER.warn("Invalid root tag in {}", path);
                }
                return null;
            } catch (Exception exception) {
                LOGGER.error("Exception reading {}", path, (Object)exception);
                return null;
            }
        };
    }

    private static FeatureFlagSet parseFeatureFlagsFromSummary(Dynamic<Tag> dynamic2) {
        Set<ResourceLocation> set = dynamic2.get("enabled_features").asStream().flatMap(dynamic -> dynamic.asString().result().map(ResourceLocation::tryParse).stream()).collect(Collectors.toSet());
        return FeatureFlags.REGISTRY.fromNames(set, resourceLocation -> {});
    }

    @Nullable
    private static Tag readLightweightData(Path path) throws IOException {
        SkipFields skipFields = new SkipFields(new FieldSelector(TAG_DATA, CompoundTag.TYPE, "Player"), new FieldSelector(TAG_DATA, CompoundTag.TYPE, "WorldGenSettings"));
        NbtIo.parseCompressed(path.toFile(), (StreamTagVisitor)skipFields);
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

    public record LevelCandidates(List<LevelDirectory> levels) implements Iterable<LevelDirectory>
    {
        public boolean isEmpty() {
            return this.levels.isEmpty();
        }

        @Override
        public Iterator<LevelDirectory> iterator() {
            return this.levels.iterator();
        }
    }

    public record LevelDirectory(Path path) {
        public String directoryName() {
            return this.path.getFileName().toString();
        }

        public Path dataFile() {
            return this.resourcePath(LevelResource.LEVEL_DATA_FILE);
        }

        public Path oldDataFile() {
            return this.resourcePath(LevelResource.OLD_LEVEL_DATA_FILE);
        }

        public Path corruptedDataFile(LocalDateTime localDateTime) {
            return this.path.resolve(LevelResource.LEVEL_DATA_FILE.getId() + "_corrupted_" + localDateTime.format(FORMATTER));
        }

        public Path iconFile() {
            return this.resourcePath(LevelResource.ICON_FILE);
        }

        public Path lockFile() {
            return this.resourcePath(LevelResource.LOCK_FILE);
        }

        public Path resourcePath(LevelResource levelResource) {
            return this.path.resolve(levelResource.getId());
        }
    }

    public class LevelStorageAccess
    implements AutoCloseable {
        final DirectoryLock lock;
        final LevelDirectory levelDirectory;
        private final String levelId;
        private final Map<LevelResource, Path> resources = Maps.newHashMap();

        public LevelStorageAccess(String string) throws IOException {
            this.levelId = string;
            this.levelDirectory = new LevelDirectory(LevelStorageSource.this.baseDir.resolve(string));
            this.lock = DirectoryLock.create(this.levelDirectory.path());
        }

        public String getLevelId() {
            return this.levelId;
        }

        public Path getLevelPath(LevelResource levelResource) {
            return this.resources.computeIfAbsent(levelResource, this.levelDirectory::resourcePath);
        }

        public Path getDimensionPath(ResourceKey<Level> resourceKey) {
            return DimensionType.getStorageFolder(resourceKey, this.levelDirectory.path());
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
            return LevelStorageSource.this.readLevelData(this.levelDirectory, LevelStorageSource.this.levelSummaryReader(this.levelDirectory, false));
        }

        @Nullable
        public Pair<WorldData, WorldDimensions.Complete> getDataTag(DynamicOps<Tag> dynamicOps, WorldDataConfiguration worldDataConfiguration, Registry<LevelStem> registry, Lifecycle lifecycle) {
            this.checkLock();
            return LevelStorageSource.this.readLevelData(this.levelDirectory, LevelStorageSource.getLevelData(dynamicOps, worldDataConfiguration, registry, lifecycle));
        }

        @Nullable
        public WorldDataConfiguration getDataConfiguration() {
            this.checkLock();
            return LevelStorageSource.this.readLevelData(this.levelDirectory, LevelStorageSource::getDataConfiguration);
        }

        public void saveDataTag(RegistryAccess registryAccess, WorldData worldData) {
            this.saveDataTag(registryAccess, worldData, null);
        }

        public void saveDataTag(RegistryAccess registryAccess, WorldData worldData, @Nullable CompoundTag compoundTag) {
            File file = this.levelDirectory.path().toFile();
            CompoundTag compoundTag2 = worldData.createTag(registryAccess, compoundTag);
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.put(LevelStorageSource.TAG_DATA, compoundTag2);
            try {
                File file2 = File.createTempFile("level", ".dat", file);
                NbtIo.writeCompressed(compoundTag3, file2);
                File file3 = this.levelDirectory.oldDataFile().toFile();
                File file4 = this.levelDirectory.dataFile().toFile();
                Util.safeReplaceFile(file4, file2, file3);
            } catch (Exception exception) {
                LOGGER.error("Failed to save level {}", (Object)file, (Object)exception);
            }
        }

        public Optional<Path> getIconFile() {
            if (!this.lock.isValid()) {
                return Optional.empty();
            }
            return Optional.of(this.levelDirectory.iconFile());
        }

        public void deleteLevel() throws IOException {
            this.checkLock();
            final Path path = this.levelDirectory.lockFile();
            LOGGER.info("Deleting level {}", (Object)this.levelId);
            for (int i = 1; i <= 5; ++i) {
                LOGGER.info("Attempt {}...", (Object)i);
                try {
                    Files.walkFileTree(this.levelDirectory.path(), (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

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
                            if (path2.equals(LevelStorageAccess.this.levelDirectory.path())) {
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
                        LOGGER.warn("Failed to delete {}", (Object)this.levelDirectory.path(), (Object)iOException);
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
            Path path = this.levelDirectory.dataFile();
            if (Files.exists(path, new LinkOption[0])) {
                CompoundTag compoundTag = NbtIo.readCompressed(path.toFile());
                CompoundTag compoundTag2 = compoundTag.getCompound(LevelStorageSource.TAG_DATA);
                compoundTag2.putString("LevelName", string);
                NbtIo.writeCompressed(compoundTag, path.toFile());
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
                Files.walkFileTree(this.levelDirectory.path(), (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                        if (path.endsWith("session.lock")) {
                            return FileVisitResult.CONTINUE;
                        }
                        String string = path3.resolve(LevelStorageAccess.this.levelDirectory.path().relativize(path)).toString().replace('\\', '/');
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

