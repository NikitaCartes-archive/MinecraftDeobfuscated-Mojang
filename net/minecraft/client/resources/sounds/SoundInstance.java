/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface SoundInstance {
    public ResourceLocation getLocation();

    @Nullable
    public WeighedSoundEvents resolve(SoundManager var1);

    public Sound getSound();

    public SoundSource getSource();

    public boolean isLooping();

    public boolean isRelative();

    public int getDelay();

    public float getVolume();

    public float getPitch();

    public double getX();

    public double getY();

    public double getZ();

    public Attenuation getAttenuation();

    default public boolean canStartSilent() {
        return false;
    }

    default public boolean canPlaySound() {
        return true;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Attenuation {
        NONE,
        LINEAR;

    }
}

