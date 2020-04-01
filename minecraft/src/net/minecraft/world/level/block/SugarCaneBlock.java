package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SugarCaneBlock extends Block {
	public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
	protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

	protected SugarCaneBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!blockState.canSurvive(serverLevel, blockPos)) {
			serverLevel.destroyBlock(blockPos, true);
		}
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (serverLevel.isEmptyBlock(blockPos.above())) {
			int i = 1;

			while (serverLevel.getBlockState(blockPos.below(i)).getBlock() == this) {
				i++;
			}

			if (i < 3) {
				int j = (Integer)blockState.getValue(AGE);
				if (j == 15) {
					serverLevel.setBlockAndUpdate(blockPos.above(), this.defaultBlockState());
					serverLevel.setBlock(blockPos, blockState.setValue(AGE, Integer.valueOf(0)), 4);
				} else {
					serverLevel.setBlock(blockPos, blockState.setValue(AGE, Integer.valueOf(j + 1)), 4);
				}
			}
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (!blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public boolean isUnstable() {
		return true;
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		Block block = levelReader.getBlockState(blockPos.below()).getBlock();
		if (block == this) {
			return true;
		} else {
			if (block == Blocks.GRASS_BLOCK
				|| block == Blocks.DIRT
				|| block == Blocks.COARSE_DIRT
				|| block == Blocks.PODZOL
				|| block == Blocks.SAND
				|| block == Blocks.RED_SAND) {
				BlockPos blockPos2 = blockPos.below();

				for (Direction direction : Direction.Plane.HORIZONTAL) {
					BlockState blockState2 = levelReader.getBlockState(blockPos2.relative(direction));
					FluidState fluidState = levelReader.getFluidState(blockPos2.relative(direction));
					if (fluidState.is(FluidTags.WATER) || blockState2.getBlock() == Blocks.FROSTED_ICE) {
						return true;
					}
				}
			}

			return false;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}
}
