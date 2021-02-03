package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;

public class SeaPickleFeature extends Feature<CountConfiguration> {
	public SeaPickleFeature(Codec<CountConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<CountConfiguration> featurePlaceContext) {
		int i = 0;
		Random random = featurePlaceContext.random();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		int j = featurePlaceContext.config().count().sample(random);

		for (int k = 0; k < j; k++) {
			int l = random.nextInt(8) - random.nextInt(8);
			int m = random.nextInt(8) - random.nextInt(8);
			int n = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX() + l, blockPos.getZ() + m);
			BlockPos blockPos2 = new BlockPos(blockPos.getX() + l, n, blockPos.getZ() + m);
			BlockState blockState = Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(random.nextInt(4) + 1));
			if (worldGenLevel.getBlockState(blockPos2).is(Blocks.WATER) && blockState.canSurvive(worldGenLevel, blockPos2)) {
				worldGenLevel.setBlock(blockPos2, blockState, 2);
				i++;
			}
		}

		return i > 0;
	}
}
