package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ConfiguredWorldCarver<WC extends CarverConfiguration> {
	public static final MapCodec<ConfiguredWorldCarver<?>> DIRECT_CODEC = Registry.CARVER
		.dispatchMap(configuredWorldCarver -> configuredWorldCarver.worldCarver, WorldCarver::configuredCodec);
	public static final Codec<Supplier<ConfiguredWorldCarver<?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC);
	private final WorldCarver<WC> worldCarver;
	private final WC config;

	public ConfiguredWorldCarver(WorldCarver<WC> worldCarver, WC carverConfiguration) {
		this.worldCarver = worldCarver;
		this.config = carverConfiguration;
	}

	public WC config() {
		return this.config;
	}

	public boolean isStartChunk(Random random, int i, int j) {
		return this.worldCarver.isStartChunk(random, i, j, this.config);
	}

	public boolean carve(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, Random random, int i, int j, int k, int l, int m, BitSet bitSet) {
		return this.worldCarver.carve(chunkAccess, function, random, i, j, k, l, m, bitSet, this.config);
	}
}
