package net.minecraft.client.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface Weighted<T> {
	int getWeight();

	T getSound();

	void preloadIfRequired(SoundEngine soundEngine);
}
