package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class IcePatchFeature extends BaseDiskFeature {
	public IcePatchFeature(Codec<DiskConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<DiskConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		ChunkGenerator chunkGenerator = featurePlaceContext.chunkGenerator();
		Random random = featurePlaceContext.random();
		DiskConfiguration diskConfiguration = featurePlaceContext.config();
		BlockPos blockPos = featurePlaceContext.origin();

		while (worldGenLevel.isEmptyBlock(blockPos) && blockPos.getY() > worldGenLevel.getMinBuildHeight() + 2) {
			blockPos = blockPos.below();
		}

		return !worldGenLevel.getBlockState(blockPos).is(Blocks.SNOW_BLOCK)
			? false
			: super.place(new FeaturePlaceContext<>(worldGenLevel, chunkGenerator, random, blockPos, diskConfiguration));
	}
}
