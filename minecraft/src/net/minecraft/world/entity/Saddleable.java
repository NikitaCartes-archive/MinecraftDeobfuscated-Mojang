package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public interface Saddleable {
	boolean isSaddleable();

	void equipSaddle(@Nullable SoundSource soundSource);

	default SoundEvent getSaddleSoundEvent() {
		return SoundEvents.HORSE_SADDLE;
	}

	boolean isSaddled();
}
