package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

@Environment(EnvType.CLIENT)
public abstract class AbstractTickableSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
	protected boolean stopped;

	protected AbstractTickableSoundInstance(SoundEvent soundEvent, SoundSource soundSource) {
		super(soundEvent, soundSource);
	}

	@Override
	public boolean isStopped() {
		return this.stopped;
	}
}
