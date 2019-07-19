/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.SoundEngine;

@Environment(value=EnvType.CLIENT)
public interface Weighted<T> {
    public int getWeight();

    public T getSound();

    public void preloadIfRequired(SoundEngine var1);
}

