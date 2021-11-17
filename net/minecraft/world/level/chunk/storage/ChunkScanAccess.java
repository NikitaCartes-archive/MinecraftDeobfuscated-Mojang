/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk.storage;

import java.util.concurrent.CompletableFuture;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.world.level.ChunkPos;

public interface ChunkScanAccess {
    public CompletableFuture<Void> scanChunk(ChunkPos var1, StreamTagVisitor var2);
}

