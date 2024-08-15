package net.minecraft.world.level.block;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TripWireHookBlock extends Block {
	public static final MapCodec<TripWireHookBlock> CODEC = simpleCodec(TripWireHookBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
	protected static final int WIRE_DIST_MIN = 1;
	protected static final int WIRE_DIST_MAX = 42;
	private static final int RECHECK_PERIOD = 10;
	protected static final int AABB_OFFSET = 3;
	protected static final VoxelShape NORTH_AABB = Block.box(5.0, 0.0, 10.0, 11.0, 10.0, 16.0);
	protected static final VoxelShape SOUTH_AABB = Block.box(5.0, 0.0, 0.0, 11.0, 10.0, 6.0);
	protected static final VoxelShape WEST_AABB = Block.box(10.0, 0.0, 5.0, 16.0, 10.0, 11.0);
	protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 5.0, 6.0, 10.0, 11.0);

	@Override
	public MapCodec<TripWireHookBlock> codec() {
		return CODEC;
	}

	public TripWireHookBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(ATTACHED, Boolean.valueOf(false))
		);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		switch ((Direction)blockState.getValue(FACING)) {
			case EAST:
			default:
				return EAST_AABB;
			case WEST:
				return WEST_AABB;
			case SOUTH:
				return SOUTH_AABB;
			case NORTH:
				return NORTH_AABB;
		}
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		Direction direction = blockState.getValue(FACING);
		BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		return direction.getAxis().isHorizontal() && blockState2.isFaceSturdy(levelReader, blockPos2, direction);
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = this.defaultBlockState().setValue(POWERED, Boolean.valueOf(false)).setValue(ATTACHED, Boolean.valueOf(false));
		LevelReader levelReader = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		Direction[] directions = blockPlaceContext.getNearestLookingDirections();

		for (Direction direction : directions) {
			if (direction.getAxis().isHorizontal()) {
				Direction direction2 = direction.getOpposite();
				blockState = blockState.setValue(FACING, direction2);
				if (blockState.canSurvive(levelReader, blockPos)) {
					return blockState;
				}
			}
		}

		return null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		calculateState(level, blockPos, blockState, false, false, -1, null);
	}

	public static void calculateState(Level level, BlockPos blockPos, BlockState blockState, boolean bl, boolean bl2, int i, @Nullable BlockState blockState2) {
		Optional<Direction> optional = blockState.getOptionalValue(FACING);
		if (optional.isPresent()) {
			Direction direction = (Direction)optional.get();
			boolean bl3 = (Boolean)blockState.getOptionalValue(ATTACHED).orElse(false);
			boolean bl4 = (Boolean)blockState.getOptionalValue(POWERED).orElse(false);
			Block block = blockState.getBlock();
			boolean bl5 = !bl;
			boolean bl6 = false;
			int j = 0;
			BlockState[] blockStates = new BlockState[42];

			for (int k = 1; k < 42; k++) {
				BlockPos blockPos2 = blockPos.relative(direction, k);
				BlockState blockState3 = level.getBlockState(blockPos2);
				if (blockState3.is(Blocks.TRIPWIRE_HOOK)) {
					if (blockState3.getValue(FACING) == direction.getOpposite()) {
						j = k;
					}
					break;
				}

				if (!blockState3.is(Blocks.TRIPWIRE) && k != i) {
					blockStates[k] = null;
					bl5 = false;
				} else {
					if (k == i) {
						blockState3 = MoreObjects.firstNonNull(blockState2, blockState3);
					}

					boolean bl7 = !(Boolean)blockState3.getValue(TripWireBlock.DISARMED);
					boolean bl8 = (Boolean)blockState3.getValue(TripWireBlock.POWERED);
					bl6 |= bl7 && bl8;
					blockStates[k] = blockState3;
					if (k == i) {
						level.scheduleTick(blockPos, block, 10);
						bl5 &= bl7;
					}
				}
			}

			bl5 &= j > 1;
			bl6 &= bl5;
			BlockState blockState4 = block.defaultBlockState().trySetValue(ATTACHED, Boolean.valueOf(bl5)).trySetValue(POWERED, Boolean.valueOf(bl6));
			if (j > 0) {
				BlockPos blockPos2x = blockPos.relative(direction, j);
				Direction direction2 = direction.getOpposite();
				level.setBlock(blockPos2x, blockState4.setValue(FACING, direction2), 3);
				notifyNeighbors(block, level, blockPos2x, direction2);
				emitState(level, blockPos2x, bl5, bl6, bl3, bl4);
			}

			emitState(level, blockPos, bl5, bl6, bl3, bl4);
			if (!bl) {
				level.setBlock(blockPos, blockState4.setValue(FACING, direction), 3);
				if (bl2) {
					notifyNeighbors(block, level, blockPos, direction);
				}
			}

			if (bl3 != bl5) {
				for (int l = 1; l < j; l++) {
					BlockPos blockPos3 = blockPos.relative(direction, l);
					BlockState blockState5 = blockStates[l];
					if (blockState5 != null && !level.getBlockState(blockPos3).isAir()) {
						level.setBlock(blockPos3, blockState5.trySetValue(ATTACHED, Boolean.valueOf(bl5)), 3);
					}
				}
			}
		}
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		calculateState(serverLevel, blockPos, blockState, false, true, -1, null);
	}

	private static void emitState(Level level, BlockPos blockPos, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		if (bl2 && !bl4) {
			level.playSound(null, blockPos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.4F, 0.6F);
			level.gameEvent(null, GameEvent.BLOCK_ACTIVATE, blockPos);
		} else if (!bl2 && bl4) {
			level.playSound(null, blockPos, SoundEvents.TRIPWIRE_CLICK_OFF, SoundSource.BLOCKS, 0.4F, 0.5F);
			level.gameEvent(null, GameEvent.BLOCK_DEACTIVATE, blockPos);
		} else if (bl && !bl3) {
			level.playSound(null, blockPos, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 0.4F, 0.7F);
			level.gameEvent(null, GameEvent.BLOCK_ATTACH, blockPos);
		} else if (!bl && bl3) {
			level.playSound(null, blockPos, SoundEvents.TRIPWIRE_DETACH, SoundSource.BLOCKS, 0.4F, 1.2F / (level.random.nextFloat() * 0.2F + 0.9F));
			level.gameEvent(null, GameEvent.BLOCK_DETACH, blockPos);
		}
	}

	private static void notifyNeighbors(Block block, Level level, BlockPos blockPos, Direction direction) {
		Direction direction2 = direction.getOpposite();
		Orientation orientation = ExperimentalRedstoneUtils.randomOrientation(level, direction2, Direction.UP);
		level.updateNeighborsAt(blockPos, block, orientation);
		level.updateNeighborsAt(blockPos.relative(direction2), block, orientation);
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!bl && !blockState.is(blockState2.getBlock())) {
			boolean bl2 = (Boolean)blockState.getValue(ATTACHED);
			boolean bl3 = (Boolean)blockState.getValue(POWERED);
			if (bl2 || bl3) {
				calculateState(level, blockPos, blockState, true, false, -1, null);
			}

			if (bl3) {
				notifyNeighbors(this, level, blockPos, blockState.getValue(FACING));
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		if (!(Boolean)blockState.getValue(POWERED)) {
			return 0;
		} else {
			return blockState.getValue(FACING) == direction ? 15 : 0;
		}
	}

	@Override
	protected boolean isSignalSource(BlockState blockState) {
		return true;
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
		builder.add(FACING, POWERED, ATTACHED);
	}
}
