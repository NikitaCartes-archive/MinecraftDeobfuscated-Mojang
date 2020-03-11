package net.minecraft.world.level.biome;

public class TheEndBiomeSourceSettings implements BiomeSourceSettings {
	private final long seed;

	public TheEndBiomeSourceSettings(long l) {
		this.seed = l;
	}

	public long getSeed() {
		return this.seed;
	}
}
