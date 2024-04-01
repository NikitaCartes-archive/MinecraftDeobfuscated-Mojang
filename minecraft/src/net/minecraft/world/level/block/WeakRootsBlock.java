package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class WeakRootsBlock extends PipeBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<WeakRootsBlock> CODEC = simpleCodec(WeakRootsBlock::new);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	@Override
	public MapCodec<WeakRootsBlock> codec() {
		return CODEC;
	}

	protected WeakRootsBlock(BlockBehaviour.Properties properties) {
		super(0.3125F, properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(NORTH, Boolean.valueOf(false))
				.setValue(EAST, Boolean.valueOf(false))
				.setValue(SOUTH, Boolean.valueOf(false))
				.setValue(WEST, Boolean.valueOf(false))
				.setValue(UP, Boolean.valueOf(false))
				.setValue(DOWN, Boolean.valueOf(false))
				.setValue(WATERLOGGED, Boolean.valueOf(false))
		);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return StrongRootsBlock.getStateWithConnections(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), this.defaultBlockState());
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		boolean bl = blockState2.is(this) || blockState2.is(Blocks.POWERFUL_POTATO);
		return blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(bl));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED);
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}
}
