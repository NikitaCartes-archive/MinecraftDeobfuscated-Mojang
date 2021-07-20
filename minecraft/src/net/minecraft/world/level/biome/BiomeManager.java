package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public class BiomeManager {
	static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
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

	public Biome getNoiseBiome(ChunkPos chunkPos, int i) {
		return this.noiseBiomeSource.getNoiseBiome(chunkPos, i);
	}

	public interface NoiseBiomeSource {
		Biome getNoiseBiome(int i, int j, int k);

		default Biome getNoiseBiome(ChunkPos chunkPos, int i) {
			return this.getNoiseBiome(
				QuartPos.fromSection(chunkPos.x) + BiomeManager.CHUNK_CENTER_QUART, i / 4, QuartPos.fromSection(chunkPos.z) + BiomeManager.CHUNK_CENTER_QUART
			);
		}
	}
}
