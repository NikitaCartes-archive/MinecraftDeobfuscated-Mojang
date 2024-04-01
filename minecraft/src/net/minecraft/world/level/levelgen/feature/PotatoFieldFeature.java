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
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class PotatoFieldFeature extends Feature<NoneFeatureConfiguration> {
	public PotatoFieldFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		RandomSource randomSource = featurePlaceContext.random();
		if (!this.isValidPlacementLocation(worldGenLevel, blockPos)) {
			return false;
		} else {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				if (!this.isValidPlacementLocation(worldGenLevel, blockPos.relative(direction))) {
					return false;
				}
			}

			double d = worldGenLevel.getLevel()
				.getChunkSource()
				.randomState()
				.router()
				.continents()
				.compute(new DensityFunction.SinglePointContext(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
			Direction direction2 = Direction.from2DDataValue((int)((d + 1.0) * 5.0));
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			if (randomSource.nextInt(8) == 0) {
				worldGenLevel.setBlock(blockPos.below(), Blocks.POTATO_FENCE.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)), 2);
				worldGenLevel.setBlock(blockPos, Blocks.POTATO_FENCE.defaultBlockState(), 2);
				worldGenLevel.setBlock(blockPos.above(), Blocks.LANTERN.defaultBlockState(), 2);
			} else {
				worldGenLevel.setBlock(blockPos.below(), Blocks.WATER.defaultBlockState(), 2);
			}

			worldGenLevel.setBlock(blockPos.below(2), Blocks.TERREDEPOMME.defaultBlockState(), 2);
			BoundingBox boundingBox = new BoundingBox(blockPos.below()).inflatedBy(4, 1, 4);

			for (int i = 0; i < 2; i++) {
				mutableBlockPos.set(blockPos);
				float f = 1.0F;
				int j = 0;
				direction2 = direction2.getOpposite();

				while (randomSource.nextFloat() < f) {
					if (++j > 6) {
						break;
					}

					mutableBlockPos.move(direction2);
					if (!this.isValidPlacementLocation(worldGenLevel, mutableBlockPos)) {
						break;
					}

					f *= 0.8F;
					Function<BlockPos, BlockState> function = blockPosx -> Blocks.POTATOES
							.defaultBlockState()
							.setValue(CropBlock.AGE, Integer.valueOf(randomSource.nextInt(7)));
					Function<BlockPos, BlockState> function2 = blockPosx -> {
						BlockState blockState = Blocks.POISON_FARMLAND.defaultBlockState();
						return boundingBox.isInside(blockPosx) ? blockState.setValue(FarmBlock.MOISTURE, Integer.valueOf(7)) : blockState;
					};
					this.runRowOfPotatoes(worldGenLevel, randomSource, mutableBlockPos, direction2.getClockWise(), function2, function);
					Direction direction3 = direction2.getCounterClockWise();
					this.runRowOfPotatoes(worldGenLevel, randomSource, mutableBlockPos.relative(direction3), direction3, function2, function);
				}

				if (randomSource.nextInt(10) == 0) {
					mutableBlockPos.move(direction2);
					if (!this.isValidPlacementLocation(worldGenLevel, mutableBlockPos)) {
						break;
					}

					List<BlockPos> list = new ArrayList();
					Function<BlockPos, BlockState> function2 = blockPosx -> {
						list.add(blockPosx.immutable());
						return Blocks.POTATO_FENCE.defaultBlockState();
					};
					this.runRowOfPotatoes(
						worldGenLevel, randomSource, mutableBlockPos, direction2.getClockWise(), blockPosx -> Blocks.PEELGRASS_BLOCK.defaultBlockState(), function2
					);
					Direction direction3 = direction2.getCounterClockWise();
					this.runRowOfPotatoes(
						worldGenLevel, randomSource, mutableBlockPos.relative(direction3), direction3, blockPosx -> Blocks.PEELGRASS_BLOCK.defaultBlockState(), function2
					);

					for (BlockPos blockPos2 : list) {
						worldGenLevel.getChunk(blockPos2).markPosForPostprocessing(blockPos2);
					}
				}
			}

			Direction direction4 = direction2.getCounterClockWise();
			this.runRowOfPotatoes(
				worldGenLevel,
				randomSource,
				blockPos.relative(direction4),
				direction4,
				blockPosx -> Blocks.POISON_PATH.defaultBlockState(),
				blockPosx -> Blocks.AIR.defaultBlockState()
			);
			direction4 = direction4.getOpposite();
			this.runRowOfPotatoes(
				worldGenLevel,
				randomSource,
				blockPos.relative(direction4),
				direction4,
				blockPosx -> Blocks.POISON_PATH.defaultBlockState(),
				blockPosx -> Blocks.AIR.defaultBlockState()
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
		Function<BlockPos, BlockState> function2
	) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPos);
		int i = randomSource.nextInt(3, 15);

		for (int j = 0; j < i; j++) {
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
		return worldGenLevel.isEmptyBlock(blockPos) && worldGenLevel.getBlockState(blockPos.below()).is(Blocks.PEELGRASS_BLOCK);
	}
}
