/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeZoomer;

public enum NearestNeighborBiomeZoomer implements BiomeZoomer
{
    INSTANCE;


    @Override
    public Biome getBiome(long l, int i, int j, int k, BiomeManager.NoiseBiomeSource noiseBiomeSource) {
        return noiseBiomeSource.getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(j), QuartPos.fromBlock(k));
    }
}

