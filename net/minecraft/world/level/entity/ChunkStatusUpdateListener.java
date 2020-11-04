/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.entity;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;

@FunctionalInterface
public interface ChunkStatusUpdateListener {
    public void onChunkStatusChange(ChunkPos var1, ChunkHolder.FullChunkStatus var2);
}

