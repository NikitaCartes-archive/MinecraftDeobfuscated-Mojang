package net.minecraft.util.worldupdate;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMaps;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RecreatingChunkStorage;
import net.minecraft.world.level.chunk.storage.RecreatingSimpleRegionStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

public class WorldUpgrader {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
	private static final String NEW_DIRECTORY_PREFIX = "new_";
	static final MutableComponent STATUS_UPGRADING_POI = Component.translatable("optimizeWorld.stage.upgrading.poi");
	static final MutableComponent STATUS_FINISHED_POI = Component.translatable("optimizeWorld.stage.finished.poi");
	static final MutableComponent STATUS_UPGRADING_ENTITIES = Component.translatable("optimizeWorld.stage.upgrading.entities");
	static final MutableComponent STATUS_FINISHED_ENTITIES = Component.translatable("optimizeWorld.stage.finished.entities");
	static final MutableComponent STATUS_UPGRADING_CHUNKS = Component.translatable("optimizeWorld.stage.upgrading.chunks");
	static final MutableComponent STATUS_FINISHED_CHUNKS = Component.translatable("optimizeWorld.stage.finished.chunks");
	final Registry<LevelStem> dimensions;
	final Set<ResourceKey<Level>> levels;
	final boolean eraseCache;
	final boolean recreateRegionFiles;
	final LevelStorageSource.LevelStorageAccess levelStorage;
	private final Thread thread;
	final DataFixer dataFixer;
	volatile boolean running = true;
	private volatile boolean finished;
	volatile float progress;
	volatile int totalChunks;
	volatile int totalFiles;
	volatile int converted;
	volatile int skipped;
	final Reference2FloatMap<ResourceKey<Level>> progressMap = Reference2FloatMaps.synchronize(new Reference2FloatOpenHashMap<>());
	volatile Component status = Component.translatable("optimizeWorld.stage.counting");
	static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
	final DimensionDataStorage overworldDataStorage;

	public WorldUpgrader(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, RegistryAccess registryAccess, boolean bl, boolean bl2) {
		this.dimensions = registryAccess.registryOrThrow(Registries.LEVEL_STEM);
		this.levels = (Set<ResourceKey<Level>>)this.dimensions.registryKeySet().stream().map(Registries::levelStemToLevel).collect(Collectors.toUnmodifiableSet());
		this.eraseCache = bl;
		this.dataFixer = dataFixer;
		this.levelStorage = levelStorageAccess;
		this.overworldDataStorage = new DimensionDataStorage(this.levelStorage.getDimensionPath(Level.OVERWORLD).resolve("data").toFile(), dataFixer, registryAccess);
		this.recreateRegionFiles = bl2;
		this.thread = THREAD_FACTORY.newThread(this::work);
		this.thread.setUncaughtExceptionHandler((thread, throwable) -> {
			LOGGER.error("Error upgrading world", throwable);
			this.status = Component.translatable("optimizeWorld.stage.failed");
			this.finished = true;
		});
		this.thread.start();
	}

	public void cancel() {
		this.running = false;

		try {
			this.thread.join();
		} catch (InterruptedException var2) {
		}
	}

	private void work() {
		long l = Util.getMillis();
		LOGGER.info("Upgrading entities");
		new WorldUpgrader.EntityUpgrader().upgrade();
		LOGGER.info("Upgrading POIs");
		new WorldUpgrader.PoiUpgrader().upgrade();
		LOGGER.info("Upgrading blocks");
		new WorldUpgrader.ChunkUpgrader().upgrade();
		this.overworldDataStorage.save();
		l = Util.getMillis() - l;
		LOGGER.info("World optimizaton finished after {} seconds", l / 1000L);
		this.finished = true;
	}

	public boolean isFinished() {
		return this.finished;
	}

	public Set<ResourceKey<Level>> levels() {
		return this.levels;
	}

	public float dimensionProgress(ResourceKey<Level> resourceKey) {
		return this.progressMap.getFloat(resourceKey);
	}

	public float getProgress() {
		return this.progress;
	}

	public int getTotalChunks() {
		return this.totalChunks;
	}

	public int getConverted() {
		return this.converted;
	}

	public int getSkipped() {
		return this.skipped;
	}

	public Component getStatus() {
		return this.status;
	}

	abstract class AbstractUpgrader<T extends AutoCloseable> {
		private final MutableComponent upgradingStatus;
		private final MutableComponent finishedStatus;
		private final String folderName;
		@Nullable
		protected CompletableFuture<Void> previousWriteFuture;
		protected final DataFixTypes dataFixType;

		AbstractUpgrader(DataFixTypes dataFixTypes, String string, MutableComponent mutableComponent, MutableComponent mutableComponent2) {
			this.dataFixType = dataFixTypes;
			this.folderName = string;
			this.upgradingStatus = mutableComponent;
			this.finishedStatus = mutableComponent2;
		}

		public void upgrade() {
			WorldUpgrader.this.totalFiles = 0;
			WorldUpgrader.this.totalChunks = 0;
			WorldUpgrader.this.converted = 0;
			WorldUpgrader.this.skipped = 0;
			List<WorldUpgrader.DimensionToUpgrade<T>> list = this.getDimensionsToUpgrade(this.folderName);
			if (WorldUpgrader.this.totalChunks != 0) {
				float f = (float)WorldUpgrader.this.totalFiles;
				WorldUpgrader.this.status = this.upgradingStatus;

				while (WorldUpgrader.this.running) {
					boolean bl = false;
					float g = 0.0F;

					for (WorldUpgrader.DimensionToUpgrade<T> dimensionToUpgrade : list) {
						ResourceKey<Level> resourceKey = dimensionToUpgrade.dimensionKey;
						ListIterator<WorldUpgrader.FileToUpgrade> listIterator = dimensionToUpgrade.files;
						T autoCloseable = dimensionToUpgrade.storage;
						if (listIterator.hasNext()) {
							WorldUpgrader.FileToUpgrade fileToUpgrade = (WorldUpgrader.FileToUpgrade)listIterator.next();
							boolean bl2 = true;

							for (ChunkPos chunkPos : fileToUpgrade.chunksToUpgrade) {
								bl2 = bl2 && this.processOnePosition(resourceKey, autoCloseable, chunkPos);
								bl = true;
							}

							if (WorldUpgrader.this.recreateRegionFiles) {
								if (bl2) {
									this.onFileFinished(fileToUpgrade.file);
								} else {
									WorldUpgrader.LOGGER.error("Failed to convert region file {}", fileToUpgrade.file.getPath());
								}
							}
						}

						float h = (float)listIterator.nextIndex() / f;
						WorldUpgrader.this.progressMap.put(resourceKey, h);
						g += h;
					}

					WorldUpgrader.this.progress = g;
					if (!bl) {
						break;
					}
				}

				WorldUpgrader.this.status = this.finishedStatus;

				for (WorldUpgrader.DimensionToUpgrade<T> dimensionToUpgrade2 : list) {
					try {
						dimensionToUpgrade2.storage.close();
					} catch (Exception var14) {
						WorldUpgrader.LOGGER.error("Error upgrading chunk", (Throwable)var14);
					}
				}
			}
		}

		private List<WorldUpgrader.DimensionToUpgrade<T>> getDimensionsToUpgrade(String string) {
			List<WorldUpgrader.DimensionToUpgrade<T>> list = Lists.<WorldUpgrader.DimensionToUpgrade<T>>newArrayList();

			for (ResourceKey<Level> resourceKey : WorldUpgrader.this.levels) {
				Path path = WorldUpgrader.this.levelStorage.getDimensionPath(resourceKey);
				Path path2 = path.resolve(string);
				T autoCloseable = this.createStorage(string, path, path2);
				ListIterator<WorldUpgrader.FileToUpgrade> listIterator = this.getFilesToProcess(string, resourceKey);
				list.add(new WorldUpgrader.DimensionToUpgrade(resourceKey, autoCloseable, listIterator));
			}

			return list;
		}

		protected abstract T createStorage(String string, Path path, Path path2);

		private ListIterator<WorldUpgrader.FileToUpgrade> getFilesToProcess(String string, ResourceKey<Level> resourceKey) {
			List<WorldUpgrader.FileToUpgrade> list = this.getAllChunkPositions(resourceKey, string);
			WorldUpgrader.this.totalFiles = WorldUpgrader.this.totalFiles + list.size();
			WorldUpgrader.this.totalChunks = WorldUpgrader.this.totalChunks + list.stream().mapToInt(fileToUpgrade -> fileToUpgrade.chunksToUpgrade.size()).sum();
			return list.listIterator();
		}

		private List<WorldUpgrader.FileToUpgrade> getAllChunkPositions(ResourceKey<Level> resourceKey, String string) {
			File file = WorldUpgrader.this.levelStorage.getDimensionPath(resourceKey).toFile();
			File file2 = new File(file, string);
			File[] files = file2.listFiles((filex, stringx) -> stringx.endsWith(".mca"));
			if (files == null) {
				return List.of();
			} else {
				List<WorldUpgrader.FileToUpgrade> list = Lists.<WorldUpgrader.FileToUpgrade>newArrayList();

				for (File file3 : files) {
					Matcher matcher = WorldUpgrader.REGEX.matcher(file3.getName());
					if (matcher.matches()) {
						int i = Integer.parseInt(matcher.group(1)) << 5;
						int j = Integer.parseInt(matcher.group(2)) << 5;
						List<ChunkPos> list2 = Lists.<ChunkPos>newArrayList();

						try (RegionFile regionFile = new RegionFile(file3.toPath(), file2.toPath(), true)) {
							for (int k = 0; k < 32; k++) {
								for (int l = 0; l < 32; l++) {
									ChunkPos chunkPos = new ChunkPos(k + i, l + j);
									if (regionFile.doesChunkExist(chunkPos)) {
										list2.add(chunkPos);
									}
								}
							}

							if (!list2.isEmpty()) {
								list.add(new WorldUpgrader.FileToUpgrade(regionFile, list2));
							}
						} catch (Throwable var21) {
							WorldUpgrader.LOGGER.error("Failed to read chunks from region file {}", file3.toPath(), var21);
						}
					}
				}

				return list;
			}
		}

		private boolean processOnePosition(ResourceKey<Level> resourceKey, T autoCloseable, ChunkPos chunkPos) {
			boolean bl = false;

			try {
				bl = this.tryProcessOnePosition(autoCloseable, chunkPos, resourceKey);
			} catch (CompletionException | ReportedException var7) {
				Throwable throwable = var7.getCause();
				if (!(throwable instanceof IOException)) {
					throw var7;
				}

				WorldUpgrader.LOGGER.error("Error upgrading chunk {}", chunkPos, throwable);
			}

			if (bl) {
				WorldUpgrader.this.converted++;
			} else {
				WorldUpgrader.this.skipped++;
			}

			return bl;
		}

		protected abstract boolean tryProcessOnePosition(T autoCloseable, ChunkPos chunkPos, ResourceKey<Level> resourceKey);

		private void onFileFinished(RegionFile regionFile) {
			if (WorldUpgrader.this.recreateRegionFiles) {
				if (this.previousWriteFuture != null) {
					this.previousWriteFuture.join();
				}

				Path path = regionFile.getPath();
				Path path2 = path.getParent();
				Path path3 = path2.resolveSibling("new_" + path2.getFileName().toString()).resolve(path.getFileName().toString());

				try {
					if (path3.toFile().exists()) {
						Files.delete(path);
						Files.move(path3, path);
					} else {
						WorldUpgrader.LOGGER.error("Failed to replace an old region file. New file {} does not exist.", path3);
					}
				} catch (IOException var6) {
					WorldUpgrader.LOGGER.error("Failed to replace an old region file", (Throwable)var6);
				}
			}
		}
	}

	class ChunkUpgrader extends WorldUpgrader.AbstractUpgrader<ChunkStorage> {
		ChunkUpgrader() {
			super(DataFixTypes.CHUNK, "region", WorldUpgrader.STATUS_UPGRADING_CHUNKS, WorldUpgrader.STATUS_FINISHED_CHUNKS);
		}

		protected boolean tryProcessOnePosition(ChunkStorage chunkStorage, ChunkPos chunkPos, ResourceKey<Level> resourceKey) {
			CompoundTag compoundTag = (CompoundTag)((Optional)chunkStorage.read(chunkPos).join()).orElse(null);
			if (compoundTag != null) {
				int i = ChunkStorage.getVersion(compoundTag);
				ChunkGenerator chunkGenerator = WorldUpgrader.this.dimensions.getOrThrow(Registries.levelToLevelStem(resourceKey)).generator();
				CompoundTag compoundTag2 = chunkStorage.upgradeChunkTag(
					resourceKey, () -> WorldUpgrader.this.overworldDataStorage, compoundTag, chunkGenerator.getTypeNameForDataFixer()
				);
				ChunkPos chunkPos2 = new ChunkPos(compoundTag2.getInt("xPos"), compoundTag2.getInt("zPos"));
				if (!chunkPos2.equals(chunkPos)) {
					WorldUpgrader.LOGGER.warn("Chunk {} has invalid position {}", chunkPos, chunkPos2);
				}

				boolean bl = i < SharedConstants.getCurrentVersion().getDataVersion().getVersion();
				if (WorldUpgrader.this.eraseCache) {
					bl = bl || compoundTag2.contains("Heightmaps");
					compoundTag2.remove("Heightmaps");
					bl = bl || compoundTag2.contains("isLightOn");
					compoundTag2.remove("isLightOn");
					ListTag listTag = compoundTag2.getList("sections", 10);

					for (int j = 0; j < listTag.size(); j++) {
						CompoundTag compoundTag3 = listTag.getCompound(j);
						bl = bl || compoundTag3.contains("BlockLight");
						compoundTag3.remove("BlockLight");
						bl = bl || compoundTag3.contains("SkyLight");
						compoundTag3.remove("SkyLight");
					}
				}

				if (bl || WorldUpgrader.this.recreateRegionFiles) {
					if (this.previousWriteFuture != null) {
						this.previousWriteFuture.join();
					}

					this.previousWriteFuture = chunkStorage.write(chunkPos, compoundTag2);
					return true;
				}
			}

			return false;
		}

		protected ChunkStorage createStorage(String string, Path path, Path path2) {
			return (ChunkStorage)(WorldUpgrader.this.recreateRegionFiles
				? new RecreatingChunkStorage(path2, path.resolve("new_" + string), WorldUpgrader.this.dataFixer, true)
				: new ChunkStorage(path2, WorldUpgrader.this.dataFixer, true));
		}
	}

	static record DimensionToUpgrade<T>(ResourceKey<Level> dimensionKey, T storage, ListIterator<WorldUpgrader.FileToUpgrade> files) {
	}

	class EntityUpgrader extends WorldUpgrader.SimpleRegionStorageUpgrader {
		EntityUpgrader() {
			super(DataFixTypes.ENTITY_CHUNK, "entities", WorldUpgrader.STATUS_UPGRADING_ENTITIES, WorldUpgrader.STATUS_FINISHED_ENTITIES);
		}

		@Override
		protected CompoundTag upgradeTag(SimpleRegionStorage simpleRegionStorage, CompoundTag compoundTag) {
			return simpleRegionStorage.upgradeChunkTag(compoundTag, -1);
		}
	}

	static record FileToUpgrade(RegionFile file, List<ChunkPos> chunksToUpgrade) {
	}

	class PoiUpgrader extends WorldUpgrader.SimpleRegionStorageUpgrader {
		PoiUpgrader() {
			super(DataFixTypes.POI_CHUNK, "poi", WorldUpgrader.STATUS_UPGRADING_POI, WorldUpgrader.STATUS_FINISHED_POI);
		}

		@Override
		protected CompoundTag upgradeTag(SimpleRegionStorage simpleRegionStorage, CompoundTag compoundTag) {
			return simpleRegionStorage.upgradeChunkTag(compoundTag, 1945);
		}
	}

	abstract class SimpleRegionStorageUpgrader extends WorldUpgrader.AbstractUpgrader<SimpleRegionStorage> {
		SimpleRegionStorageUpgrader(DataFixTypes dataFixTypes, String string, MutableComponent mutableComponent, MutableComponent mutableComponent2) {
			super(dataFixTypes, string, mutableComponent, mutableComponent2);
		}

		protected SimpleRegionStorage createStorage(String string, Path path, Path path2) {
			return (SimpleRegionStorage)(WorldUpgrader.this.recreateRegionFiles
				? new RecreatingSimpleRegionStorage(path2, path.resolve("new_" + string), WorldUpgrader.this.dataFixer, true, string, this.dataFixType)
				: new SimpleRegionStorage(path2, WorldUpgrader.this.dataFixer, true, string, this.dataFixType));
		}

		protected boolean tryProcessOnePosition(SimpleRegionStorage simpleRegionStorage, ChunkPos chunkPos, ResourceKey<Level> resourceKey) {
			CompoundTag compoundTag = (CompoundTag)((Optional)simpleRegionStorage.read(chunkPos).join()).orElse(null);
			if (compoundTag != null) {
				int i = ChunkStorage.getVersion(compoundTag);
				CompoundTag compoundTag2 = this.upgradeTag(simpleRegionStorage, compoundTag);
				boolean bl = i < SharedConstants.getCurrentVersion().getDataVersion().getVersion();
				if (bl || WorldUpgrader.this.recreateRegionFiles) {
					if (this.previousWriteFuture != null) {
						this.previousWriteFuture.join();
					}

					this.previousWriteFuture = simpleRegionStorage.write(chunkPos, compoundTag2);
					return true;
				}
			}

			return false;
		}

		protected abstract CompoundTag upgradeTag(SimpleRegionStorage simpleRegionStorage, CompoundTag compoundTag);
	}
}
