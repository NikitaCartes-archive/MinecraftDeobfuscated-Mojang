package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DoorBlock extends Block {
	public static final MapCodec<DoorBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter(DoorBlock::type), propertiesCodec()).apply(instance, DoorBlock::new)
	);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	protected static final float AABB_DOOR_THICKNESS = 3.0F;
	protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
	protected static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape WEST_AABB = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
	private final BlockSetType type;

	public static boolean shouldTrigger(Level level, BlockPos blockPos) {
		return level.hasNeighborSignal(blockPos) || level.hasNeighborSignal(blockPos.above());
	}

	@Override
	public MapCodec<? extends DoorBlock> codec() {
		return CODEC;
	}

	protected DoorBlock(BlockSetType blockSetType, BlockBehaviour.Properties properties) {
		super(properties.sound(blockSetType.soundType()));
		this.type = blockSetType;
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(OPEN, Boolean.valueOf(false))
				.setValue(HINGE, DoorHingeSide.LEFT)
				.setValue(POWERED, Boolean.valueOf(false))
				.setValue(HALF, DoubleBlockHalf.LOWER)
		);
	}

	public BlockSetType type() {
		return this.type;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Direction direction = blockState.getValue(FACING);
		boolean bl = !(Boolean)blockState.getValue(OPEN);
		boolean bl2 = blockState.getValue(HINGE) == DoorHingeSide.RIGHT;

		return switch (direction) {
			case SOUTH -> bl ? SOUTH_AABB : (bl2 ? EAST_AABB : WEST_AABB);
			case WEST -> bl ? WEST_AABB : (bl2 ? SOUTH_AABB : NORTH_AABB);
			case NORTH -> bl ? NORTH_AABB : (bl2 ? WEST_AABB : EAST_AABB);
			default -> bl ? EAST_AABB : (bl2 ? NORTH_AABB : SOUTH_AABB);
		};
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		DoubleBlockHalf doubleBlockHalf = blockState.getValue(HALF);
		if (direction.getAxis() != Direction.Axis.Y || doubleBlockHalf == DoubleBlockHalf.LOWER != (direction == Direction.UP)) {
			return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)
				? Blocks.AIR.defaultBlockState()
				: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		} else {
			return blockState2.getBlock() instanceof DoorBlock && blockState2.getValue(HALF) != doubleBlockHalf
				? blockState2.setValue(HALF, doubleBlockHalf)
				: Blocks.AIR.defaultBlockState();
		}
	}

	@Override
	protected void onExplosionHit(BlockState blockState, Level level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
		if (explosion.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK
			&& blockState.getValue(HALF) == DoubleBlockHalf.LOWER
			&& !level.isClientSide()
			&& this.type.canOpenByWindCharge()
			&& !(Boolean)blockState.getValue(POWERED)) {
			this.setOpen(null, level, blockState, blockPos, !this.isOpen(blockState));
		}

		super.onExplosionHit(blockState, level, blockPos, explosion, biConsumer);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (!level.isClientSide && (player.isCreative() || !player.hasCorrectToolForDrops(blockState))) {
			DoublePlantBlock.preventDropFromBottomPart(level, blockPos, blockState, player);
		}

		return super.playerWillDestroy(level, blockPos, blockState, player);
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return switch (pathComputationType) {
			case LAND, AIR -> blockState.getValue(OPEN);
			case WATER -> false;
		};
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		Level level = blockPlaceContext.getLevel();
		if (blockPos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(blockPos.above()).canBeReplaced(blockPlaceContext)) {
			boolean bl = shouldTrigger(level, blockPos);
			return this.defaultBlockState()
				.setValue(FACING, blockPlaceContext.getHorizontalDirection())
				.setValue(HINGE, this.getHinge(blockPlaceContext))
				.setValue(POWERED, Boolean.valueOf(bl))
				.setValue(OPEN, Boolean.valueOf(bl))
				.setValue(HALF, DoubleBlockHalf.LOWER);
		} else {
			return null;
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		level.setBlock(blockPos.above(), blockState.setValue(HALF, DoubleBlockHalf.UPPER), 3);
	}

	private DoorHingeSide getHinge(BlockPlaceContext blockPlaceContext) {
		BlockGetter blockGetter = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		Direction direction = blockPlaceContext.getHorizontalDirection();
		BlockPos blockPos2 = blockPos.above();
		Direction direction2 = direction.getCounterClockWise();
		BlockPos blockPos3 = blockPos.relative(direction2);
		BlockState blockState = blockGetter.getBlockState(blockPos3);
		BlockPos blockPos4 = blockPos2.relative(direction2);
		BlockState blockState2 = blockGetter.getBlockState(blockPos4);
		Direction direction3 = direction.getClockWise();
		BlockPos blockPos5 = blockPos.relative(direction3);
		BlockState blockState3 = blockGetter.getBlockState(blockPos5);
		BlockPos blockPos6 = blockPos2.relative(direction3);
		BlockState blockState4 = blockGetter.getBlockState(blockPos6);
		int i = (blockState.isCollisionShapeFullBlock(blockGetter, blockPos3) ? -1 : 0)
			+ (blockState2.isCollisionShapeFullBlock(blockGetter, blockPos4) ? -1 : 0)
			+ (blockState3.isCollisionShapeFullBlock(blockGetter, blockPos5) ? 1 : 0)
			+ (blockState4.isCollisionShapeFullBlock(blockGetter, blockPos6) ? 1 : 0);
		boolean bl = blockState.is(this) && blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
		boolean bl2 = blockState3.is(this) && blockState3.getValue(HALF) == DoubleBlockHalf.LOWER;
		if ((!bl || bl2) && i <= 0) {
			if ((!bl2 || bl) && i >= 0) {
				int j = direction.getStepX();
				int k = direction.getStepZ();
				Vec3 vec3 = blockPlaceContext.getClickLocation();
				double d = vec3.x - (double)blockPos.getX();
				double e = vec3.z - (double)blockPos.getZ();
				return (j >= 0 || !(e < 0.5)) && (j <= 0 || !(e > 0.5)) && (k >= 0 || !(d > 0.5)) && (k <= 0 || !(d < 0.5)) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
			} else {
				return DoorHingeSide.LEFT;
			}
		} else {
			return DoorHingeSide.RIGHT;
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (!this.type.canOpenByHand()) {
			return InteractionResult.PASS;
		} else {
			blockState = blockState.cycle(OPEN);
			level.setBlock(blockPos, blockState, 10);
			this.playSound(player, level, blockPos, (Boolean)blockState.getValue(OPEN));
			level.gameEvent(player, this.isOpen(blockState) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	public boolean isOpen(BlockState blockState) {
		return (Boolean)blockState.getValue(OPEN);
	}

	public void setOpen(@Nullable Entity entity, Level level, BlockState blockState, BlockPos blockPos, boolean bl) {
		if (blockState.is(this) && (Boolean)blockState.getValue(OPEN) != bl) {
			level.setBlock(blockPos, blockState.setValue(OPEN, Boolean.valueOf(bl)), 10);
			this.playSound(entity, level, blockPos, bl);
			level.gameEvent(entity, bl ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
		}
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		boolean bl2 = level.hasNeighborSignal(blockPos)
			|| level.hasNeighborSignal(blockPos.relative(blockState.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
		if (!this.defaultBlockState().is(block) && bl2 != (Boolean)blockState.getValue(POWERED)) {
			if (bl2 != (Boolean)blockState.getValue(OPEN)) {
				this.playSound(null, level, blockPos, bl2);
				level.gameEvent(null, bl2 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
			}

			level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(bl2)).setValue(OPEN, Boolean.valueOf(bl2)), 2);
		}
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		return blockState.getValue(HALF) == DoubleBlockHalf.LOWER ? blockState2.isFaceSturdy(levelReader, blockPos2, Direction.UP) : blockState2.is(this);
	}

	private void playSound(@Nullable Entity entity, Level level, BlockPos blockPos, boolean bl) {
		level.playSound(entity, blockPos, bl ? this.type.doorOpen() : this.type.doorClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		return mirror == Mirror.NONE ? blockState : blockState.rotate(mirror.getRotation(blockState.getValue(FACING))).cycle(HINGE);
	}

	@Override
	protected long getSeed(BlockState blockState, BlockPos blockPos) {
		return Mth.getSeed(blockPos.getX(), blockPos.below(blockState.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), blockPos.getZ());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, FACING, OPEN, HINGE, POWERED);
	}

	public static boolean isWoodenDoor(Level level, BlockPos blockPos) {
		return isWoodenDoor(level.getBlockState(blockPos));
	}

	public static boolean isWoodenDoor(BlockState blockState) {
		if (blockState.getBlock() instanceof DoorBlock doorBlock && doorBlock.type().canOpenByHand()) {
			return true;
		}

		return false;
	}
}
