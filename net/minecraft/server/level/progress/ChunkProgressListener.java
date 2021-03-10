/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level.progress;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public interface ChunkProgressListener {
    public void updateSpawnPos(ChunkPos var1);

    public void onStatusChange(ChunkPos var1, @Nullable ChunkStatus var2);

    @Environment(value=EnvType.CLIENT)
    public void start();

    public void stop();
}

