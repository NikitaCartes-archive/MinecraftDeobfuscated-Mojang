package net.minecraft.world.level.levelgen;

import java.util.Random;

public class NetherGeneratorSettings extends ChunkGeneratorSettings {
	@Override
	public int getBedrockFloorPosition() {
		return 0;
	}

	@Override
	public int getBedrockRoofPosition() {
		return 127;
	}

	public NetherGeneratorSettings() {
	}

	public NetherGeneratorSettings(Random random) {
		this.defaultBlock = this.randomGroundBlock(random);
		this.defaultFluid = this.randomLiquidBlock(random);
	}
}
