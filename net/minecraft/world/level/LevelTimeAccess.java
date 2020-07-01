/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.dimension.DimensionType;

public interface LevelTimeAccess
extends LevelReader {
    public long dayTime();

    default public float getMoonBrightness() {
        return DimensionType.MOON_BRIGHTNESS_PER_PHASE[this.dimensionType().moonPhase(this.dayTime())];
    }

    default public float getTimeOfDay(float f) {
        return this.dimensionType().timeOfDay(this.dayTime());
    }

    @Environment(value=EnvType.CLIENT)
    default public int getMoonPhase() {
        return this.dimensionType().moonPhase(this.dayTime());
    }
}

