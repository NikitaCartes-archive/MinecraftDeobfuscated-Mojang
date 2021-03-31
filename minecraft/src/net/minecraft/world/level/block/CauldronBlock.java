package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class CauldronBlock extends AbstractCauldronBlock {
	public CauldronBlock(BlockBehaviour.Properties properties) {
		super(properties, CauldronInteraction.EMPTY);
	}

	@Override
	public boolean isFull(BlockState blockState) {
		return false;
	}

	protected static boolean shouldHandlePrecipitation(Level level) {
		return level.random.nextInt(20) == 1;
	}

	@Override
	public void handlePrecipitation(BlockState blockState, Level level, BlockPos blockPos, Biome.Precipitation precipitation) {
		if (shouldHandlePrecipitation(level)) {
			if (precipitation == Biome.Precipitation.RAIN) {
				level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState());
				level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
			} else if (precipitation == Biome.Precipitation.SNOW) {
				level.setBlockAndUpdate(blockPos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState());
				level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
			}
		}
	}

	@Override
	protected boolean canReceiveStalactiteDrip(Fluid fluid) {
		return true;
	}

	@Override
	protected void receiveStalactiteDrip(BlockState blockState, Level level, BlockPos blockPos, Fluid fluid) {
		if (fluid == Fluids.WATER) {
			level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState());
			level.levelEvent(1047, blockPos, 0);
			level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
		} else if (fluid == Fluids.LAVA) {
			level.setBlockAndUpdate(blockPos, Blocks.LAVA_CAULDRON.defaultBlockState());
			level.levelEvent(1046, blockPos, 0);
			level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
		}
	}
}
