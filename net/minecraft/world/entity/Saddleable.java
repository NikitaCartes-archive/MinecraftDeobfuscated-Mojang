/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

public interface Saddleable {
    public boolean isSaddleable();

    public void equipSaddle(@Nullable SoundSource var1);

    public boolean isSaddled();
}

