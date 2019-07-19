/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.WeighedSoundEvents;

@Environment(value=EnvType.CLIENT)
public interface SoundEventListener {
    public void onPlaySound(SoundInstance var1, WeighedSoundEvents var2);
}

