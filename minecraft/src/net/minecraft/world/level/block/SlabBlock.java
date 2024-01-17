package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabBlock extends Block implements SimpleWaterloggedBlock {
	public static final MapCodec<SlabBlock> CODEC = simpleCodec(SlabBlock::new);
	public static final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
	protected static final VoxelShape TOP_AABB = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);

	@Override
	public MapCodec<? extends SlabBlock> codec() {
		return CODEC;
	}

	public SlabBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, Boolean.valueOf(false)));
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState blockState) {
		return blockState.getValue(TYPE) != SlabType.DOUBLE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TYPE, WATERLOGGED);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		SlabType slabType = blockState.getValue(TYPE);
		switch (slabType) {
			case DOUBLE:
				return Shapes.block();
			case TOP:
				return TOP_AABB;
			default:
				return BOTTOM_AABB;
		}
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPos);
		if (blockState.is(this)) {
			return blockState.setValue(TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, Boolean.valueOf(false));
		} else {
			FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPos);
			BlockState blockState2 = this.defaultBlockState()
				.setValue(TYPE, SlabType.BOTTOM)
				.setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
			Direction direction = blockPlaceContext.getClickedFace();
			return direction != Direction.DOWN && (direction == Direction.UP || !(blockPlaceContext.getClickLocation().y - (double)blockPos.getY() > 0.5))
				? blockState2
				: blockState2.setValue(TYPE, SlabType.TOP);
		}
	}

	@Override
	protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		ItemStack itemStack = blockPlaceContext.getItemInHand();
		SlabType slabType = blockState.getValue(TYPE);
		if (slabType == SlabType.DOUBLE || !itemStack.is(this.asItem())) {
			return false;
		} else if (blockPlaceContext.replacingClickedOnBlock()) {
			boolean bl = blockPlaceContext.getClickLocation().y - (double)blockPlaceContext.getClickedPos().getY() > 0.5;
			Direction direction = blockPlaceContext.getClickedFace();
			return slabType == SlabType.BOTTOM
				? direction == Direction.UP || bl && direction.getAxis().isHorizontal()
				: direction == Direction.DOWN || !bl && direction.getAxis().isHorizontal();
		} else {
			return true;
		}
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
		return blockState.getValue(TYPE) != SlabType.DOUBLE ? SimpleWaterloggedBlock.super.placeLiquid(levelAccessor, blockPos, blockState, fluidState) : false;
	}

	@Override
	public boolean canPlaceLiquid(@Nullable Player player, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
		return blockState.getValue(TYPE) != SlabType.DOUBLE ? SimpleWaterloggedBlock.super.canPlaceLiquid(player, blockGetter, blockPos, blockState, fluid) : false;
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		switch (pathComputationType) {
			case LAND:
				return false;
			case WATER:
				return blockGetter.getFluidState(blockPos).is(FluidTags.WATER);
			case AIR:
				return false;
			default:
				return false;
		}
	}
}
