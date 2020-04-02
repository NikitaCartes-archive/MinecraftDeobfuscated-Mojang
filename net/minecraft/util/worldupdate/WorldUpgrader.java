/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.worldupdate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldUpgrader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
    private final String levelName;
    private final boolean eraseCache;
    private final LevelStorage levelStorage;
    private final Thread thread;
    private final File pathToWorld;
    private volatile boolean running = true;
    private volatile boolean finished;
    private volatile float progress;
    private volatile int totalChunks;
    private volatile int converted;
    private volatile int skipped;
    private final Object2FloatMap<DimensionType> progressMap = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap(Util.identityStrategy()));
    private volatile Component status = new TranslatableComponent("optimizeWorld.stage.counting", new Object[0]);
    private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    private final DimensionDataStorage overworldDataStorage;

    public WorldUpgrader(LevelStorageSource.LevelStorageAccess levelStorageAccess, LevelData levelData, boolean bl) {
        this.levelName = levelData.getLevelName();
        this.eraseCache = bl;
        this.levelStorage = levelStorageAccess.selectLevel(null);
        this.levelStorage.saveLevelData(levelData);
        this.overworldDataStorage = new DimensionDataStorage(new File(DimensionType.OVERWORLD.getStorageFolder(this.levelStorage.getFolder()), "data"), this.levelStorage.getFixerUpper());
        this.pathToWorld = this.levelStorage.getFolder();
        this.thread = THREAD_FACTORY.newThread(this::work);
        this.thread.setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Error upgrading world", throwable);
            this.status = new TranslatableComponent("optimizeWorld.stage.failed", new Object[0]);
            this.finished = true;
        });
        this.thread.start();
    }

    public void cancel() {
        this.running = false;
        try {
            this.thread.join();
        } catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    private void work() {
        File file = this.levelStorage.getFolder();
        this.totalChunks = 0;
        ImmutableMap.Builder<DimensionType, ListIterator<ChunkPos>> builder = ImmutableMap.builder();
        for (DimensionType dimensionType : DimensionType.getAllTypes()) {
            List<ChunkPos> list = this.getAllChunkPos(dimensionType);
            builder.put(dimensionType, list.listIterator());
            this.totalChunks += list.size();
        }
        if (this.totalChunks == 0) {
            this.finished = true;
            return;
        }
        float f = this.totalChunks;
        ImmutableMap immutableMap = builder.build();
        ImmutableMap.Builder<DimensionType, ChunkStorage> builder2 = ImmutableMap.builder();
        for (DimensionType dimensionType2 : DimensionType.getAllTypes()) {
            File file2 = dimensionType2.getStorageFolder(file);
            builder2.put(dimensionType2, new ChunkStorage(new File(file2, "region"), this.levelStorage.getFixerUpper(), true));
        }
        ImmutableMap immutableMap2 = builder2.build();
        long l = Util.getMillis();
        this.status = new TranslatableComponent("optimizeWorld.stage.upgrading", new Object[0]);
        while (this.running) {
            boolean bl = false;
            float g = 0.0f;
            for (DimensionType dimensionType3 : DimensionType.getAllTypes()) {
                ListIterator listIterator = (ListIterator)immutableMap.get(dimensionType3);
                ChunkStorage chunkStorage = (ChunkStorage)immutableMap2.get(dimensionType3);
                if (listIterator.hasNext()) {
                    ChunkPos chunkPos = (ChunkPos)listIterator.next();
                    boolean bl2 = false;
                    try {
                        CompoundTag compoundTag = chunkStorage.read(chunkPos);
                        if (compoundTag != null) {
                            boolean bl3;
                            int i = ChunkStorage.getVersion(compoundTag);
                            CompoundTag compoundTag2 = chunkStorage.upgradeChunkTag(dimensionType3, () -> this.overworldDataStorage, compoundTag);
                            CompoundTag compoundTag3 = compoundTag2.getCompound("Level");
                            ChunkPos chunkPos2 = new ChunkPos(compoundTag3.getInt("xPos"), compoundTag3.getInt("zPos"));
                            if (!chunkPos2.equals(chunkPos)) {
                                LOGGER.warn("Chunk {} has invalid position {}", (Object)chunkPos, (Object)chunkPos2);
                            }
                            boolean bl4 = bl3 = i < SharedConstants.getCurrentVersion().getWorldVersion();
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
                    } catch (ReportedException reportedException) {
                        Throwable throwable = reportedException.getCause();
                        if (throwable instanceof IOException) {
                            LOGGER.error("Error upgrading chunk {}", (Object)chunkPos, (Object)throwable);
                        }
                        throw reportedException;
                    } catch (IOException iOException) {
                        LOGGER.error("Error upgrading chunk {}", (Object)chunkPos, (Object)iOException);
                    }
                    if (bl2) {
                        ++this.converted;
                    } else {
                        ++this.skipped;
                    }
                    bl = true;
                }
                float h = (float)listIterator.nextIndex() / f;
                this.progressMap.put(dimensionType3, h);
                g += h;
            }
            this.progress = g;
            if (bl) continue;
            this.running = false;
        }
        this.status = new TranslatableComponent("optimizeWorld.stage.finished", new Object[0]);
        for (ChunkStorage chunkStorage2 : immutableMap2.values()) {
            try {
                chunkStorage2.close();
            } catch (IOException iOException2) {
                LOGGER.error("Error upgrading chunk", (Throwable)iOException2);
            }
        }
        this.overworldDataStorage.save();
        l = Util.getMillis() - l;
        LOGGER.info("World optimizaton finished after {} ms", (Object)l);
        this.finished = true;
    }

    private List<ChunkPos> getAllChunkPos(DimensionType dimensionType) {
        File file2 = dimensionType.getStorageFolder(this.pathToWorld);
        File file22 = new File(file2, "region");
        File[] files = file22.listFiles((file, string) -> string.endsWith(".mca"));
        if (files == null) {
            return ImmutableList.of();
        }
        ArrayList<ChunkPos> list = Lists.newArrayList();
        for (File file3 : files) {
            Matcher matcher = REGEX.matcher(file3.getName());
            if (!matcher.matches()) continue;
            int i = Integer.parseInt(matcher.group(1)) << 5;
            int j = Integer.parseInt(matcher.group(2)) << 5;
            try (RegionFile regionFile = new RegionFile(file3, file22, true);){
                for (int k = 0; k < 32; ++k) {
                    for (int l = 0; l < 32; ++l) {
                        ChunkPos chunkPos = new ChunkPos(k + i, l + j);
                        if (!regionFile.doesChunkExist(chunkPos)) continue;
                        list.add(chunkPos);
                    }
                }
            } catch (Throwable throwable) {
                // empty catch block
            }
        }
        return list;
    }

    public boolean isFinished() {
        return this.finished;
    }

    @Environment(value=EnvType.CLIENT)
    public float dimensionProgress(DimensionType dimensionType) {
        return this.progressMap.getFloat(dimensionType);
    }

    @Environment(value=EnvType.CLIENT)
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

