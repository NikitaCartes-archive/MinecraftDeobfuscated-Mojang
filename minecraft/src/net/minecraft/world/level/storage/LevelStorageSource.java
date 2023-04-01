package net.minecraft.world.level.storage;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
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
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.voting.votes.VoteStorage;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.slf4j.Logger;

public class LevelStorageSource {
	static final Logger LOGGER = LogUtils.getLogger();
	static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
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
	private static final String TAG_DATA = "Data";
	final Path baseDir;
	private final Path backupDir;
	final DataFixer fixerUpper;

	public LevelStorageSource(Path path, Path path2, DataFixer dataFixer) {
		this.fixerUpper = dataFixer;

		try {
			FileUtil.createDirectoriesSafe(path);
		} catch (IOException var5) {
			throw new RuntimeException(var5);
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
			if (optional.isPresent()) {
				dynamic2 = dynamic2.set(string, (Dynamic<?>)optional.get());
			}
		}

		Dynamic<T> dynamic3 = DataFixTypes.WORLD_GEN_SETTINGS.updateToCurrentVersion(dataFixer, dynamic2, i);
		return WorldGenSettings.CODEC.parse(dynamic3);
	}

	private static WorldDataConfiguration readDataConfig(Dynamic<?> dynamic) {
		return (WorldDataConfiguration)WorldDataConfiguration.CODEC.parse(dynamic).resultOrPartial(LOGGER::error).orElse(WorldDataConfiguration.DEFAULT);
	}

	public String getName() {
		return "Anvil";
	}

	public LevelStorageSource.LevelCandidates findLevelCandidates() throws LevelStorageException {
		if (!Files.isDirectory(this.baseDir, new LinkOption[0])) {
			throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
		} else {
			try {
				List<LevelStorageSource.LevelDirectory> list = Files.list(this.baseDir)
					.filter(path -> Files.isDirectory(path, new LinkOption[0]))
					.map(LevelStorageSource.LevelDirectory::new)
					.filter(
						levelDirectory -> Files.isRegularFile(levelDirectory.dataFile(), new LinkOption[0])
								|| Files.isRegularFile(levelDirectory.oldDataFile(), new LinkOption[0])
					)
					.toList();
				return new LevelStorageSource.LevelCandidates(list);
			} catch (IOException var2) {
				throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
			}
		}
	}

	public CompletableFuture<List<LevelSummary>> loadLevelSummaries(LevelStorageSource.LevelCandidates levelCandidates) {
		List<CompletableFuture<LevelSummary>> list = new ArrayList(levelCandidates.levels.size());

		for (LevelStorageSource.LevelDirectory levelDirectory : levelCandidates.levels) {
			list.add(
				CompletableFuture.supplyAsync(
					() -> {
						boolean bl;
						try {
							bl = DirectoryLock.isLocked(levelDirectory.path());
						} catch (Exception var6) {
							LOGGER.warn("Failed to read {} lock", levelDirectory.path(), var6);
							return null;
						}

						try {
							LevelSummary levelSummary = this.readLevelData(levelDirectory, this.levelSummaryReader(levelDirectory, bl));
							return levelSummary != null ? levelSummary : null;
						} catch (OutOfMemoryError var4x) {
							MemoryReserve.release();
							System.gc();
							LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of memory trying to read summary of {}", levelDirectory.directoryName());
							throw var4x;
						} catch (StackOverflowError var5) {
							LOGGER.error(
								LogUtils.FATAL_MARKER,
								"Ran out of stack trying to read summary of {}. Assuming corruption; attempting to restore from from level.dat_old.",
								levelDirectory.directoryName()
							);
							Util.safeReplaceOrMoveFile(levelDirectory.dataFile(), levelDirectory.oldDataFile(), levelDirectory.corruptedDataFile(LocalDateTime.now()), true);
							throw var5;
						}
					},
					Util.backgroundExecutor()
				)
			);
		}

		return Util.sequenceFailFastAndCancel(list).thenApply(listx -> listx.stream().filter(Objects::nonNull).sorted().toList());
	}

	private int getStorageVersion() {
		return 19133;
	}

	@Nullable
	<T> T readLevelData(LevelStorageSource.LevelDirectory levelDirectory, BiFunction<Path, DataFixer, T> biFunction) {
		if (!Files.exists(levelDirectory.path(), new LinkOption[0])) {
			return null;
		} else {
			Path path = levelDirectory.dataFile();
			if (Files.exists(path, new LinkOption[0])) {
				T object = (T)biFunction.apply(path, this.fixerUpper);
				if (object != null) {
					return object;
				}
			}

			path = levelDirectory.oldDataFile();
			return (T)(Files.exists(path, new LinkOption[0]) ? biFunction.apply(path, this.fixerUpper) : null);
		}
	}

	@Nullable
	private static WorldDataConfiguration getDataConfiguration(Path path, DataFixer dataFixer) {
		try {
			if (readLightweightData(path) instanceof CompoundTag compoundTag) {
				CompoundTag compoundTag2 = compoundTag.getCompound("Data");
				int i = NbtUtils.getDataVersion(compoundTag2, -1);
				Dynamic<?> dynamic = DataFixTypes.LEVEL.updateToCurrentVersion(dataFixer, new Dynamic<>(NbtOps.INSTANCE, compoundTag2), i);
				return readDataConfig(dynamic);
			}
		} catch (Exception var7) {
			LOGGER.error("Exception reading {}", path, var7);
		}

		return null;
	}

	static BiFunction<Path, DataFixer, Pair<WorldData, WorldDimensions.Complete>> getLevelData(
		DynamicOps<Tag> dynamicOps, WorldDataConfiguration worldDataConfiguration, Registry<LevelStem> registry, Lifecycle lifecycle
	) {
		return (path, dataFixer) -> {
			CompoundTag compoundTag;
			try {
				compoundTag = NbtIo.readCompressed(path.toFile());
			} catch (IOException var17) {
				throw new UncheckedIOException(var17);
			}

			CompoundTag compoundTag2 = compoundTag.getCompound("Data");
			CompoundTag compoundTag3 = compoundTag2.contains("Player", 10) ? compoundTag2.getCompound("Player") : null;
			compoundTag2.remove("Player");
			int i = NbtUtils.getDataVersion(compoundTag2, -1);
			Dynamic<?> dynamic = DataFixTypes.LEVEL.updateToCurrentVersion(dataFixer, new Dynamic<>(dynamicOps, compoundTag2), i);
			WorldGenSettings worldGenSettings = readWorldGenSettings(dynamic, dataFixer, i).getOrThrow(false, Util.prefix("WorldGenSettings: ", LOGGER::error));
			LevelVersion levelVersion = LevelVersion.parse(dynamic);
			LevelSettings levelSettings = LevelSettings.parse(dynamic, worldDataConfiguration);
			WorldDimensions.Complete complete = worldGenSettings.dimensions().bake(registry);
			Lifecycle lifecycle2 = complete.lifecycle().add(lifecycle);
			PrimaryLevelData primaryLevelData = PrimaryLevelData.parse(
				dynamic, dataFixer, i, compoundTag3, levelSettings, levelVersion, complete.specialWorldProperty(), worldGenSettings.options(), lifecycle2
			);
			return Pair.of(primaryLevelData, complete);
		};
	}

	BiFunction<Path, DataFixer, LevelSummary> levelSummaryReader(LevelStorageSource.LevelDirectory levelDirectory, boolean bl) {
		return (path, dataFixer) -> {
			try {
				if (readLightweightData(path) instanceof CompoundTag compoundTag) {
					CompoundTag compoundTag2 = compoundTag.getCompound("Data");
					int i = NbtUtils.getDataVersion(compoundTag2, -1);
					Dynamic<?> dynamic = DataFixTypes.LEVEL.updateToCurrentVersion(dataFixer, new Dynamic<>(NbtOps.INSTANCE, compoundTag2), i);
					LevelVersion levelVersion = LevelVersion.parse(dynamic);
					int j = levelVersion.levelDataVersion();
					if (j == 19132 || j == 19133) {
						boolean bl2 = j != this.getStorageVersion();
						Path path2 = levelDirectory.iconFile();
						WorldDataConfiguration worldDataConfiguration = readDataConfig(dynamic);
						LevelSettings levelSettings = LevelSettings.parse(dynamic, worldDataConfiguration);
						FeatureFlagSet featureFlagSet = parseFeatureFlagsFromSummary(dynamic);
						boolean bl3 = FeatureFlags.isExperimental(featureFlagSet);
						return new LevelSummary(levelSettings, levelVersion, levelDirectory.directoryName(), bl2, bl, bl3, path2);
					}
				} else {
					LOGGER.warn("Invalid root tag in {}", path);
				}

				return null;
			} catch (Exception var18) {
				LOGGER.error("Exception reading {}", path, var18);
				return null;
			}
		};
	}

	private static FeatureFlagSet parseFeatureFlagsFromSummary(Dynamic<?> dynamic) {
		Set<ResourceLocation> set = (Set<ResourceLocation>)dynamic.get("enabled_features")
			.asStream()
			.flatMap(dynamicx -> dynamicx.asString().result().map(ResourceLocation::tryParse).stream())
			.collect(Collectors.toSet());
		return FeatureFlags.REGISTRY.fromNames(set, resourceLocation -> {
		});
	}

	@Nullable
	private static Tag readLightweightData(Path path) throws IOException {
		SkipFields skipFields = new SkipFields(new FieldSelector("Data", CompoundTag.TYPE, "Player"), new FieldSelector("Data", CompoundTag.TYPE, "WorldGenSettings"));
		NbtIo.parseCompressed(path.toFile(), skipFields);
		return skipFields.getResult();
	}

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

	public boolean levelExists(String string) {
		return Files.isDirectory(this.baseDir.resolve(string), new LinkOption[0]);
	}

	public Path getBaseDir() {
		return this.baseDir;
	}

	public Path getBackupPath() {
		return this.backupDir;
	}

	public LevelStorageSource.LevelStorageAccess createAccess(String string) throws IOException {
		return new LevelStorageSource.LevelStorageAccess(string);
	}

	public static record LevelCandidates(List<LevelStorageSource.LevelDirectory> levels) implements Iterable<LevelStorageSource.LevelDirectory> {

		public boolean isEmpty() {
			return this.levels.isEmpty();
		}

		public Iterator<LevelStorageSource.LevelDirectory> iterator() {
			return this.levels.iterator();
		}
	}

	public static record LevelDirectory(Path path) {

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
			return this.path.resolve(LevelResource.LEVEL_DATA_FILE.getId() + "_corrupted_" + localDateTime.format(LevelStorageSource.FORMATTER));
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

	public class LevelStorageAccess implements AutoCloseable {
		final DirectoryLock lock;
		final LevelStorageSource.LevelDirectory levelDirectory;
		private final String levelId;
		private final Map<LevelResource, Path> resources = Maps.<LevelResource, Path>newHashMap();

		public LevelStorageAccess(String string) throws IOException {
			this.levelId = string;
			this.levelDirectory = new LevelStorageSource.LevelDirectory(LevelStorageSource.this.baseDir.resolve(string));
			this.lock = DirectoryLock.create(this.levelDirectory.path());
		}

		public String getLevelId() {
			return this.levelId;
		}

		public Path getLevelPath(LevelResource levelResource) {
			return (Path)this.resources.computeIfAbsent(levelResource, this.levelDirectory::resourcePath);
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
		public Pair<WorldData, WorldDimensions.Complete> getDataTag(
			DynamicOps<Tag> dynamicOps, WorldDataConfiguration worldDataConfiguration, Registry<LevelStem> registry, Lifecycle lifecycle
		) {
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
			compoundTag3.put("Data", compoundTag2);

			try {
				File file2 = File.createTempFile("level", ".dat", file);
				NbtIo.writeCompressed(compoundTag3, file2);
				File file3 = this.levelDirectory.oldDataFile().toFile();
				File file4 = this.levelDirectory.dataFile().toFile();
				Util.safeReplaceFile(file4, file2, file3);
			} catch (Exception var10) {
				LevelStorageSource.LOGGER.error("Failed to save level {}", file, var10);
			}
		}

		public Optional<Path> getIconFile() {
			return !this.lock.isValid() ? Optional.empty() : Optional.of(this.levelDirectory.iconFile());
		}

		public void deleteLevel() throws IOException {
			this.checkLock();
			final Path path = this.levelDirectory.lockFile();
			LevelStorageSource.LOGGER.info("Deleting level {}", this.levelId);

			for (int i = 1; i <= 5; i++) {
				LevelStorageSource.LOGGER.info("Attempt {}...", i);

				try {
					Files.walkFileTree(this.levelDirectory.path(), new SimpleFileVisitor<Path>() {
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
								if (path.equals(LevelStorageAccess.this.levelDirectory.path())) {
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

					LevelStorageSource.LOGGER.warn("Failed to delete {}", this.levelDirectory.path(), var6);

					try {
						Thread.sleep(500L);
					} catch (InterruptedException var5) {
					}
				}
			}
		}

		public void renameLevel(String string) throws IOException {
			this.checkLock();
			Path path = this.levelDirectory.dataFile();
			if (Files.exists(path, new LinkOption[0])) {
				CompoundTag compoundTag = NbtIo.readCompressed(path.toFile());
				CompoundTag compoundTag2 = compoundTag.getCompound("Data");
				compoundTag2.putString("LevelName", string);
				NbtIo.writeCompressed(compoundTag, path.toFile());
			}
		}

		public long makeWorldBackup() throws IOException {
			this.checkLock();
			String string = LocalDateTime.now().format(LevelStorageSource.FORMATTER) + "_" + this.levelId;
			Path path = LevelStorageSource.this.getBackupPath();

			try {
				FileUtil.createDirectoriesSafe(path);
			} catch (IOException var9) {
				throw new RuntimeException(var9);
			}

			Path path2 = path.resolve(FileUtil.findAvailableName(path, string, ".zip"));
			final ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path2)));

			try {
				final Path path3 = Paths.get(this.levelId);
				Files.walkFileTree(this.levelDirectory.path(), new SimpleFileVisitor<Path>() {
					public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
						if (path.endsWith("session.lock")) {
							return FileVisitResult.CONTINUE;
						} else {
							String string = path3.resolve(LevelStorageAccess.this.levelDirectory.path().relativize(path)).toString().replace('\\', '/');
							ZipEntry zipEntry = new ZipEntry(string);
							zipOutputStream.putNextEntry(zipEntry);
							com.google.common.io.Files.asByteSource(path.toFile()).copyTo(zipOutputStream);
							zipOutputStream.closeEntry();
							return FileVisitResult.CONTINUE;
						}
					}
				});
			} catch (Throwable var8) {
				try {
					zipOutputStream.close();
				} catch (Throwable var7) {
					var8.addSuppressed(var7);
				}

				throw var8;
			}

			zipOutputStream.close();
			return Files.size(path2);
		}

		public void close() throws IOException {
			this.lock.close();
		}

		public VoteStorage loadVotes() {
			this.checkLock();
			Path path = this.getLevelPath(LevelResource.VOTES);
			if (Files.exists(path, new LinkOption[0])) {
				try {
					BufferedReader bufferedReader = Files.newBufferedReader(path, Charsets.UTF_8);

					VoteStorage var5;
					try {
						JsonReader jsonReader = new JsonReader(bufferedReader);

						try {
							JsonElement jsonElement = Streams.parse(jsonReader);
							if (!jsonElement.isJsonNull() && jsonReader.peek() != JsonToken.END_DOCUMENT) {
								throw new JsonSyntaxException("Did not consume the entire document.");
							}

							var5 = (VoteStorage)VoteStorage.CODEC
								.parse(new Dynamic<>(JsonOps.INSTANCE, jsonElement))
								.resultOrPartial(Util.prefix("Rule decoding: ", LevelStorageSource.LOGGER::error))
								.orElseGet(VoteStorage::new);
						} catch (Throwable var8) {
							try {
								jsonReader.close();
							} catch (Throwable var7) {
								var8.addSuppressed(var7);
							}

							throw var8;
						}

						jsonReader.close();
					} catch (Throwable var9) {
						if (bufferedReader != null) {
							try {
								bufferedReader.close();
							} catch (Throwable var6) {
								var9.addSuppressed(var6);
							}
						}

						throw var9;
					}

					if (bufferedReader != null) {
						bufferedReader.close();
					}

					return var5;
				} catch (Exception var10) {
					LevelStorageSource.LOGGER.warn("Failed to read votes from {}", path, var10);
				}
			}

			return new VoteStorage();
		}

		public void saveVotes(VoteStorage voteStorage) {
			this.checkLock();
			Path path = this.getLevelPath(LevelResource.VOTES);

			try {
				Path path2 = Files.createTempFile(this.levelDirectory.path, "votes", ".json");
				BufferedWriter bufferedWriter = Files.newBufferedWriter(path2, Charsets.UTF_8);

				try {
					DataResult<JsonElement> dataResult = VoteStorage.CODEC.encodeStart(JsonOps.INSTANCE, voteStorage);
					JsonWriter jsonWriter = new JsonWriter(bufferedWriter);

					try {
						jsonWriter.setIndent("  ");
						Streams.write(Util.getOrThrow(dataResult, IOException::new), jsonWriter);
					} catch (Throwable var11) {
						try {
							jsonWriter.close();
						} catch (Throwable var10) {
							var11.addSuppressed(var10);
						}

						throw var11;
					}

					jsonWriter.close();
				} catch (Throwable var12) {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (Throwable var9) {
							var12.addSuppressed(var9);
						}
					}

					throw var12;
				}

				if (bufferedWriter != null) {
					bufferedWriter.close();
				}

				Path path3 = this.getLevelPath(LevelResource.OLD_VOTES);
				Util.safeReplaceFile(path, path2, path3);
			} catch (Exception var13) {
				LevelStorageSource.LOGGER.warn("Failed to write votes to {}", path, var13);
			}
		}
	}
}
