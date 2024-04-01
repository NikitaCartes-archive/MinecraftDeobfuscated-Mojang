package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PointedDripstoneBlock extends Block implements Fallable, SimpleWaterloggedBlock {
	public static final MapCodec<PointedDripstoneBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(Codec.BOOL.fieldOf("organic").forGetter(pointedDripstoneBlock -> pointedDripstoneBlock.organic), propertiesCodec())
				.apply(instance, PointedDripstoneBlock::new)
	);
	public static final DirectionProperty TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
	public static final EnumProperty<DripstoneThickness> THICKNESS = BlockStateProperties.DRIPSTONE_THICKNESS;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final int MAX_SEARCH_LENGTH_WHEN_CHECKING_DRIP_TYPE = 11;
	private static final int DELAY_BEFORE_FALLING = 2;
	private static final float DRIP_PROBABILITY_PER_ANIMATE_TICK = 0.02F;
	private static final float DRIP_PROBABILITY_PER_ANIMATE_TICK_IF_UNDER_LIQUID_SOURCE = 0.12F;
	private static final int MAX_SEARCH_LENGTH_BETWEEN_STALACTITE_TIP_AND_CAULDRON = 11;
	private static final float WATER_TRANSFER_PROBABILITY_PER_RANDOM_TICK = 0.17578125F;
	private static final float LAVA_TRANSFER_PROBABILITY_PER_RANDOM_TICK = 0.05859375F;
	private static final double MIN_TRIDENT_VELOCITY_TO_BREAK_DRIPSTONE = 0.6;
	private static final float STALACTITE_DAMAGE_PER_FALL_DISTANCE_AND_SIZE = 1.0F;
	private static final int STALACTITE_MAX_DAMAGE = 40;
	private static final int MAX_STALACTITE_HEIGHT_FOR_DAMAGE_CALCULATION = 6;
	private static final float STALAGMITE_FALL_DISTANCE_OFFSET = 2.0F;
	private static final int STALAGMITE_FALL_DAMAGE_MODIFIER = 2;
	private static final float AVERAGE_DAYS_PER_GROWTH = 5.0F;
	private static final float GROWTH_PROBABILITY_PER_RANDOM_TICK = 0.011377778F;
	private static final int MAX_GROWTH_LENGTH = 7;
	private static final int MAX_STALAGMITE_SEARCH_RANGE_WHEN_GROWING = 10;
	private static final float STALACTITE_DRIP_START_PIXEL = 0.6875F;
	private static final VoxelShape TIP_MERGE_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
	private static final VoxelShape TIP_SHAPE_UP = Block.box(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
	private static final VoxelShape TIP_SHAPE_DOWN = Block.box(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
	private static final VoxelShape FRUSTUM_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
	private static final VoxelShape MIDDLE_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
	private static final VoxelShape BASE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
	private static final float MAX_HORIZONTAL_OFFSET = 0.125F;
	private static final VoxelShape REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
	private final boolean organic;

	public boolean isBase(BlockState blockState) {
		return this.organic ? blockState.is(BlockTags.POTATOSTONE_BASE) : blockState.is(Blocks.DRIPSTONE_BLOCK);
	}

	@Override
	public MapCodec<PointedDripstoneBlock> codec() {
		return CODEC;
	}

	public PointedDripstoneBlock(boolean bl, BlockBehaviour.Properties properties) {
		super(properties);
		this.organic = bl;
		this.registerDefaultState(
			this.stateDefinition.any().setValue(TIP_DIRECTION, Direction.UP).setValue(THICKNESS, DripstoneThickness.TIP).setValue(WATERLOGGED, Boolean.valueOf(false))
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TIP_DIRECTION, THICKNESS, WATERLOGGED);
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return this.isValidPointedDripstonePlacement(levelReader, blockPos, blockState.getValue(TIP_DIRECTION));
	}

	public Block getLargeBlock() {
		return this.organic ? Blocks.TERREDEPOMME : Blocks.DRIPSTONE_BLOCK;
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		if (direction != Direction.UP && direction != Direction.DOWN) {
			return blockState;
		} else {
			Direction direction2 = blockState.getValue(TIP_DIRECTION);
			if (direction2 == Direction.DOWN && levelAccessor.getBlockTicks().hasScheduledTick(blockPos, this)) {
				return blockState;
			} else if (direction == direction2.getOpposite() && !this.canSurvive(blockState, levelAccessor, blockPos)) {
				if (direction2 == Direction.DOWN) {
					levelAccessor.scheduleTick(blockPos, this, 2);
				} else {
					levelAccessor.scheduleTick(blockPos, this, 1);
				}

				return blockState;
			} else {
				boolean bl = blockState.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
				DripstoneThickness dripstoneThickness = this.calculateDripstoneThickness(levelAccessor, blockPos, direction2, bl);
				return blockState.setValue(THICKNESS, dripstoneThickness);
			}
		}
	}

	@Override
	protected void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
		if (!level.isClientSide) {
			BlockPos blockPos = blockHitResult.getBlockPos();
			if (projectile.mayInteract(level, blockPos)
				&& projectile.mayBreak(level)
				&& projectile instanceof ThrownTrident
				&& projectile.getDeltaMovement().length() > 0.6) {
				level.destroyBlock(blockPos, true);
			}
		}
	}

	@Override
	public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, float f) {
		if (blockState.getValue(TIP_DIRECTION) == Direction.UP && blockState.getValue(THICKNESS) == DripstoneThickness.TIP) {
			entity.causeFallDamage(f + 2.0F, 2.0F, level.damageSources().stalagmite());
		} else {
			super.fallOn(level, blockState, blockPos, entity, f);
		}
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if (this.canDrip(blockState)) {
			float f = randomSource.nextFloat();
			if (!(f > 0.12F)) {
				this.getFluidAboveStalactite(level, blockPos, blockState)
					.filter(fluidInfo -> f < 0.02F || canFillCauldron(fluidInfo.fluid))
					.ifPresent(fluidInfo -> spawnDripParticle(level, blockPos, blockState, fluidInfo.fluid));
			}
		}
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (this.isStalagmite(blockState) && !this.canSurvive(blockState, serverLevel, blockPos)) {
			serverLevel.destroyBlock(blockPos, true);
		} else {
			this.spawnFallingStalactite(blockState, serverLevel, blockPos);
		}
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		this.maybeTransferFluid(blockState, serverLevel, blockPos, randomSource.nextFloat());
		float f = 0.011377778F;
		if (this.organic) {
			f = 0.2F;
		}

		if (randomSource.nextFloat() < f) {
			if (this.organic) {
				this.growUp(serverLevel, blockPos);
				this.growDown(serverLevel, blockPos);
			} else if (this.isStalactiteStartPos(blockState, serverLevel, blockPos)) {
				this.growStalactiteOrStalagmiteIfPossible(blockState, serverLevel, blockPos, randomSource);
			}
		}
	}

	@VisibleForTesting
	public void maybeTransferFluid(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, float f) {
		if (!(f > 0.17578125F) || !(f > 0.05859375F)) {
			if (this.isStalactiteStartPos(blockState, serverLevel, blockPos)) {
				Optional<PointedDripstoneBlock.FluidInfo> optional = this.getFluidAboveStalactite(serverLevel, blockPos, blockState);
				if (!optional.isEmpty()) {
					Fluid fluid = ((PointedDripstoneBlock.FluidInfo)optional.get()).fluid;
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
						BlockPos blockPos2 = this.findTip(blockState, serverLevel, blockPos, 11, false);
						if (blockPos2 != null) {
							if (((PointedDripstoneBlock.FluidInfo)optional.get()).sourceState.is(Blocks.MUD) && fluid == Fluids.WATER) {
								BlockState blockState2 = Blocks.CLAY.defaultBlockState();
								serverLevel.setBlockAndUpdate(((PointedDripstoneBlock.FluidInfo)optional.get()).pos, blockState2);
								Block.pushEntitiesUp(
									((PointedDripstoneBlock.FluidInfo)optional.get()).sourceState, blockState2, serverLevel, ((PointedDripstoneBlock.FluidInfo)optional.get()).pos
								);
								serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, ((PointedDripstoneBlock.FluidInfo)optional.get()).pos, GameEvent.Context.of(blockState2));
								serverLevel.levelEvent(1504, blockPos2, 0);
							} else {
								BlockPos blockPos3 = findFillableCauldronBelowStalactiteTip(serverLevel, blockPos2, fluid);
								if (blockPos3 != null) {
									serverLevel.levelEvent(1504, blockPos2, 0);
									int i = blockPos2.getY() - blockPos3.getY();
									int j = 50 + i;
									BlockState blockState3 = serverLevel.getBlockState(blockPos3);
									serverLevel.scheduleTick(blockPos3, blockState3.getBlock(), j);
								}
							}
						}
					}
				}
			}
		}
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		LevelAccessor levelAccessor = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		Direction direction = blockPlaceContext.getNearestLookingVerticalDirection().getOpposite();
		Direction direction2 = this.calculateTipDirection(levelAccessor, blockPos, direction);
		if (direction2 == null) {
			return null;
		} else {
			boolean bl = !blockPlaceContext.isSecondaryUseActive();
			DripstoneThickness dripstoneThickness = this.calculateDripstoneThickness(levelAccessor, blockPos, direction2, bl);
			return dripstoneThickness == null
				? null
				: this.defaultBlockState()
					.setValue(TIP_DIRECTION, direction2)
					.setValue(THICKNESS, dripstoneThickness)
					.setValue(WATERLOGGED, Boolean.valueOf(levelAccessor.getFluidState(blockPos).getType() == Fluids.WATER));
		}
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return Shapes.empty();
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
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
	protected boolean isCollisionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return false;
	}

	@Override
	protected float getMaxHorizontalOffset() {
		return 0.125F;
	}

	@Override
	public void onBrokenAfterFall(Level level, BlockPos blockPos, FallingBlockEntity fallingBlockEntity) {
		if (!fallingBlockEntity.isSilent()) {
			level.levelEvent(1045, blockPos, 0);
		}
	}

	@Override
	public DamageSource getFallDamageSource(Entity entity) {
		return entity.damageSources().fallingStalactite(entity);
	}

	private void spawnFallingStalactite(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		BlockState blockState2 = blockState;

		while (this.isStalactite(blockState2)) {
			FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(serverLevel, mutableBlockPos, blockState2);
			if (this.isTip(blockState2, true)) {
				int i = Math.max(1 + blockPos.getY() - mutableBlockPos.getY(), 6);
				float f = 1.0F * (float)i;
				fallingBlockEntity.setHurtsEntities(f, 40);
				break;
			}

			mutableBlockPos.move(Direction.DOWN);
			blockState2 = serverLevel.getBlockState(mutableBlockPos);
		}
	}

	@VisibleForTesting
	public void growStalactiteOrStalagmiteIfPossible(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		BlockState blockState2 = serverLevel.getBlockState(blockPos.above(1));
		BlockState blockState3 = serverLevel.getBlockState(blockPos.above(2));
		if (this.canGrow(blockState2, blockState3)) {
			BlockPos blockPos2 = this.findTip(blockState, serverLevel, blockPos, 7, false);
			if (blockPos2 != null) {
				BlockState blockState4 = serverLevel.getBlockState(blockPos2);
				if (this.canDrip(blockState4) && this.canTipGrow(blockState4, serverLevel, blockPos2)) {
					if (randomSource.nextBoolean()) {
						this.grow(serverLevel, blockPos2, Direction.DOWN);
					} else {
						this.growStalagmiteBelow(serverLevel, blockPos2);
					}
				}
			}
		}
	}

	public void growUp(ServerLevel serverLevel, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = 0; i < 3; i++) {
			BlockState blockState = serverLevel.getBlockState(mutableBlockPos);
			if (this.isUnmergedTipWithDirection(blockState, Direction.UP) && !serverLevel.getBlockState(mutableBlockPos.offset(0, -24, 0)).is(this)) {
				this.grow(serverLevel, mutableBlockPos, Direction.UP);
				return;
			}

			mutableBlockPos.move(Direction.UP);
		}
	}

	public void growDown(ServerLevel serverLevel, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = 0; i < 3; i++) {
			BlockState blockState = serverLevel.getBlockState(mutableBlockPos);
			if (this.isUnmergedTipWithDirection(blockState, Direction.DOWN) && !serverLevel.getBlockState(mutableBlockPos.offset(0, 24, 0)).is(this)) {
				this.grow(serverLevel, mutableBlockPos, Direction.DOWN);
				return;
			}

			mutableBlockPos.move(Direction.DOWN);
		}
	}

	private void growStalagmiteBelow(ServerLevel serverLevel, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = 0; i < 10; i++) {
			mutableBlockPos.move(Direction.DOWN);
			BlockState blockState = serverLevel.getBlockState(mutableBlockPos);
			if (!blockState.getFluidState().isEmpty()) {
				return;
			}

			if (this.isUnmergedTipWithDirection(blockState, Direction.UP) && this.canTipGrow(blockState, serverLevel, mutableBlockPos)) {
				this.grow(serverLevel, mutableBlockPos, Direction.UP);
				return;
			}

			if (this.isValidPointedDripstonePlacement(serverLevel, mutableBlockPos, Direction.UP) && !serverLevel.isWaterAt(mutableBlockPos.below())) {
				this.grow(serverLevel, mutableBlockPos.below(), Direction.UP);
				return;
			}

			if (!canDripThrough(serverLevel, mutableBlockPos, blockState)) {
				return;
			}
		}
	}

	private void grow(ServerLevel serverLevel, BlockPos blockPos, Direction direction) {
		BlockPos blockPos2 = blockPos.relative(direction);
		BlockState blockState = serverLevel.getBlockState(blockPos2);
		if (this.isUnmergedTipWithDirection(blockState, direction.getOpposite())) {
			this.createMergedTips(blockState, serverLevel, blockPos2);
		} else if (blockState.isAir() || blockState.is(Blocks.WATER)) {
			this.createDripstone(serverLevel, blockPos2, direction, DripstoneThickness.TIP);
		}
	}

	private void createDripstone(LevelAccessor levelAccessor, BlockPos blockPos, Direction direction, DripstoneThickness dripstoneThickness) {
		BlockState blockState = this.defaultBlockState()
			.setValue(TIP_DIRECTION, direction)
			.setValue(THICKNESS, dripstoneThickness)
			.setValue(WATERLOGGED, Boolean.valueOf(levelAccessor.getFluidState(blockPos).getType() == Fluids.WATER));
		levelAccessor.setBlock(blockPos, blockState, 3);
	}

	private void createMergedTips(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockPos blockPos3;
		BlockPos blockPos2;
		if (blockState.getValue(TIP_DIRECTION) == Direction.UP) {
			blockPos2 = blockPos;
			blockPos3 = blockPos.above();
		} else {
			blockPos3 = blockPos;
			blockPos2 = blockPos.below();
		}

		this.createDripstone(levelAccessor, blockPos3, Direction.DOWN, DripstoneThickness.TIP_MERGE);
		this.createDripstone(levelAccessor, blockPos2, Direction.UP, DripstoneThickness.TIP_MERGE);
	}

	public static void spawnDripParticle(Level level, BlockPos blockPos, BlockState blockState) {
		if (blockState.getBlock() instanceof PointedDripstoneBlock pointedDripstoneBlock) {
			pointedDripstoneBlock.getFluidAboveStalactite(level, blockPos, blockState)
				.ifPresent(fluidInfo -> spawnDripParticle(level, blockPos, blockState, fluidInfo.fluid));
		}
	}

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
	private BlockPos findTip(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i, boolean bl) {
		if (this.isTip(blockState, bl)) {
			return blockPos;
		} else {
			Direction direction = blockState.getValue(TIP_DIRECTION);
			BiPredicate<BlockPos, BlockState> biPredicate = (blockPosx, blockStatex) -> blockStatex.is(this) && blockStatex.getValue(TIP_DIRECTION) == direction;
			return (BlockPos)findBlockVertical(levelAccessor, blockPos, direction.getAxisDirection(), biPredicate, blockStatex -> this.isTip(blockStatex, bl), i)
				.orElse(null);
		}
	}

	@Nullable
	private Direction calculateTipDirection(LevelReader levelReader, BlockPos blockPos, Direction direction) {
		Direction direction2;
		if (this.isValidPointedDripstonePlacement(levelReader, blockPos, direction)) {
			direction2 = direction;
		} else {
			if (!this.isValidPointedDripstonePlacement(levelReader, blockPos, direction.getOpposite())) {
				return null;
			}

			direction2 = direction.getOpposite();
		}

		return direction2;
	}

	private DripstoneThickness calculateDripstoneThickness(LevelReader levelReader, BlockPos blockPos, Direction direction, boolean bl) {
		Direction direction2 = direction.getOpposite();
		BlockState blockState = levelReader.getBlockState(blockPos.relative(direction));
		if (this.isPointedDripstoneWithDirection(blockState, direction2)) {
			return !bl && blockState.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE ? DripstoneThickness.TIP : DripstoneThickness.TIP_MERGE;
		} else if (!this.isPointedDripstoneWithDirection(blockState, direction)) {
			return DripstoneThickness.TIP;
		} else {
			DripstoneThickness dripstoneThickness = blockState.getValue(THICKNESS);
			if (dripstoneThickness != DripstoneThickness.TIP && dripstoneThickness != DripstoneThickness.TIP_MERGE) {
				BlockState blockState2 = levelReader.getBlockState(blockPos.relative(direction2));
				return !this.isPointedDripstoneWithDirection(blockState2, direction) ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
			} else {
				return DripstoneThickness.FRUSTUM;
			}
		}
	}

	public boolean canDrip(BlockState blockState) {
		return this.isStalactite(blockState) && blockState.getValue(THICKNESS) == DripstoneThickness.TIP && !(Boolean)blockState.getValue(WATERLOGGED);
	}

	private boolean canTipGrow(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
		Direction direction = blockState.getValue(TIP_DIRECTION);
		BlockPos blockPos2 = blockPos.relative(direction);
		BlockState blockState2 = serverLevel.getBlockState(blockPos2);
		if (!blockState2.getFluidState().isEmpty()) {
			return false;
		} else {
			return blockState2.isAir() ? true : this.isUnmergedTipWithDirection(blockState2, direction.getOpposite());
		}
	}

	private Optional<BlockPos> findRootBlock(Level level, BlockPos blockPos, BlockState blockState, int i) {
		Direction direction = blockState.getValue(TIP_DIRECTION);
		BiPredicate<BlockPos, BlockState> biPredicate = (blockPosx, blockStatex) -> blockStatex.is(this) && blockStatex.getValue(TIP_DIRECTION) == direction;
		return findBlockVertical(level, blockPos, direction.getOpposite().getAxisDirection(), biPredicate, blockStatex -> !blockStatex.is(this), i);
	}

	private boolean isValidPointedDripstonePlacement(LevelReader levelReader, BlockPos blockPos, Direction direction) {
		BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
		BlockState blockState = levelReader.getBlockState(blockPos2);
		return (this.organic ? this.isBase(blockState) : blockState.isFaceSturdy(levelReader, blockPos2, direction))
			|| this.isPointedDripstoneWithDirection(blockState, direction);
	}

	private boolean isTip(BlockState blockState, boolean bl) {
		if (!blockState.is(this)) {
			return false;
		} else {
			DripstoneThickness dripstoneThickness = blockState.getValue(THICKNESS);
			return dripstoneThickness == DripstoneThickness.TIP || bl && dripstoneThickness == DripstoneThickness.TIP_MERGE;
		}
	}

	private boolean isUnmergedTipWithDirection(BlockState blockState, Direction direction) {
		return this.isTip(blockState, false) && blockState.getValue(TIP_DIRECTION) == direction;
	}

	private boolean isStalactite(BlockState blockState) {
		return this.isPointedDripstoneWithDirection(blockState, Direction.DOWN);
	}

	private boolean isStalagmite(BlockState blockState) {
		return this.isPointedDripstoneWithDirection(blockState, Direction.UP);
	}

	private boolean isStalactiteStartPos(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return this.isStalactite(blockState) && !levelReader.getBlockState(blockPos.above()).is(this);
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}

	private boolean isPointedDripstoneWithDirection(BlockState blockState, Direction direction) {
		return blockState.is(this) && blockState.getValue(TIP_DIRECTION) == direction;
	}

	@Nullable
	private static BlockPos findFillableCauldronBelowStalactiteTip(Level level, BlockPos blockPos, Fluid fluid) {
		Predicate<BlockState> predicate = blockState -> blockState.getBlock() instanceof AbstractCauldronBlock
				&& ((AbstractCauldronBlock)blockState.getBlock()).canReceiveStalactiteDrip(fluid);
		BiPredicate<BlockPos, BlockState> biPredicate = (blockPosx, blockState) -> canDripThrough(level, blockPosx, blockState);
		return (BlockPos)findBlockVertical(level, blockPos, Direction.DOWN.getAxisDirection(), biPredicate, predicate, 11).orElse(null);
	}

	@Nullable
	public static BlockPos findStalactiteTipAboveCauldron(Block block, Level level, BlockPos blockPos) {
		if (block instanceof PointedDripstoneBlock pointedDripstoneBlock) {
			BiPredicate biPredicate = (blockPosx, blockState) -> canDripThrough(level, blockPosx, blockState);
			return (BlockPos)findBlockVertical(level, blockPos, Direction.UP.getAxisDirection(), biPredicate, pointedDripstoneBlock::canDrip, 11).orElse(null);
		} else {
			return null;
		}
	}

	public Fluid getCauldronFillFluidType(ServerLevel serverLevel, BlockPos blockPos) {
		return (Fluid)this.getFluidAboveStalactite(serverLevel, blockPos, serverLevel.getBlockState(blockPos))
			.map(fluidInfo -> fluidInfo.fluid)
			.filter(PointedDripstoneBlock::canFillCauldron)
			.orElse(Fluids.EMPTY);
	}

	private Optional<PointedDripstoneBlock.FluidInfo> getFluidAboveStalactite(Level level, BlockPos blockPos, BlockState blockState) {
		return !this.isStalactite(blockState) ? Optional.empty() : this.findRootBlock(level, blockPos, blockState, 11).map(blockPosx -> {
			BlockPos blockPos2 = blockPosx.above();
			BlockState blockStatex = level.getBlockState(blockPos2);
			Fluid fluid;
			if (blockStatex.is(Blocks.MUD) && !level.dimensionType().ultraWarm()) {
				fluid = Fluids.WATER;
			} else {
				fluid = level.getFluidState(blockPos2).getType();
			}

			return new PointedDripstoneBlock.FluidInfo(blockPos2, fluid, blockStatex);
		});
	}

	private static boolean canFillCauldron(Fluid fluid) {
		return fluid == Fluids.LAVA || fluid == Fluids.WATER;
	}

	private boolean canGrow(BlockState blockState, BlockState blockState2) {
		return blockState.is(Blocks.DRIPSTONE_BLOCK) && blockState2.is(Blocks.WATER) && blockState2.getFluidState().isSource();
	}

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
		BiPredicate<BlockPos, BlockState> biPredicate,
		Predicate<BlockState> predicate,
		int i
	) {
		Direction direction = Direction.get(axisDirection, Direction.Axis.Y);
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int j = 1; j < i; j++) {
			mutableBlockPos.move(direction);
			BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
			if (predicate.test(blockState)) {
				return Optional.of(mutableBlockPos.immutable());
			}

			if (levelAccessor.isOutsideBuildHeight(mutableBlockPos.getY()) || !biPredicate.test(mutableBlockPos, blockState)) {
				return Optional.empty();
			}
		}

		return Optional.empty();
	}

	private static boolean canDripThrough(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		if (blockState.isAir()) {
			return true;
		} else if (blockState.isSolidRender(blockGetter, blockPos)) {
			return false;
		} else if (!blockState.getFluidState().isEmpty()) {
			return false;
		} else {
			VoxelShape voxelShape = blockState.getCollisionShape(blockGetter, blockPos);
			return !Shapes.joinIsNotEmpty(REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK, voxelShape, BooleanOp.AND);
		}
	}

	static record FluidInfo(BlockPos pos, Fluid fluid, BlockState sourceState) {
	}
}
