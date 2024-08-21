package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class WorldGenerationContext {
	private final int minY;
	private final int height;

	public WorldGenerationContext(ChunkGenerator chunkGenerator, LevelHeightAccessor levelHeightAccessor) {
		this.minY = Math.max(levelHeightAccessor.getMinY(), chunkGenerator.getMinY());
		this.height = Math.min(levelHeightAccessor.getHeight(), chunkGenerator.getGenDepth());
	}

	public int getMinGenY() {
		return this.minY;
	}

	public int getGenDepth() {
		return this.height;
	}
}
