package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FireChargeItem extends Item {
	public FireChargeItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			BlockPos blockPos = useOnContext.getClickedPos();
			BlockState blockState = level.getBlockState(blockPos);
			if (blockState.getBlock() == Blocks.CAMPFIRE) {
				if (!(Boolean)blockState.getValue(CampfireBlock.LIT) && !(Boolean)blockState.getValue(CampfireBlock.WATERLOGGED)) {
					this.playSound(level, blockPos);
					level.setBlockAndUpdate(blockPos, blockState.setValue(CampfireBlock.LIT, Boolean.valueOf(true)));
				}
			} else {
				blockPos = blockPos.relative(useOnContext.getClickedFace());
				if (level.getBlockState(blockPos).isAir()) {
					this.playSound(level, blockPos);
					level.setBlockAndUpdate(blockPos, ((FireBlock)Blocks.FIRE).getStateForPlacement(level, blockPos));
				}
			}

			useOnContext.getItemInHand().shrink(1);
			return InteractionResult.SUCCESS;
		}
	}

	private void playSound(Level level, BlockPos blockPos) {
		level.playSound(null, blockPos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
	}
}
