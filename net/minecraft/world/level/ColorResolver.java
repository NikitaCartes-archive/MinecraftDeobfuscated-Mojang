/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import net.minecraft.world.level.biome.Biome;

@FunctionalInterface
public interface ColorResolver {
    public int getColor(Biome var1, double var2, double var4);
}

