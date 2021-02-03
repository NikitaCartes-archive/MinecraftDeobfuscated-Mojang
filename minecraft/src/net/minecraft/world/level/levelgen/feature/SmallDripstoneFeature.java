package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SmallDripstoneConfiguration;

public class SmallDripstoneFeature extends Feature<SmallDripstoneConfiguration> {
	public SmallDripstoneFeature(Codec<SmallDripstoneConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<SmallDripstoneConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		Random random = featurePlaceContext.random();
		SmallDripstoneConfiguration smallDripstoneConfiguration = featurePlaceContext.config();
		if (!DripstoneUtils.isEmptyOrWater(worldGenLevel, blockPos)) {
			return false;
		} else {
			int i = Mth.randomBetweenInclusive(random, 1, smallDripstoneConfiguration.maxPlacements);
			boolean bl = false;

			for (int j = 0; j < i; j++) {
				BlockPos blockPos2 = randomOffset(random, blockPos, smallDripstoneConfiguration);
				if (searchAndTryToPlaceDripstone(worldGenLevel, random, blockPos2, smallDripstoneConfiguration)) {
					bl = true;
				}
			}

			return bl;
		}
	}

	private static boolean searchAndTryToPlaceDripstone(
		WorldGenLevel worldGenLevel, Random random, BlockPos blockPos, SmallDripstoneConfiguration smallDripstoneConfiguration
	) {
		Direction direction = Direction.getRandom(random);
		Direction direction2 = random.nextBoolean() ? Direction.UP : Direction.DOWN;
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = 0; i < smallDripstoneConfiguration.emptySpaceSearchRadius; i++) {
			if (!DripstoneUtils.isEmptyOrWater(worldGenLevel, mutableBlockPos)) {
				return false;
			}

			if (tryToPlaceDripstone(worldGenLevel, random, mutableBlockPos, direction2, smallDripstoneConfiguration)) {
				return true;
			}

			if (tryToPlaceDripstone(worldGenLevel, random, mutableBlockPos, direction2.getOpposite(), smallDripstoneConfiguration)) {
				return true;
			}

			mutableBlockPos.move(direction);
		}

		return false;
	}

	private static boolean tryToPlaceDripstone(
		WorldGenLevel worldGenLevel, Random random, BlockPos blockPos, Direction direction, SmallDripstoneConfiguration smallDripstoneConfiguration
	) {
		if (!DripstoneUtils.isEmptyOrWater(worldGenLevel, blockPos)) {
			return false;
		} else {
			BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
			BlockState blockState = worldGenLevel.getBlockState(blockPos2);
			if (!DripstoneUtils.isDripstoneBase(blockState)) {
				return false;
			} else {
				createPatchOfDripstoneBlocks(worldGenLevel, random, blockPos2);
				int i = random.nextFloat() < smallDripstoneConfiguration.chanceOfTallerDripstone
						&& DripstoneUtils.isEmptyOrWater(worldGenLevel, blockPos.relative(direction))
					? 2
					: 1;
				DripstoneUtils.growPointedDripstone(worldGenLevel, blockPos, direction, i, false);
				return true;
			}
		}
	}

	private static void createPatchOfDripstoneBlocks(WorldGenLevel worldGenLevel, Random random, BlockPos blockPos) {
		DripstoneUtils.placeDripstoneBlockIfPossible(worldGenLevel, blockPos);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (!(random.nextFloat() < 0.3F)) {
				BlockPos blockPos2 = blockPos.relative(direction);
				DripstoneUtils.placeDripstoneBlockIfPossible(worldGenLevel, blockPos2);
				if (!random.nextBoolean()) {
					BlockPos blockPos3 = blockPos2.relative(Direction.getRandom(random));
					DripstoneUtils.placeDripstoneBlockIfPossible(worldGenLevel, blockPos3);
					if (!random.nextBoolean()) {
						BlockPos blockPos4 = blockPos3.relative(Direction.getRandom(random));
						DripstoneUtils.placeDripstoneBlockIfPossible(worldGenLevel, blockPos4);
					}
				}
			}
		}
	}

	private static BlockPos randomOffset(Random random, BlockPos blockPos, SmallDripstoneConfiguration smallDripstoneConfiguration) {
		return blockPos.offset(
			Mth.randomBetweenInclusive(random, -smallDripstoneConfiguration.maxOffsetFromOrigin, smallDripstoneConfiguration.maxOffsetFromOrigin),
			Mth.randomBetweenInclusive(random, -smallDripstoneConfiguration.maxOffsetFromOrigin, smallDripstoneConfiguration.maxOffsetFromOrigin),
			Mth.randomBetweenInclusive(random, -smallDripstoneConfiguration.maxOffsetFromOrigin, smallDripstoneConfiguration.maxOffsetFromOrigin)
		);
	}
}
