package net.minecraft.world.level.levelgen.carver;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class CarvingContext extends WorldGenerationContext {
	public CarvingContext(ChunkGenerator chunkGenerator, LevelHeightAccessor levelHeightAccessor) {
		super(chunkGenerator, levelHeightAccessor);
	}
}
