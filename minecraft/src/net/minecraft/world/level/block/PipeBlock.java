package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PipeBlock extends Block {
	private static final Direction[] DIRECTIONS = Direction.values();
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
		enumMap.put(Direction.NORTH, NORTH);
		enumMap.put(Direction.EAST, EAST);
		enumMap.put(Direction.SOUTH, SOUTH);
		enumMap.put(Direction.WEST, WEST);
		enumMap.put(Direction.UP, UP);
		enumMap.put(Direction.DOWN, DOWN);
	}));
	protected final VoxelShape[] shapeByIndex;

	protected PipeBlock(float f, BlockBehaviour.Properties properties) {
		super(properties);
		this.shapeByIndex = this.makeShapes(f);
	}

	private VoxelShape[] makeShapes(float f) {
		float g = 0.5F - f;
		float h = 0.5F + f;
		VoxelShape voxelShape = Block.box(
			(double)(g * 16.0F), (double)(g * 16.0F), (double)(g * 16.0F), (double)(h * 16.0F), (double)(h * 16.0F), (double)(h * 16.0F)
		);
		VoxelShape[] voxelShapes = new VoxelShape[DIRECTIONS.length];

		for (int i = 0; i < DIRECTIONS.length; i++) {
			Direction direction = DIRECTIONS[i];
			voxelShapes[i] = Shapes.box(
				0.5 + Math.min((double)(-f), (double)direction.getStepX() * 0.5),
				0.5 + Math.min((double)(-f), (double)direction.getStepY() * 0.5),
				0.5 + Math.min((double)(-f), (double)direction.getStepZ() * 0.5),
				0.5 + Math.max((double)f, (double)direction.getStepX() * 0.5),
				0.5 + Math.max((double)f, (double)direction.getStepY() * 0.5),
				0.5 + Math.max((double)f, (double)direction.getStepZ() * 0.5)
			);
		}

		VoxelShape[] voxelShapes2 = new VoxelShape[64];

		for (int j = 0; j < 64; j++) {
			VoxelShape voxelShape2 = voxelShape;

			for (int k = 0; k < DIRECTIONS.length; k++) {
				if ((j & 1 << k) != 0) {
					voxelShape2 = Shapes.or(voxelShape2, voxelShapes[k]);
				}
			}

			voxelShapes2[j] = voxelShape2;
		}

		return voxelShapes2;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.shapeByIndex[this.getAABBIndex(blockState)];
	}

	protected int getAABBIndex(BlockState blockState) {
		int i = 0;

		for (int j = 0; j < DIRECTIONS.length; j++) {
			if ((Boolean)blockState.getValue((Property)PROPERTY_BY_DIRECTION.get(DIRECTIONS[j]))) {
				i |= 1 << j;
			}
		}

		return i;
	}
}
