package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedStoneWireBlock extends Block {
	public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
	public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(
		ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST)
	);
	private static final VoxelShape SHAPE_DOT = Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0);
	private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(
		ImmutableMap.of(
			Direction.NORTH,
			Block.box(3.0, 0.0, 0.0, 13.0, 1.0, 13.0),
			Direction.SOUTH,
			Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 16.0),
			Direction.EAST,
			Block.box(3.0, 0.0, 3.0, 16.0, 1.0, 13.0),
			Direction.WEST,
			Block.box(0.0, 0.0, 3.0, 13.0, 1.0, 13.0)
		)
	);
	private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(
		ImmutableMap.of(
			Direction.NORTH,
			Shapes.or((VoxelShape)SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0, 0.0, 0.0, 13.0, 16.0, 1.0)),
			Direction.SOUTH,
			Shapes.or((VoxelShape)SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0, 0.0, 15.0, 13.0, 16.0, 16.0)),
			Direction.EAST,
			Shapes.or((VoxelShape)SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0, 0.0, 3.0, 16.0, 16.0, 13.0)),
			Direction.WEST,
			Shapes.or((VoxelShape)SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0, 0.0, 3.0, 1.0, 16.0, 13.0))
		)
	);
	private final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.<BlockState, VoxelShape>newHashMap();
	private boolean shouldSignal = true;

	public RedStoneWireBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(NORTH, RedstoneSide.NONE)
				.setValue(EAST, RedstoneSide.NONE)
				.setValue(SOUTH, RedstoneSide.NONE)
				.setValue(WEST, RedstoneSide.NONE)
				.setValue(POWER, Integer.valueOf(0))
		);

		for (BlockState blockState : this.getStateDefinition().getPossibleStates()) {
			if ((Integer)blockState.getValue(POWER) == 0) {
				this.SHAPES_CACHE.put(blockState, this.calculateShape(blockState));
			}
		}
	}

	private VoxelShape calculateShape(BlockState blockState) {
		VoxelShape voxelShape = SHAPE_DOT;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneSide = blockState.getValue((Property<RedstoneSide>)PROPERTY_BY_DIRECTION.get(direction));
			if (redstoneSide == RedstoneSide.SIDE) {
				voxelShape = Shapes.or(voxelShape, (VoxelShape)SHAPES_FLOOR.get(direction));
			} else if (redstoneSide == RedstoneSide.UP) {
				voxelShape = Shapes.or(voxelShape, (VoxelShape)SHAPES_UP.get(direction));
			}
		}

		return voxelShape;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)this.SHAPES_CACHE.get(blockState.setValue(POWER, Integer.valueOf(0)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.getConnectionState(blockPlaceContext.getLevel(), this.defaultBlockState(), blockPlaceContext.getClickedPos());
	}

	private BlockState getConnectionState(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
		blockState = this.getMissingConnections(blockGetter, this.defaultBlockState().setValue(POWER, blockState.getValue(POWER)), blockPos);
		boolean bl = ((RedstoneSide)blockState.getValue(NORTH)).isConnected();
		boolean bl2 = ((RedstoneSide)blockState.getValue(SOUTH)).isConnected();
		boolean bl3 = ((RedstoneSide)blockState.getValue(EAST)).isConnected();
		boolean bl4 = ((RedstoneSide)blockState.getValue(WEST)).isConnected();
		boolean bl5 = !bl && !bl2;
		boolean bl6 = !bl3 && !bl4;
		if (!bl4 && bl5) {
			blockState = blockState.setValue(WEST, RedstoneSide.SIDE);
		}

		if (!bl3 && bl5) {
			blockState = blockState.setValue(EAST, RedstoneSide.SIDE);
		}

		if (!bl && bl6) {
			blockState = blockState.setValue(NORTH, RedstoneSide.SIDE);
		}

		if (!bl2 && bl6) {
			blockState = blockState.setValue(SOUTH, RedstoneSide.SIDE);
		}

		return blockState;
	}

	private BlockState getMissingConnections(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
		boolean bl = !blockGetter.getBlockState(blockPos.above()).isRedstoneConductor(blockGetter, blockPos);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (!((RedstoneSide)blockState.getValue((Property)PROPERTY_BY_DIRECTION.get(direction))).isConnected()) {
				RedstoneSide redstoneSide = this.getConnectingSide(blockGetter, blockPos, direction, bl);
				blockState = blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), redstoneSide);
			}
		}

		return blockState;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction == Direction.DOWN) {
			return blockState;
		} else if (direction == Direction.UP) {
			return this.getConnectionState(levelAccessor, blockState, blockPos);
		} else {
			RedstoneSide redstoneSide = this.getConnectingSide(levelAccessor, blockPos, direction);
			return redstoneSide.isConnected() == ((RedstoneSide)blockState.getValue((Property)PROPERTY_BY_DIRECTION.get(direction))).isConnected()
					&& !isCross(blockState)
				? blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), redstoneSide)
				: this.getConnectionState(
					levelAccessor,
					this.defaultBlockState().setValue(POWER, blockState.getValue(POWER)).setValue((Property)PROPERTY_BY_DIRECTION.get(direction), redstoneSide),
					blockPos
				);
		}
	}

	private static boolean isCross(BlockState blockState) {
		return ((RedstoneSide)blockState.getValue(NORTH)).isConnected()
			&& ((RedstoneSide)blockState.getValue(SOUTH)).isConnected()
			&& ((RedstoneSide)blockState.getValue(EAST)).isConnected()
			&& ((RedstoneSide)blockState.getValue(WEST)).isConnected();
	}

	@Override
	public void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneSide = blockState.getValue((Property<RedstoneSide>)PROPERTY_BY_DIRECTION.get(direction));
			if (redstoneSide != RedstoneSide.NONE && !levelAccessor.getBlockState(mutableBlockPos.setWithOffset(blockPos, direction)).is(this)) {
				mutableBlockPos.move(Direction.DOWN);
				BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
				if (!blockState2.is(Blocks.OBSERVER)) {
					BlockPos blockPos2 = mutableBlockPos.relative(direction.getOpposite());
					BlockState blockState3 = blockState2.updateShape(
						direction.getOpposite(), levelAccessor.getBlockState(blockPos2), levelAccessor, mutableBlockPos, blockPos2
					);
					updateOrDestroy(blockState2, blockState3, levelAccessor, mutableBlockPos, i);
				}

				mutableBlockPos.setWithOffset(blockPos, direction).move(Direction.UP);
				BlockState blockState4 = levelAccessor.getBlockState(mutableBlockPos);
				if (!blockState4.is(Blocks.OBSERVER)) {
					BlockPos blockPos3 = mutableBlockPos.relative(direction.getOpposite());
					BlockState blockState5 = blockState4.updateShape(
						direction.getOpposite(), levelAccessor.getBlockState(blockPos3), levelAccessor, mutableBlockPos, blockPos3
					);
					updateOrDestroy(blockState4, blockState5, levelAccessor, mutableBlockPos, i);
				}
			}
		}
	}

	private RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return this.getConnectingSide(blockGetter, blockPos, direction, !blockGetter.getBlockState(blockPos.above()).isRedstoneConductor(blockGetter, blockPos));
	}

	private RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction, boolean bl) {
		BlockPos blockPos2 = blockPos.relative(direction);
		BlockState blockState = blockGetter.getBlockState(blockPos2);
		if (bl) {
			boolean bl2 = blockState.isFaceSturdy(blockGetter, blockPos2, Direction.UP) || blockState.is(Blocks.HOPPER);
			if (bl2 && shouldConnectTo(blockGetter.getBlockState(blockPos2.above()))) {
				if (blockState.isCollisionShapeFullBlock(blockGetter, blockPos2)) {
					return RedstoneSide.UP;
				}

				return RedstoneSide.SIDE;
			}
		}

		return !shouldConnectTo(blockState, direction)
				&& (blockState.isRedstoneConductor(blockGetter, blockPos2) || !shouldConnectTo(blockGetter.getBlockState(blockPos2.below())))
			? RedstoneSide.NONE
			: RedstoneSide.SIDE;
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		return blockState2.isFaceSturdy(levelReader, blockPos2, Direction.UP) || blockState2.is(Blocks.HOPPER);
	}

	private void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState) {
		int i = this.calculateTargetStrength(level, blockPos);
		if ((Integer)blockState.getValue(POWER) != i) {
			if (level.getBlockState(blockPos) == blockState) {
				level.setBlock(blockPos, blockState.setValue(POWER, Integer.valueOf(i)), 2);
			}

			Set<BlockPos> set = Sets.<BlockPos>newHashSet();
			set.add(blockPos);

			for (Direction direction : Direction.values()) {
				set.add(blockPos.relative(direction));
			}

			for (BlockPos blockPos2 : set) {
				level.updateNeighborsAt(blockPos2, this);
			}
		}
	}

	private int calculateTargetStrength(Level level, BlockPos blockPos) {
		this.shouldSignal = false;
		int i = level.getBestNeighborSignal(blockPos);
		this.shouldSignal = true;
		int j = 0;
		if (i < 15) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				BlockPos blockPos2 = blockPos.relative(direction);
				BlockState blockState = level.getBlockState(blockPos2);
				j = Math.max(j, this.getWireSignal(blockState));
				BlockPos blockPos3 = blockPos.above();
				if (blockState.isRedstoneConductor(level, blockPos2) && !level.getBlockState(blockPos3).isRedstoneConductor(level, blockPos3)) {
					j = Math.max(j, this.getWireSignal(level.getBlockState(blockPos2.above())));
				} else if (!blockState.isRedstoneConductor(level, blockPos2)) {
					j = Math.max(j, this.getWireSignal(level.getBlockState(blockPos2.below())));
				}
			}
		}

		return Math.max(i, j - 1);
	}

	private int getWireSignal(BlockState blockState) {
		return blockState.is(this) ? (Integer)blockState.getValue(POWER) : 0;
	}

	private void checkCornerChangeAt(Level level, BlockPos blockPos) {
		if (level.getBlockState(blockPos).is(this)) {
			level.updateNeighborsAt(blockPos, this);

			for (Direction direction : Direction.values()) {
				level.updateNeighborsAt(blockPos.relative(direction), this);
			}
		}
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock()) && !level.isClientSide) {
			this.updatePowerStrength(level, blockPos, blockState);

			for (Direction direction : Direction.Plane.VERTICAL) {
				level.updateNeighborsAt(blockPos.relative(direction), this);
			}

			this.updateNeighborsOfNeighboringWires(level, blockPos);
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!bl && !blockState.is(blockState2.getBlock())) {
			super.onRemove(blockState, level, blockPos, blockState2, bl);
			if (!level.isClientSide) {
				for (Direction direction : Direction.values()) {
					level.updateNeighborsAt(blockPos.relative(direction), this);
				}

				this.updatePowerStrength(level, blockPos, blockState);
				this.updateNeighborsOfNeighboringWires(level, blockPos);
			}
		}
	}

	private void updateNeighborsOfNeighboringWires(Level level, BlockPos blockPos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			this.checkCornerChangeAt(level, blockPos.relative(direction));
		}

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockPos2 = blockPos.relative(direction);
			if (level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
				this.checkCornerChangeAt(level, blockPos2.above());
			} else {
				this.checkCornerChangeAt(level, blockPos2.below());
			}
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (!level.isClientSide) {
			if (blockState.canSurvive(level, blockPos)) {
				this.updatePowerStrength(level, blockPos, blockState);
			} else {
				dropResources(blockState, level, blockPos);
				level.removeBlock(blockPos, false);
			}
		}
	}

	@Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return !this.shouldSignal ? 0 : blockState.getSignal(blockGetter, blockPos, direction);
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		if (this.shouldSignal && direction != Direction.DOWN) {
			int i = (Integer)blockState.getValue(POWER);
			if (i == 0) {
				return 0;
			} else {
				return direction != Direction.UP
						&& !((RedstoneSide)this.getConnectionState(blockGetter, blockState, blockPos).getValue((Property)PROPERTY_BY_DIRECTION.get(direction.getOpposite())))
							.isConnected()
					? 0
					: i;
			}
		} else {
			return 0;
		}
	}

	protected static boolean shouldConnectTo(BlockState blockState) {
		return shouldConnectTo(blockState, null);
	}

	protected static boolean shouldConnectTo(BlockState blockState, @Nullable Direction direction) {
		if (blockState.is(Blocks.REDSTONE_WIRE)) {
			return true;
		} else if (blockState.is(Blocks.REPEATER)) {
			Direction direction2 = blockState.getValue(RepeaterBlock.FACING);
			return direction2 == direction || direction2.getOpposite() == direction;
		} else {
			return blockState.is(Blocks.OBSERVER) ? direction == blockState.getValue(ObserverBlock.FACING) : blockState.isSignalSource() && direction != null;
		}
	}

	@Override
	public boolean isSignalSource(BlockState blockState) {
		return this.shouldSignal;
	}

	@Environment(EnvType.CLIENT)
	public static int getColorForData(int i) {
		float f = (float)i / 15.0F;
		float g = f * 0.6F + 0.4F;
		if (i == 0) {
			g = 0.3F;
		}

		float h = f * f * 0.7F - 0.5F;
		float j = f * f * 0.6F - 0.7F;
		if (h < 0.0F) {
			h = 0.0F;
		}

		if (j < 0.0F) {
			j = 0.0F;
		}

		int k = Mth.clamp((int)(g * 255.0F), 0, 255);
		int l = Mth.clamp((int)(h * 255.0F), 0, 255);
		int m = Mth.clamp((int)(j * 255.0F), 0, 255);
		return 0xFF000000 | k << 16 | l << 8 | m;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		int i = (Integer)blockState.getValue(POWER);
		if (i != 0) {
			double d = (double)blockPos.getX() + 0.5 + ((double)random.nextFloat() - 0.5) * 0.2;
			double e = (double)((float)blockPos.getY() + 0.0625F);
			double f = (double)blockPos.getZ() + 0.5 + ((double)random.nextFloat() - 0.5) * 0.2;
			float g = (float)i / 15.0F;
			float h = g * 0.6F + 0.4F;
			float j = Math.max(0.0F, g * g * 0.7F - 0.5F);
			float k = Math.max(0.0F, g * g * 0.6F - 0.7F);
			level.addParticle(new DustParticleOptions(h, j, k, 1.0F), d, e, f, 0.0, 0.0, 0.0);
		}
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
		builder.add(NORTH, EAST, SOUTH, WEST, POWER);
	}
}
