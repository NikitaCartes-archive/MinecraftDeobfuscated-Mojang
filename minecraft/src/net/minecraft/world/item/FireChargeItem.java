package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FireChargeItem extends Item {
	public FireChargeItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		boolean bl = false;
		if (CampfireBlock.canLight(blockState)) {
			this.playSound(level, blockPos);
			level.setBlockAndUpdate(blockPos, blockState.setValue(CampfireBlock.LIT, Boolean.valueOf(true)));
			bl = true;
		} else {
			blockPos = blockPos.relative(useOnContext.getClickedFace());
			if (BaseFireBlock.canBePlacedAt(level, blockPos)) {
				this.playSound(level, blockPos);
				level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(level, blockPos));
				bl = true;
			}
		}

		if (bl) {
			useOnContext.getItemInHand().shrink(1);
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return InteractionResult.FAIL;
		}
	}

	private void playSound(Level level, BlockPos blockPos) {
		level.playSound(null, blockPos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
	}
}
