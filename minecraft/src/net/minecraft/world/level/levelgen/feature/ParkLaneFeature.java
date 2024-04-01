package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ParkLaneFeature extends Feature<NoneFeatureConfiguration> {
	public ParkLaneFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		RandomSource randomSource = featurePlaceContext.random();
		if (worldGenLevel.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, blockPos).getY() <= blockPos.getY() + 2) {
			return false;
		} else if (!this.isValidPlacementLocation(worldGenLevel, blockPos)) {
			return false;
		} else {
			Direction direction = Direction.from2DDataValue(randomSource.nextInt(4));
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			worldGenLevel.setBlock(blockPos.below(), Blocks.POISON_PATH.defaultBlockState(), 2);
			int i = randomSource.nextInt(6, 12);
			List<BlockPos> list = new ArrayList();

			for (int j = 0; j < 2; j++) {
				mutableBlockPos.set(blockPos);
				float f = 1.0F;
				int k = 0;
				direction = direction.getOpposite();

				while (randomSource.nextFloat() < f) {
					if (++k > 2) {
						break;
					}

					mutableBlockPos.move(direction);
					if (!this.isValidPlacementLocation(worldGenLevel, mutableBlockPos)) {
						break;
					}

					f *= 0.8F;
					Function<BlockPos, BlockState> function = blockPosx -> {
						list.add(blockPosx.immutable());
						return Blocks.AIR.defaultBlockState();
					};
					Function<BlockPos, BlockState> function2 = blockPosx -> Blocks.POISON_PATH.defaultBlockState();
					this.runRowOfPotatoes(worldGenLevel, randomSource, mutableBlockPos, direction.getClockWise(), function2, function, i);
					Direction direction2 = direction.getCounterClockWise();
					this.runRowOfPotatoes(worldGenLevel, randomSource, mutableBlockPos.relative(direction2), direction2, function2, function, i);
				}

				if (randomSource.nextInt(2) == 0) {
					mutableBlockPos.move(direction);
					if (this.isValidPlacementLocation(worldGenLevel, mutableBlockPos)) {
						Function<BlockPos, BlockState> function = blockPosx -> {
							list.add(blockPosx.immutable());
							if (randomSource.nextInt(10) == 0) {
								worldGenLevel.setBlock(blockPosx.above(), Blocks.LANTERN.defaultBlockState(), 3);
							}

							return Blocks.POTATO_FENCE.defaultBlockState();
						};
						this.runRowOfPotatoes(
							worldGenLevel, randomSource, mutableBlockPos, direction.getClockWise(), blockPosx -> Blocks.PEELGRASS_BLOCK.defaultBlockState(), function, i
						);
						Direction direction3 = direction.getCounterClockWise();
						this.runRowOfPotatoes(
							worldGenLevel, randomSource, mutableBlockPos.relative(direction3), direction3, blockPosx -> Blocks.PEELGRASS_BLOCK.defaultBlockState(), function, i
						);
					}
				}
			}

			for (BlockPos blockPos2 : list) {
				worldGenLevel.getChunk(blockPos2).markPosForPostprocessing(blockPos2);
			}

			Direction direction4 = direction.getCounterClockWise();
			this.runRowOfPotatoes(
				worldGenLevel,
				randomSource,
				blockPos.relative(direction4),
				direction4,
				blockPosx -> Blocks.POISON_PATH.defaultBlockState(),
				blockPosx -> Blocks.AIR.defaultBlockState(),
				i
			);
			direction4 = direction4.getOpposite();
			this.runRowOfPotatoes(
				worldGenLevel,
				randomSource,
				blockPos.relative(direction4),
				direction4,
				blockPosx -> Blocks.POISON_PATH.defaultBlockState(),
				blockPosx -> Blocks.AIR.defaultBlockState(),
				i
			);
			return false;
		}
	}

	private void runRowOfPotatoes(
		WorldGenLevel worldGenLevel,
		RandomSource randomSource,
		BlockPos blockPos,
		Direction direction,
		Function<BlockPos, BlockState> function,
		Function<BlockPos, BlockState> function2,
		int i
	) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPos);
		int j = i + randomSource.nextInt(3);

		for (int k = 0; k < j; k++) {
			if (!this.isValidPlacementLocation(worldGenLevel, mutableBlockPos)) {
				mutableBlockPos.move(Direction.UP);
				if (!this.isValidPlacementLocation(worldGenLevel, mutableBlockPos)) {
					mutableBlockPos.move(Direction.DOWN, 2);
					if (!this.isValidPlacementLocation(worldGenLevel, mutableBlockPos)) {
						break;
					}
				}
			}

			worldGenLevel.setBlock(mutableBlockPos.below(), (BlockState)function.apply(mutableBlockPos.below()), 3);
			worldGenLevel.setBlock(mutableBlockPos, (BlockState)function2.apply(mutableBlockPos), 3);
			mutableBlockPos.move(direction);
		}
	}

	private boolean isValidPlacementLocation(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		return (worldGenLevel.isEmptyBlock(blockPos) || worldGenLevel.getBlockState(blockPos).is(Blocks.POTATO_FENCE))
			&& worldGenLevel.getBlockState(blockPos.below()).is(Blocks.PEELGRASS_BLOCK);
	}
}
