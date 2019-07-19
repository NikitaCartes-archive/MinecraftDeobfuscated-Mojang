/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level.progress;

import net.minecraft.server.level.progress.ChunkProgressListener;

public interface ChunkProgressListenerFactory {
    public ChunkProgressListener create(int var1);
}

