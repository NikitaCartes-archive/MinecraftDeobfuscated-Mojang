package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LecternBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;
	public static final VoxelShape SHAPE_BASE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
	public static final VoxelShape SHAPE_POST = Block.box(4.0, 2.0, 4.0, 12.0, 14.0, 12.0);
	public static final VoxelShape SHAPE_COMMON = Shapes.or(SHAPE_BASE, SHAPE_POST);
	public static final VoxelShape SHAPE_TOP_PLATE = Block.box(0.0, 15.0, 0.0, 16.0, 15.0, 16.0);
	public static final VoxelShape SHAPE_COLLISION = Shapes.or(SHAPE_COMMON, SHAPE_TOP_PLATE);
	public static final VoxelShape SHAPE_WEST = Shapes.or(
		Block.box(1.0, 10.0, 0.0, 5.333333, 14.0, 16.0),
		Block.box(5.333333, 12.0, 0.0, 9.666667, 16.0, 16.0),
		Block.box(9.666667, 14.0, 0.0, 14.0, 18.0, 16.0),
		SHAPE_COMMON
	);
	public static final VoxelShape SHAPE_NORTH = Shapes.or(
		Block.box(0.0, 10.0, 1.0, 16.0, 14.0, 5.333333),
		Block.box(0.0, 12.0, 5.333333, 16.0, 16.0, 9.666667),
		Block.box(0.0, 14.0, 9.666667, 16.0, 18.0, 14.0),
		SHAPE_COMMON
	);
	public static final VoxelShape SHAPE_EAST = Shapes.or(
		Block.box(15.0, 10.0, 0.0, 10.666667, 14.0, 16.0),
		Block.box(10.666667, 12.0, 0.0, 6.333333, 16.0, 16.0),
		Block.box(6.333333, 14.0, 0.0, 2.0, 18.0, 16.0),
		SHAPE_COMMON
	);
	public static final VoxelShape SHAPE_SOUTH = Shapes.or(
		Block.box(0.0, 10.0, 15.0, 16.0, 14.0, 10.666667),
		Block.box(0.0, 12.0, 10.666667, 16.0, 16.0, 6.333333),
		Block.box(0.0, 14.0, 6.333333, 16.0, 18.0, 2.0),
		SHAPE_COMMON
	);

	protected LecternBlock(Block.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(HAS_BOOK, Boolean.valueOf(false))
		);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return SHAPE_COMMON;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE_COLLISION;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		switch ((Direction)blockState.getValue(FACING)) {
			case NORTH:
				return SHAPE_NORTH;
			case SOUTH:
				return SHAPE_SOUTH;
			case EAST:
				return SHAPE_EAST;
			case WEST:
				return SHAPE_WEST;
			default:
				return SHAPE_COMMON;
		}
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED, HAS_BOOK);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockGetter blockGetter) {
		return new LecternBlockEntity();
	}

	public static boolean tryPlaceBook(Level level, BlockPos blockPos, BlockState blockState, ItemStack itemStack) {
		if (!(Boolean)blockState.getValue(HAS_BOOK)) {
			if (!level.isClientSide) {
				placeBook(level, blockPos, blockState, itemStack);
			}

			return true;
		} else {
			return false;
		}
	}

	private static void placeBook(Level level, BlockPos blockPos, BlockState blockState, ItemStack itemStack) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof LecternBlockEntity) {
			LecternBlockEntity lecternBlockEntity = (LecternBlockEntity)blockEntity;
			lecternBlockEntity.setBook(itemStack.split(1));
			resetBookState(level, blockPos, blockState, true);
			level.playSound(null, blockPos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}

	public static void resetBookState(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
		level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(false)).setValue(HAS_BOOK, Boolean.valueOf(bl)), 3);
		updateBelow(level, blockPos, blockState);
	}

	public static void signalPageChange(Level level, BlockPos blockPos, BlockState blockState) {
		changePowered(level, blockPos, blockState, true);
		level.getBlockTicks().scheduleTick(blockPos, blockState.getBlock(), 2);
		level.levelEvent(1043, blockPos, 0);
	}

	private static void changePowered(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
		level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(bl)), 3);
		updateBelow(level, blockPos, blockState);
	}

	private static void updateBelow(Level level, BlockPos blockPos, BlockState blockState) {
		level.updateNeighborsAt(blockPos.below(), blockState.getBlock());
	}

	@Override
	public void tick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if (!level.isClientSide) {
			changePowered(level, blockPos, blockState, false);
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (blockState.getBlock() != blockState2.getBlock()) {
			if ((Boolean)blockState.getValue(HAS_BOOK)) {
				this.popBook(blockState, level, blockPos);
			}

			if ((Boolean)blockState.getValue(POWERED)) {
				level.updateNeighborsAt(blockPos.below(), this);
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	private void popBook(BlockState blockState, Level level, BlockPos blockPos) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof LecternBlockEntity) {
			LecternBlockEntity lecternBlockEntity = (LecternBlockEntity)blockEntity;
			Direction direction = blockState.getValue(FACING);
			ItemStack itemStack = lecternBlockEntity.getBook().copy();
			float f = 0.25F * (float)direction.getStepX();
			float g = 0.25F * (float)direction.getStepZ();
			ItemEntity itemEntity = new ItemEntity(
				level, (double)blockPos.getX() + 0.5 + (double)f, (double)(blockPos.getY() + 1), (double)blockPos.getZ() + 0.5 + (double)g, itemStack
			);
			itemEntity.setDefaultPickUpDelay();
			level.addFreshEntity(itemEntity);
			lecternBlockEntity.clearContent();
		}
	}

	@Override
	public boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return direction == Direction.UP && blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		if ((Boolean)blockState.getValue(HAS_BOOK)) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof LecternBlockEntity) {
				return ((LecternBlockEntity)blockEntity).getRedstoneSignal();
			}
		}

		return 0;
	}

	@Override
	public boolean use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		if ((Boolean)blockState.getValue(HAS_BOOK)) {
			if (!level.isClientSide) {
				this.openScreen(level, blockPos, player);
			}

			return true;
		} else {
			return false;
		}
	}

	@Nullable
	@Override
	public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
		return !blockState.getValue(HAS_BOOK) ? null : super.getMenuProvider(blockState, level, blockPos);
	}

	private void openScreen(Level level, BlockPos blockPos, Player player) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof LecternBlockEntity) {
			player.openMenu((LecternBlockEntity)blockEntity);
			player.awardStat(Stats.INTERACT_WITH_LECTERN);
		}
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
