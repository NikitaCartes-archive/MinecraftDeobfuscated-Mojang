/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.LevelData;

public interface WritableLevelData
extends LevelData {
    public void setXSpawn(int var1);

    public void setYSpawn(int var1);

    public void setZSpawn(int var1);

    default public void setSpawn(BlockPos blockPos) {
        this.setXSpawn(blockPos.getX());
        this.setYSpawn(blockPos.getY());
        this.setZSpawn(blockPos.getZ());
    }
}

