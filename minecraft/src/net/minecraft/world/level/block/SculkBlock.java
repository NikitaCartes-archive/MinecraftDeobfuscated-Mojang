package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public class SculkBlock extends DropExperienceBlock implements SculkBehaviour {
	public SculkBlock(BlockBehaviour.Properties properties) {
		super(properties, ConstantInt.of(1));
	}

	@Override
	public int attemptUseCharge(
		SculkSpreader.ChargeCursor chargeCursor, LevelAccessor levelAccessor, BlockPos blockPos, Random random, SculkSpreader sculkSpreader, boolean bl
	) {
		int i = chargeCursor.getCharge();
		if (i != 0 && random.nextInt(sculkSpreader.chargeDecayRate()) == 0) {
			BlockPos blockPos2 = chargeCursor.getPos();
			boolean bl2 = blockPos2.closerThan(blockPos, (double)sculkSpreader.noGrowthRadius());
			if (!bl2 && canPlaceGrowth(levelAccessor, blockPos2)) {
				int j = sculkSpreader.growthSpawnCost();
				if (random.nextInt(j) < i) {
					BlockPos blockPos3 = blockPos2.above();
					BlockState blockState = this.getRandomGrowthState(levelAccessor, blockPos3, random, sculkSpreader.isWorldGeneration());
					levelAccessor.setBlock(blockPos3, blockState, 3);
					levelAccessor.playSound(null, blockPos2, blockState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
				}

				return Math.max(0, i - j);
			} else {
				return random.nextInt(sculkSpreader.additionalDecayRate()) != 0 ? i : i - (bl2 ? 1 : getDecayPenalty(sculkSpreader, blockPos2, blockPos, i));
			}
		} else {
			return i;
		}
	}

	private static int getDecayPenalty(SculkSpreader sculkSpreader, BlockPos blockPos, BlockPos blockPos2, int i) {
		int j = sculkSpreader.noGrowthRadius();
		float f = Mth.square((float)Math.sqrt(blockPos.distSqr(blockPos2)) - (float)j);
		int k = Mth.square(24 - j);
		float g = Math.min(1.0F, f / (float)k);
		return Math.max(1, (int)((float)i * g * 0.5F));
	}

	private BlockState getRandomGrowthState(LevelAccessor levelAccessor, BlockPos blockPos, Random random, boolean bl) {
		BlockState blockState;
		if (random.nextInt(11) == 0) {
			blockState = Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, Boolean.valueOf(bl));
		} else {
			blockState = Blocks.SCULK_SENSOR.defaultBlockState();
		}

		return blockState.hasProperty(BlockStateProperties.WATERLOGGED) && !levelAccessor.getFluidState(blockPos).isEmpty()
			? blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true))
			: blockState;
	}

	private static boolean canPlaceGrowth(LevelAccessor levelAccessor, BlockPos blockPos) {
		BlockState blockState = levelAccessor.getBlockState(blockPos.above());
		if (blockState.isAir() || blockState.is(Blocks.WATER) && blockState.getFluidState().is(Fluids.WATER)) {
			int i = 0;

			for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-4, 0, -4), blockPos.offset(4, 2, 4))) {
				BlockState blockState2 = levelAccessor.getBlockState(blockPos2);
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
