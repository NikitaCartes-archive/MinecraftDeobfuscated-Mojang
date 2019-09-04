package net.minecraft.world.level.biome;

import net.minecraft.core.BlockPos;

public class BiomeManager {
	private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
	private final long biomeZoomSeed;
	private final BiomeZoomer zoomer;

	public BiomeManager(BiomeManager.NoiseBiomeSource noiseBiomeSource, long l, BiomeZoomer biomeZoomer) {
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

	public interface NoiseBiomeSource {
		Biome getNoiseBiome(int i, int j, int k);
	}
}
