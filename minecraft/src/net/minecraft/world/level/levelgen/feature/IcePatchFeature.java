package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
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
		RandomSource randomSource = featurePlaceContext.random();
		DiskConfiguration diskConfiguration = featurePlaceContext.config();
		BlockPos blockPos = featurePlaceContext.origin();

		while (worldGenLevel.isEmptyBlock(blockPos) && blockPos.getY() > worldGenLevel.getMinBuildHeight() + 2) {
			blockPos = blockPos.below();
		}

		return !featurePlaceContext.level().getBlockState(blockPos).is(diskConfiguration.canOriginReplace())
			? false
			: super.place(
				new FeaturePlaceContext<>(
					featurePlaceContext.topFeature(),
					worldGenLevel,
					featurePlaceContext.chunkGenerator(),
					featurePlaceContext.random(),
					blockPos,
					featurePlaceContext.config()
				)
			);
	}
}
