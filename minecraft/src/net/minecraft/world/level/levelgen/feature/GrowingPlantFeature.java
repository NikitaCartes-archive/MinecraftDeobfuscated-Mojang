package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.GrowingPlantConfiguration;

public class GrowingPlantFeature extends Feature<GrowingPlantConfiguration> {
	public GrowingPlantFeature(Codec<GrowingPlantConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<GrowingPlantConfiguration> featurePlaceContext) {
		LevelAccessor levelAccessor = featurePlaceContext.level();
		GrowingPlantConfiguration growingPlantConfiguration = featurePlaceContext.config();
		Random random = featurePlaceContext.random();
		int i = ((IntProvider)growingPlantConfiguration.heightDistribution.getRandomValue(random).orElseThrow(IllegalStateException::new)).sample(random);
		BlockPos.MutableBlockPos mutableBlockPos = featurePlaceContext.origin().mutable();
		BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.mutable().move(growingPlantConfiguration.direction);
		BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);

		for (int j = 1; j <= i; j++) {
			BlockState blockState2 = blockState;
			blockState = levelAccessor.getBlockState(mutableBlockPos2);
			if (blockState2.isAir() || growingPlantConfiguration.allowWater && blockState2.getFluidState().is(FluidTags.WATER)) {
				if (j == i || !blockState.isAir()) {
					levelAccessor.setBlock(mutableBlockPos, growingPlantConfiguration.headProvider.getState(random, mutableBlockPos), 2);
					break;
				}

				levelAccessor.setBlock(mutableBlockPos, growingPlantConfiguration.bodyProvider.getState(random, mutableBlockPos), 2);
			}

			mutableBlockPos2.move(growingPlantConfiguration.direction);
			mutableBlockPos.move(growingPlantConfiguration.direction);
		}

		return true;
	}
}
