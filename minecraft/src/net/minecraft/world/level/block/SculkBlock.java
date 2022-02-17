package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SculkBlock extends DropExperienceBlock implements SculkBehaviour {
	private static final int VARYING_GROWTH_RATE_DISTANCE_SQUARED = 400;

	public SculkBlock(BlockBehaviour.Properties properties) {
		super(properties, ConstantInt.of(1));
	}

	@Override
	public short attemptUseCharge(SculkSpreader.ChargeCursor chargeCursor, Level level, BlockPos blockPos, Random random) {
		short s = chargeCursor.getCharge();
		if (s != 0 && random.nextInt(10) == 0) {
			BlockPos blockPos2 = chargeCursor.getPos();
			boolean bl = this.isCloseToCatalyst(blockPos2, blockPos);
			boolean bl2 = random.nextInt(10) == 0;
			if (!bl && this.canPlaceGrowth(level, blockPos2)) {
				if (random.nextInt(10) < s) {
					level.setBlock(blockPos2.above(), (bl2 ? Blocks.SCULK_SHRIEKER : Blocks.SCULK_SENSOR).defaultBlockState(), 3);
					level.playSound(null, blockPos2, bl2 ? SoundEvents.SCULK_SHRIEKER_PLACE : SoundEvents.SCULK_SENSOR_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
				}

				return (short)Math.max(0, s - 10);
			} else {
				return random.nextInt(5) != 0 ? s : (short)(s - (bl ? 1 : this.getDecayPenalty(blockPos2, blockPos, s)));
			}
		} else {
			return s;
		}
	}

	private boolean isCloseToCatalyst(BlockPos blockPos, BlockPos blockPos2) {
		return blockPos.distSqr(blockPos2) <= 16.0;
	}

	private int getDecayPenalty(BlockPos blockPos, BlockPos blockPos2, int i) {
		float f = (float)Math.sqrt(blockPos.distSqr(blockPos2)) - 4.0F;
		float g = Math.min(1.0F, f * f / 400.0F);
		return Math.max(1, (int)(g * (float)i * 0.5F));
	}

	private boolean canPlaceGrowth(Level level, BlockPos blockPos) {
		BlockState blockState = level.getBlockState(blockPos.above());
		if (blockState.isAir() || blockState.is(Blocks.WATER) && blockState.getFluidState().isSource()) {
			int i = 0;

			for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-4, 0, -4), blockPos.offset(4, 2, 4))) {
				BlockState blockState2 = level.getBlockState(blockPos2);
				if (blockState2.is(Blocks.SCULK_SENSOR) || blockState2.is(Blocks.SCULK_SHRIEKER)) {
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
