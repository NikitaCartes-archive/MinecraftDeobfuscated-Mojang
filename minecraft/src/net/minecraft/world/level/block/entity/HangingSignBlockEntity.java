package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;

public class HangingSignBlockEntity extends SignBlockEntity {
	private static final int MAX_TEXT_LINE_WIDTH = 60;
	private static final int TEXT_LINE_HEIGHT = 9;

	public HangingSignBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.HANGING_SIGN, blockPos, blockState);
	}

	@Override
	public int getTextLineHeight() {
		return 9;
	}

	@Override
	public int getMaxTextLineWidth() {
		return 60;
	}

	@Override
	public SoundEvent getSignInteractionFailedSoundEvent() {
		return SoundEvents.WAXED_HANGING_SIGN_INTERACT_FAIL;
	}
}
