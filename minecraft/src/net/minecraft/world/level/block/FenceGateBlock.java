package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FenceGateBlock extends HorizontalDirectionalBlock {
	public static final MapCodec<FenceGateBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(WoodType.CODEC.fieldOf("wood_type").forGetter(fenceGateBlock -> fenceGateBlock.type), propertiesCodec())
				.apply(instance, FenceGateBlock::new)
	);
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty IN_WALL = BlockStateProperties.IN_WALL;
	protected static final VoxelShape Z_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
	protected static final VoxelShape X_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
	protected static final VoxelShape Z_SHAPE_LOW = Block.box(0.0, 0.0, 6.0, 16.0, 13.0, 10.0);
	protected static final VoxelShape X_SHAPE_LOW = Block.box(6.0, 0.0, 0.0, 10.0, 13.0, 16.0);
	protected static final VoxelShape Z_COLLISION_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 24.0, 10.0);
	protected static final VoxelShape X_COLLISION_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 24.0, 16.0);
	protected static final VoxelShape Z_SUPPORT_SHAPE = Block.box(0.0, 5.0, 6.0, 16.0, 24.0, 10.0);
	protected static final VoxelShape X_SUPPORT_SHAPE = Block.box(6.0, 5.0, 0.0, 10.0, 24.0, 16.0);
	protected static final VoxelShape Z_OCCLUSION_SHAPE = Shapes.or(Block.box(0.0, 5.0, 7.0, 2.0, 16.0, 9.0), Block.box(14.0, 5.0, 7.0, 16.0, 16.0, 9.0));
	protected static final VoxelShape X_OCCLUSION_SHAPE = Shapes.or(Block.box(7.0, 5.0, 0.0, 9.0, 16.0, 2.0), Block.box(7.0, 5.0, 14.0, 9.0, 16.0, 16.0));
	protected static final VoxelShape Z_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(0.0, 2.0, 7.0, 2.0, 13.0, 9.0), Block.box(14.0, 2.0, 7.0, 16.0, 13.0, 9.0));
	protected static final VoxelShape X_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(7.0, 2.0, 0.0, 9.0, 13.0, 2.0), Block.box(7.0, 2.0, 14.0, 9.0, 13.0, 16.0));
	private final WoodType type;

	@Override
	public MapCodec<FenceGateBlock> codec() {
		return CODEC;
	}

	public FenceGateBlock(WoodType woodType, BlockBehaviour.Properties properties) {
		super(properties.sound(woodType.soundType()));
		this.type = woodType;
		this.registerDefaultState(
			this.stateDefinition.any().setValue(OPEN, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false)).setValue(IN_WALL, Boolean.valueOf(false))
		);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		if ((Boolean)blockState.getValue(IN_WALL)) {
			return ((Direction)blockState.getValue(FACING)).getAxis() == Direction.Axis.X ? X_SHAPE_LOW : Z_SHAPE_LOW;
		} else {
			return ((Direction)blockState.getValue(FACING)).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		Direction.Axis axis = direction.getAxis();
		if (((Direction)blockState.getValue(FACING)).getClockWise().getAxis() != axis) {
			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		} else {
			boolean bl = this.isWall(blockState2) || this.isWall(levelAccessor.getBlockState(blockPos.relative(direction.getOpposite())));
			return blockState.setValue(IN_WALL, Boolean.valueOf(bl));
		}
	}

	@Override
	public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		if ((Boolean)blockState.getValue(OPEN)) {
			return Shapes.empty();
		} else {
			return ((Direction)blockState.getValue(FACING)).getAxis() == Direction.Axis.Z ? Z_SUPPORT_SHAPE : X_SUPPORT_SHAPE;
		}
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		if ((Boolean)blockState.getValue(OPEN)) {
			return Shapes.empty();
		} else {
			return ((Direction)blockState.getValue(FACING)).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE : X_COLLISION_SHAPE;
		}
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		if ((Boolean)blockState.getValue(IN_WALL)) {
			return ((Direction)blockState.getValue(FACING)).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE_LOW : Z_OCCLUSION_SHAPE_LOW;
		} else {
			return ((Direction)blockState.getValue(FACING)).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE : Z_OCCLUSION_SHAPE;
		}
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		switch (pathComputationType) {
			case LAND:
				return (Boolean)blockState.getValue(OPEN);
			case WATER:
				return false;
			case AIR:
				return (Boolean)blockState.getValue(OPEN);
			default:
				return false;
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Level level = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		boolean bl = level.hasNeighborSignal(blockPos);
		Direction direction = blockPlaceContext.getHorizontalDirection();
		Direction.Axis axis = direction.getAxis();
		boolean bl2 = axis == Direction.Axis.Z && (this.isWall(level.getBlockState(blockPos.west())) || this.isWall(level.getBlockState(blockPos.east())))
			|| axis == Direction.Axis.X && (this.isWall(level.getBlockState(blockPos.north())) || this.isWall(level.getBlockState(blockPos.south())));
		return this.defaultBlockState()
			.setValue(FACING, direction)
			.setValue(OPEN, Boolean.valueOf(bl))
			.setValue(POWERED, Boolean.valueOf(bl))
			.setValue(IN_WALL, Boolean.valueOf(bl2));
	}

	private boolean isWall(BlockState blockState) {
		return blockState.is(BlockTags.WALLS);
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if ((Boolean)blockState.getValue(OPEN)) {
			blockState = blockState.setValue(OPEN, Boolean.valueOf(false));
			level.setBlock(blockPos, blockState, 10);
		} else {
			Direction direction = player.getDirection();
			if (blockState.getValue(FACING) == direction.getOpposite()) {
				blockState = blockState.setValue(FACING, direction);
			}

			blockState = blockState.setValue(OPEN, Boolean.valueOf(true));
			level.setBlock(blockPos, blockState, 10);
		}

		boolean bl = (Boolean)blockState.getValue(OPEN);
		level.playSound(
			player, blockPos, bl ? this.type.fenceGateOpen() : this.type.fenceGateClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F
		);
		level.gameEvent(player, bl ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public void onExplosionHit(BlockState blockState, Level level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
		if (explosion.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK && !level.isClientSide() && !(Boolean)blockState.getValue(POWERED)) {
			boolean bl = (Boolean)blockState.getValue(OPEN);
			level.setBlockAndUpdate(blockPos, blockState.setValue(OPEN, Boolean.valueOf(!bl)));
			level.playSound(
				null, blockPos, bl ? this.type.fenceGateClose() : this.type.fenceGateOpen(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F
			);
			level.gameEvent(bl ? GameEvent.BLOCK_CLOSE : GameEvent.BLOCK_OPEN, blockPos, GameEvent.Context.of(blockState));
		}

		super.onExplosionHit(blockState, level, blockPos, explosion, biConsumer);
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (!level.isClientSide) {
			boolean bl2 = level.hasNeighborSignal(blockPos);
			if ((Boolean)blockState.getValue(POWERED) != bl2) {
				level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(bl2)).setValue(OPEN, Boolean.valueOf(bl2)), 2);
				if ((Boolean)blockState.getValue(OPEN) != bl2) {
					level.playSound(
						null, blockPos, bl2 ? this.type.fenceGateOpen() : this.type.fenceGateClose(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F
					);
					level.gameEvent(null, bl2 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, blockPos);
				}
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, OPEN, POWERED, IN_WALL);
	}

	public static boolean connectsToDirection(BlockState blockState, Direction direction) {
		return ((Direction)blockState.getValue(FACING)).getAxis() == direction.getClockWise().getAxis();
	}
}
