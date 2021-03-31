package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

public class DetectorRailBlock extends BaseRailBlock {
	public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	private static final int PRESSED_CHECK_PERIOD = 20;

	public DetectorRailBlock(BlockBehaviour.Properties properties) {
		super(true, properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)).setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(false))
		);
	}

	@Override
	public boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!level.isClientSide) {
			if (!(Boolean)blockState.getValue(POWERED)) {
				this.checkPressed(level, blockPos, blockState);
			}
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if ((Boolean)blockState.getValue(POWERED)) {
			this.checkPressed(serverLevel, blockPos, blockState);
		}
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		if (!(Boolean)blockState.getValue(POWERED)) {
			return 0;
		} else {
			return direction == Direction.UP ? 15 : 0;
		}
	}

	private void checkPressed(Level level, BlockPos blockPos, BlockState blockState) {
		if (this.canSurvive(blockState, level, blockPos)) {
			boolean bl = (Boolean)blockState.getValue(POWERED);
			boolean bl2 = false;
			List<AbstractMinecart> list = this.getInteractingMinecartOfType(level, blockPos, AbstractMinecart.class, entity -> true);
			if (!list.isEmpty()) {
				bl2 = true;
			}

			if (bl2 && !bl) {
				BlockState blockState2 = blockState.setValue(POWERED, Boolean.valueOf(true));
				level.setBlock(blockPos, blockState2, 3);
				this.updatePowerToConnected(level, blockPos, blockState2, true);
				level.updateNeighborsAt(blockPos, this);
				level.updateNeighborsAt(blockPos.below(), this);
				level.setBlocksDirty(blockPos, blockState, blockState2);
			}

			if (!bl2 && bl) {
				BlockState blockState2 = blockState.setValue(POWERED, Boolean.valueOf(false));
				level.setBlock(blockPos, blockState2, 3);
				this.updatePowerToConnected(level, blockPos, blockState2, false);
				level.updateNeighborsAt(blockPos, this);
				level.updateNeighborsAt(blockPos.below(), this);
				level.setBlocksDirty(blockPos, blockState, blockState2);
			}

			if (bl2) {
				level.getBlockTicks().scheduleTick(blockPos, this, 20);
			}

			level.updateNeighbourForOutputSignal(blockPos, this);
		}
	}

	protected void updatePowerToConnected(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
		RailState railState = new RailState(level, blockPos, blockState);

		for (BlockPos blockPos2 : railState.getConnections()) {
			BlockState blockState2 = level.getBlockState(blockPos2);
			blockState2.neighborChanged(level, blockPos2, blockState2.getBlock(), blockPos, false);
		}
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
			BlockState blockState3 = this.updateState(blockState, level, blockPos, bl);
			this.checkPressed(level, blockPos, blockState3);
		}
	}

	@Override
	public Property<RailShape> getShapeProperty() {
		return SHAPE;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		if ((Boolean)blockState.getValue(POWERED)) {
			List<MinecartCommandBlock> list = this.getInteractingMinecartOfType(level, blockPos, MinecartCommandBlock.class, entity -> true);
			if (!list.isEmpty()) {
				return ((MinecartCommandBlock)list.get(0)).getCommandBlock().getSuccessCount();
			}

			List<AbstractMinecart> list2 = this.getInteractingMinecartOfType(level, blockPos, AbstractMinecart.class, EntitySelector.CONTAINER_ENTITY_SELECTOR);
			if (!list2.isEmpty()) {
				return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)list2.get(0));
			}
		}

		return 0;
	}

	private <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(Level level, BlockPos blockPos, Class<T> class_, Predicate<Entity> predicate) {
		return level.getEntitiesOfClass(class_, this.getSearchBB(blockPos), predicate);
	}

	private AABB getSearchBB(BlockPos blockPos) {
		double d = 0.2;
		return new AABB(
			(double)blockPos.getX() + 0.2,
			(double)blockPos.getY(),
			(double)blockPos.getZ() + 0.2,
			(double)(blockPos.getX() + 1) - 0.2,
			(double)(blockPos.getY() + 1) - 0.2,
			(double)(blockPos.getZ() + 1) - 0.2
		);
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		switch (rotation) {
			case CLOCKWISE_180:
				switch ((RailShape)blockState.getValue(SHAPE)) {
					case ASCENDING_EAST:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_WEST);
					case ASCENDING_WEST:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_EAST);
					case ASCENDING_NORTH:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
					case ASCENDING_SOUTH:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
					case SOUTH_EAST:
						return blockState.setValue(SHAPE, RailShape.NORTH_WEST);
					case SOUTH_WEST:
						return blockState.setValue(SHAPE, RailShape.NORTH_EAST);
					case NORTH_WEST:
						return blockState.setValue(SHAPE, RailShape.SOUTH_EAST);
					case NORTH_EAST:
						return blockState.setValue(SHAPE, RailShape.SOUTH_WEST);
				}
			case COUNTERCLOCKWISE_90:
				switch ((RailShape)blockState.getValue(SHAPE)) {
					case ASCENDING_EAST:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
					case ASCENDING_WEST:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
					case ASCENDING_NORTH:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_WEST);
					case ASCENDING_SOUTH:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_EAST);
					case SOUTH_EAST:
						return blockState.setValue(SHAPE, RailShape.NORTH_EAST);
					case SOUTH_WEST:
						return blockState.setValue(SHAPE, RailShape.SOUTH_EAST);
					case NORTH_WEST:
						return blockState.setValue(SHAPE, RailShape.SOUTH_WEST);
					case NORTH_EAST:
						return blockState.setValue(SHAPE, RailShape.NORTH_WEST);
					case NORTH_SOUTH:
						return blockState.setValue(SHAPE, RailShape.EAST_WEST);
					case EAST_WEST:
						return blockState.setValue(SHAPE, RailShape.NORTH_SOUTH);
				}
			case CLOCKWISE_90:
				switch ((RailShape)blockState.getValue(SHAPE)) {
					case ASCENDING_EAST:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
					case ASCENDING_WEST:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
					case ASCENDING_NORTH:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_EAST);
					case ASCENDING_SOUTH:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_WEST);
					case SOUTH_EAST:
						return blockState.setValue(SHAPE, RailShape.SOUTH_WEST);
					case SOUTH_WEST:
						return blockState.setValue(SHAPE, RailShape.NORTH_WEST);
					case NORTH_WEST:
						return blockState.setValue(SHAPE, RailShape.NORTH_EAST);
					case NORTH_EAST:
						return blockState.setValue(SHAPE, RailShape.SOUTH_EAST);
					case NORTH_SOUTH:
						return blockState.setValue(SHAPE, RailShape.EAST_WEST);
					case EAST_WEST:
						return blockState.setValue(SHAPE, RailShape.NORTH_SOUTH);
				}
			default:
				return blockState;
		}
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		RailShape railShape = blockState.getValue(SHAPE);
		switch (mirror) {
			case LEFT_RIGHT:
				switch (railShape) {
					case ASCENDING_NORTH:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
					case ASCENDING_SOUTH:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
					case SOUTH_EAST:
						return blockState.setValue(SHAPE, RailShape.NORTH_EAST);
					case SOUTH_WEST:
						return blockState.setValue(SHAPE, RailShape.NORTH_WEST);
					case NORTH_WEST:
						return blockState.setValue(SHAPE, RailShape.SOUTH_WEST);
					case NORTH_EAST:
						return blockState.setValue(SHAPE, RailShape.SOUTH_EAST);
					default:
						return super.mirror(blockState, mirror);
				}
			case FRONT_BACK:
				switch (railShape) {
					case ASCENDING_EAST:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_WEST);
					case ASCENDING_WEST:
						return blockState.setValue(SHAPE, RailShape.ASCENDING_EAST);
					case ASCENDING_NORTH:
					case ASCENDING_SOUTH:
					default:
						break;
					case SOUTH_EAST:
						return blockState.setValue(SHAPE, RailShape.SOUTH_WEST);
					case SOUTH_WEST:
						return blockState.setValue(SHAPE, RailShape.SOUTH_EAST);
					case NORTH_WEST:
						return blockState.setValue(SHAPE, RailShape.NORTH_EAST);
					case NORTH_EAST:
						return blockState.setValue(SHAPE, RailShape.NORTH_WEST);
				}
		}

		return super.mirror(blockState, mirror);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SHAPE, POWERED, WATERLOGGED);
	}
}
