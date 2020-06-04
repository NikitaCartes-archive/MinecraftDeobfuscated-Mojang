package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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

public class BedBlock extends HorizontalDirectionalBlock implements EntityBlock {
	public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
	public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
	protected static final VoxelShape BASE = Block.box(0.0, 3.0, 0.0, 16.0, 9.0, 16.0);
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
	@Environment(EnvType.CLIENT)
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

			if (!canSetSpawn(level, blockPos)) {
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

	public static boolean canSetSpawn(Level level, BlockPos blockPos) {
		return level.dimensionType().isOverworld();
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
	public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
		super.fallOn(level, blockPos, entity, f * 0.5F);
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
				? blockState.setValue(OCCUPIED, blockState2.getValue(OCCUPIED))
				: Blocks.AIR.defaultBlockState();
		} else {
			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		}
	}

	private static Direction getNeighbourDirection(BedPart bedPart, Direction direction) {
		return bedPart == BedPart.FOOT ? direction : direction.getOpposite();
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
		super.playerDestroy(level, player, blockPos, Blocks.AIR.defaultBlockState(), blockEntity, itemStack);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		BedPart bedPart = blockState.getValue(PART);
		BlockPos blockPos2 = blockPos.relative(getNeighbourDirection(bedPart, blockState.getValue(FACING)));
		BlockState blockState2 = level.getBlockState(blockPos2);
		if (blockState2.is(this) && blockState2.getValue(PART) != bedPart) {
			level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 35);
			level.levelEvent(player, 2001, blockPos2, Block.getId(blockState2));
			if (!level.isClientSide && !player.isCreative()) {
				ItemStack itemStack = player.getMainHandItem();
				dropResources(blockState, level, blockPos, null, player, itemStack);
				dropResources(blockState2, level, blockPos2, null, player, itemStack);
			}

			player.awardStat(Stats.BLOCK_MINED.get(this));
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

	@Environment(EnvType.CLIENT)
	public static DoubleBlockCombiner.BlockType getBlockType(BlockState blockState) {
		BedPart bedPart = blockState.getValue(PART);
		return bedPart == BedPart.HEAD ? DoubleBlockCombiner.BlockType.FIRST : DoubleBlockCombiner.BlockType.SECOND;
	}

	public static Optional<Vec3> findStandUpPosition(EntityType<?> entityType, LevelReader levelReader, BlockPos blockPos, int i) {
		Direction direction = levelReader.getBlockState(blockPos).getValue(FACING);
		int j = blockPos.getX();
		int k = blockPos.getY();
		int l = blockPos.getZ();

		for (int m = 0; m <= 1; m++) {
			int n = j - direction.getStepX() * m - 1;
			int o = l - direction.getStepZ() * m - 1;
			int p = n + 2;
			int q = o + 2;

			for (int r = n; r <= p; r++) {
				for (int s = o; s <= q; s++) {
					BlockPos blockPos2 = new BlockPos(r, k, s);
					Optional<Vec3> optional = getStandingLocationAtOrBelow(entityType, levelReader, blockPos2);
					if (optional.isPresent()) {
						if (i <= 0) {
							return optional;
						}

						i--;
					}
				}
			}
		}

		return Optional.empty();
	}

	public static Optional<Vec3> getStandingLocationAtOrBelow(EntityType<?> entityType, LevelReader levelReader, BlockPos blockPos) {
		VoxelShape voxelShape = levelReader.getBlockState(blockPos).getCollisionShape(levelReader, blockPos);
		if (voxelShape.max(Direction.Axis.Y) > 0.4375) {
			return Optional.empty();
		} else {
			BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

			while (
				mutableBlockPos.getY() >= 0
					&& blockPos.getY() - mutableBlockPos.getY() <= 2
					&& levelReader.getBlockState(mutableBlockPos).getCollisionShape(levelReader, mutableBlockPos).isEmpty()
			) {
				mutableBlockPos.move(Direction.DOWN);
			}

			VoxelShape voxelShape2 = levelReader.getBlockState(mutableBlockPos).getCollisionShape(levelReader, mutableBlockPos);
			if (voxelShape2.isEmpty()) {
				return Optional.empty();
			} else {
				double d = (double)mutableBlockPos.getY() + voxelShape2.max(Direction.Axis.Y) + 2.0E-7;
				if ((double)blockPos.getY() - d > 2.0) {
					return Optional.empty();
				} else {
					Vec3 vec3 = new Vec3((double)mutableBlockPos.getX() + 0.5, d, (double)mutableBlockPos.getZ() + 0.5);
					AABB aABB = entityType.getAABB(vec3.x, vec3.y, vec3.z);
					return levelReader.noCollision(aABB) && levelReader.getBlockStates(aABB.expandTowards(0.0, -0.2F, 0.0)).noneMatch(entityType::isBlockDangerous)
						? Optional.of(vec3)
						: Optional.empty();
				}
			}
		}
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
	public BlockEntity newBlockEntity(BlockGetter blockGetter) {
		return new BedBlockEntity(this.color);
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

	@Environment(EnvType.CLIENT)
	public DyeColor getColor() {
		return this.color;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public long getSeed(BlockState blockState, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.relative(blockState.getValue(FACING), blockState.getValue(PART) == BedPart.HEAD ? 0 : 1);
		return Mth.getSeed(blockPos2.getX(), blockPos.getY(), blockPos2.getZ());
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
