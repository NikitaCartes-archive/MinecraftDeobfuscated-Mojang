package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class CoralFanBlock extends BaseCoralFanBlock {
	private final Block deadBlock;

	protected CoralFanBlock(Block block, BlockBehaviour.Properties properties) {
		super(properties);
		this.deadBlock = block;
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		this.tryScheduleDieTick(blockState, level, blockPos);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!scanForWater(blockState, serverLevel, blockPos)) {
			serverLevel.setBlock(blockPos, this.deadBlock.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)), 2);
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)) {
			return Blocks.AIR.defaultBlockState();
		} else {
			this.tryScheduleDieTick(blockState, levelAccessor, blockPos);
			if ((Boolean)blockState.getValue(WATERLOGGED)) {
				levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
			}

			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		}
	}
}
