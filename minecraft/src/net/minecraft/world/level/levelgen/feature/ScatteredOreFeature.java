package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class ScatteredOreFeature extends Feature<OreConfiguration> {
	private static final int MAX_DIST_FROM_ORIGIN = 7;

	ScatteredOreFeature(Codec<OreConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<OreConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		Random random = featurePlaceContext.random();
		OreConfiguration oreConfiguration = featurePlaceContext.config();
		BlockPos blockPos = featurePlaceContext.origin();
		int i = random.nextInt(oreConfiguration.size + 1);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int j = 0; j < i; j++) {
			this.offsetTargetPos(mutableBlockPos, random, blockPos, Math.min(j, 7));
			BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);

			for (OreConfiguration.TargetBlockState targetBlockState : oreConfiguration.targetStates) {
				if (OreFeature.canPlaceOre(blockState, worldGenLevel::getBlockState, random, oreConfiguration, targetBlockState, mutableBlockPos)) {
					worldGenLevel.setBlock(mutableBlockPos, targetBlockState.state, 2);
					break;
				}
			}
		}

		return true;
	}

	private void offsetTargetPos(BlockPos.MutableBlockPos mutableBlockPos, Random random, BlockPos blockPos, int i) {
		int j = this.getRandomPlacementInOneAxisRelativeToOrigin(random, i);
		int k = this.getRandomPlacementInOneAxisRelativeToOrigin(random, i);
		int l = this.getRandomPlacementInOneAxisRelativeToOrigin(random, i);
		mutableBlockPos.setWithOffset(blockPos, j, k, l);
	}

	private int getRandomPlacementInOneAxisRelativeToOrigin(Random random, int i) {
		return Math.round((random.nextFloat() - random.nextFloat()) * (float)i);
	}
}
