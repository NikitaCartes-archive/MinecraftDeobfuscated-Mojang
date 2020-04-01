package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ConfiguredWorldCarver<WC extends CarverConfiguration> {
	public final WorldCarver<WC> worldCarver;
	public final WC config;

	public ConfiguredWorldCarver(WorldCarver<WC> worldCarver, WC carverConfiguration) {
		this.worldCarver = worldCarver;
		this.config = carverConfiguration;
	}

	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("name"),
					dynamicOps.createString(Registry.CARVER.getKey(this.worldCarver).toString()),
					dynamicOps.createString("config"),
					this.config.serialize(dynamicOps).getValue()
				)
			)
		);
	}

	public boolean isStartChunk(Random random, int i, int j) {
		return this.worldCarver.isStartChunk(random, i, j, this.config);
	}

	public boolean carve(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, Random random, int i, int j, int k, int l, int m, BitSet bitSet) {
		return this.worldCarver.carve(chunkAccess, function, random, i, j, k, l, m, bitSet, this.config);
	}
}
