/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;

public abstract class ChunkSource
implements LightChunkGetter,
AutoCloseable {
    @Nullable
    public LevelChunk getChunk(int i, int j, boolean bl) {
        return (LevelChunk)this.getChunk(i, j, ChunkStatus.FULL, bl);
    }

    @Nullable
    public LevelChunk getChunkNow(int i, int j) {
        return this.getChunk(i, j, false);
    }

    @Override
    @Nullable
    public BlockGetter getChunkForLighting(int i, int j) {
        return this.getChunk(i, j, ChunkStatus.EMPTY, false);
    }

    public boolean hasChunk(int i, int j) {
        return this.getChunk(i, j, ChunkStatus.FULL, false) != null;
    }

    @Nullable
    public abstract ChunkAccess getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    public abstract void tick(BooleanSupplier var1);

    public abstract String gatherStats();

    public abstract int getLoadedChunksCount();

    @Override
    public void close() throws IOException {
    }

    public abstract LevelLightEngine getLightEngine();

    public void setSpawnSettings(boolean bl, boolean bl2) {
    }

    public void updateChunkForced(ChunkPos chunkPos, boolean bl) {
    }
}

