package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public class RailBlock extends BaseRailBlock {
	public static final MapCodec<RailBlock> CODEC = simpleCodec(RailBlock::new);
	public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;

	@Override
	public MapCodec<RailBlock> codec() {
		return CODEC;
	}

	protected RailBlock(BlockBehaviour.Properties properties) {
		super(false, properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
	}

	@Override
	protected void updateState(BlockState blockState, Level level, BlockPos blockPos, Block block) {
		if (block.defaultBlockState().isSignalSource() && new RailState(level, blockPos, blockState).countPotentialConnections() == 3) {
			this.updateDir(level, blockPos, blockState, false);
		}
	}

	@Override
	public Property<RailShape> getShapeProperty() {
		return SHAPE;
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		RailShape railShape = blockState.getValue(SHAPE);

		return blockState.setValue(SHAPE, switch (rotation) {
			case CLOCKWISE_180 -> {
				switch (railShape) {
					case NORTH_SOUTH:
						yield RailShape.NORTH_SOUTH;
					case EAST_WEST:
						yield RailShape.EAST_WEST;
					case ASCENDING_EAST:
						yield RailShape.ASCENDING_WEST;
					case ASCENDING_WEST:
						yield RailShape.ASCENDING_EAST;
					case ASCENDING_NORTH:
						yield RailShape.ASCENDING_SOUTH;
					case ASCENDING_SOUTH:
						yield RailShape.ASCENDING_NORTH;
					case SOUTH_EAST:
						yield RailShape.NORTH_WEST;
					case SOUTH_WEST:
						yield RailShape.NORTH_EAST;
					case NORTH_WEST:
						yield RailShape.SOUTH_EAST;
					case NORTH_EAST:
						yield RailShape.SOUTH_WEST;
					default:
						throw new IncompatibleClassChangeError();
				}
			}
			case COUNTERCLOCKWISE_90 -> {
				switch (railShape) {
					case NORTH_SOUTH:
						yield RailShape.EAST_WEST;
					case EAST_WEST:
						yield RailShape.NORTH_SOUTH;
					case ASCENDING_EAST:
						yield RailShape.ASCENDING_NORTH;
					case ASCENDING_WEST:
						yield RailShape.ASCENDING_SOUTH;
					case ASCENDING_NORTH:
						yield RailShape.ASCENDING_WEST;
					case ASCENDING_SOUTH:
						yield RailShape.ASCENDING_EAST;
					case SOUTH_EAST:
						yield RailShape.NORTH_EAST;
					case SOUTH_WEST:
						yield RailShape.SOUTH_EAST;
					case NORTH_WEST:
						yield RailShape.SOUTH_WEST;
					case NORTH_EAST:
						yield RailShape.NORTH_WEST;
					default:
						throw new IncompatibleClassChangeError();
				}
			}
			case CLOCKWISE_90 -> {
				switch (railShape) {
					case NORTH_SOUTH:
						yield RailShape.EAST_WEST;
					case EAST_WEST:
						yield RailShape.NORTH_SOUTH;
					case ASCENDING_EAST:
						yield RailShape.ASCENDING_SOUTH;
					case ASCENDING_WEST:
						yield RailShape.ASCENDING_NORTH;
					case ASCENDING_NORTH:
						yield RailShape.ASCENDING_EAST;
					case ASCENDING_SOUTH:
						yield RailShape.ASCENDING_WEST;
					case SOUTH_EAST:
						yield RailShape.SOUTH_WEST;
					case SOUTH_WEST:
						yield RailShape.NORTH_WEST;
					case NORTH_WEST:
						yield RailShape.NORTH_EAST;
					case NORTH_EAST:
						yield RailShape.SOUTH_EAST;
					default:
						throw new IncompatibleClassChangeError();
				}
			}
			default -> railShape;
		});
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
		builder.add(SHAPE, WATERLOGGED);
	}
}
