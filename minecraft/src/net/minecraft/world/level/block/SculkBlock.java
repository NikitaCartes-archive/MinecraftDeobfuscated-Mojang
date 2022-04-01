package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class SculkBlock extends DropExperienceBlock implements SculkBehaviour {
	private static final int VARYING_GROWTH_RATE_DISTANCE_SQUARED = 400;

	public SculkBlock(BlockBehaviour.Properties properties) {
		super(properties, ConstantInt.of(1));
	}

	@Override
	public int attemptUseCharge(SculkSpreader.ChargeCursor chargeCursor, Level level, BlockPos blockPos, Random random) {
		int i = chargeCursor.getCharge();
		if (i != 0 && random.nextInt(10) == 0) {
			BlockPos blockPos2 = chargeCursor.getPos();
			boolean bl = blockPos2.closerThan(blockPos, 4.0);
			if (!bl && canPlaceGrowth(level, blockPos2)) {
				if (random.nextInt(10) < i) {
					level.setBlock(blockPos2.above(), Blocks.SCULK_SENSOR.defaultBlockState(), 3);
					level.playSound(null, blockPos2, SoundEvents.SCULK_SENSOR_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
				}

				return Math.max(0, i - 10);
			} else {
				return random.nextInt(5) != 0 ? i : i - (bl ? 1 : getDecayPenalty(blockPos2, blockPos, i));
			}
		} else {
			return i;
		}
	}

	private static int getDecayPenalty(BlockPos blockPos, BlockPos blockPos2, int i) {
		float f = (float)Math.sqrt(blockPos.distSqr(blockPos2)) - 4.0F;
		float g = Math.min(1.0F, f * f / 400.0F);
		return Math.max(1, (int)((float)i * g * 0.5F));
	}

	private static boolean canPlaceGrowth(Level level, BlockPos blockPos) {
		BlockState blockState = level.getBlockState(blockPos.above());
		if (blockState.isAir() || blockState.is(Blocks.WATER) && blockState.getFluidState().is(Fluids.WATER)) {
			int i = 0;

			for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-4, 0, -4), blockPos.offset(4, 2, 4))) {
				BlockState blockState2 = level.getBlockState(blockPos2);
				if (blockState2.is(Blocks.SCULK_SENSOR)) {
					i++;
				}

				if (i > 2) {
					return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canChangeBlockStateOnSpread() {
		return false;
	}
}
