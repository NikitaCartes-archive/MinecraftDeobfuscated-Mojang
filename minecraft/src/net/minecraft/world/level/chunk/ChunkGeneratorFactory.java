package net.minecraft.world.level.chunk;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

interface ChunkGeneratorFactory<C extends ChunkGeneratorSettings, T extends ChunkGenerator<C>> {
	T create(LevelAccessor levelAccessor, BiomeSource biomeSource, C chunkGeneratorSettings);
}
