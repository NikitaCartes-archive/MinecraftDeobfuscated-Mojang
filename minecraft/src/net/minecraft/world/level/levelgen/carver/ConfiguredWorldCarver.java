package net.minecraft.world.level.levelgen.carver;

import java.util.BitSet;
import java.util.Random;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ConfiguredWorldCarver<WC extends CarverConfiguration> {
	public final WorldCarver<WC> worldCarver;
	public final WC config;

	public ConfiguredWorldCarver(WorldCarver<WC> worldCarver, WC carverConfiguration) {
		this.worldCarver = worldCarver;
		this.config = carverConfiguration;
	}

	public boolean isStartChunk(Random random, int i, int j) {
		return this.worldCarver.isStartChunk(random, i, j, this.config);
	}

	public boolean carve(ChunkAccess chunkAccess, Random random, int i, int j, int k, int l, int m, BitSet bitSet) {
		return this.worldCarver.carve(chunkAccess, random, i, j, k, l, m, bitSet, this.config);
	}
}
