package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class SnowyDirtBlock extends Block {
	public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

	protected SnowyDirtBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(SNOWY, Boolean.valueOf(false)));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction != Direction.UP) {
			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		} else {
			Block block = blockState2.getBlock();
			return blockState.setValue(SNOWY, Boolean.valueOf(block == Blocks.SNOW_BLOCK || block == Blocks.SNOW));
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Block block = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().above()).getBlock();
		return this.defaultBlockState().setValue(SNOWY, Boolean.valueOf(block == Blocks.SNOW_BLOCK || block == Blocks.SNOW));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SNOWY);
	}
}
