package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BellBlock extends BaseEntityBlock {
	public static final MapCodec<BellBlock> CODEC = simpleCodec(BellBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<BellAttachType> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	private static final VoxelShape NORTH_SOUTH_FLOOR_SHAPE = Block.box(0.0, 0.0, 4.0, 16.0, 16.0, 12.0);
	private static final VoxelShape EAST_WEST_FLOOR_SHAPE = Block.box(4.0, 0.0, 0.0, 12.0, 16.0, 16.0);
	private static final VoxelShape BELL_TOP_SHAPE = Block.box(5.0, 6.0, 5.0, 11.0, 13.0, 11.0);
	private static final VoxelShape BELL_BOTTOM_SHAPE = Block.box(4.0, 4.0, 4.0, 12.0, 6.0, 12.0);
	private static final VoxelShape BELL_SHAPE = Shapes.or(BELL_BOTTOM_SHAPE, BELL_TOP_SHAPE);
	private static final VoxelShape NORTH_SOUTH_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 0.0, 9.0, 15.0, 16.0));
	private static final VoxelShape EAST_WEST_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(0.0, 13.0, 7.0, 16.0, 15.0, 9.0));
	private static final VoxelShape TO_WEST = Shapes.or(BELL_SHAPE, Block.box(0.0, 13.0, 7.0, 13.0, 15.0, 9.0));
	private static final VoxelShape TO_EAST = Shapes.or(BELL_SHAPE, Block.box(3.0, 13.0, 7.0, 16.0, 15.0, 9.0));
	private static final VoxelShape TO_NORTH = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 0.0, 9.0, 15.0, 13.0));
	private static final VoxelShape TO_SOUTH = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 3.0, 9.0, 15.0, 16.0));
	private static final VoxelShape CEILING_SHAPE = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 7.0, 9.0, 16.0, 9.0));
	public static final int EVENT_BELL_RING = 1;

	@Override
	public MapCodec<BellBlock> codec() {
		return CODEC;
	}

	public BellBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACHMENT, BellAttachType.FLOOR).setValue(POWERED, Boolean.valueOf(false))
		);
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
		boolean bl2 = level.hasNeighborSignal(blockPos);
		if (bl2 != (Boolean)blockState.getValue(POWERED)) {
			if (bl2) {
				this.attemptToRing(level, blockPos, null);
			}

			level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(bl2)), 3);
		}
	}

	@Override
	protected void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
		Entity entity = projectile.getOwner();
		Player player = entity instanceof Player ? (Player)entity : null;
		this.onHit(level, blockState, blockHitResult, player, true);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		return (InteractionResult)(this.onHit(level, blockState, blockHitResult, player, true) ? InteractionResult.SUCCESS : InteractionResult.PASS);
	}

	public boolean onHit(Level level, BlockState blockState, BlockHitResult blockHitResult, @Nullable Player player, boolean bl) {
		Direction direction = blockHitResult.getDirection();
		BlockPos blockPos = blockHitResult.getBlockPos();
		boolean bl2 = !bl || this.isProperHit(blockState, direction, blockHitResult.getLocation().y - (double)blockPos.getY());
		if (bl2) {
			boolean bl3 = this.attemptToRing(player, level, blockPos, direction);
			if (bl3 && player != null) {
				player.awardStat(Stats.BELL_RING);
			}

			return true;
		} else {
			return false;
		}
	}

	private boolean isProperHit(BlockState blockState, Direction direction, double d) {
		if (direction.getAxis() != Direction.Axis.Y && !(d > 0.8124F)) {
			Direction direction2 = blockState.getValue(FACING);
			BellAttachType bellAttachType = blockState.getValue(ATTACHMENT);
			switch (bellAttachType) {
				case FLOOR:
					return direction2.getAxis() == direction.getAxis();
				case SINGLE_WALL:
				case DOUBLE_WALL:
					return direction2.getAxis() != direction.getAxis();
				case CEILING:
					return true;
				default:
					return false;
			}
		} else {
			return false;
		}
	}

	public boolean attemptToRing(Level level, BlockPos blockPos, @Nullable Direction direction) {
		return this.attemptToRing(null, level, blockPos, direction);
	}

	public boolean attemptToRing(@Nullable Entity entity, Level level, BlockPos blockPos, @Nullable Direction direction) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (!level.isClientSide && blockEntity instanceof BellBlockEntity) {
			if (direction == null) {
				direction = level.getBlockState(blockPos).getValue(FACING);
			}

			((BellBlockEntity)blockEntity).onHit(direction);
			level.playSound(null, blockPos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 1.0F);
			level.gameEvent(entity, GameEvent.BLOCK_CHANGE, blockPos);
			return true;
		} else {
			return false;
		}
	}

	private VoxelShape getVoxelShape(BlockState blockState) {
		Direction direction = blockState.getValue(FACING);
		BellAttachType bellAttachType = blockState.getValue(ATTACHMENT);
		if (bellAttachType == BellAttachType.FLOOR) {
			return direction != Direction.NORTH && direction != Direction.SOUTH ? EAST_WEST_FLOOR_SHAPE : NORTH_SOUTH_FLOOR_SHAPE;
		} else if (bellAttachType == BellAttachType.CEILING) {
			return CEILING_SHAPE;
		} else if (bellAttachType == BellAttachType.DOUBLE_WALL) {
			return direction != Direction.NORTH && direction != Direction.SOUTH ? EAST_WEST_BETWEEN : NORTH_SOUTH_BETWEEN;
		} else if (direction == Direction.NORTH) {
			return TO_NORTH;
		} else if (direction == Direction.SOUTH) {
			return TO_SOUTH;
		} else {
			return direction == Direction.EAST ? TO_EAST : TO_WEST;
		}
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.getVoxelShape(blockState);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.getVoxelShape(blockState);
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Direction direction = blockPlaceContext.getClickedFace();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		Level level = blockPlaceContext.getLevel();
		Direction.Axis axis = direction.getAxis();
		if (axis == Direction.Axis.Y) {
			BlockState blockState = this.defaultBlockState()
				.setValue(ATTACHMENT, direction == Direction.DOWN ? BellAttachType.CEILING : BellAttachType.FLOOR)
				.setValue(FACING, blockPlaceContext.getHorizontalDirection());
			if (blockState.canSurvive(blockPlaceContext.getLevel(), blockPos)) {
				return blockState;
			}
		} else {
			boolean bl = axis == Direction.Axis.X
					&& level.getBlockState(blockPos.west()).isFaceSturdy(level, blockPos.west(), Direction.EAST)
					&& level.getBlockState(blockPos.east()).isFaceSturdy(level, blockPos.east(), Direction.WEST)
				|| axis == Direction.Axis.Z
					&& level.getBlockState(blockPos.north()).isFaceSturdy(level, blockPos.north(), Direction.SOUTH)
					&& level.getBlockState(blockPos.south()).isFaceSturdy(level, blockPos.south(), Direction.NORTH);
			BlockState blockState = this.defaultBlockState()
				.setValue(FACING, direction.getOpposite())
				.setValue(ATTACHMENT, bl ? BellAttachType.DOUBLE_WALL : BellAttachType.SINGLE_WALL);
			if (blockState.canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
				return blockState;
			}

			boolean bl2 = level.getBlockState(blockPos.below()).isFaceSturdy(level, blockPos.below(), Direction.UP);
			blockState = blockState.setValue(ATTACHMENT, bl2 ? BellAttachType.FLOOR : BellAttachType.CEILING);
			if (blockState.canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
				return blockState;
			}
		}

		return null;
	}

	@Override
	protected void onExplosionHit(
		BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer
	) {
		if (explosion.canTriggerBlocks()) {
			this.attemptToRing(serverLevel, blockPos, null);
		}

		super.onExplosionHit(blockState, serverLevel, blockPos, explosion, biConsumer);
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		BellAttachType bellAttachType = blockState.getValue(ATTACHMENT);
		Direction direction2 = getConnectedDirection(blockState).getOpposite();
		if (direction2 == direction && !blockState.canSurvive(levelAccessor, blockPos) && bellAttachType != BellAttachType.DOUBLE_WALL) {
			return Blocks.AIR.defaultBlockState();
		} else {
			if (direction.getAxis() == ((Direction)blockState.getValue(FACING)).getAxis()) {
				if (bellAttachType == BellAttachType.DOUBLE_WALL && !blockState2.isFaceSturdy(levelAccessor, blockPos2, direction)) {
					return blockState.setValue(ATTACHMENT, BellAttachType.SINGLE_WALL).setValue(FACING, direction.getOpposite());
				}

				if (bellAttachType == BellAttachType.SINGLE_WALL
					&& direction2.getOpposite() == direction
					&& blockState2.isFaceSturdy(levelAccessor, blockPos2, blockState.getValue(FACING))) {
					return blockState.setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL);
				}
			}

			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		}
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		Direction direction = getConnectedDirection(blockState).getOpposite();
		return direction == Direction.UP
			? Block.canSupportCenter(levelReader, blockPos.above(), Direction.DOWN)
			: FaceAttachedHorizontalDirectionalBlock.canAttach(levelReader, blockPos, direction);
	}

	private static Direction getConnectedDirection(BlockState blockState) {
		switch ((BellAttachType)blockState.getValue(ATTACHMENT)) {
			case FLOOR:
				return Direction.UP;
			case CEILING:
				return Direction.DOWN;
			default:
				return ((Direction)blockState.getValue(FACING)).getOpposite();
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ATTACHMENT, POWERED);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new BellBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return createTickerHelper(blockEntityType, BlockEntityType.BELL, level.isClientSide ? BellBlockEntity::clientTick : BellBlockEntity::serverTick);
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}
}
