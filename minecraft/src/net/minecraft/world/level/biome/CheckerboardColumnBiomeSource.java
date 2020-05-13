package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class CheckerboardColumnBiomeSource extends BiomeSource {
	private final Biome[] allowedBiomes;
	private final int bitShift;

	public CheckerboardColumnBiomeSource(Biome[] biomes, int i) {
		super(ImmutableSet.copyOf(biomes));
		this.allowedBiomes = biomes;
		this.bitShift = i + 2;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public BiomeSource withSeed(long l) {
		return this;
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		return this.allowedBiomes[Math.floorMod((i >> this.bitShift) + (k >> this.bitShift), this.allowedBiomes.length)];
	}
}
