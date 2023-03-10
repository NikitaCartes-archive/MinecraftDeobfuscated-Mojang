/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level.progress;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LoggerChunkProgressListener
implements ChunkProgressListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final int maxCount;
    private int count;
    private long startTime;
    private long nextTickTime = Long.MAX_VALUE;

    public LoggerChunkProgressListener(int i) {
        int j = i * 2 + 1;
        this.maxCount = j * j;
    }

    @Override
    public void updateSpawnPos(ChunkPos chunkPos) {
        this.startTime = this.nextTickTime = Util.getMillis();
    }

    @Override
    public void onStatusChange(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus) {
        if (chunkStatus == ChunkStatus.FULL) {
            ++this.count;
        }
        int i = this.getProgress();
        if (Util.getMillis() > this.nextTickTime) {
            this.nextTickTime += 500L;
            LOGGER.info(Component.translatable("menu.preparingSpawn", Mth.clamp(i, 0, 100)).getString());
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        LOGGER.info("Time elapsed: {} ms", (Object)(Util.getMillis() - this.startTime));
        this.nextTickTime = Long.MAX_VALUE;
    }

    public int getProgress() {
        return Mth.floor((float)this.count * 100.0f / (float)this.maxCount);
    }
}

