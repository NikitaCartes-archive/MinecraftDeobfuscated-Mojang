package net.minecraft.client.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.SoundInstance;

@Environment(EnvType.CLIENT)
public interface SoundEventListener {
	void onPlaySound(SoundInstance soundInstance, WeighedSoundEvents weighedSoundEvents, float f);
}
