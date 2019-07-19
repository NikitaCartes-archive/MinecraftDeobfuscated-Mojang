package net.minecraft.world.level.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class WoodButtonBlock extends ButtonBlock {
	protected WoodButtonBlock(Block.Properties properties) {
		super(true, properties);
	}

	@Override
	protected SoundEvent getSound(boolean bl) {
		return bl ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF;
	}
}
