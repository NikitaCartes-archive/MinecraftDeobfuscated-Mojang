/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class AmbientMoodSettings {
    public static final AmbientMoodSettings LEGACY_CAVE_SETTINGS = new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0);
    private SoundEvent soundEvent;
    private int tickDelay;
    private int blockSearchExtent;
    private double soundPositionOffset;

    public AmbientMoodSettings(SoundEvent soundEvent, int i, int j, double d) {
        this.soundEvent = soundEvent;
        this.tickDelay = i;
        this.blockSearchExtent = j;
        this.soundPositionOffset = d;
    }

    @Environment(value=EnvType.CLIENT)
    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    @Environment(value=EnvType.CLIENT)
    public int getTickDelay() {
        return this.tickDelay;
    }

    @Environment(value=EnvType.CLIENT)
    public int getBlockSearchExtent() {
        return this.blockSearchExtent;
    }

    @Environment(value=EnvType.CLIENT)
    public double getSoundPositionOffset() {
        return this.soundPositionOffset;
    }
}

