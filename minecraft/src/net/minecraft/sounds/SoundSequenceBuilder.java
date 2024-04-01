package net.minecraft.sounds;

@FunctionalInterface
public interface SoundSequenceBuilder {
	void waitThenPlay(int i, SoundEvent soundEvent, SoundSource soundSource, float f, float g);
}
