package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.storage.OldChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class McRegionUpgrader {
	private static final Logger LOGGER = LogManager.getLogger();

	static boolean convertLevel(LevelStorageSource.LevelStorageAccess levelStorageAccess, ProgressListener progressListener) {
		progressListener.progressStagePercentage(0);
		List<File> list = Lists.<File>newArrayList();
		List<File> list2 = Lists.<File>newArrayList();
		List<File> list3 = Lists.<File>newArrayList();
		File file = levelStorageAccess.getDimensionPath(Level.OVERWORLD);
		File file2 = levelStorageAccess.getDimensionPath(Level.NETHER);
		File file3 = levelStorageAccess.getDimensionPath(Level.END);
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
		RegistryAccess.RegistryHolder registryHolder = RegistryAccess.builtin();
		RegistryReadOps<Tag> registryReadOps = RegistryReadOps.create(NbtOps.INSTANCE, ResourceManager.Empty.INSTANCE, registryHolder);
		WorldData worldData = levelStorageAccess.getDataTag(registryReadOps, DataPackConfig.DEFAULT);
		long l = worldData != null ? worldData.worldGenSettings().seed() : 0L;
		Registry<Biome> registry = registryHolder.registryOrThrow(Registry.BIOME_REGISTRY);
		BiomeSource biomeSource;
		if (worldData != null && worldData.worldGenSettings().isFlatWorld()) {
			biomeSource = new FixedBiomeSource(registry.getOrThrow(Biomes.PLAINS));
		} else {
			biomeSource = new OverworldBiomeSource(l, false, false, registry);
		}

		convertRegions(registryHolder, new File(file, "region"), list, biomeSource, 0, i, progressListener);
		convertRegions(
			registryHolder, new File(file2, "region"), list2, new FixedBiomeSource(registry.getOrThrow(Biomes.NETHER_WASTES)), list.size(), i, progressListener
		);
		convertRegions(
			registryHolder, new File(file3, "region"), list3, new FixedBiomeSource(registry.getOrThrow(Biomes.THE_END)), list.size() + list2.size(), i, progressListener
		);
		makeMcrLevelDatBackup(levelStorageAccess);
		levelStorageAccess.saveDataTag(registryHolder, worldData);
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

	private static void convertRegions(
		RegistryAccess.RegistryHolder registryHolder, File file, Iterable<File> iterable, BiomeSource biomeSource, int i, int j, ProgressListener progressListener
	) {
		for (File file2 : iterable) {
			convertRegion(registryHolder, file, file2, biomeSource, i, j, progressListener);
			i++;
			int k = (int)Math.round(100.0 * (double)i / (double)j);
			progressListener.progressStagePercentage(k);
		}
	}

	private static void convertRegion(
		RegistryAccess.RegistryHolder registryHolder, File file, File file2, BiomeSource biomeSource, int i, int j, ProgressListener progressListener
	) {
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
							} catch (Throwable var105) {
								oldLevelChunk = var105;
								throw var105;
							} finally {
								if (dataInputStream != null) {
									if (oldLevelChunk != null) {
										try {
											dataInputStream.close();
										} catch (Throwable var102) {
											oldLevelChunk.addSuppressed(var102);
										}
									} else {
										dataInputStream.close();
									}
								}
							}
						} catch (IOException var107) {
							LOGGER.warn("Failed to read data for chunk {}", chunkPos, var107);
							continue;
						}

						CompoundTag compoundTag2 = compoundTag.getCompound("Level");
						OldChunkStorage.OldLevelChunk oldLevelChunk = OldChunkStorage.load(compoundTag2);
						CompoundTag compoundTag3 = new CompoundTag();
						CompoundTag compoundTag4 = new CompoundTag();
						compoundTag3.put("Level", compoundTag4);
						OldChunkStorage.convertToAnvilFormat(registryHolder, oldLevelChunk, compoundTag4, biomeSource);
						DataOutputStream dataOutputStream = regionFile2.getChunkDataOutputStream(chunkPos);
						Throwable var21 = null;

						try {
							NbtIo.write(compoundTag3, dataOutputStream);
						} catch (Throwable var103) {
							var21 = var103;
							throw var103;
						} finally {
							if (dataOutputStream != null) {
								if (var21 != null) {
									try {
										dataOutputStream.close();
									} catch (Throwable var101) {
										var21.addSuppressed(var101);
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
		} catch (IOException var112) {
			LOGGER.error("Failed to upgrade region file {}", file2, var112);
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
