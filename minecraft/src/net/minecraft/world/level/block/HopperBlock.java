package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HopperBlock extends BaseEntityBlock {
	public static final MapCodec<HopperBlock> CODEC = simpleCodec(HopperBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING_HOPPER;
	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
	private static final VoxelShape TOP = Block.box(0.0, 10.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape FUNNEL = Block.box(4.0, 4.0, 4.0, 12.0, 10.0, 12.0);
	private static final VoxelShape CONVEX_BASE = Shapes.or(FUNNEL, TOP);
	private static final VoxelShape INSIDE = box(2.0, 11.0, 2.0, 14.0, 16.0, 14.0);
	private static final VoxelShape BASE = Shapes.join(CONVEX_BASE, INSIDE, BooleanOp.ONLY_FIRST);
	private static final VoxelShape DOWN_SHAPE = Shapes.or(BASE, Block.box(6.0, 0.0, 6.0, 10.0, 4.0, 10.0));
	private static final VoxelShape EAST_SHAPE = Shapes.or(BASE, Block.box(12.0, 4.0, 6.0, 16.0, 8.0, 10.0));
	private static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, Block.box(6.0, 4.0, 0.0, 10.0, 8.0, 4.0));
	private static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, Block.box(6.0, 4.0, 12.0, 10.0, 8.0, 16.0));
	private static final VoxelShape WEST_SHAPE = Shapes.or(BASE, Block.box(0.0, 4.0, 6.0, 4.0, 8.0, 10.0));
	private static final VoxelShape DOWN_INTERACTION_SHAPE = INSIDE;
	private static final VoxelShape EAST_INTERACTION_SHAPE = Shapes.or(INSIDE, Block.box(12.0, 8.0, 6.0, 16.0, 10.0, 10.0));
	private static final VoxelShape NORTH_INTERACTION_SHAPE = Shapes.or(INSIDE, Block.box(6.0, 8.0, 0.0, 10.0, 10.0, 4.0));
	private static final VoxelShape SOUTH_INTERACTION_SHAPE = Shapes.or(INSIDE, Block.box(6.0, 8.0, 12.0, 10.0, 10.0, 16.0));
	private static final VoxelShape WEST_INTERACTION_SHAPE = Shapes.or(INSIDE, Block.box(0.0, 8.0, 6.0, 4.0, 10.0, 10.0));

	@Override
	public MapCodec<HopperBlock> codec() {
		return CODEC;
	}

	public HopperBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(ENABLED, Boolean.valueOf(true)));
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		switch ((Direction)blockState.getValue(FACING)) {
			case DOWN:
				return DOWN_SHAPE;
			case NORTH:
				return NORTH_SHAPE;
			case SOUTH:
				return SOUTH_SHAPE;
			case WEST:
				return WEST_SHAPE;
			case EAST:
				return EAST_SHAPE;
			default:
				return BASE;
		}
	}

	@Override
	protected VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		switch ((Direction)blockState.getValue(FACING)) {
			case DOWN:
				return DOWN_INTERACTION_SHAPE;
			case NORTH:
				return NORTH_INTERACTION_SHAPE;
			case SOUTH:
				return SOUTH_INTERACTION_SHAPE;
			case WEST:
				return WEST_INTERACTION_SHAPE;
			case EAST:
				return EAST_INTERACTION_SHAPE;
			default:
				return INSIDE;
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Direction direction = blockPlaceContext.getClickedFace().getOpposite();
		return this.defaultBlockState()
			.setValue(FACING, direction.getAxis() == Direction.Axis.Y ? Direction.DOWN : direction)
			.setValue(ENABLED, Boolean.valueOf(true));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new HopperBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return level.isClientSide ? null : createTickerHelper(blockEntityType, BlockEntityType.HOPPER, HopperBlockEntity::pushItemsTick);
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
			this.checkPoweredState(level, blockPos, blockState);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (!level.isClientSide && level.getBlockEntity(blockPos) instanceof HopperBlockEntity hopperBlockEntity) {
			player.openMenu(hopperBlockEntity);
			player.awardStat(Stats.INSPECT_HOPPER);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
		this.checkPoweredState(level, blockPos, blockState);
	}

	private void checkPoweredState(Level level, BlockPos blockPos, BlockState blockState) {
		boolean bl = !level.hasNeighborSignal(blockPos);
		if (bl != (Boolean)blockState.getValue(ENABLED)) {
			level.setBlock(blockPos, blockState.setValue(ENABLED, Boolean.valueOf(bl)), 2);
		}
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		Containers.dropContentsOnDestroy(blockState, blockState2, level, blockPos);
		super.onRemove(blockState, level, blockPos, blockState2, bl);
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ENABLED);
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof HopperBlockEntity) {
			HopperBlockEntity.entityInside(level, blockPos, blockState, entity, (HopperBlockEntity)blockEntity);
		}
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}
}
