package net.minecraft.world.level.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WoodButtonBlock extends ButtonBlock {
	protected WoodButtonBlock(BlockBehaviour.Properties properties) {
		super(true, properties);
	}

	@Override
	protected SoundEvent getSound(boolean bl) {
		return bl ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF;
	}
}
