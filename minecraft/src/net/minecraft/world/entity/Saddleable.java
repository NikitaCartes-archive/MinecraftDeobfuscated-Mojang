package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundSource;

public interface Saddleable {
	boolean isSaddleable();

	void equipSaddle(@Nullable SoundSource soundSource);

	boolean isSaddled();
}
