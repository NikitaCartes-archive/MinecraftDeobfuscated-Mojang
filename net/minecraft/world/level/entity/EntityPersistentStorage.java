/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.entity;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;

public interface EntityPersistentStorage<T>
extends AutoCloseable {
    public CompletableFuture<ChunkEntities<T>> loadEntities(ChunkPos var1);

    public void storeEntities(ChunkEntities<T> var1);

    public void flush(boolean var1);

    @Override
    default public void close() throws IOException {
    }
}

