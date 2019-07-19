package net.minecraft.world.level.biome;

public class TheEndBiomeSourceSettings implements BiomeSourceSettings {
	private long seed;

	public TheEndBiomeSourceSettings setSeed(long l) {
		this.seed = l;
		return this;
	}

	public long getSeed() {
		return this.seed;
	}
}
