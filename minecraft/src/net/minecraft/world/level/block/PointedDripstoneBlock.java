package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
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
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PointedDripstoneBlock extends Block implements Fallable, SimpleWaterloggedBlock {
	public static final DirectionProperty TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
	public static final EnumProperty<DripstoneThickness> THICKNESS = BlockStateProperties.DRIPSTONE_THICKNESS;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape TIP_MERGE_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
	private static final VoxelShape TIP_SHAPE_UP = Block.box(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
	private static final VoxelShape TIP_SHAPE_DOWN = Block.box(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
	private static final VoxelShape FRUSTUM_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
	private static final VoxelShape MIDDLE_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
	private static final VoxelShape BASE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

	public PointedDripstoneBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(TIP_DIRECTION, Direction.UP).setValue(THICKNESS, DripstoneThickness.TIP).setValue(WATERLOGGED, Boolean.valueOf(false))
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TIP_DIRECTION, THICKNESS, WATERLOGGED);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return isValidPointedDripstonePlacement(levelReader, blockPos, blockState.getValue(TIP_DIRECTION));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		if (direction != Direction.UP && direction != Direction.DOWN) {
			return blockState;
		} else if (levelAccessor.getBlockTicks().hasScheduledTick(blockPos, this)) {
			return blockState;
		} else {
			Direction direction2 = blockState.getValue(TIP_DIRECTION);
			if (direction != direction2.getOpposite() || isValidPointedDripstonePlacement(levelAccessor, blockPos, direction2)) {
				boolean bl = blockState.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
				DripstoneThickness dripstoneThickness = calculateDripstoneThickness(levelAccessor, blockPos, direction2, bl);
				return dripstoneThickness == null ? getAirOrWater(blockState) : blockState.setValue(THICKNESS, dripstoneThickness);
			} else if (direction2 == Direction.DOWN) {
				this.scheduleStalactiteFallTicks(blockState, levelAccessor, blockPos);
				return blockState;
			} else {
				return getAirOrWater(blockState);
			}
		}
	}

	private static BlockState getAirOrWater(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
	}

	@Override
	public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
		if (projectile instanceof ThrownTrident && projectile.getDeltaMovement().length() > 0.6) {
			level.destroyBlock(blockHitResult.getBlockPos(), true);
		}
	}

	@Override
	public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.getValue(TIP_DIRECTION) == Direction.UP && blockState.getValue(THICKNESS) == DripstoneThickness.TIP) {
			entity.causeFallDamage(f + 2.0F, 2.0F);
		} else {
			super.fallOn(level, blockPos, entity, f);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if (canDrip(blockState)) {
			float f = random.nextFloat();
			if (!(f > 0.12F)) {
				getFluidAboveStalactite(level, blockPos, blockState)
					.filter(fluid -> f < 0.02F || canFillCauldron(fluid))
					.ifPresent(fluid -> spawnDripParticle(level, blockPos, blockState, fluid));
			}
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		spawnFallingStalactite(blockState, serverLevel, blockPos);
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		maybeFillCauldron(blockState, serverLevel, blockPos, random.nextFloat());
	}

	@VisibleForTesting
	public static void maybeFillCauldron(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, float f) {
		if (!(f > 0.17578125F) || !(f > 0.05859375F)) {
			if (isStalactiteStartPos(blockState, serverLevel, blockPos)) {
				Fluid fluid = getCauldronFillFluidType(serverLevel, blockPos);
				float g;
				if (fluid == Fluids.WATER) {
					g = 0.17578125F;
				} else {
					if (fluid != Fluids.LAVA) {
						return;
					}

					g = 0.05859375F;
				}

				if (!(f >= g)) {
					BlockPos blockPos2 = findTip(blockState, serverLevel, blockPos, 10);
					if (blockPos2 != null) {
						BlockPos blockPos3 = findFillableCauldronBelowStalactiteTip(serverLevel, blockPos2, fluid);
						if (blockPos3 != null) {
							serverLevel.levelEvent(1504, blockPos2, 0);
							int i = blockPos2.getY() - blockPos3.getY();
							int j = 50 + i;
							BlockState blockState2 = serverLevel.getBlockState(blockPos3);
							serverLevel.getBlockTicks().scheduleTick(blockPos3, blockState2.getBlock(), j);
						}
					}
				}
			}
		}
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState blockState) {
		return PushReaction.DESTROY;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		LevelAccessor levelAccessor = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		Direction direction = blockPlaceContext.getNearestLookingVerticalDirection().getOpposite();
		Direction direction2 = calculateTipDirection(levelAccessor, blockPos, direction);
		if (direction2 == null) {
			return null;
		} else {
			boolean bl = !blockPlaceContext.isSecondaryUseActive();
			DripstoneThickness dripstoneThickness = calculateDripstoneThickness(levelAccessor, blockPos, direction2, bl);
			return dripstoneThickness == null
				? null
				: this.defaultBlockState()
					.setValue(TIP_DIRECTION, direction2)
					.setValue(THICKNESS, dripstoneThickness)
					.setValue(WATERLOGGED, Boolean.valueOf(levelAccessor.getFluidState(blockPos).getType() == Fluids.WATER));
		}
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return Shapes.empty();
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		DripstoneThickness dripstoneThickness = blockState.getValue(THICKNESS);
		VoxelShape voxelShape;
		if (dripstoneThickness == DripstoneThickness.TIP_MERGE) {
			voxelShape = TIP_MERGE_SHAPE;
		} else if (dripstoneThickness == DripstoneThickness.TIP) {
			if (blockState.getValue(TIP_DIRECTION) == Direction.DOWN) {
				voxelShape = TIP_SHAPE_DOWN;
			} else {
				voxelShape = TIP_SHAPE_UP;
			}
		} else if (dripstoneThickness == DripstoneThickness.FRUSTUM) {
			voxelShape = FRUSTUM_SHAPE;
		} else if (dripstoneThickness == DripstoneThickness.MIDDLE) {
			voxelShape = MIDDLE_SHAPE;
		} else {
			voxelShape = BASE_SHAPE;
		}

		Vec3 vec3 = blockState.getOffset(blockGetter, blockPos);
		return voxelShape.move(vec3.x, 0.0, vec3.z);
	}

	@Override
	public BlockBehaviour.OffsetType getOffsetType() {
		return BlockBehaviour.OffsetType.XZ;
	}

	@Override
	public float getMaxHorizontalOffset() {
		return 0.125F;
	}

	@Override
	public void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
		if (!fallingBlockEntity.isSilent()) {
			level.levelEvent(1045, blockPos, 0);
		}
	}

	@Override
	public DamageSource getFallDamageSource() {
		return DamageSource.FALLING_STALACTITE;
	}

	@Override
	public Predicate<Entity> getHurtsEntitySelector() {
		return EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE);
	}

	private void scheduleStalactiteFallTicks(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockPos blockPos2 = findTip(blockState, levelAccessor, blockPos, Integer.MAX_VALUE);
		if (blockPos2 != null) {
			BlockPos.MutableBlockPos mutableBlockPos = blockPos2.mutable();

			while (isStalactite(levelAccessor.getBlockState(mutableBlockPos))) {
				levelAccessor.getBlockTicks().scheduleTick(mutableBlockPos, this, 2);
				mutableBlockPos.move(Direction.UP);
			}
		}
	}

	private static int getStalactiteSizeFromTip(ServerLevel serverLevel, BlockPos blockPos, int i) {
		int j = 1;
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.UP);

		while (j < i && isStalactite(serverLevel.getBlockState(mutableBlockPos))) {
			j++;
			mutableBlockPos.move(Direction.UP);
		}

		return j;
	}

	private static void spawnFallingStalactite(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
		Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
		FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(serverLevel, vec3.x, vec3.y, vec3.z, blockState);
		if (isTip(blockState)) {
			int i = getStalactiteSizeFromTip(serverLevel, blockPos, 6);
			float f = 1.0F * (float)i;
			fallingBlockEntity.setHurtsEntities(f, 40);
		}

		serverLevel.addFreshEntity(fallingBlockEntity);
	}

	@Environment(EnvType.CLIENT)
	public static void spawnDripParticle(Level level, BlockPos blockPos, BlockState blockState) {
		getFluidAboveStalactite(level, blockPos, blockState).ifPresent(fluid -> spawnDripParticle(level, blockPos, blockState, fluid));
	}

	@Environment(EnvType.CLIENT)
	private static void spawnDripParticle(Level level, BlockPos blockPos, BlockState blockState, Fluid fluid) {
		Vec3 vec3 = blockState.getOffset(level, blockPos);
		double d = 0.0625;
		double e = (double)blockPos.getX() + 0.5 + vec3.x;
		double f = (double)((float)(blockPos.getY() + 1) - 0.6875F) - 0.0625;
		double g = (double)blockPos.getZ() + 0.5 + vec3.z;
		Fluid fluid2 = getDripFluid(level, fluid);
		ParticleOptions particleOptions = fluid2.is(FluidTags.LAVA) ? ParticleTypes.DRIPPING_DRIPSTONE_LAVA : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
		level.addParticle(particleOptions, e, f, g, 0.0, 0.0, 0.0);
	}

	@Nullable
	private static BlockPos findTip(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i) {
		if (isTip(blockState)) {
			return blockPos;
		} else {
			Direction direction = blockState.getValue(TIP_DIRECTION);
			Predicate<BlockState> predicate = blockStatex -> blockStatex.is(Blocks.POINTED_DRIPSTONE) && blockStatex.getValue(TIP_DIRECTION) == direction;
			return (BlockPos)findBlockVertical(levelAccessor, blockPos, direction.getAxisDirection(), predicate, PointedDripstoneBlock::isTip, i).orElse(null);
		}
	}

	@Nullable
	private static Direction calculateTipDirection(LevelReader levelReader, BlockPos blockPos, Direction direction) {
		Direction direction2;
		if (isValidPointedDripstonePlacement(levelReader, blockPos, direction)) {
			direction2 = direction;
		} else {
			if (!isValidPointedDripstonePlacement(levelReader, blockPos, direction.getOpposite())) {
				return null;
			}

			direction2 = direction.getOpposite();
		}

		return direction2;
	}

	@Nullable
	private static DripstoneThickness calculateDripstoneThickness(LevelReader levelReader, BlockPos blockPos, Direction direction, boolean bl) {
		Direction direction2 = direction.getOpposite();
		BlockState blockState = levelReader.getBlockState(blockPos.relative(direction));
		if (isPointedDripstoneWithDirection(blockState, direction2)) {
			return !bl && blockState.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE ? DripstoneThickness.TIP : DripstoneThickness.TIP_MERGE;
		} else if (!isPointedDripstoneWithDirection(blockState, direction)) {
			return DripstoneThickness.TIP;
		} else {
			DripstoneThickness dripstoneThickness = blockState.getValue(THICKNESS);
			if (dripstoneThickness != DripstoneThickness.TIP && dripstoneThickness != DripstoneThickness.TIP_MERGE) {
				BlockState blockState2 = levelReader.getBlockState(blockPos.relative(direction2));
				if (dripstoneThickness != DripstoneThickness.FRUSTUM && dripstoneThickness != DripstoneThickness.MIDDLE) {
					return null;
				} else {
					return isPointedDripstoneWithDirection(blockState2, direction) ? DripstoneThickness.MIDDLE : DripstoneThickness.BASE;
				}
			} else {
				return DripstoneThickness.FRUSTUM;
			}
		}
	}

	public static boolean canDrip(BlockState blockState) {
		return isStalactite(blockState) && blockState.getValue(THICKNESS) == DripstoneThickness.TIP && !(Boolean)blockState.getValue(WATERLOGGED);
	}

	private static Optional<BlockPos> findRootBlock(Level level, BlockPos blockPos, BlockState blockState, int i) {
		Direction direction = blockState.getValue(TIP_DIRECTION);
		Predicate<BlockState> predicate = blockStatex -> blockStatex.is(Blocks.POINTED_DRIPSTONE) && blockStatex.getValue(TIP_DIRECTION) == direction;
		return findBlockVertical(level, blockPos, direction.getOpposite().getAxisDirection(), predicate, blockStatex -> !blockStatex.is(Blocks.POINTED_DRIPSTONE), i);
	}

	private static boolean isValidPointedDripstonePlacement(LevelReader levelReader, BlockPos blockPos, Direction direction) {
		BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
		BlockState blockState = levelReader.getBlockState(blockPos2);
		return blockState.isFaceSturdy(levelReader, blockPos2, direction) || isPointedDripstoneWithDirection(blockState, direction);
	}

	private static boolean isTip(BlockState blockState) {
		if (!blockState.is(Blocks.POINTED_DRIPSTONE)) {
			return false;
		} else {
			DripstoneThickness dripstoneThickness = blockState.getValue(THICKNESS);
			return dripstoneThickness == DripstoneThickness.TIP || dripstoneThickness == DripstoneThickness.TIP_MERGE;
		}
	}

	private static boolean isStalactite(BlockState blockState) {
		return isPointedDripstoneWithDirection(blockState, Direction.DOWN);
	}

	private static boolean isStalactiteStartPos(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return isStalactite(blockState) && !levelReader.getBlockState(blockPos.above()).is(Blocks.POINTED_DRIPSTONE);
	}

	private static boolean isPointedDripstoneWithDirection(BlockState blockState, Direction direction) {
		return blockState.is(Blocks.POINTED_DRIPSTONE) && blockState.getValue(TIP_DIRECTION) == direction;
	}

	@Nullable
	private static BlockPos findFillableCauldronBelowStalactiteTip(Level level, BlockPos blockPos, Fluid fluid) {
		Predicate<BlockState> predicate = blockState -> blockState.getBlock() instanceof AbstractCauldronBlock
				&& ((AbstractCauldronBlock)blockState.getBlock()).canReceiveStalactiteDrip(fluid);
		return (BlockPos)findBlockVertical(level, blockPos, Direction.DOWN.getAxisDirection(), BlockBehaviour.BlockStateBase::isAir, predicate, 10).orElse(null);
	}

	@Nullable
	public static BlockPos findStalactiteTipAboveCauldron(Level level, BlockPos blockPos) {
		return (BlockPos)findBlockVertical(level, blockPos, Direction.UP.getAxisDirection(), BlockBehaviour.BlockStateBase::isAir, PointedDripstoneBlock::canDrip, 10)
			.orElse(null);
	}

	public static Fluid getCauldronFillFluidType(Level level, BlockPos blockPos) {
		return (Fluid)getFluidAboveStalactite(level, blockPos, level.getBlockState(blockPos)).filter(PointedDripstoneBlock::canFillCauldron).orElse(Fluids.EMPTY);
	}

	private static Optional<Fluid> getFluidAboveStalactite(Level level, BlockPos blockPos, BlockState blockState) {
		return !isStalactite(blockState)
			? Optional.empty()
			: findRootBlock(level, blockPos, blockState, 10).map(blockPosx -> level.getFluidState(blockPosx.above()).getType());
	}

	private static boolean canFillCauldron(Fluid fluid) {
		return fluid == Fluids.LAVA || fluid == Fluids.WATER;
	}

	@Environment(EnvType.CLIENT)
	private static Fluid getDripFluid(Level level, Fluid fluid) {
		if (fluid.isSame(Fluids.EMPTY)) {
			return level.dimensionType().ultraWarm() ? Fluids.LAVA : Fluids.WATER;
		} else {
			return fluid;
		}
	}

	private static Optional<BlockPos> findBlockVertical(
		LevelAccessor levelAccessor,
		BlockPos blockPos,
		Direction.AxisDirection axisDirection,
		Predicate<BlockState> predicate,
		Predicate<BlockState> predicate2,
		int i
	) {
		Direction direction = Direction.get(axisDirection, Direction.Axis.Y);
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int j = 0; j < i; j++) {
			mutableBlockPos.move(direction);
			BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
			if (predicate2.test(blockState)) {
				return Optional.of(mutableBlockPos);
			}

			if (levelAccessor.isOutsideBuildHeight(mutableBlockPos.getY()) || !predicate.test(blockState)) {
				return Optional.empty();
			}
		}

		return Optional.empty();
	}
}
