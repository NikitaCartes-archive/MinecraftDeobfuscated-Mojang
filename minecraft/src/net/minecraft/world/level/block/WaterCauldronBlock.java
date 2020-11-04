package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WaterCauldronBlock extends AbstractCauldronBlock {
	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;

	public WaterCauldronBlock(BlockBehaviour.Properties properties) {
		super(properties, CauldronInteraction.WATER);
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(1)));
	}

	@Override
	protected double getContentHeight(BlockState blockState) {
		return (double)(6 + (Integer)blockState.getValue(LEVEL) * 3) / 16.0;
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!level.isClientSide && entity.isOnFire() && this.isEntityInsideContent(blockState, blockPos, entity)) {
			entity.clearFire();
			lowerWaterLevel(blockState, level, blockPos);
		}
	}

	public static void lowerWaterLevel(BlockState blockState, Level level, BlockPos blockPos) {
		int i = (Integer)blockState.getValue(LEVEL) - 1;
		level.setBlockAndUpdate(blockPos, i == 0 ? Blocks.CAULDRON.defaultBlockState() : blockState.setValue(LEVEL, Integer.valueOf(i)));
	}

	@Override
	public void handleRain(BlockState blockState, Level level, BlockPos blockPos) {
		if (CauldronBlock.shouldHandleRain(level, blockPos) && (Integer)blockState.getValue(LEVEL) != 3) {
			level.setBlockAndUpdate(blockPos, blockState.cycle(LEVEL));
		}
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return (Integer)blockState.getValue(LEVEL);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}
}
