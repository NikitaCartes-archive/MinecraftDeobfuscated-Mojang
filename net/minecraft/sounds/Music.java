/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvent;

public class Music {
    private final SoundEvent event;
    private final int minDelay;
    private final int maxDelay;
    private final boolean replaceCurrentMusic;

    public Music(SoundEvent soundEvent, int i, int j, boolean bl) {
        this.event = soundEvent;
        this.minDelay = i;
        this.maxDelay = j;
        this.replaceCurrentMusic = bl;
    }

    @Environment(value=EnvType.CLIENT)
    public SoundEvent getEvent() {
        return this.event;
    }

    @Environment(value=EnvType.CLIENT)
    public int getMinDelay() {
        return this.minDelay;
    }

    @Environment(value=EnvType.CLIENT)
    public int getMaxDelay() {
        return this.maxDelay;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean replaceCurrentMusic() {
        return this.replaceCurrentMusic;
    }
}

