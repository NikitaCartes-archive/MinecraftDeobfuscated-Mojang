package net.minecraft.world.level.levelgen.feature;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;

public class DripstoneUtilsFlex {
	protected static double getDripstoneHeight(double d, double e, double f, double g) {
		if (d < g) {
			d = g;
		}

		double h = 0.384;
		double i = d / e * 0.384;
		double j = 0.75 * Math.pow(i, 1.3333333333333333);
		double k = Math.pow(i, 0.6666666666666666);
		double l = 0.3333333333333333 * Math.log(i);
		double m = f * (j - k - l);
		m = Math.max(m, 0.0);
		return m / 0.384 * e;
	}

	protected static boolean isCircleMostlyEmbeddedInStone(WorldGenLevel worldGenLevel, BlockPos blockPos, int i) {
		if (isEmptyOrWaterOrLava(worldGenLevel, blockPos)) {
			return false;
		} else {
			float f = 6.0F;
			float g = 6.0F / (float)i;

			for (float h = 0.0F; h < (float) (Math.PI * 2); h += g) {
				int j = (int)(Mth.cos(h) * (float)i);
				int k = (int)(Mth.sin(h) * (float)i);
				if (isEmptyOrWaterOrLava(worldGenLevel, blockPos.offset(j, 0, k))) {
					return false;
				}
			}

			return true;
		}
	}

	protected static boolean isEmptyOrWater(LevelAccessor levelAccessor, BlockPos blockPos) {
		return levelAccessor.isStateAtPosition(blockPos, DripstoneUtilsFlex::isEmptyOrWater);
	}

	protected static boolean isEmptyOrWaterOrLava(LevelAccessor levelAccessor, BlockPos blockPos) {
		return levelAccessor.isStateAtPosition(blockPos, DripstoneUtilsFlex::isEmptyOrWaterOrLava);
	}

	protected static void buildBaseToTipColumn(PointedDripstoneBlock pointedDripstoneBlock, Direction direction, int i, boolean bl, Consumer<BlockState> consumer) {
		if (i >= 3) {
			consumer.accept(createPointedDripstone(pointedDripstoneBlock, direction, DripstoneThickness.BASE));

			for (int j = 0; j < i - 3; j++) {
				consumer.accept(createPointedDripstone(pointedDripstoneBlock, direction, DripstoneThickness.MIDDLE));
			}
		}

		if (i >= 2) {
			consumer.accept(createPointedDripstone(pointedDripstoneBlock, direction, DripstoneThickness.FRUSTUM));
		}

		if (i >= 1) {
			consumer.accept(createPointedDripstone(pointedDripstoneBlock, direction, bl ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP));
		}
	}

	protected static void growPointedDripstone(Block block, LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, int i, boolean bl) {
		if (block instanceof PointedDripstoneBlock pointedDripstoneBlock) {
			if (isDripstoneBase(pointedDripstoneBlock, levelAccessor.getBlockState(blockPos.relative(direction.getOpposite())))) {
				BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
				buildBaseToTipColumn(pointedDripstoneBlock, direction, i, bl, blockState -> {
					if (blockState.getBlock() instanceof PointedDripstoneBlock) {
						blockState = blockState.setValue(PointedDripstoneBlock.WATERLOGGED, Boolean.valueOf(levelAccessor.isWaterAt(mutableBlockPos)));
					}

					levelAccessor.setBlock(mutableBlockPos, blockState, 2);
					mutableBlockPos.move(direction);
				});
			}
		}
	}

	protected static boolean placeDripstoneBlockIfPossible(LevelAccessor levelAccessor, BlockPos blockPos, Block block) {
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		if (blockState.is(BlockTags.DRIPSTONE_REPLACEABLE)) {
			levelAccessor.setBlock(blockPos, block.defaultBlockState(), 2);
			return true;
		} else {
			return false;
		}
	}

	private static BlockState createPointedDripstone(PointedDripstoneBlock pointedDripstoneBlock, Direction direction, DripstoneThickness dripstoneThickness) {
		return pointedDripstoneBlock.defaultBlockState()
			.setValue(PointedDripstoneBlock.TIP_DIRECTION, direction)
			.setValue(PointedDripstoneBlock.THICKNESS, dripstoneThickness);
	}

	public static boolean isDripstoneBaseOrLava(PointedDripstoneBlock pointedDripstoneBlock, BlockState blockState) {
		return isDripstoneBase(pointedDripstoneBlock, blockState) || blockState.is(Blocks.LAVA);
	}

	public static boolean isDripstoneBase(PointedDripstoneBlock pointedDripstoneBlock, BlockState blockState) {
		return pointedDripstoneBlock.isBase(blockState) || blockState.is(BlockTags.DRIPSTONE_REPLACEABLE);
	}

	public static boolean isEmptyOrWater(BlockState blockState) {
		return blockState.isAir() || blockState.is(Blocks.WATER);
	}

	public static boolean isNeitherEmptyNorWater(BlockState blockState) {
		return !blockState.isAir() && !blockState.is(Blocks.WATER);
	}

	public static boolean isEmptyOrWaterOrLava(BlockState blockState) {
		return blockState.isAir() || blockState.is(Blocks.WATER) || blockState.is(Blocks.LAVA);
	}
}
