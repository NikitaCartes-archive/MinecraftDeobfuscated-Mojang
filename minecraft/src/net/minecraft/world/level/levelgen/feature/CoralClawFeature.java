package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralClawFeature extends CoralFeature {
	public CoralClawFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	protected boolean placeFeature(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		if (!this.placeCoralBlock(levelAccessor, randomSource, blockPos, blockState)) {
			return false;
		} else {
			Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
			int i = randomSource.nextInt(2) + 2;
			List<Direction> list = Util.toShuffledList(Stream.of(direction, direction.getClockWise(), direction.getCounterClockWise()), randomSource);

			for (Direction direction2 : list.subList(0, i)) {
				BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
				int j = randomSource.nextInt(2) + 1;
				mutableBlockPos.move(direction2);
				int k;
				Direction direction3;
				if (direction2 == direction) {
					direction3 = direction;
					k = randomSource.nextInt(3) + 2;
				} else {
					mutableBlockPos.move(Direction.UP);
					Direction[] directions = new Direction[]{direction2, Direction.UP};
					direction3 = Util.getRandom(directions, randomSource);
					k = randomSource.nextInt(3) + 3;
				}

				for (int l = 0; l < j && this.placeCoralBlock(levelAccessor, randomSource, mutableBlockPos, blockState); l++) {
					mutableBlockPos.move(direction3);
				}

				mutableBlockPos.move(direction3.getOpposite());
				mutableBlockPos.move(Direction.UP);

				for (int l = 0; l < k; l++) {
					mutableBlockPos.move(direction);
					if (!this.placeCoralBlock(levelAccessor, randomSource, mutableBlockPos, blockState)) {
						break;
					}

					if (randomSource.nextFloat() < 0.25F) {
						mutableBlockPos.move(Direction.UP);
					}
				}
			}

			return true;
		}
	}
}
