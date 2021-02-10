package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigDripleafBlock extends HorizontalDirectionalBlock implements BonemealableBlock, SimpleWaterloggedBlock {
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final EnumProperty<Tilt> TILT = BlockStateProperties.TILT;
	private static final Object2IntMap<Tilt> DELAY_UNTIL_NEXT_TILT_STATE = Util.make(new Object2IntArrayMap<>(), object2IntArrayMap -> {
		object2IntArrayMap.defaultReturnValue(-1);
		object2IntArrayMap.put(Tilt.UNSTABLE, 10);
		object2IntArrayMap.put(Tilt.PARTIAL, 10);
		object2IntArrayMap.put(Tilt.FULL, 100);
	});
	private static final Map<Tilt, VoxelShape> LEAF_SHAPES = ImmutableMap.of(
		Tilt.NONE,
		Block.box(0.0, 11.0, 0.0, 16.0, 15.0, 16.0),
		Tilt.UNSTABLE,
		Block.box(0.0, 11.0, 0.0, 16.0, 15.0, 16.0),
		Tilt.PARTIAL,
		Block.box(0.0, 11.0, 0.0, 16.0, 13.0, 16.0),
		Tilt.FULL,
		Shapes.empty()
	);
	private static final Map<Direction, VoxelShape> STEM_SHAPES = ImmutableMap.of(
		Direction.NORTH,
		Block.box(5.0, 0.0, 8.0, 11.0, 11.0, 14.0),
		Direction.SOUTH,
		Block.box(5.0, 0.0, 2.0, 11.0, 11.0, 8.0),
		Direction.EAST,
		Block.box(2.0, 0.0, 5.0, 8.0, 11.0, 11.0),
		Direction.WEST,
		Block.box(8.0, 0.0, 5.0, 14.0, 11.0, 11.0)
	);
	private final Map<BlockState, VoxelShape> shapesCache;

	protected BigDripleafBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH).setValue(TILT, Tilt.NONE)
		);
		this.shapesCache = this.getShapeForEachState(BigDripleafBlock::calculateShape);
	}

	private static VoxelShape calculateShape(BlockState blockState) {
		return Shapes.or(getLeafShape(blockState), getStemShape(blockState));
	}

	private static VoxelShape getStemShape(BlockState blockState) {
		return (VoxelShape)STEM_SHAPES.get(blockState.getValue(FACING));
	}

	private static VoxelShape getLeafShape(BlockState blockState) {
		return (VoxelShape)LEAF_SHAPES.get(blockState.getValue(TILT));
	}

	protected static void placeWithRandomHeight(Level level, Random random, BlockPos blockPos) {
		int i = 1 + random.nextInt(5);
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		int j = 0;

		while (j < i && canPlaceAt(level, mutableBlockPos, level.getBlockState(mutableBlockPos))) {
			j++;
			mutableBlockPos.move(Direction.UP);
		}

		int k = blockPos.getY() + j - 1;
		mutableBlockPos.setY(blockPos.getY());

		while (mutableBlockPos.getY() < k) {
			BigDripleafStemBlock.place(level, mutableBlockPos, level.getFluidState(mutableBlockPos), direction);
			mutableBlockPos.move(Direction.UP);
		}

		place(level, mutableBlockPos, level.getFluidState(mutableBlockPos), direction);
	}

	private static boolean canReplace(BlockState blockState) {
		return blockState.isAir() || blockState.is(Blocks.WATER) || blockState.is(Blocks.SMALL_DRIPLEAF);
	}

	private static boolean canPlaceAt(Level level, BlockPos blockPos, BlockState blockState) {
		return level.isInWorldBounds(blockPos) && canReplace(blockState);
	}

	protected static boolean place(LevelAccessor levelAccessor, BlockPos blockPos, FluidState fluidState, Direction direction) {
		BlockState blockState = Blocks.BIG_DRIPLEAF
			.defaultBlockState()
			.setValue(WATERLOGGED, Boolean.valueOf(fluidState.isSourceOfType(Fluids.WATER)))
			.setValue(FACING, direction);
		return levelAccessor.setBlock(blockPos, blockState, 2);
	}

	@Override
	public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
		this.setTiltAndScheduleTick(blockState, level, blockHitResult.getBlockPos(), Tilt.FULL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		return blockState2.is(Blocks.BIG_DRIPLEAF_STEM) || blockState2.isFaceSturdy(levelReader, blockPos2, Direction.UP);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)) {
			return blockState.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
		} else {
			if ((Boolean)blockState.getValue(WATERLOGGED)) {
				levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
			}

			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		}
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		BlockState blockState2 = blockGetter.getBlockState(blockPos.above());
		return canReplace(blockState2);
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		BlockPos blockPos2 = blockPos.above();
		BlockState blockState2 = serverLevel.getBlockState(blockPos2);
		if (canPlaceAt(serverLevel, blockPos2, blockState2)) {
			Direction direction = blockState.getValue(FACING);
			BigDripleafStemBlock.place(serverLevel, blockPos, blockState.getFluidState(), direction);
			place(serverLevel, blockPos2, blockState2.getFluidState(), direction);
		}
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!level.isClientSide) {
			if (blockState.getValue(TILT) == Tilt.NONE && canEntityTilt(blockPos, entity)) {
				this.setTiltAndScheduleTick(blockState, level, blockPos, Tilt.UNSTABLE, null);
			}
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (serverLevel.hasNeighborSignal(blockPos)) {
			resetTilt(blockState, serverLevel, blockPos);
		} else {
			Tilt tilt = blockState.getValue(TILT);
			if (tilt == Tilt.UNSTABLE) {
				this.setTiltAndScheduleTick(blockState, serverLevel, blockPos, Tilt.PARTIAL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
			} else if (tilt == Tilt.PARTIAL) {
				this.setTiltAndScheduleTick(blockState, serverLevel, blockPos, Tilt.FULL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
			} else if (tilt == Tilt.FULL) {
				resetTilt(blockState, serverLevel, blockPos);
			}
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (level.hasNeighborSignal(blockPos)) {
			resetTilt(blockState, level, blockPos);
		}
	}

	private static void playTiltSound(Level level, BlockPos blockPos, SoundEvent soundEvent) {
		float f = Mth.randomBetween(level.random, 0.8F, 1.2F);
		level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, f);
	}

	private static boolean canEntityTilt(BlockPos blockPos, Entity entity) {
		return entity.position().y > (double)((float)blockPos.getY() + 0.6875F);
	}

	private void setTiltAndScheduleTick(BlockState blockState, Level level, BlockPos blockPos, Tilt tilt, @Nullable SoundEvent soundEvent) {
		setTilt(blockState, level, blockPos, tilt);
		if (soundEvent != null) {
			playTiltSound(level, blockPos, soundEvent);
		}

		int i = DELAY_UNTIL_NEXT_TILT_STATE.getInt(tilt);
		if (i != -1) {
			level.getBlockTicks().scheduleTick(blockPos, this, i);
		}
	}

	private static void resetTilt(BlockState blockState, Level level, BlockPos blockPos) {
		setTilt(blockState, level, blockPos, Tilt.NONE);
		playTiltSound(level, blockPos, SoundEvents.BIG_DRIPLEAF_TILT_UP);
	}

	private static void setTilt(BlockState blockState, Level level, BlockPos blockPos, Tilt tilt) {
		level.setBlock(blockPos, blockState.setValue(TILT, tilt), 2);
		if (tilt.causesVibration()) {
			level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos);
		}
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return getLeafShape(blockState);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)this.shapesCache.get(blockState);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		return this.defaultBlockState()
			.setValue(WATERLOGGED, Boolean.valueOf(fluidState.isSourceOfType(Fluids.WATER)))
			.setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED, FACING, TILT);
	}
}
