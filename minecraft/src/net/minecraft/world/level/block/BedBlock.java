package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.ArrayUtils;

public class BedBlock extends HorizontalDirectionalBlock implements EntityBlock {
	public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
	public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
	protected static final int HEIGHT = 9;
	protected static final VoxelShape BASE = Block.box(0.0, 3.0, 0.0, 16.0, 9.0, 16.0);
	private static final int LEG_WIDTH = 3;
	protected static final VoxelShape LEG_NORTH_WEST = Block.box(0.0, 0.0, 0.0, 3.0, 3.0, 3.0);
	protected static final VoxelShape LEG_SOUTH_WEST = Block.box(0.0, 0.0, 13.0, 3.0, 3.0, 16.0);
	protected static final VoxelShape LEG_NORTH_EAST = Block.box(13.0, 0.0, 0.0, 16.0, 3.0, 3.0);
	protected static final VoxelShape LEG_SOUTH_EAST = Block.box(13.0, 0.0, 13.0, 16.0, 3.0, 16.0);
	protected static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, LEG_NORTH_WEST, LEG_NORTH_EAST);
	protected static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, LEG_SOUTH_WEST, LEG_SOUTH_EAST);
	protected static final VoxelShape WEST_SHAPE = Shapes.or(BASE, LEG_NORTH_WEST, LEG_SOUTH_WEST);
	protected static final VoxelShape EAST_SHAPE = Shapes.or(BASE, LEG_NORTH_EAST, LEG_SOUTH_EAST);
	private final DyeColor color;

	public BedBlock(DyeColor dyeColor, BlockBehaviour.Properties properties) {
		super(properties);
		this.color = dyeColor;
		this.registerDefaultState(this.stateDefinition.any().setValue(PART, BedPart.FOOT).setValue(OCCUPIED, Boolean.valueOf(false)));
	}

	@Nullable
	public static Direction getBedOrientation(BlockGetter blockGetter, BlockPos blockPos) {
		BlockState blockState = blockGetter.getBlockState(blockPos);
		return blockState.getBlock() instanceof BedBlock ? blockState.getValue(FACING) : null;
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (level.isClientSide) {
			return InteractionResult.CONSUME;
		} else {
			if (blockState.getValue(PART) != BedPart.HEAD) {
				blockPos = blockPos.relative(blockState.getValue(FACING));
				blockState = level.getBlockState(blockPos);
				if (!blockState.is(this)) {
					return InteractionResult.CONSUME;
				}
			}

			if (!canSetSpawn(level)) {
				level.removeBlock(blockPos, false);
				BlockPos blockPos2 = blockPos.relative(((Direction)blockState.getValue(FACING)).getOpposite());
				if (level.getBlockState(blockPos2).is(this)) {
					level.removeBlock(blockPos2, false);
				}

				level.explode(
					null,
					DamageSource.badRespawnPointExplosion(),
					null,
					(double)blockPos.getX() + 0.5,
					(double)blockPos.getY() + 0.5,
					(double)blockPos.getZ() + 0.5,
					5.0F,
					true,
					Explosion.BlockInteraction.DESTROY
				);
				return InteractionResult.SUCCESS;
			} else if ((Boolean)blockState.getValue(OCCUPIED)) {
				if (!this.kickVillagerOutOfBed(level, blockPos)) {
					player.displayClientMessage(new TranslatableComponent("block.minecraft.bed.occupied"), true);
				}

				return InteractionResult.SUCCESS;
			} else {
				player.startSleepInBed(blockPos).ifLeft(bedSleepingProblem -> {
					if (bedSleepingProblem != null) {
						player.displayClientMessage(bedSleepingProblem.getMessage(), true);
					}
				});
				return InteractionResult.SUCCESS;
			}
		}
	}

	public static boolean canSetSpawn(Level level) {
		return level.dimensionType().bedWorks();
	}

	private boolean kickVillagerOutOfBed(Level level, BlockPos blockPos) {
		List<Villager> list = level.getEntitiesOfClass(Villager.class, new AABB(blockPos), LivingEntity::isSleeping);
		if (list.isEmpty()) {
			return false;
		} else {
			((Villager)list.get(0)).stopSleeping();
			return true;
		}
	}

	@Override
	public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, float f) {
		super.fallOn(level, blockState, blockPos, entity, f * 0.5F);
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter blockGetter, Entity entity) {
		if (entity.isSuppressingBounce()) {
			super.updateEntityAfterFallOn(blockGetter, entity);
		} else {
			this.bounceUp(entity);
		}
	}

	private void bounceUp(Entity entity) {
		Vec3 vec3 = entity.getDeltaMovement();
		if (vec3.y < 0.0) {
			double d = entity instanceof LivingEntity ? 1.0 : 0.8;
			entity.setDeltaMovement(vec3.x, -vec3.y * 0.66F * d, vec3.z);
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction == getNeighbourDirection(blockState.getValue(PART), blockState.getValue(FACING))) {
			return blockState2.is(this) && blockState2.getValue(PART) != blockState.getValue(PART)
				? blockState.setValue(OCCUPIED, (Boolean)blockState2.getValue(OCCUPIED))
				: Blocks.AIR.defaultBlockState();
		} else {
			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		}
	}

	private static Direction getNeighbourDirection(BedPart bedPart, Direction direction) {
		return bedPart == BedPart.FOOT ? direction : direction.getOpposite();
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (!level.isClientSide && player.isCreative()) {
			BedPart bedPart = blockState.getValue(PART);
			if (bedPart == BedPart.FOOT) {
				BlockPos blockPos2 = blockPos.relative(getNeighbourDirection(bedPart, blockState.getValue(FACING)));
				BlockState blockState2 = level.getBlockState(blockPos2);
				if (blockState2.is(this) && blockState2.getValue(PART) == BedPart.HEAD) {
					level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 35);
					level.levelEvent(player, 2001, blockPos2, Block.getId(blockState2));
				}
			}
		}

		super.playerWillDestroy(level, blockPos, blockState, player);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Direction direction = blockPlaceContext.getHorizontalDirection();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		BlockPos blockPos2 = blockPos.relative(direction);
		return blockPlaceContext.getLevel().getBlockState(blockPos2).canBeReplaced(blockPlaceContext) ? this.defaultBlockState().setValue(FACING, direction) : null;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Direction direction = getConnectedDirection(blockState).getOpposite();
		switch (direction) {
			case NORTH:
				return NORTH_SHAPE;
			case SOUTH:
				return SOUTH_SHAPE;
			case WEST:
				return WEST_SHAPE;
			default:
				return EAST_SHAPE;
		}
	}

	public static Direction getConnectedDirection(BlockState blockState) {
		Direction direction = blockState.getValue(FACING);
		return blockState.getValue(PART) == BedPart.HEAD ? direction.getOpposite() : direction;
	}

	public static DoubleBlockCombiner.BlockType getBlockType(BlockState blockState) {
		BedPart bedPart = blockState.getValue(PART);
		return bedPart == BedPart.HEAD ? DoubleBlockCombiner.BlockType.FIRST : DoubleBlockCombiner.BlockType.SECOND;
	}

	private static boolean isBunkBed(BlockGetter blockGetter, BlockPos blockPos) {
		return blockGetter.getBlockState(blockPos.below()).getBlock() instanceof BedBlock;
	}

	public static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, float f) {
		Direction direction = collisionGetter.getBlockState(blockPos).getValue(FACING);
		Direction direction2 = direction.getClockWise();
		Direction direction3 = direction2.isFacingAngle(f) ? direction2.getOpposite() : direction2;
		if (isBunkBed(collisionGetter, blockPos)) {
			return findBunkBedStandUpPosition(entityType, collisionGetter, blockPos, direction, direction3);
		} else {
			int[][] is = bedStandUpOffsets(direction, direction3);
			Optional<Vec3> optional = findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, is, true);
			return optional.isPresent() ? optional : findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, is, false);
		}
	}

	private static Optional<Vec3> findBunkBedStandUpPosition(
		EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, Direction direction, Direction direction2
	) {
		int[][] is = bedSurroundStandUpOffsets(direction, direction2);
		Optional<Vec3> optional = findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, is, true);
		if (optional.isPresent()) {
			return optional;
		} else {
			BlockPos blockPos2 = blockPos.below();
			Optional<Vec3> optional2 = findStandUpPositionAtOffset(entityType, collisionGetter, blockPos2, is, true);
			if (optional2.isPresent()) {
				return optional2;
			} else {
				int[][] js = bedAboveStandUpOffsets(direction);
				Optional<Vec3> optional3 = findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, js, true);
				if (optional3.isPresent()) {
					return optional3;
				} else {
					Optional<Vec3> optional4 = findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, is, false);
					if (optional4.isPresent()) {
						return optional4;
					} else {
						Optional<Vec3> optional5 = findStandUpPositionAtOffset(entityType, collisionGetter, blockPos2, is, false);
						return optional5.isPresent() ? optional5 : findStandUpPositionAtOffset(entityType, collisionGetter, blockPos, js, false);
					}
				}
			}
		}
	}

	private static Optional<Vec3> findStandUpPositionAtOffset(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, int[][] is, boolean bl) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int[] js : is) {
			mutableBlockPos.set(blockPos.getX() + js[0], blockPos.getY(), blockPos.getZ() + js[1]);
			Vec3 vec3 = DismountHelper.findSafeDismountLocation(entityType, collisionGetter, mutableBlockPos, bl);
			if (vec3 != null) {
				return Optional.of(vec3);
			}
		}

		return Optional.empty();
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState blockState) {
		return PushReaction.DESTROY;
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, PART, OCCUPIED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new BedBlockEntity(blockPos, blockState, this.color);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
		if (!level.isClientSide) {
			BlockPos blockPos2 = blockPos.relative(blockState.getValue(FACING));
			level.setBlock(blockPos2, blockState.setValue(PART, BedPart.HEAD), 3);
			level.blockUpdated(blockPos, Blocks.AIR);
			blockState.updateNeighbourShapes(level, blockPos, 3);
		}
	}

	public DyeColor getColor() {
		return this.color;
	}

	@Override
	public long getSeed(BlockState blockState, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.relative(blockState.getValue(FACING), blockState.getValue(PART) == BedPart.HEAD ? 0 : 1);
		return Mth.getSeed(blockPos2.getX(), blockPos.getY(), blockPos2.getZ());
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}

	private static int[][] bedStandUpOffsets(Direction direction, Direction direction2) {
		return ArrayUtils.addAll((int[][])bedSurroundStandUpOffsets(direction, direction2), (int[][])bedAboveStandUpOffsets(direction));
	}

	private static int[][] bedSurroundStandUpOffsets(Direction direction, Direction direction2) {
		return new int[][]{
			{direction2.getStepX(), direction2.getStepZ()},
			{direction2.getStepX() - direction.getStepX(), direction2.getStepZ() - direction.getStepZ()},
			{direction2.getStepX() - direction.getStepX() * 2, direction2.getStepZ() - direction.getStepZ() * 2},
			{-direction.getStepX() * 2, -direction.getStepZ() * 2},
			{-direction2.getStepX() - direction.getStepX() * 2, -direction2.getStepZ() - direction.getStepZ() * 2},
			{-direction2.getStepX() - direction.getStepX(), -direction2.getStepZ() - direction.getStepZ()},
			{-direction2.getStepX(), -direction2.getStepZ()},
			{-direction2.getStepX() + direction.getStepX(), -direction2.getStepZ() + direction.getStepZ()},
			{direction.getStepX(), direction.getStepZ()},
			{direction2.getStepX() + direction.getStepX(), direction2.getStepZ() + direction.getStepZ()}
		};
	}

	private static int[][] bedAboveStandUpOffsets(Direction direction) {
		return new int[][]{{0, 0}, {-direction.getStepX(), -direction.getStepZ()}};
	}
}
