package net.minecraft.world.level.levelgen;

public class OverworldGeneratorSettings extends ChunkGeneratorSettings {
	private final int biomeSize = 4;
	private final int riverSize = 4;
	private final int fixedBiome = -1;
	private final int seaLevel = 63;

	public int getBiomeSize() {
		return 4;
	}

	public int getRiverSize() {
		return 4;
	}

	public int getFixedBiome() {
		return -1;
	}

	@Override
	public int getBedrockFloorPosition() {
		return 0;
	}
}
