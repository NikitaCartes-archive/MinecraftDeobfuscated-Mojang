package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class SimpleBlockFeature extends Feature<SimpleBlockConfiguration> {
	public SimpleBlockFeature(Codec<SimpleBlockConfiguration> codec) {
		super(codec);
	}

	public boolean place(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BlockPos blockPos,
		SimpleBlockConfiguration simpleBlockConfiguration
	) {
		if (simpleBlockConfiguration.placeOn.contains(worldGenLevel.getBlockState(blockPos.below()))
			&& simpleBlockConfiguration.placeIn.contains(worldGenLevel.getBlockState(blockPos))
			&& simpleBlockConfiguration.placeUnder.contains(worldGenLevel.getBlockState(blockPos.above()))) {
			worldGenLevel.setBlock(blockPos, simpleBlockConfiguration.toPlace, 2);
			return true;
		} else {
			return false;
		}
	}
}
