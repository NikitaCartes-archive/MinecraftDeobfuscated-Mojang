package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrossCollisionBlock extends Block implements SimpleWaterloggedBlock {
	public static final BooleanProperty NORTH = PipeBlock.NORTH;
	public static final BooleanProperty EAST = PipeBlock.EAST;
	public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
	public static final BooleanProperty WEST = PipeBlock.WEST;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = (Map<Direction, BooleanProperty>)PipeBlock.PROPERTY_BY_DIRECTION
		.entrySet()
		.stream()
		.filter(entry -> ((Direction)entry.getKey()).getAxis().isHorizontal())
		.collect(Util.toMap());
	protected final VoxelShape[] collisionShapeByIndex;
	protected final VoxelShape[] shapeByIndex;
	private final Object2IntMap<BlockState> stateToIndex = new Object2IntOpenHashMap<>();

	protected CrossCollisionBlock(float f, float g, float h, float i, float j, BlockBehaviour.Properties properties) {
		super(properties);
		this.collisionShapeByIndex = this.makeShapes(f, g, j, 0.0F, j);
		this.shapeByIndex = this.makeShapes(f, g, h, 0.0F, i);

		for (BlockState blockState : this.stateDefinition.getPossibleStates()) {
			this.getAABBIndex(blockState);
		}
	}

	protected VoxelShape[] makeShapes(float f, float g, float h, float i, float j) {
		float k = 8.0F - f;
		float l = 8.0F + f;
		float m = 8.0F - g;
		float n = 8.0F + g;
		VoxelShape voxelShape = Block.box((double)k, 0.0, (double)k, (double)l, (double)h, (double)l);
		VoxelShape voxelShape2 = Block.box((double)m, (double)i, 0.0, (double)n, (double)j, (double)n);
		VoxelShape voxelShape3 = Block.box((double)m, (double)i, (double)m, (double)n, (double)j, 16.0);
		VoxelShape voxelShape4 = Block.box(0.0, (double)i, (double)m, (double)n, (double)j, (double)n);
		VoxelShape voxelShape5 = Block.box((double)m, (double)i, (double)m, 16.0, (double)j, (double)n);
		VoxelShape voxelShape6 = Shapes.or(voxelShape2, voxelShape5);
		VoxelShape voxelShape7 = Shapes.or(voxelShape3, voxelShape4);
		VoxelShape[] voxelShapes = new VoxelShape[]{
			Shapes.empty(),
			voxelShape3,
			voxelShape4,
			voxelShape7,
			voxelShape2,
			Shapes.or(voxelShape3, voxelShape2),
			Shapes.or(voxelShape4, voxelShape2),
			Shapes.or(voxelShape7, voxelShape2),
			voxelShape5,
			Shapes.or(voxelShape3, voxelShape5),
			Shapes.or(voxelShape4, voxelShape5),
			Shapes.or(voxelShape7, voxelShape5),
			voxelShape6,
			Shapes.or(voxelShape3, voxelShape6),
			Shapes.or(voxelShape4, voxelShape6),
			Shapes.or(voxelShape7, voxelShape6)
		};

		for (int o = 0; o < 16; o++) {
			voxelShapes[o] = Shapes.or(voxelShape, voxelShapes[o]);
		}

		return voxelShapes;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return !(Boolean)blockState.getValue(WATERLOGGED);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.shapeByIndex[this.getAABBIndex(blockState)];
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.collisionShapeByIndex[this.getAABBIndex(blockState)];
	}

	private static int indexFor(Direction direction) {
		return 1 << direction.get2DDataValue();
	}

	protected int getAABBIndex(BlockState blockState) {
		return this.stateToIndex.computeIntIfAbsent(blockState, blockStatex -> {
			int i = 0;
			if ((Boolean)blockStatex.getValue(NORTH)) {
				i |= indexFor(Direction.NORTH);
			}

			if ((Boolean)blockStatex.getValue(EAST)) {
				i |= indexFor(Direction.EAST);
			}

			if ((Boolean)blockStatex.getValue(SOUTH)) {
				i |= indexFor(Direction.SOUTH);
			}

			if ((Boolean)blockStatex.getValue(WEST)) {
				i |= indexFor(Direction.WEST);
			}

			return i;
		});
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
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
}
