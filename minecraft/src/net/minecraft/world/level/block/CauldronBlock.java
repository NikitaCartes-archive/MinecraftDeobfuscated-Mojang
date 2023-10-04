package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
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
	public static final MapCodec<CauldronBlock> CODEC = simpleCodec(CauldronBlock::new);
	private static final float RAIN_FILL_CHANCE = 0.05F;
	private static final float POWDER_SNOW_FILL_CHANCE = 0.1F;

	@Override
	public MapCodec<CauldronBlock> codec() {
		return CODEC;
	}

	public CauldronBlock(BlockBehaviour.Properties properties) {
		super(properties, CauldronInteraction.EMPTY);
	}

	@Override
	public boolean isFull(BlockState blockState) {
		return false;
	}

	protected static boolean shouldHandlePrecipitation(Level level, Biome.Precipitation precipitation) {
		if (precipitation == Biome.Precipitation.RAIN) {
			return level.getRandom().nextFloat() < 0.05F;
		} else {
			return precipitation == Biome.Precipitation.SNOW ? level.getRandom().nextFloat() < 0.1F : false;
		}
	}

	@Override
	public void handlePrecipitation(BlockState blockState, Level level, BlockPos blockPos, Biome.Precipitation precipitation) {
		if (shouldHandlePrecipitation(level, precipitation)) {
			if (precipitation == Biome.Precipitation.RAIN) {
				level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState());
				level.gameEvent(null, GameEvent.BLOCK_CHANGE, blockPos);
			} else if (precipitation == Biome.Precipitation.SNOW) {
				level.setBlockAndUpdate(blockPos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState());
				level.gameEvent(null, GameEvent.BLOCK_CHANGE, blockPos);
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
			BlockState blockState2 = Blocks.WATER_CAULDRON.defaultBlockState();
			level.setBlockAndUpdate(blockPos, blockState2);
			level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState2));
			level.levelEvent(1047, blockPos, 0);
		} else if (fluid == Fluids.LAVA) {
			BlockState blockState2 = Blocks.LAVA_CAULDRON.defaultBlockState();
			level.setBlockAndUpdate(blockPos, blockState2);
			level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState2));
			level.levelEvent(1046, blockPos, 0);
		}
	}
}
