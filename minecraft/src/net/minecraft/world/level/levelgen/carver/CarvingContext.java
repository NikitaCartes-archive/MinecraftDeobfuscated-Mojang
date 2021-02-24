package net.minecraft.world.level.levelgen.carver;

import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class CarvingContext implements WorldGenerationContext {
	private final ChunkGenerator generator;

	public CarvingContext(ChunkGenerator chunkGenerator) {
		this.generator = chunkGenerator;
	}

	@Override
	public int getMinGenY() {
		return this.generator.getMinY();
	}

	@Override
	public int getGenDepth() {
		return this.generator.getGenDepth();
	}
}
