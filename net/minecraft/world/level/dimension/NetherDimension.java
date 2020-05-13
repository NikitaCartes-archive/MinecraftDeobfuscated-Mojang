/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public class NetherDimension
extends Dimension {
    public NetherDimension(Level level, DimensionType dimensionType) {
        super(level, dimensionType, 0.1f);
    }

    @Override
    public boolean isNaturalDimension() {
        return false;
    }

    @Override
    @Nullable
    public BlockPos getSpawnPosInChunk(long l, ChunkPos chunkPos, boolean bl) {
        return null;
    }

    @Override
    @Nullable
    public BlockPos getValidSpawnPosition(long l, int i, int j, boolean bl) {
        return null;
    }

    @Override
    public float getTimeOfDay(long l, float f) {
        return 0.5f;
    }

    @Override
    public boolean mayRespawn() {
        return false;
    }

    @Override
    public WorldBorder createWorldBorder() {
        return new WorldBorder(){

            @Override
            public double getCenterX() {
                return super.getCenterX() / 8.0;
            }

            @Override
            public double getCenterZ() {
                return super.getCenterZ() / 8.0;
            }
        };
    }

    @Override
    public DimensionType getType() {
        return DimensionType.NETHER;
    }
}

