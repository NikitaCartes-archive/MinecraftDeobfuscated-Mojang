package net.minecraft.util.worldupdate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldUpgrader {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
	private final String levelName;
	private final ImmutableMap<ResourceKey<DimensionType>, DimensionType> dimensionTypes;
	private final boolean eraseCache;
	private final LevelStorageSource.LevelStorageAccess levelStorage;
	private final Thread thread;
	private final DataFixer dataFixer;
	private volatile boolean running = true;
	private volatile boolean finished;
	private volatile float progress;
	private volatile int totalChunks;
	private volatile int converted;
	private volatile int skipped;
	private final Object2FloatMap<DimensionType> progressMap = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap<>(Util.identityStrategy()));
	private volatile Component status = new TranslatableComponent("optimizeWorld.stage.counting");
	private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
	private final DimensionDataStorage overworldDataStorage;

	public WorldUpgrader(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, WorldData worldData, boolean bl) {
		this.levelName = worldData.getLevelName();
		this.dimensionTypes = (ImmutableMap<ResourceKey<DimensionType>, DimensionType>)worldData.worldGenSettings()
			.dimensions()
			.entrySet()
			.stream()
			.collect(ImmutableMap.toImmutableMap(Entry::getKey, entry -> (DimensionType)((Pair)entry.getValue()).getFirst()));
		this.eraseCache = bl;
		this.dataFixer = dataFixer;
		this.levelStorage = levelStorageAccess;
		levelStorageAccess.saveDataTag(worldData);
		this.overworldDataStorage = new DimensionDataStorage(new File(this.levelStorage.getDimensionPath(DimensionType.OVERWORLD_LOCATION), "data"), dataFixer);
		this.thread = THREAD_FACTORY.newThread(this::work);
		this.thread.setUncaughtExceptionHandler((thread, throwable) -> {
			LOGGER.error("Error upgrading world", throwable);
			this.status = new TranslatableComponent("optimizeWorld.stage.failed");
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
		this.totalChunks = 0;
		Builder<DimensionType, ListIterator<ChunkPos>> builder = ImmutableMap.builder();

		for (Entry<ResourceKey<DimensionType>, DimensionType> entry : this.dimensionTypes.entrySet()) {
			List<ChunkPos> list = this.getAllChunkPos((ResourceKey<DimensionType>)entry.getKey());
			builder.put((DimensionType)entry.getValue(), list.listIterator());
			this.totalChunks = this.totalChunks + list.size();
		}

		if (this.totalChunks == 0) {
			this.finished = true;
		} else {
			float f = (float)this.totalChunks;
			ImmutableMap<DimensionType, ListIterator<ChunkPos>> immutableMap = builder.build();
			Builder<DimensionType, ChunkStorage> builder2 = ImmutableMap.builder();

			for (Entry<ResourceKey<DimensionType>, DimensionType> entry2 : this.dimensionTypes.entrySet()) {
				File file = this.levelStorage.getDimensionPath((ResourceKey<DimensionType>)entry2.getKey());
				builder2.put((DimensionType)entry2.getValue(), new ChunkStorage(new File(file, "region"), this.dataFixer, true));
			}

			ImmutableMap<DimensionType, ChunkStorage> immutableMap2 = builder2.build();
			long l = Util.getMillis();
			this.status = new TranslatableComponent("optimizeWorld.stage.upgrading");

			while (this.running) {
				boolean bl = false;
				float g = 0.0F;

				for (DimensionType dimensionType : this.dimensionTypes.values()) {
					ListIterator<ChunkPos> listIterator = immutableMap.get(dimensionType);
					ChunkStorage chunkStorage = immutableMap2.get(dimensionType);
					if (listIterator.hasNext()) {
						ChunkPos chunkPos = (ChunkPos)listIterator.next();
						boolean bl2 = false;

						try {
							CompoundTag compoundTag = chunkStorage.read(chunkPos);
							if (compoundTag != null) {
								int i = ChunkStorage.getVersion(compoundTag);
								CompoundTag compoundTag2 = chunkStorage.upgradeChunkTag(dimensionType, () -> this.overworldDataStorage, compoundTag);
								CompoundTag compoundTag3 = compoundTag2.getCompound("Level");
								ChunkPos chunkPos2 = new ChunkPos(compoundTag3.getInt("xPos"), compoundTag3.getInt("zPos"));
								if (!chunkPos2.equals(chunkPos)) {
									LOGGER.warn("Chunk {} has invalid position {}", chunkPos, chunkPos2);
								}

								boolean bl3 = i < SharedConstants.getCurrentVersion().getWorldVersion();
								if (this.eraseCache) {
									bl3 = bl3 || compoundTag3.contains("Heightmaps");
									compoundTag3.remove("Heightmaps");
									bl3 = bl3 || compoundTag3.contains("isLightOn");
									compoundTag3.remove("isLightOn");
								}

								if (bl3) {
									chunkStorage.write(chunkPos, compoundTag2);
									bl2 = true;
								}
							}
						} catch (ReportedException var23) {
							Throwable throwable = var23.getCause();
							if (!(throwable instanceof IOException)) {
								throw var23;
							}

							LOGGER.error("Error upgrading chunk {}", chunkPos, throwable);
						} catch (IOException var24) {
							LOGGER.error("Error upgrading chunk {}", chunkPos, var24);
						}

						if (bl2) {
							this.converted++;
						} else {
							this.skipped++;
						}

						bl = true;
					}

					float h = (float)listIterator.nextIndex() / f;
					this.progressMap.put(dimensionType, h);
					g += h;
				}

				this.progress = g;
				if (!bl) {
					this.running = false;
				}
			}

			this.status = new TranslatableComponent("optimizeWorld.stage.finished");

			for (ChunkStorage chunkStorage2 : immutableMap2.values()) {
				try {
					chunkStorage2.close();
				} catch (IOException var22) {
					LOGGER.error("Error upgrading chunk", (Throwable)var22);
				}
			}

			this.overworldDataStorage.save();
			l = Util.getMillis() - l;
			LOGGER.info("World optimizaton finished after {} ms", l);
			this.finished = true;
		}
	}

	private List<ChunkPos> getAllChunkPos(ResourceKey<DimensionType> resourceKey) {
		File file = this.levelStorage.getDimensionPath(resourceKey);
		File file2 = new File(file, "region");
		File[] files = file2.listFiles((filex, string) -> string.endsWith(".mca"));
		if (files == null) {
			return ImmutableList.of();
		} else {
			List<ChunkPos> list = Lists.<ChunkPos>newArrayList();

			for (File file3 : files) {
				Matcher matcher = REGEX.matcher(file3.getName());
				if (matcher.matches()) {
					int i = Integer.parseInt(matcher.group(1)) << 5;
					int j = Integer.parseInt(matcher.group(2)) << 5;

					try (RegionFile regionFile = new RegionFile(file3, file2, true)) {
						for (int k = 0; k < 32; k++) {
							for (int l = 0; l < 32; l++) {
								ChunkPos chunkPos = new ChunkPos(k + i, l + j);
								if (regionFile.doesChunkExist(chunkPos)) {
									list.add(chunkPos);
								}
							}
						}
					} catch (Throwable var28) {
					}
				}
			}

			return list;
		}
	}

	public boolean isFinished() {
		return this.finished;
	}

	@Environment(EnvType.CLIENT)
	public ImmutableMap<ResourceKey<DimensionType>, DimensionType> dimensionTypes() {
		return this.dimensionTypes;
	}

	@Environment(EnvType.CLIENT)
	public float dimensionProgress(DimensionType dimensionType) {
		return this.progressMap.getFloat(dimensionType);
	}

	@Environment(EnvType.CLIENT)
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
}
