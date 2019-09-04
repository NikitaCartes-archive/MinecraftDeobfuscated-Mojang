/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeZoomer;

public class BiomeManager {
    private final NoiseBiomeSource noiseBiomeSource;
    private final long biomeZoomSeed;
    private final BiomeZoomer zoomer;

    public BiomeManager(NoiseBiomeSource noiseBiomeSource, long l, BiomeZoomer biomeZoomer) {
        this.noiseBiomeSource = noiseBiomeSource;
        this.biomeZoomSeed = l;
        this.zoomer = biomeZoomer;
    }

    public BiomeManager withDifferentSource(BiomeSource biomeSource) {
        return new BiomeManager(biomeSource, this.biomeZoomSeed, this.zoomer);
    }

    public Biome getBiome(BlockPos blockPos) {
        return this.zoomer.getBiome(this.biomeZoomSeed, blockPos.getX(), blockPos.getY(), blockPos.getZ(), this.noiseBiomeSource);
    }

    public static interface NoiseBiomeSource {
        public Biome getNoiseBiome(int var1, int var2, int var3);
    }
}

