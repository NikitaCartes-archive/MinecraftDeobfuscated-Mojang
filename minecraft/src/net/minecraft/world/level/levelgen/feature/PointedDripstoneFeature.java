package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;

public class PointedDripstoneFeature extends Feature<PointedDripstoneConfiguration> {
	public PointedDripstoneFeature(Codec<PointedDripstoneConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<PointedDripstoneConfiguration> featurePlaceContext) {
		LevelAccessor levelAccessor = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		RandomSource randomSource = featurePlaceContext.random();
		PointedDripstoneConfiguration pointedDripstoneConfiguration = featurePlaceContext.config();
		Optional<Direction> optional = getTipDirection(levelAccessor, blockPos, randomSource);
		if (optional.isEmpty()) {
			return false;
		} else {
			BlockPos blockPos2 = blockPos.relative(((Direction)optional.get()).getOpposite());
			createPatchOfDripstoneBlocks(levelAccessor, randomSource, blockPos2, pointedDripstoneConfiguration);
			int i = randomSource.nextFloat() < pointedDripstoneConfiguration.chanceOfTallerDripstone
					&& DripstoneUtils.isEmptyOrWater(levelAccessor.getBlockState(blockPos.relative((Direction)optional.get())))
				? 2
				: 1;
			DripstoneUtils.growPointedDripstone(levelAccessor, blockPos, (Direction)optional.get(), i, false);
			return true;
		}
	}

	private static Optional<Direction> getTipDirection(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource) {
		boolean bl = DripstoneUtils.isDripstoneBase(levelAccessor.getBlockState(blockPos.above()));
		boolean bl2 = DripstoneUtils.isDripstoneBase(levelAccessor.getBlockState(blockPos.below()));
		if (bl && bl2) {
			return Optional.of(randomSource.nextBoolean() ? Direction.DOWN : Direction.UP);
		} else if (bl) {
			return Optional.of(Direction.DOWN);
		} else {
			return bl2 ? Optional.of(Direction.UP) : Optional.empty();
		}
	}

	private static void createPatchOfDripstoneBlocks(
		LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, PointedDripstoneConfiguration pointedDripstoneConfiguration
	) {
		DripstoneUtils.placeDripstoneBlockIfPossible(levelAccessor, blockPos);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (!(randomSource.nextFloat() > pointedDripstoneConfiguration.chanceOfDirectionalSpread)) {
				BlockPos blockPos2 = blockPos.relative(direction);
				DripstoneUtils.placeDripstoneBlockIfPossible(levelAccessor, blockPos2);
				if (!(randomSource.nextFloat() > pointedDripstoneConfiguration.chanceOfSpreadRadius2)) {
					BlockPos blockPos3 = blockPos2.relative(Direction.getRandom(randomSource));
					DripstoneUtils.placeDripstoneBlockIfPossible(levelAccessor, blockPos3);
					if (!(randomSource.nextFloat() > pointedDripstoneConfiguration.chanceOfSpreadRadius3)) {
						BlockPos blockPos4 = blockPos3.relative(Direction.getRandom(randomSource));
						DripstoneUtils.placeDripstoneBlockIfPossible(levelAccessor, blockPos4);
					}
				}
			}
		}
	}
}
