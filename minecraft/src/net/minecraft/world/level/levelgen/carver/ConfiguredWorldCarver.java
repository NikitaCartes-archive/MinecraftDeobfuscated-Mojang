package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ConfiguredWorldCarver<WC extends CarverConfiguration> {
	public static final Codec<ConfiguredWorldCarver<?>> DIRECT_CODEC = Registry.CARVER
		.dispatch(configuredWorldCarver -> configuredWorldCarver.worldCarver, WorldCarver::configuredCodec);
	public static final Codec<Supplier<ConfiguredWorldCarver<?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC);
	public static final Codec<List<Supplier<ConfiguredWorldCarver<?>>>> LIST_CODEC = RegistryFileCodec.homogeneousList(
		Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC
	);
	private final WorldCarver<WC> worldCarver;
	private final WC config;

	public ConfiguredWorldCarver(WorldCarver<WC> worldCarver, WC carverConfiguration) {
		this.worldCarver = worldCarver;
		this.config = carverConfiguration;
	}

	public WC config() {
		return this.config;
	}

	public boolean isStartChunk(Random random) {
		return this.worldCarver.isStartChunk(this.config, random);
	}

	public boolean carve(
		CarvingContext carvingContext, ChunkAccess chunkAccess, Function<BlockPos, Biome> function, Random random, int i, ChunkPos chunkPos, BitSet bitSet
	) {
		return this.worldCarver.carve(carvingContext, this.config, chunkAccess, function, random, i, chunkPos, bitSet);
	}
}
