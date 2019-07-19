package net.minecraft.world.level.levelgen;

public class NetherGeneratorSettings extends ChunkGeneratorSettings {
	@Override
	public int getBedrockFloorPosition() {
		return 0;
	}

	@Override
	public int getBedrockRoofPosition() {
		return 127;
	}
}
