package net.minecraft.world.level.levelgen;

public class NetherGeneratorSettings extends NoiseGeneratorSettings {
	public NetherGeneratorSettings(ChunkGeneratorSettings chunkGeneratorSettings) {
		super(chunkGeneratorSettings);
		chunkGeneratorSettings.ruinedPortalSpacing = 25;
		chunkGeneratorSettings.ruinedPortalSeparation = 10;
	}

	@Override
	public int getBedrockFloorPosition() {
		return 0;
	}

	@Override
	public int getBedrockRoofPosition() {
		return 127;
	}
}
