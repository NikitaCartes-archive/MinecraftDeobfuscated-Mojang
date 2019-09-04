/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ThreadedLevelLightEngine
extends LevelLightEngine
implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ProcessorMailbox<Runnable> taskMailbox;
    private final ObjectList<Pair<TaskType, Runnable>> lightTasks = new ObjectArrayList<Pair<TaskType, Runnable>>();
    private final ChunkMap chunkMap;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> sorterMailbox;
    private volatile int taskPerBatch = 5;
    private final AtomicBoolean scheduled = new AtomicBoolean();

    public ThreadedLevelLightEngine(LightChunkGetter lightChunkGetter, ChunkMap chunkMap, boolean bl, ProcessorMailbox<Runnable> processorMailbox, ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> processorHandle) {
        super(lightChunkGetter, true, bl);
        this.chunkMap = chunkMap;
        this.sorterMailbox = processorHandle;
        this.taskMailbox = processorMailbox;
    }

    @Override
    public void close() {
    }

    @Override
    public int runUpdates(int i, boolean bl, boolean bl2) {
        throw Util.pauseInIde(new UnsupportedOperationException("Ran authomatically on a different thread!"));
    }

    @Override
    public void onBlockEmissionIncrease(BlockPos blockPos, int i) {
        throw Util.pauseInIde(new UnsupportedOperationException("Ran authomatically on a different thread!"));
    }

    @Override
    public void checkBlock(BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.immutable();
        this.addTask(blockPos.getX() >> 4, blockPos.getZ() >> 4, TaskType.POST_UPDATE, Util.name(() -> super.checkBlock(blockPos2), () -> "checkBlock " + blockPos2));
    }

    protected void updateChunkStatus(ChunkPos chunkPos) {
        this.addTask(chunkPos.x, chunkPos.z, () -> 0, TaskType.PRE_UPDATE, Util.name(() -> {
            int i;
            super.retainData(chunkPos, false);
            super.enableLightSources(chunkPos, false);
            for (i = -1; i < 17; ++i) {
                super.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkPos, i), null);
                super.queueSectionData(LightLayer.SKY, SectionPos.of(chunkPos, i), null);
            }
            for (i = 0; i < 16; ++i) {
                super.updateSectionStatus(SectionPos.of(chunkPos, i), true);
            }
        }, () -> "updateChunkStatus " + chunkPos + " " + true));
    }

    @Override
    public void updateSectionStatus(SectionPos sectionPos, boolean bl) {
        this.addTask(sectionPos.x(), sectionPos.z(), () -> 0, TaskType.PRE_UPDATE, Util.name(() -> super.updateSectionStatus(sectionPos, bl), () -> "updateSectionStatus " + sectionPos + " " + bl));
    }

    @Override
    public void enableLightSources(ChunkPos chunkPos, boolean bl) {
        this.addTask(chunkPos.x, chunkPos.z, TaskType.PRE_UPDATE, Util.name(() -> super.enableLightSources(chunkPos, bl), () -> "enableLight " + chunkPos + " " + bl));
    }

    @Override
    public void queueSectionData(LightLayer lightLayer, SectionPos sectionPos, @Nullable DataLayer dataLayer) {
        this.addTask(sectionPos.x(), sectionPos.z(), () -> 0, TaskType.PRE_UPDATE, Util.name(() -> super.queueSectionData(lightLayer, sectionPos, dataLayer), () -> "queueData " + sectionPos));
    }

    private void addTask(int i, int j, TaskType taskType, Runnable runnable) {
        this.addTask(i, j, this.chunkMap.getChunkQueueLevel(ChunkPos.asLong(i, j)), taskType, runnable);
    }

    private void addTask(int i, int j, IntSupplier intSupplier, TaskType taskType, Runnable runnable) {
        this.sorterMailbox.tell(ChunkTaskPriorityQueueSorter.message(() -> {
            this.lightTasks.add(Pair.of(taskType, runnable));
            if (this.lightTasks.size() >= this.taskPerBatch) {
                this.runUpdate();
            }
        }, ChunkPos.asLong(i, j), intSupplier));
    }

    @Override
    public void retainData(ChunkPos chunkPos, boolean bl) {
        this.addTask(chunkPos.x, chunkPos.z, () -> 0, TaskType.PRE_UPDATE, Util.name(() -> super.retainData(chunkPos, bl), () -> "retainData " + chunkPos));
    }

    public CompletableFuture<ChunkAccess> lightChunk(ChunkAccess chunkAccess, boolean bl) {
        ChunkPos chunkPos = chunkAccess.getPos();
        chunkAccess.setLightCorrect(false);
        this.addTask(chunkPos.x, chunkPos.z, TaskType.PRE_UPDATE, Util.name(() -> {
            LevelChunkSection[] levelChunkSections = chunkAccess.getSections();
            for (int i = 0; i < 16; ++i) {
                LevelChunkSection levelChunkSection = levelChunkSections[i];
                if (LevelChunkSection.isEmpty(levelChunkSection)) continue;
                super.updateSectionStatus(SectionPos.of(chunkPos, i), false);
            }
            super.enableLightSources(chunkPos, true);
            if (!bl) {
                chunkAccess.getLights().forEach(blockPos -> super.onBlockEmissionIncrease((BlockPos)blockPos, chunkAccess.getLightEmission((BlockPos)blockPos)));
            }
            this.chunkMap.releaseLightTicket(chunkPos);
        }, () -> "lightChunk " + chunkPos + " " + bl));
        return CompletableFuture.supplyAsync(() -> {
            chunkAccess.setLightCorrect(true);
            super.retainData(chunkPos, false);
            return chunkAccess;
        }, runnable -> this.addTask(chunkPos.x, chunkPos.z, TaskType.POST_UPDATE, runnable));
    }

    public void tryScheduleUpdate() {
        if ((!this.lightTasks.isEmpty() || super.hasLightWork()) && this.scheduled.compareAndSet(false, true)) {
            this.taskMailbox.tell(() -> {
                this.runUpdate();
                this.scheduled.set(false);
            });
        }
    }

    private void runUpdate() {
        Pair pair;
        int j;
        int i = Math.min(this.lightTasks.size(), this.taskPerBatch);
        Iterator objectListIterator = this.lightTasks.iterator();
        for (j = 0; objectListIterator.hasNext() && j < i; ++j) {
            pair = (Pair)objectListIterator.next();
            if (pair.getFirst() != TaskType.PRE_UPDATE) continue;
            ((Runnable)pair.getSecond()).run();
        }
        objectListIterator.back(j);
        super.runUpdates(Integer.MAX_VALUE, true, true);
        for (j = 0; objectListIterator.hasNext() && j < i; ++j) {
            pair = (Pair)objectListIterator.next();
            if (pair.getFirst() == TaskType.POST_UPDATE) {
                ((Runnable)pair.getSecond()).run();
            }
            objectListIterator.remove();
        }
    }

    public void setTaskPerBatch(int i) {
        this.taskPerBatch = i;
    }

    static enum TaskType {
        PRE_UPDATE,
        POST_UPDATE;

    }
}

