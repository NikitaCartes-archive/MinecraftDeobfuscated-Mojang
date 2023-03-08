package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DecoratedPotBlock extends BaseEntityBlock {
	private static final VoxelShape BOUNDING_BOX = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
	private static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	protected DecoratedPotBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		return this.defaultBlockState()
			.setValue(HORIZONTAL_FACING, blockPlaceContext.getHorizontalDirection())
			.setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return BOUNDING_BOX;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, WATERLOGGED);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new DecoratedPotBlockEntity(blockPos, blockState);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (!level.isClientSide && level.getBlockEntity(blockPos) instanceof DecoratedPotBlockEntity decoratedPotBlockEntity) {
			decoratedPotBlockEntity.playerDestroy(level, blockPos, player.getMainHandItem(), player);
		}

		super.playerWillDestroy(level, blockPos, blockState, player);
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!level.isClientSide && level.getBlockEntity(blockPos) instanceof DecoratedPotBlockEntity decoratedPotBlockEntity && !decoratedPotBlockEntity.isBroken()) {
			Containers.dropItemStack(level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), decoratedPotBlockEntity.getItem());
			level.playSound(null, blockPos, SoundEvents.DECORATED_POT_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
		}

		super.onRemove(blockState, level, blockPos, blockState2, bl);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}
}
