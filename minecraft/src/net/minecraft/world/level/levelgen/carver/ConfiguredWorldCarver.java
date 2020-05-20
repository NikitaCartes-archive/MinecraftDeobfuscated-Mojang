package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ConfiguredWorldCarver<WC extends CarverConfiguration> {
	public static final Codec<ConfiguredWorldCarver<?>> CODEC = Registry.CARVER
		.dispatch("name", configuredWorldCarver -> configuredWorldCarver.worldCarver, WorldCarver::configuredCodec);
	public final WorldCarver<WC> worldCarver;
	public final WC config;

	public ConfiguredWorldCarver(WorldCarver<WC> worldCarver, WC carverConfiguration) {
		this.worldCarver = worldCarver;
		this.config = carverConfiguration;
	}

	public boolean isStartChunk(Random random, int i, int j) {
		return this.worldCarver.isStartChunk(random, i, j, this.config);
	}

	public boolean carve(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, Random random, int i, int j, int k, int l, int m, BitSet bitSet) {
		return this.worldCarver.carve(chunkAccess, function, random, i, j, k, l, m, bitSet, this.config);
	}
}
