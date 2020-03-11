package net.minecraft.world.level.biome;

public class CheckerboardBiomeSourceSettings implements BiomeSourceSettings {
	private Biome[] allowedBiomes = new Biome[]{Biomes.PLAINS};
	private int size = 1;

	public CheckerboardBiomeSourceSettings(long l) {
	}

	public CheckerboardBiomeSourceSettings setAllowedBiomes(Biome[] biomes) {
		this.allowedBiomes = biomes;
		return this;
	}

	public CheckerboardBiomeSourceSettings setSize(int i) {
		this.size = i;
		return this;
	}

	public Biome[] getAllowedBiomes() {
		return this.allowedBiomes;
	}

	public int getSize() {
		return this.size;
	}
}
