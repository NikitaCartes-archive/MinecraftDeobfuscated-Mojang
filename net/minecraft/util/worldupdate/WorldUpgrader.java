/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.worldupdate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
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
    private final Object2FloatMap<DimensionType> progressMap = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap(Util.identityStrategy()));
    private volatile Component status = new TranslatableComponent("optimizeWorld.stage.counting");
    private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    private final DimensionDataStorage overworldDataStorage;

    public WorldUpgrader(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, WorldData worldData, boolean bl) {
        this.levelName = worldData.getLevelName();
        this.dimensionTypes = worldData.worldGenSettings().dimensions().entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> (DimensionType)((Pair)entry.getValue()).getFirst()));
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
        } catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    private void work() {
        this.totalChunks = 0;
        ImmutableMap.Builder builder = ImmutableMap.builder();
        for (Map.Entry entry : this.dimensionTypes.entrySet()) {
            List<ChunkPos> list = this.getAllChunkPos((ResourceKey)entry.getKey());
            builder.put(entry.getValue(), list.listIterator());
            this.totalChunks += list.size();
        }
        if (this.totalChunks == 0) {
            this.finished = true;
            return;
        }
        float f = this.totalChunks;
        ImmutableMap immutableMap = builder.build();
        ImmutableMap.Builder builder2 = ImmutableMap.builder();
        for (Map.Entry entry2 : this.dimensionTypes.entrySet()) {
            File file = this.levelStorage.getDimensionPath((ResourceKey)entry2.getKey());
            builder2.put(entry2.getValue(), new ChunkStorage(new File(file, "region"), this.dataFixer, true));
        }
        ImmutableMap immutableMap2 = builder2.build();
        long l = Util.getMillis();
        this.status = new TranslatableComponent("optimizeWorld.stage.upgrading");
        while (this.running) {
            boolean bl = false;
            float g = 0.0f;
            for (DimensionType dimensionType : this.dimensionTypes.values()) {
                ListIterator listIterator = (ListIterator)immutableMap.get(dimensionType);
                ChunkStorage chunkStorage = (ChunkStorage)immutableMap2.get(dimensionType);
                if (listIterator.hasNext()) {
                    ChunkPos chunkPos = (ChunkPos)listIterator.next();
                    boolean bl2 = false;
                    try {
                        CompoundTag compoundTag = chunkStorage.read(chunkPos);
                        if (compoundTag != null) {
                            boolean bl3;
                            int i = ChunkStorage.getVersion(compoundTag);
                            CompoundTag compoundTag2 = chunkStorage.upgradeChunkTag(dimensionType, () -> this.overworldDataStorage, compoundTag);
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
                this.progressMap.put(dimensionType, h);
                g += h;
            }
            this.progress = g;
            if (bl) continue;
            this.running = false;
        }
        this.status = new TranslatableComponent("optimizeWorld.stage.finished");
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

    private List<ChunkPos> getAllChunkPos(ResourceKey<DimensionType> resourceKey) {
        File file2 = this.levelStorage.getDimensionPath(resourceKey);
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
    public ImmutableMap<ResourceKey<DimensionType>, DimensionType> dimensionTypes() {
        return this.dimensionTypes;
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

