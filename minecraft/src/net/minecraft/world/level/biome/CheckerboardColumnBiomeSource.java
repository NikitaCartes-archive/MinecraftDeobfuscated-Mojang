package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableSet;

public class CheckerboardColumnBiomeSource extends BiomeSource {
	private final Biome[] allowedBiomes;
	private final int bitShift;

	public CheckerboardColumnBiomeSource(CheckerboardBiomeSourceSettings checkerboardBiomeSourceSettings) {
		super(ImmutableSet.copyOf(checkerboardBiomeSourceSettings.getAllowedBiomes()));
		this.allowedBiomes = checkerboardBiomeSourceSettings.getAllowedBiomes();
		this.bitShift = checkerboardBiomeSourceSettings.getSize() + 2;
	}

	@Override
	public Biome getNoiseBiome(int i, int j, int k) {
		return this.allowedBiomes[Math.floorMod((i >> this.bitShift) + (k >> this.bitShift), this.allowedBiomes.length)];
	}
}
