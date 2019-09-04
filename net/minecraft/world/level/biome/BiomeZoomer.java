/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;

public interface BiomeZoomer {
    public Biome getBiome(long var1, int var3, int var4, int var5, BiomeManager.NoiseBiomeSource var6);
}

