package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSourceSettings;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSourceSettings;
import net.minecraft.world.level.chunk.storage.OldChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class McRegionUpgrader {
	private static final Logger LOGGER = LogManager.getLogger();

	static boolean convertLevel(LevelStorageSource.LevelStorageAccess levelStorageAccess, ProgressListener progressListener) {
		progressListener.progressStagePercentage(0);
		List<File> list = Lists.<File>newArrayList();
		List<File> list2 = Lists.<File>newArrayList();
		List<File> list3 = Lists.<File>newArrayList();
		File file = levelStorageAccess.getDimensionPath(DimensionType.OVERWORLD);
		File file2 = levelStorageAccess.getDimensionPath(DimensionType.NETHER);
		File file3 = levelStorageAccess.getDimensionPath(DimensionType.THE_END);
		LOGGER.info("Scanning folders...");
		addRegionFiles(file, list);
		if (file2.exists()) {
			addRegionFiles(file2, list2);
		}

		if (file3.exists()) {
			addRegionFiles(file3, list3);
		}

		int i = list.size() + list2.size() + list3.size();
		LOGGER.info("Total conversion count is {}", i);
		WorldData worldData = levelStorageAccess.getDataTag();
		long l = worldData != null ? worldData.getSeed() : 0L;
		BiomeSourceType<FixedBiomeSourceSettings, FixedBiomeSource> biomeSourceType = BiomeSourceType.FIXED;
		BiomeSourceType<OverworldBiomeSourceSettings, OverworldBiomeSource> biomeSourceType2 = BiomeSourceType.VANILLA_LAYERED;
		BiomeSource biomeSource;
		if (worldData != null && worldData.getLevelData(DimensionType.OVERWORLD).getGeneratorType() == LevelType.FLAT) {
			biomeSource = biomeSourceType.create(biomeSourceType.createSettings(worldData.getSeed()).setBiome(Biomes.PLAINS));
		} else {
			biomeSource = biomeSourceType2.create(biomeSourceType2.createSettings(l));
		}

		convertRegions(new File(file, "region"), list, biomeSource, 0, i, progressListener);
		convertRegions(
			new File(file2, "region"), list2, biomeSourceType.create(biomeSourceType.createSettings(l).setBiome(Biomes.NETHER_WASTES)), list.size(), i, progressListener
		);
		convertRegions(
			new File(file3, "region"),
			list3,
			biomeSourceType.create(biomeSourceType.createSettings(l).setBiome(Biomes.THE_END)),
			list.size() + list2.size(),
			i,
			progressListener
		);
		makeMcrLevelDatBackup(levelStorageAccess);
		levelStorageAccess.saveDataTag(worldData);
		return true;
	}

	private static void makeMcrLevelDatBackup(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		File file = levelStorageAccess.getLevelPath(LevelResource.LEVEL_DATA_FILE).toFile();
		if (!file.exists()) {
			LOGGER.warn("Unable to create level.dat_mcr backup");
		} else {
			File file2 = new File(file.getParent(), "level.dat_mcr");
			if (!file.renameTo(file2)) {
				LOGGER.warn("Unable to create level.dat_mcr backup");
			}
		}
	}

	private static void convertRegions(File file, Iterable<File> iterable, BiomeSource biomeSource, int i, int j, ProgressListener progressListener) {
		for (File file2 : iterable) {
			convertRegion(file, file2, biomeSource, i, j, progressListener);
			i++;
			int k = (int)Math.round(100.0 * (double)i / (double)j);
			progressListener.progressStagePercentage(k);
		}
	}

	private static void convertRegion(File file, File file2, BiomeSource biomeSource, int i, int j, ProgressListener progressListener) {
		String string = file2.getName();

		try (
			RegionFile regionFile = new RegionFile(file2, file, true);
			RegionFile regionFile2 = new RegionFile(new File(file, string.substring(0, string.length() - ".mcr".length()) + ".mca"), file, true);
		) {
			for (int k = 0; k < 32; k++) {
				for (int l = 0; l < 32; l++) {
					ChunkPos chunkPos = new ChunkPos(k, l);
					if (regionFile.hasChunk(chunkPos) && !regionFile2.hasChunk(chunkPos)) {
						CompoundTag compoundTag;
						try {
							DataInputStream dataInputStream = regionFile.getChunkDataInputStream(chunkPos);
							Throwable oldLevelChunk = null;

							try {
								if (dataInputStream == null) {
									LOGGER.warn("Failed to fetch input stream for chunk {}", chunkPos);
									continue;
								}

								compoundTag = NbtIo.read(dataInputStream);
							} catch (Throwable var104) {
								oldLevelChunk = var104;
								throw var104;
							} finally {
								if (dataInputStream != null) {
									if (oldLevelChunk != null) {
										try {
											dataInputStream.close();
										} catch (Throwable var101) {
											oldLevelChunk.addSuppressed(var101);
										}
									} else {
										dataInputStream.close();
									}
								}
							}
						} catch (IOException var106) {
							LOGGER.warn("Failed to read data for chunk {}", chunkPos, var106);
							continue;
						}

						CompoundTag compoundTag2 = compoundTag.getCompound("Level");
						OldChunkStorage.OldLevelChunk oldLevelChunk = OldChunkStorage.load(compoundTag2);
						CompoundTag compoundTag3 = new CompoundTag();
						CompoundTag compoundTag4 = new CompoundTag();
						compoundTag3.put("Level", compoundTag4);
						OldChunkStorage.convertToAnvilFormat(oldLevelChunk, compoundTag4, biomeSource);
						DataOutputStream dataOutputStream = regionFile2.getChunkDataOutputStream(chunkPos);
						Throwable var20 = null;

						try {
							NbtIo.write(compoundTag3, dataOutputStream);
						} catch (Throwable var102) {
							var20 = var102;
							throw var102;
						} finally {
							if (dataOutputStream != null) {
								if (var20 != null) {
									try {
										dataOutputStream.close();
									} catch (Throwable var100) {
										var20.addSuppressed(var100);
									}
								} else {
									dataOutputStream.close();
								}
							}
						}
					}
				}

				int lx = (int)Math.round(100.0 * (double)(i * 1024) / (double)(j * 1024));
				int m = (int)Math.round(100.0 * (double)((k + 1) * 32 + i * 1024) / (double)(j * 1024));
				if (m > lx) {
					progressListener.progressStagePercentage(m);
				}
			}
		} catch (IOException var111) {
			LOGGER.error("Failed to upgrade region file {}", file2, var111);
		}
	}

	private static void addRegionFiles(File file, Collection<File> collection) {
		File file2 = new File(file, "region");
		File[] files = file2.listFiles((filex, string) -> string.endsWith(".mcr"));
		if (files != null) {
			Collections.addAll(collection, files);
		}
	}
}
