package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CauldronBlock extends AbstractCauldronBlock {
	public CauldronBlock(BlockBehaviour.Properties properties) {
		super(properties, CauldronInteraction.EMPTY);
	}

	protected static boolean shouldHandlePrecipitation(Level level) {
		return level.random.nextInt(20) == 1;
	}

	@Override
	public void handlePrecipitation(BlockState blockState, Level level, BlockPos blockPos, Biome.Precipitation precipitation) {
		if (shouldHandlePrecipitation(level)) {
			if (precipitation == Biome.Precipitation.RAIN) {
				level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState());
			} else if (precipitation == Biome.Precipitation.SNOW) {
				level.setBlockAndUpdate(blockPos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState());
			}
		}
	}
}
