/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class Dimension {
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 0.25f, 0.5f, 0.75f};
    protected final Level level;
    private final DimensionType type;
    protected boolean ultraWarm;
    protected boolean hasCeiling;
    protected final float[] brightnessRamp = new float[16];
    private final float[] sunriseCol = new float[4];

    public Dimension(Level level, DimensionType dimensionType) {
        this.level = level;
        this.type = dimensionType;
        this.updateLightRamp();
    }

    protected void updateLightRamp() {
        float f = 0.0f;
        for (int i = 0; i <= 15; ++i) {
            float g = 1.0f - (float)i / 15.0f;
            this.brightnessRamp[i] = (1.0f - g) / (g * 3.0f + 1.0f) * 1.0f + 0.0f;
        }
    }

    public int getMoonPhase(long l) {
        return (int)(l / 24000L % 8L + 8L) % 8;
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public float[] getSunriseColor(float f, float g) {
        float h = 0.4f;
        float i = Mth.cos(f * ((float)Math.PI * 2)) - 0.0f;
        float j = -0.0f;
        if (i >= -0.4f && i <= 0.4f) {
            float k = (i - -0.0f) / 0.4f * 0.5f + 0.5f;
            float l = 1.0f - (1.0f - Mth.sin(k * (float)Math.PI)) * 0.99f;
            l *= l;
            this.sunriseCol[0] = k * 0.3f + 0.7f;
            this.sunriseCol[1] = k * k * 0.7f + 0.2f;
            this.sunriseCol[2] = k * k * 0.0f + 0.2f;
            this.sunriseCol[3] = l;
            return this.sunriseCol;
        }
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public float getCloudHeight() {
        return 128.0f;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean hasGround() {
        return true;
    }

    @Nullable
    public BlockPos getDimensionSpecificSpawn() {
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public double getClearColorScale() {
        if (this.level.getLevelData().getGeneratorType() == LevelType.FLAT) {
            return 1.0;
        }
        return 0.03125;
    }

    public boolean isUltraWarm() {
        return this.ultraWarm;
    }

    public boolean isHasSkyLight() {
        return this.type.hasSkyLight();
    }

    public boolean isHasCeiling() {
        return this.hasCeiling;
    }

    public float[] getBrightnessRamp() {
        return this.brightnessRamp;
    }

    public WorldBorder createWorldBorder() {
        return new WorldBorder();
    }

    public void saveData() {
    }

    public void tick() {
    }

    public abstract ChunkGenerator<?> createRandomLevelGenerator();

    @Nullable
    public abstract BlockPos getSpawnPosInChunk(ChunkPos var1, boolean var2);

    @Nullable
    public abstract BlockPos getValidSpawnPosition(int var1, int var2, boolean var3);

    public abstract float getTimeOfDay(long var1, float var3);

    public abstract boolean isNaturalDimension();

    @Environment(value=EnvType.CLIENT)
    public abstract Vec3 getFogColor(float var1, float var2);

    public abstract boolean mayRespawn();

    @Environment(value=EnvType.CLIENT)
    public abstract boolean isFoggyAt(int var1, int var2);

    public abstract DimensionType getType();
}

