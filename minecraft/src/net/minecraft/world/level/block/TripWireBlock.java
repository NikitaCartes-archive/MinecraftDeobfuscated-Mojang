package net.minecraft.world.level.block;

import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TripWireBlock extends Block {
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
	public static final BooleanProperty DISARMED = BlockStateProperties.DISARMED;
	public static final BooleanProperty NORTH = PipeBlock.NORTH;
	public static final BooleanProperty EAST = PipeBlock.EAST;
	public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
	public static final BooleanProperty WEST = PipeBlock.WEST;
	private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = CrossCollisionBlock.PROPERTY_BY_DIRECTION;
	protected static final VoxelShape AABB = Block.box(0.0, 1.0, 0.0, 16.0, 2.5, 16.0);
	protected static final VoxelShape NOT_ATTACHED_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
	private final TripWireHookBlock hook;

	public TripWireBlock(TripWireHookBlock tripWireHookBlock, BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(POWERED, Boolean.valueOf(false))
				.setValue(ATTACHED, Boolean.valueOf(false))
				.setValue(DISARMED, Boolean.valueOf(false))
				.setValue(NORTH, Boolean.valueOf(false))
				.setValue(EAST, Boolean.valueOf(false))
				.setValue(SOUTH, Boolean.valueOf(false))
				.setValue(WEST, Boolean.valueOf(false))
		);
		this.hook = tripWireHookBlock;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return blockState.getValue(ATTACHED) ? AABB : NOT_ATTACHED_AABB;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockGetter blockGetter = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		return this.defaultBlockState()
			.setValue(NORTH, Boolean.valueOf(this.shouldConnectTo(blockGetter.getBlockState(blockPos.north()), Direction.NORTH)))
			.setValue(EAST, Boolean.valueOf(this.shouldConnectTo(blockGetter.getBlockState(blockPos.east()), Direction.EAST)))
			.setValue(SOUTH, Boolean.valueOf(this.shouldConnectTo(blockGetter.getBlockState(blockPos.south()), Direction.SOUTH)))
			.setValue(WEST, Boolean.valueOf(this.shouldConnectTo(blockGetter.getBlockState(blockPos.west()), Direction.WEST)));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return direction.getAxis().isHorizontal()
			? blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(this.shouldConnectTo(blockState2, direction)))
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
			this.updateSource(level, blockPos, blockState);
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!bl && !blockState.is(blockState2.getBlock())) {
			this.updateSource(level, blockPos, blockState.setValue(POWERED, Boolean.valueOf(true)));
		}
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (!level.isClientSide && !player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() == Items.SHEARS) {
			level.setBlock(blockPos, blockState.setValue(DISARMED, Boolean.valueOf(true)), 4);
		}

		super.playerWillDestroy(level, blockPos, blockState, player);
	}

	private void updateSource(Level level, BlockPos blockPos, BlockState blockState) {
		for (Direction direction : new Direction[]{Direction.SOUTH, Direction.WEST}) {
			for (int i = 1; i < 42; i++) {
				BlockPos blockPos2 = blockPos.relative(direction, i);
				BlockState blockState2 = level.getBlockState(blockPos2);
				if (blockState2.is(this.hook)) {
					if (blockState2.getValue(TripWireHookBlock.FACING) == direction.getOpposite()) {
						this.hook.calculateState(level, blockPos2, blockState2, false, true, i, blockState);
					}
					break;
				}

				if (!blockState2.is(this)) {
					break;
				}
			}
		}
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!level.isClientSide) {
			if (!(Boolean)blockState.getValue(POWERED)) {
				this.checkPressed(level, blockPos);
			}
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if ((Boolean)serverLevel.getBlockState(blockPos).getValue(POWERED)) {
			this.checkPressed(serverLevel, blockPos);
		}
	}

	private void checkPressed(Level level, BlockPos blockPos) {
		BlockState blockState = level.getBlockState(blockPos);
		boolean bl = (Boolean)blockState.getValue(POWERED);
		boolean bl2 = false;
		List<? extends Entity> list = level.getEntities(null, blockState.getShape(level, blockPos).bounds().move(blockPos));
		if (!list.isEmpty()) {
			for (Entity entity : list) {
				if (!entity.isIgnoringBlockTriggers()) {
					bl2 = true;
					break;
				}
			}
		}

		if (bl2 != bl) {
			blockState = blockState.setValue(POWERED, Boolean.valueOf(bl2));
			level.setBlock(blockPos, blockState, 3);
			this.updateSource(level, blockPos, blockState);
		}

		if (bl2) {
			level.getBlockTicks().scheduleTick(new BlockPos(blockPos), this, 10);
		}
	}

	public boolean shouldConnectTo(BlockState blockState, Direction direction) {
		Block block = blockState.getBlock();
		return block == this.hook ? blockState.getValue(TripWireHookBlock.FACING) == direction.getOpposite() : block == this;
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		switch (rotation) {
			case CLOCKWISE_180:
				return blockState.setValue(NORTH, blockState.getValue(SOUTH))
					.setValue(EAST, blockState.getValue(WEST))
					.setValue(SOUTH, blockState.getValue(NORTH))
					.setValue(WEST, blockState.getValue(EAST));
			case COUNTERCLOCKWISE_90:
				return blockState.setValue(NORTH, blockState.getValue(EAST))
					.setValue(EAST, blockState.getValue(SOUTH))
					.setValue(SOUTH, blockState.getValue(WEST))
					.setValue(WEST, blockState.getValue(NORTH));
			case CLOCKWISE_90:
				return blockState.setValue(NORTH, blockState.getValue(WEST))
					.setValue(EAST, blockState.getValue(NORTH))
					.setValue(SOUTH, blockState.getValue(EAST))
					.setValue(WEST, blockState.getValue(SOUTH));
			default:
				return blockState;
		}
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		switch (mirror) {
			case LEFT_RIGHT:
				return blockState.setValue(NORTH, blockState.getValue(SOUTH)).setValue(SOUTH, blockState.getValue(NORTH));
			case FRONT_BACK:
				return blockState.setValue(EAST, blockState.getValue(WEST)).setValue(WEST, blockState.getValue(EAST));
			default:
				return super.mirror(blockState, mirror);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH);
	}
}
