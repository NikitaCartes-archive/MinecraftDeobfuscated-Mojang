/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;

public abstract class Dimension {
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 0.25f, 0.5f, 0.75f};
    protected final Level level;
    private final DimensionType type;
    protected final float[] brightnessRamp = new float[16];

    public Dimension(Level level, DimensionType dimensionType, float f) {
        this.level = level;
        this.type = dimensionType;
        for (int i = 0; i <= 15; ++i) {
            float g = (float)i / 15.0f;
            float h = g / (4.0f - 3.0f * g);
            this.brightnessRamp[i] = Mth.lerp(f, h, 1.0f);
        }
    }

    public int getMoonPhase(long l) {
        return (int)(l / 24000L % 8L + 8L) % 8;
    }

    public float getBrightness(int i) {
        return this.brightnessRamp[i];
    }

    public abstract float getTimeOfDay(long var1, float var3);

    public WorldBorder createWorldBorder() {
        return new WorldBorder();
    }

    public abstract DimensionType getType();

    @Nullable
    public BlockPos getDimensionSpecificSpawn() {
        return null;
    }

    public void saveData(ServerLevelData serverLevelData) {
    }

    public void tick() {
    }

    @Nullable
    public abstract BlockPos getSpawnPosInChunk(long var1, ChunkPos var3, boolean var4);

    @Nullable
    public abstract BlockPos getValidSpawnPosition(long var1, int var3, int var4, boolean var5);

    public abstract boolean isNaturalDimension();

    public abstract boolean mayRespawn();
}

