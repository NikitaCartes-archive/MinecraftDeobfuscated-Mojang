package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
		Random random = featurePlaceContext.random();
		PointedDripstoneConfiguration pointedDripstoneConfiguration = featurePlaceContext.config();
		Optional<Direction> optional = getTipDirection(levelAccessor, blockPos, random);
		if (optional.isEmpty()) {
			return false;
		} else {
			BlockPos blockPos2 = blockPos.relative(((Direction)optional.get()).getOpposite());
			createPatchOfDripstoneBlocks(levelAccessor, random, blockPos2, pointedDripstoneConfiguration);
			int i = random.nextFloat() < pointedDripstoneConfiguration.chanceOfTallerDripstone
					&& DripstoneUtils.isEmptyOrWater(levelAccessor.getBlockState(blockPos.relative((Direction)optional.get())))
				? 2
				: 1;
			DripstoneUtils.growPointedDripstone(levelAccessor, blockPos, (Direction)optional.get(), i, false);
			return true;
		}
	}

	private static Optional<Direction> getTipDirection(LevelAccessor levelAccessor, BlockPos blockPos, Random random) {
		boolean bl = DripstoneUtils.isDripstoneBase(levelAccessor.getBlockState(blockPos.above()));
		boolean bl2 = DripstoneUtils.isDripstoneBase(levelAccessor.getBlockState(blockPos.below()));
		if (bl && bl2) {
			return Optional.of(random.nextBoolean() ? Direction.DOWN : Direction.UP);
		} else if (bl) {
			return Optional.of(Direction.DOWN);
		} else {
			return bl2 ? Optional.of(Direction.UP) : Optional.empty();
		}
	}

	private static void createPatchOfDripstoneBlocks(
		LevelAccessor levelAccessor, Random random, BlockPos blockPos, PointedDripstoneConfiguration pointedDripstoneConfiguration
	) {
		DripstoneUtils.placeDripstoneBlockIfPossible(levelAccessor, blockPos);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (!(random.nextFloat() > pointedDripstoneConfiguration.chanceOfDirectionalSpread)) {
				BlockPos blockPos2 = blockPos.relative(direction);
				DripstoneUtils.placeDripstoneBlockIfPossible(levelAccessor, blockPos2);
				if (!(random.nextFloat() > pointedDripstoneConfiguration.chanceOfSpreadRadius2)) {
					BlockPos blockPos3 = blockPos2.relative(Direction.getRandom(random));
					DripstoneUtils.placeDripstoneBlockIfPossible(levelAccessor, blockPos3);
					if (!(random.nextFloat() > pointedDripstoneConfiguration.chanceOfSpreadRadius3)) {
						BlockPos blockPos4 = blockPos3.relative(Direction.getRandom(random));
						DripstoneUtils.placeDripstoneBlockIfPossible(levelAccessor, blockPos4);
					}
				}
			}
		}
	}
}
