package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class NoiseGeneratorSettings {
	private final ChunkGeneratorSettings structureSettings;
	protected BlockState defaultBlock = Blocks.STONE.defaultBlockState();
	protected BlockState defaultFluid = Blocks.WATER.defaultBlockState();

	public NoiseGeneratorSettings(ChunkGeneratorSettings chunkGeneratorSettings) {
		this.structureSettings = chunkGeneratorSettings;
	}

	public BlockState getDefaultBlock() {
		return this.defaultBlock;
	}

	public BlockState getDefaultFluid() {
		return this.defaultFluid;
	}

	public void setDefaultBlock(BlockState blockState) {
		this.defaultBlock = blockState;
	}

	public void setDefaultFluid(BlockState blockState) {
		this.defaultFluid = blockState;
	}

	public int getBedrockRoofPosition() {
		return 0;
	}

	public int getBedrockFloorPosition() {
		return 256;
	}

	public ChunkGeneratorSettings structureSettings() {
		return this.structureSettings;
	}
}
