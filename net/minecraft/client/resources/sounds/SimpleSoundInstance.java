/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

@Environment(value=EnvType.CLIENT)
public class SimpleSoundInstance
extends AbstractSoundInstance {
    public SimpleSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float g, BlockPos blockPos) {
        this(soundEvent, soundSource, f, g, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
    }

    public static SimpleSoundInstance forUI(SoundEvent soundEvent, float f) {
        return SimpleSoundInstance.forUI(soundEvent, f, 0.25f);
    }

    public static SimpleSoundInstance forUI(SoundEvent soundEvent, float f, float g) {
        return new SimpleSoundInstance(soundEvent.getLocation(), SoundSource.MASTER, g, f, false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
    }

    public static SimpleSoundInstance forMusic(SoundEvent soundEvent) {
        return new SimpleSoundInstance(soundEvent.getLocation(), SoundSource.MUSIC, 1.0f, 1.0f, false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
    }

    public static SimpleSoundInstance forRecord(SoundEvent soundEvent, double d, double e, double f) {
        return new SimpleSoundInstance(soundEvent, SoundSource.RECORDS, 4.0f, 1.0f, false, 0, SoundInstance.Attenuation.LINEAR, d, e, f);
    }

    public static SimpleSoundInstance forLocalAmbience(SoundEvent soundEvent, float f, float g) {
        return new SimpleSoundInstance(soundEvent.getLocation(), SoundSource.AMBIENT, g, f, false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
    }

    public static SimpleSoundInstance forAmbientAddition(SoundEvent soundEvent) {
        return SimpleSoundInstance.forLocalAmbience(soundEvent, 1.0f, 1.0f);
    }

    public static SimpleSoundInstance forAmbientMood(SoundEvent soundEvent, double d, double e, double f) {
        return new SimpleSoundInstance(soundEvent, SoundSource.AMBIENT, 1.0f, 1.0f, false, 0, SoundInstance.Attenuation.LINEAR, d, e, f);
    }

    public SimpleSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float g, double d, double e, double h) {
        this(soundEvent, soundSource, f, g, false, 0, SoundInstance.Attenuation.LINEAR, d, e, h);
    }

    private SimpleSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float g, boolean bl, int i, SoundInstance.Attenuation attenuation, double d, double e, double h) {
        this(soundEvent.getLocation(), soundSource, f, g, bl, i, attenuation, d, e, h, false);
    }

    public SimpleSoundInstance(ResourceLocation resourceLocation, SoundSource soundSource, float f, float g, boolean bl, int i, SoundInstance.Attenuation attenuation, double d, double e, double h, boolean bl2) {
        super(resourceLocation, soundSource);
        this.volume = f;
        this.pitch = g;
        this.x = d;
        this.y = e;
        this.z = h;
        this.looping = bl;
        this.delay = i;
        this.attenuation = attenuation;
        this.relative = bl2;
    }
}

