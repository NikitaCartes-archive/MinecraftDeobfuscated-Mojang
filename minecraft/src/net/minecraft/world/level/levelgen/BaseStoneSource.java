package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.block.state.BlockState;

public interface BaseStoneSource {
	BlockState getBaseStone(int i, int j, int k, NoiseGeneratorSettings noiseGeneratorSettings);
}
