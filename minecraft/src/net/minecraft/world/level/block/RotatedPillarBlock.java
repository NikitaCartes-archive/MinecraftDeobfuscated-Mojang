package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class RotatedPillarBlock extends Block {
	public static final MapCodec<RotatedPillarBlock> CODEC = simpleCodec(RotatedPillarBlock::new);
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	@Override
	public MapCodec<? extends RotatedPillarBlock> codec() {
		return CODEC;
	}

	public RotatedPillarBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return rotatePillar(blockState, rotation);
	}

	public static BlockState rotatePillar(BlockState blockState, Rotation rotation) {
		switch (rotation) {
			case COUNTERCLOCKWISE_90:
			case CLOCKWISE_90:
				switch ((Direction.Axis)blockState.getValue(AXIS)) {
					case X:
						return blockState.setValue(AXIS, Direction.Axis.Z);
					case Z:
						return blockState.setValue(AXIS, Direction.Axis.X);
					default:
						return blockState;
				}
			default:
				return blockState;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(AXIS, blockPlaceContext.getClickedFace().getAxis());
	}
}
