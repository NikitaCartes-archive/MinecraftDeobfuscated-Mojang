package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;

public class BastionFeature extends JigsawFeature {
	public BastionFeature(Codec<JigsawConfiguration> codec) {
		super(codec, 33, false, false);
	}

	protected boolean isFeatureChunk(
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		long l,
		WorldgenRandom worldgenRandom,
		int i,
		int j,
		Biome biome,
		ChunkPos chunkPos,
		JigsawConfiguration jigsawConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		return worldgenRandom.nextInt(5) >= 2;
	}
}
