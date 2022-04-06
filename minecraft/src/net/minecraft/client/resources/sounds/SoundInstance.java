package net.minecraft.client.resources.sounds;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public interface SoundInstance {
	ResourceLocation getLocation();

	@Nullable
	WeighedSoundEvents resolve(SoundManager soundManager);

	Sound getSound();

	SoundSource getSource();

	boolean isLooping();

	boolean isRelative();

	int getDelay();

	float getVolume();

	float getPitch();

	double getX();

	double getY();

	double getZ();

	SoundInstance.Attenuation getAttenuation();

	default boolean canStartSilent() {
		return false;
	}

	default boolean canPlaySound() {
		return true;
	}

	static RandomSource createUnseededRandom() {
		return RandomSource.create();
	}

	@Environment(EnvType.CLIENT)
	public static enum Attenuation {
		NONE,
		LINEAR;
	}
}
