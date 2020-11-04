package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CauldronBlock extends AbstractCauldronBlock {
	public CauldronBlock(BlockBehaviour.Properties properties) {
		super(properties, CauldronInteraction.EMPTY);
	}

	protected static boolean shouldHandleRain(Level level, BlockPos blockPos) {
		return level.random.nextInt(20) != 1 ? false : level.getBiome(blockPos).getTemperature(blockPos) >= 0.15F;
	}

	@Override
	public void handleRain(BlockState blockState, Level level, BlockPos blockPos) {
		if (shouldHandleRain(level, blockPos)) {
			level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState());
		}
	}
}
