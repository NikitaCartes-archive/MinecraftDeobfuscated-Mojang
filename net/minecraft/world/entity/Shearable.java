/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import net.minecraft.sounds.SoundSource;

public interface Shearable {
    public void shear(SoundSource var1);

    public boolean readyForShearing();
}

