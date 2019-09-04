/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class McRegionUpgrader {
    private static final Logger LOGGER = LogManager.getLogger();

    static boolean convertLevel(Path path, DataFixer dataFixer, String string, ProgressListener progressListener) {
        progressListener.progressStagePercentage(0);
        ArrayList<File> list = Lists.newArrayList();
        ArrayList<File> list2 = Lists.newArrayList();
        ArrayList<File> list3 = Lists.newArrayList();
        File file = new File(path.toFile(), string);
        File file2 = DimensionType.NETHER.getStorageFolder(file);
        File file3 = DimensionType.THE_END.getStorageFolder(file);
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
        LevelData levelData = LevelStorageSource.getDataTagFor(path, dataFixer, string);
        BiomeSourceType<FixedBiomeSourceSettings, FixedBiomeSource> biomeSourceType = BiomeSourceType.FIXED;
        BiomeSourceType<OverworldBiomeSourceSettings, OverworldBiomeSource> biomeSourceType2 = BiomeSourceType.VANILLA_LAYERED;
        BiomeSource biomeSource = levelData != null && levelData.getGeneratorType() == LevelType.FLAT ? biomeSourceType.create(biomeSourceType.createSettings(levelData).setBiome(Biomes.PLAINS)) : biomeSourceType2.create(biomeSourceType2.createSettings(levelData));
        McRegionUpgrader.convertRegions(new File(file, "region"), list, biomeSource, 0, i, progressListener);
        McRegionUpgrader.convertRegions(new File(file2, "region"), list2, biomeSourceType.create(biomeSourceType.createSettings(levelData).setBiome(Biomes.NETHER)), list.size(), i, progressListener);
        McRegionUpgrader.convertRegions(new File(file3, "region"), list3, biomeSourceType.create(biomeSourceType.createSettings(levelData).setBiome(Biomes.THE_END)), list.size() + list2.size(), i, progressListener);
        levelData.setVersion(19133);
        if (levelData.getGeneratorType() == LevelType.NORMAL_1_1) {
            levelData.setGenerator(LevelType.NORMAL);
        }
        McRegionUpgrader.makeMcrLevelDatBackup(path, string);
        LevelStorage levelStorage = LevelStorageSource.selectLevel(path, dataFixer, string, null);
        levelStorage.saveLevelData(levelData);
        return true;
    }

    private static void makeMcrLevelDatBackup(Path path, String string) {
        File file = new File(path.toFile(), string);
        if (!file.exists()) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
            return;
        }
        File file2 = new File(file, "level.dat");
        if (!file2.exists()) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
            return;
        }
        File file3 = new File(file, "level.dat_mcr");
        if (!file2.renameTo(file3)) {
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
        try (RegionFile regionFile = new RegionFile(file2, file);
             RegionFile regionFile2 = new RegionFile(new File(file, string.substring(0, string.length() - ".mcr".length()) + ".mca"), file);){
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

