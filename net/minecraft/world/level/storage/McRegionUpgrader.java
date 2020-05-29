/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.storage.OldChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class McRegionUpgrader {
    private static final Logger LOGGER = LogManager.getLogger();

    static boolean convertLevel(LevelStorageSource.LevelStorageAccess levelStorageAccess, ProgressListener progressListener) {
        progressListener.progressStagePercentage(0);
        ArrayList<File> list = Lists.newArrayList();
        ArrayList<File> list2 = Lists.newArrayList();
        ArrayList<File> list3 = Lists.newArrayList();
        File file = levelStorageAccess.getDimensionPath(Level.OVERWORLD);
        File file2 = levelStorageAccess.getDimensionPath(Level.NETHER);
        File file3 = levelStorageAccess.getDimensionPath(Level.END);
        LOGGER.info("Scanning folders...");
        McRegionUpgrader.addRegionFiles(file, list);
        if (file2.exists()) {
            McRegionUpgrader.addRegionFiles(file2, list2);
        }
        if (file3.exists()) {
            McRegionUpgrader.addRegionFiles(file3, list3);
        }
        int i = list.size() + list2.size() + list3.size();
        LOGGER.info("Total conversion count is {}", (Object)i);
        WorldData worldData = levelStorageAccess.getDataTag();
        long l = worldData != null ? worldData.worldGenSettings().seed() : 0L;
        BiomeSource biomeSource = worldData != null && worldData.worldGenSettings().isFlatWorld() ? new FixedBiomeSource(Biomes.PLAINS) : new OverworldBiomeSource(l, false, false);
        McRegionUpgrader.convertRegions(new File(file, "region"), list, biomeSource, 0, i, progressListener);
        McRegionUpgrader.convertRegions(new File(file2, "region"), list2, new FixedBiomeSource(Biomes.NETHER_WASTES), list.size(), i, progressListener);
        McRegionUpgrader.convertRegions(new File(file3, "region"), list3, new FixedBiomeSource(Biomes.THE_END), list.size() + list2.size(), i, progressListener);
        McRegionUpgrader.makeMcrLevelDatBackup(levelStorageAccess);
        levelStorageAccess.saveDataTag(worldData);
        return true;
    }

    private static void makeMcrLevelDatBackup(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        File file = levelStorageAccess.getLevelPath(LevelResource.LEVEL_DATA_FILE).toFile();
        if (!file.exists()) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
            return;
        }
        File file2 = new File(file.getParent(), "level.dat_mcr");
        if (!file.renameTo(file2)) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
        }
    }

    private static void convertRegions(File file, Iterable<File> iterable, BiomeSource biomeSource, int i, int j, ProgressListener progressListener) {
        for (File file2 : iterable) {
            McRegionUpgrader.convertRegion(file, file2, biomeSource, i, j, progressListener);
            int k = (int)Math.round(100.0 * (double)(++i) / (double)j);
            progressListener.progressStagePercentage(k);
        }
    }

    private static void convertRegion(File file, File file2, BiomeSource biomeSource, int i, int j, ProgressListener progressListener) {
        String string = file2.getName();
        try (RegionFile regionFile = new RegionFile(file2, file, true);
             RegionFile regionFile2 = new RegionFile(new File(file, string.substring(0, string.length() - ".mcr".length()) + ".mca"), file, true);){
            for (int k = 0; k < 32; ++k) {
                int l;
                for (l = 0; l < 32; ++l) {
                    CompoundTag compoundTag;
                    ChunkPos chunkPos = new ChunkPos(k, l);
                    if (!regionFile.hasChunk(chunkPos) || regionFile2.hasChunk(chunkPos)) continue;
                    try (DataInputStream dataInputStream = regionFile.getChunkDataInputStream(chunkPos);){
                        if (dataInputStream == null) {
                            LOGGER.warn("Failed to fetch input stream for chunk {}", (Object)chunkPos);
                            continue;
                        }
                        compoundTag = NbtIo.read(dataInputStream);
                    } catch (IOException iOException) {
                        LOGGER.warn("Failed to read data for chunk {}", (Object)chunkPos, (Object)iOException);
                        continue;
                    }
                    CompoundTag compoundTag2 = compoundTag.getCompound("Level");
                    OldChunkStorage.OldLevelChunk oldLevelChunk = OldChunkStorage.load(compoundTag2);
                    CompoundTag compoundTag3 = new CompoundTag();
                    CompoundTag compoundTag4 = new CompoundTag();
                    compoundTag3.put("Level", compoundTag4);
                    OldChunkStorage.convertToAnvilFormat(oldLevelChunk, compoundTag4, biomeSource);
                    try (DataOutputStream dataOutputStream = regionFile2.getChunkDataOutputStream(chunkPos);){
                        NbtIo.write(compoundTag3, dataOutputStream);
                        continue;
                    }
                }
                l = (int)Math.round(100.0 * (double)(i * 1024) / (double)(j * 1024));
                int m = (int)Math.round(100.0 * (double)((k + 1) * 32 + i * 1024) / (double)(j * 1024));
                if (m <= l) continue;
                progressListener.progressStagePercentage(m);
            }
        } catch (IOException iOException2) {
            LOGGER.error("Failed to upgrade region file {}", (Object)file2, (Object)iOException2);
        }
    }

    private static void addRegionFiles(File file2, Collection<File> collection) {
        File file22 = new File(file2, "region");
        File[] files = file22.listFiles((file, string) -> string.endsWith(".mcr"));
        if (files != null) {
            Collections.addAll(collection, files);
        }
    }
}

