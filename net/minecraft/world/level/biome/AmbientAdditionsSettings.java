/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvent;

public class AmbientAdditionsSettings {
    private SoundEvent soundEvent;
    private double tickChance;

    public AmbientAdditionsSettings(SoundEvent soundEvent, double d) {
        this.soundEvent = soundEvent;
        this.tickChance = d;
    }

    @Environment(value=EnvType.CLIENT)
    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    @Environment(value=EnvType.CLIENT)
    public double getTickChance() {
        return this.tickChance;
    }
}

