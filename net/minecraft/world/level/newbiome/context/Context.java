/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.context;

import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

public interface Context {
    public int nextRandom(int var1);

    public ImprovedNoise getBiomeNoise();
}

