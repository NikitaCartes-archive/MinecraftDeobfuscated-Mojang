package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.List;
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
	protected static final VoxelShape[] SHAPE_BY_INDEX = new VoxelShape[]{
		Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0),
		Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 16.0),
		Block.box(0.0, 0.0, 3.0, 13.0, 1.0, 13.0),
		Block.box(0.0, 0.0, 3.0, 13.0, 1.0, 16.0),
		Block.box(3.0, 0.0, 0.0, 13.0, 1.0, 13.0),
		Block.box(3.0, 0.0, 0.0, 13.0, 1.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 13.0, 1.0, 13.0),
		Block.box(0.0, 0.0, 0.0, 13.0, 1.0, 16.0),
		Block.box(3.0, 0.0, 3.0, 16.0, 1.0, 13.0),
		Block.box(3.0, 0.0, 3.0, 16.0, 1.0, 16.0),
		Block.box(0.0, 0.0, 3.0, 16.0, 1.0, 13.0),
		Block.box(0.0, 0.0, 3.0, 16.0, 1.0, 16.0),
		Block.box(3.0, 0.0, 0.0, 16.0, 1.0, 13.0),
		Block.box(3.0, 0.0, 0.0, 16.0, 1.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 13.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0)
	};
	private boolean shouldSignal = true;
	private final Set<BlockPos> toUpdate = Sets.<BlockPos>newHashSet();

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
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE_BY_INDEX[getAABBIndex(blockState)];
	}

	private static int getAABBIndex(BlockState blockState) {
		int i = 0;
		boolean bl = blockState.getValue(NORTH) != RedstoneSide.NONE;
		boolean bl2 = blockState.getValue(EAST) != RedstoneSide.NONE;
		boolean bl3 = blockState.getValue(SOUTH) != RedstoneSide.NONE;
		boolean bl4 = blockState.getValue(WEST) != RedstoneSide.NONE;
		if (bl || bl3 && !bl && !bl2 && !bl4) {
			i |= 1 << Direction.NORTH.get2DDataValue();
		}

		if (bl2 || bl4 && !bl && !bl2 && !bl3) {
			i |= 1 << Direction.EAST.get2DDataValue();
		}

		if (bl3 || bl && !bl2 && !bl3 && !bl4) {
			i |= 1 << Direction.SOUTH.get2DDataValue();
		}

		if (bl4 || bl2 && !bl && !bl3 && !bl4) {
			i |= 1 << Direction.WEST.get2DDataValue();
		}

		return i;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockGetter blockGetter = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		return this.defaultBlockState()
			.setValue(WEST, this.getConnectingSide(blockGetter, blockPos, Direction.WEST))
			.setValue(EAST, this.getConnectingSide(blockGetter, blockPos, Direction.EAST))
			.setValue(NORTH, this.getConnectingSide(blockGetter, blockPos, Direction.NORTH))
			.setValue(SOUTH, this.getConnectingSide(blockGetter, blockPos, Direction.SOUTH));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (direction == Direction.DOWN) {
			return blockState;
		} else {
			return direction == Direction.UP
				? blockState.setValue(WEST, this.getConnectingSide(levelAccessor, blockPos, Direction.WEST))
					.setValue(EAST, this.getConnectingSide(levelAccessor, blockPos, Direction.EAST))
					.setValue(NORTH, this.getConnectingSide(levelAccessor, blockPos, Direction.NORTH))
					.setValue(SOUTH, this.getConnectingSide(levelAccessor, blockPos, Direction.SOUTH))
				: blockState.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), this.getConnectingSide(levelAccessor, blockPos, direction));
		}
	}

	@Override
	public void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneSide = blockState.getValue((Property<RedstoneSide>)PROPERTY_BY_DIRECTION.get(direction));
			if (redstoneSide != RedstoneSide.NONE && levelAccessor.getBlockState(mutableBlockPos.setWithOffset(blockPos, direction)).getBlock() != this) {
				mutableBlockPos.move(Direction.DOWN);
				BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
				if (blockState2.getBlock() != Blocks.OBSERVER) {
					BlockPos blockPos2 = mutableBlockPos.relative(direction.getOpposite());
					BlockState blockState3 = blockState2.updateShape(
						direction.getOpposite(), levelAccessor.getBlockState(blockPos2), levelAccessor, mutableBlockPos, blockPos2
					);
					updateOrDestroy(blockState2, blockState3, levelAccessor, mutableBlockPos, i);
				}

				mutableBlockPos.setWithOffset(blockPos, direction).move(Direction.UP);
				BlockState blockState4 = levelAccessor.getBlockState(mutableBlockPos);
				if (blockState4.getBlock() != Blocks.OBSERVER) {
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
		BlockPos blockPos2 = blockPos.relative(direction);
		BlockState blockState = blockGetter.getBlockState(blockPos2);
		BlockPos blockPos3 = blockPos.above();
		BlockState blockState2 = blockGetter.getBlockState(blockPos3);
		if (!blockState2.isRedstoneConductor(blockGetter, blockPos3)) {
			boolean bl = blockState.isFaceSturdy(blockGetter, blockPos2, Direction.UP) || blockState.getBlock() == Blocks.HOPPER;
			if (bl && shouldConnectTo(blockGetter.getBlockState(blockPos2.above()))) {
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
	public boolean isUnstable() {
		return true;
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		return blockState2.isFaceSturdy(levelReader, blockPos2, Direction.UP) || blockState2.getBlock() == Blocks.HOPPER;
	}

	private BlockState updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState) {
		blockState = this.updatePowerStrengthImpl(level, blockPos, blockState);
		List<BlockPos> list = Lists.<BlockPos>newArrayList(this.toUpdate);
		this.toUpdate.clear();

		for (BlockPos blockPos2 : list) {
			level.updateNeighborsAt(blockPos2, this);
		}

		return blockState;
	}

	private BlockState updatePowerStrengthImpl(Level level, BlockPos blockPos, BlockState blockState) {
		BlockState blockState2 = blockState;
		int i = (Integer)blockState.getValue(POWER);
		this.shouldSignal = false;
		int j = level.getBestNeighborSignal(blockPos);
		this.shouldSignal = true;
		int k = 0;
		if (j < 15) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				BlockPos blockPos2 = blockPos.relative(direction);
				BlockState blockState3 = level.getBlockState(blockPos2);
				k = this.checkTarget(k, blockState3);
				BlockPos blockPos3 = blockPos.above();
				if (blockState3.isRedstoneConductor(level, blockPos2) && !level.getBlockState(blockPos3).isRedstoneConductor(level, blockPos3)) {
					k = this.checkTarget(k, level.getBlockState(blockPos2.above()));
				} else if (!blockState3.isRedstoneConductor(level, blockPos2)) {
					k = this.checkTarget(k, level.getBlockState(blockPos2.below()));
				}
			}
		}

		int l = k - 1;
		if (j > l) {
			l = j;
		}

		if (i != l) {
			blockState = blockState.setValue(POWER, Integer.valueOf(l));
			if (level.getBlockState(blockPos) == blockState2) {
				level.setBlock(blockPos, blockState, 2);
			}

			this.toUpdate.add(blockPos);

			for (Direction direction2 : Direction.values()) {
				this.toUpdate.add(blockPos.relative(direction2));
			}
		}

		return blockState;
	}

	private void checkCornerChangeAt(Level level, BlockPos blockPos) {
		if (level.getBlockState(blockPos).getBlock() == this) {
			level.updateNeighborsAt(blockPos, this);

			for (Direction direction : Direction.values()) {
				level.updateNeighborsAt(blockPos.relative(direction), this);
			}
		}
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (blockState2.getBlock() != blockState.getBlock() && !level.isClientSide) {
			this.updatePowerStrength(level, blockPos, blockState);

			for (Direction direction : Direction.Plane.VERTICAL) {
				level.updateNeighborsAt(blockPos.relative(direction), this);
			}

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
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!bl && blockState.getBlock() != blockState2.getBlock()) {
			super.onRemove(blockState, level, blockPos, blockState2, bl);
			if (!level.isClientSide) {
				for (Direction direction : Direction.values()) {
					level.updateNeighborsAt(blockPos.relative(direction), this);
				}

				this.updatePowerStrength(level, blockPos, blockState);

				for (Direction direction2 : Direction.Plane.HORIZONTAL) {
					this.checkCornerChangeAt(level, blockPos.relative(direction2));
				}

				for (Direction direction2 : Direction.Plane.HORIZONTAL) {
					BlockPos blockPos2 = blockPos.relative(direction2);
					if (level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
						this.checkCornerChangeAt(level, blockPos2.above());
					} else {
						this.checkCornerChangeAt(level, blockPos2.below());
					}
				}
			}
		}
	}

	private int checkTarget(int i, BlockState blockState) {
		if (blockState.getBlock() != this) {
			return i;
		} else {
			int j = (Integer)blockState.getValue(POWER);
			return j > i ? j : i;
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
		if (!this.shouldSignal) {
			return 0;
		} else {
			int i = (Integer)blockState.getValue(POWER);
			if (i == 0) {
				return 0;
			} else if (direction == Direction.UP) {
				return i;
			} else {
				EnumSet<Direction> enumSet = EnumSet.noneOf(Direction.class);

				for (Direction direction2 : Direction.Plane.HORIZONTAL) {
					if (this.isPowerSourceAt(blockGetter, blockPos, direction2)) {
						enumSet.add(direction2);
					}
				}

				if (direction.getAxis().isHorizontal() && enumSet.isEmpty()) {
					return i;
				} else {
					return enumSet.contains(direction) && !enumSet.contains(direction.getCounterClockWise()) && !enumSet.contains(direction.getClockWise()) ? i : 0;
				}
			}
		}
	}

	private boolean isPowerSourceAt(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		BlockPos blockPos2 = blockPos.relative(direction);
		BlockState blockState = blockGetter.getBlockState(blockPos2);
		boolean bl = blockState.isRedstoneConductor(blockGetter, blockPos2);
		BlockPos blockPos3 = blockPos.above();
		boolean bl2 = blockGetter.getBlockState(blockPos3).isRedstoneConductor(blockGetter, blockPos3);
		if (!bl2 && bl && shouldConnectTo(blockGetter, blockPos2.above())) {
			return true;
		} else if (shouldConnectTo(blockState, direction)) {
			return true;
		} else {
			return blockState.getBlock() == Blocks.REPEATER && blockState.getValue(DiodeBlock.POWERED) && blockState.getValue(DiodeBlock.FACING) == direction
				? true
				: !bl && shouldConnectTo(blockGetter, blockPos2.below());
		}
	}

	protected static boolean shouldConnectTo(BlockGetter blockGetter, BlockPos blockPos) {
		return shouldConnectTo(blockGetter.getBlockState(blockPos));
	}

	protected static boolean shouldConnectTo(BlockState blockState) {
		return shouldConnectTo(blockState, null);
	}

	protected static boolean shouldConnectTo(BlockState blockState, @Nullable Direction direction) {
		Block block = blockState.getBlock();
		if (block == Blocks.REDSTONE_WIRE) {
			return true;
		} else if (blockState.getBlock() == Blocks.REPEATER) {
			Direction direction2 = blockState.getValue(RepeaterBlock.FACING);
			return direction2 == direction || direction2.getOpposite() == direction;
		} else {
			return Blocks.OBSERVER == blockState.getBlock() ? direction == blockState.getValue(ObserverBlock.FACING) : blockState.isSignalSource() && direction != null;
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
