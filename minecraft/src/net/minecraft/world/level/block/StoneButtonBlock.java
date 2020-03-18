package net.minecraft.world.level.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class StoneButtonBlock extends ButtonBlock {
	protected StoneButtonBlock(BlockBehaviour.Properties properties) {
		super(false, properties);
	}

	@Override
	protected SoundEvent getSound(boolean bl) {
		return bl ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF;
	}
}
