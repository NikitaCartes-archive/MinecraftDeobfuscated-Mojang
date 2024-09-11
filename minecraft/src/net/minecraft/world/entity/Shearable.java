package net.minecraft.world.entity;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public interface Shearable {
	void shear(SoundSource soundSource, ItemStack itemStack);

	boolean readyForShearing();
}
