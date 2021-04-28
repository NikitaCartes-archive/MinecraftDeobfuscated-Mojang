/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

@FunctionalInterface
public interface NoiseModifier {
    public static final NoiseModifier PASSTHROUGH = (d, i, j, k) -> d;

    public double modifyNoise(double var1, int var3, int var4, int var5);
}

