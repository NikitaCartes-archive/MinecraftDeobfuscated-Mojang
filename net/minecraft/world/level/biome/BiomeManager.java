/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeZoomer;

public class BiomeManager {
    static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
    private final NoiseBiomeSource noiseBiomeSource;
    private final long biomeZoomSeed;
    private final BiomeZoomer zoomer;

    public BiomeManager(NoiseBiomeSource noiseBiomeSource, long l, BiomeZoomer biomeZoomer) {
        this.noiseBiomeSource = noiseBiomeSource;
        this.biomeZoomSeed = l;
        this.zoomer = biomeZoomer;
    }

    public static long obfuscateSeed(long l) {
        return Hashing.sha256().hashLong(l).asLong();
    }

    public BiomeManager withDifferentSource(BiomeSource biomeSource) {
        return new BiomeManager(biomeSource, this.biomeZoomSeed, this.zoomer);
    }

    public Biome getBiome(BlockPos blockPos) {
        return this.zoomer.getBiome(this.biomeZoomSeed, blockPos.getX(), blockPos.getY(), blockPos.getZ(), this.noiseBiomeSource);
    }

    public Biome getNoiseBiomeAtPosition(double d, double e, double f) {
        int i = QuartPos.fromBlock(Mth.floor(d));
        int j = QuartPos.fromBlock(Mth.floor(e));
        int k = QuartPos.fromBlock(Mth.floor(f));
        return this.getNoiseBiomeAtQuart(i, j, k);
    }

    public Biome getNoiseBiomeAtPosition(BlockPos blockPos) {
        int i = QuartPos.fromBlock(blockPos.getX());
        int j = QuartPos.fromBlock(blockPos.getY());
        int k = QuartPos.fromBlock(blockPos.getZ());
        return this.getNoiseBiomeAtQuart(i, j, k);
    }

    public Biome getNoiseBiomeAtQuart(int i, int j, int k) {
        return this.noiseBiomeSource.getNoiseBiome(i, j, k);
    }

    public Biome getPrimaryBiomeAtChunk(ChunkPos chunkPos) {
        return this.noiseBiomeSource.getPrimaryBiome(chunkPos);
    }

    public static interface NoiseBiomeSource {
        public Biome getNoiseBiome(int var1, int var2, int var3);

        default public Biome getPrimaryBiome(ChunkPos chunkPos) {
            return this.getNoiseBiome(QuartPos.fromSection(chunkPos.x) + CHUNK_CENTER_QUART, 0, QuartPos.fromSection(chunkPos.z) + CHUNK_CENTER_QUART);
        }
    }
}

