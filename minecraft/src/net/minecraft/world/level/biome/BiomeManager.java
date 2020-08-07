package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class BiomeManager {
	private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
	private final long biomeZoomSeed;
	private final BiomeZoomer zoomer;

	public BiomeManager(BiomeManager.NoiseBiomeSource noiseBiomeSource, long l, BiomeZoomer biomeZoomer) {
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

	@Environment(EnvType.CLIENT)
	public Biome getNoiseBiomeAtPosition(double d, double e, double f) {
		int i = Mth.floor(d) >> 2;
		int j = Mth.floor(e) >> 2;
		int k = Mth.floor(f) >> 2;
		return this.getNoiseBiomeAtQuart(i, j, k);
	}

	@Environment(EnvType.CLIENT)
	public Biome getNoiseBiomeAtPosition(BlockPos blockPos) {
		int i = blockPos.getX() >> 2;
		int j = blockPos.getY() >> 2;
		int k = blockPos.getZ() >> 2;
		return this.getNoiseBiomeAtQuart(i, j, k);
	}

	@Environment(EnvType.CLIENT)
	public Biome getNoiseBiomeAtQuart(int i, int j, int k) {
		return this.noiseBiomeSource.getNoiseBiome(i, j, k);
	}

	public interface NoiseBiomeSource {
		Biome getNoiseBiome(int i, int j, int k);
	}
}
