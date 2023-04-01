package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class RotatedPillarBlock extends Block {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	public RotatedPillarBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y));
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
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

	@Override
	public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
		if (blockState.is(BlockTags.LOGS) && Rules.PREVENT_FLOATING_TREES.get()) {
			BlockState blockState2 = serverLevel.getBlockState(blockPos.above());
			boolean bl2 = blockState2.is(BlockTags.LOGS) || blockState2.is(Blocks.END_ROD);
			BlockState blockState3 = serverLevel.getBlockState(blockPos.below());
			boolean bl3 = blockState3.is(BlockTags.LOGS) || blockState2.is(Blocks.END_ROD);
			if (bl2 || bl3) {
				serverLevel.setBlock(blockPos, Blocks.END_ROD.defaultBlockState(), 16);
			}
		}

		super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
	}
}
