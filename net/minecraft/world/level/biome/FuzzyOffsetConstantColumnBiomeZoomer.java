/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;

public enum FuzzyOffsetConstantColumnBiomeZoomer implements BiomeZoomer
{
    INSTANCE;


    @Override
    public Biome getBiome(long l, int i, int j, int k, BiomeManager.NoiseBiomeSource noiseBiomeSource) {
        return FuzzyOffsetBiomeZoomer.INSTANCE.getBiome(l, i, 0, k, noiseBiomeSource);
    }
}

