package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class AntBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public AntBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		BlockState blockState2 = serverLevel.getBlockState(blockPos.below());
		if (blockState2.getBlock() == Blocks.WHITE_CONCRETE) {
			this.move(blockState, serverLevel, blockPos, AntBlock.Step.CW);
		} else if (blockState2.getBlock() == Blocks.BLACK_CONCRETE) {
			this.move(blockState, serverLevel, blockPos, AntBlock.Step.CCW);
		}
	}

	private void move(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, AntBlock.Step step) {
		Direction direction = blockState.getValue(FACING);
		Direction direction2 = step == AntBlock.Step.CW ? direction.getClockWise() : direction.getCounterClockWise();
		BlockPos blockPos2 = blockPos.relative(direction2);
		if (serverLevel.isLoaded(blockPos2)) {
			switch (step) {
				case CW:
					serverLevel.setBlock(blockPos.below(), Blocks.BLACK_CONCRETE.defaultBlockState(), 19);
					serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
					serverLevel.setBlock(blockPos2, blockState.setValue(FACING, direction2), 3);
					break;
				case CCW:
					serverLevel.setBlock(blockPos.below(), Blocks.WHITE_CONCRETE.defaultBlockState(), 19);
					serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
					serverLevel.setBlock(blockPos2, blockState.setValue(FACING, direction2), 3);
			}
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		level.getBlockTicks().scheduleTick(blockPos, this, 1);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	static enum Step {
		CW,
		CCW;
	}
}
