package net.minecraft.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class Music {
	private final SoundEvent event;
	private final int minDelay;
	private final int maxDelay;
	private final boolean replaceCurrentMusic;

	public Music(SoundEvent soundEvent, int i, int j, boolean bl) {
		this.event = soundEvent;
		this.minDelay = i;
		this.maxDelay = j;
		this.replaceCurrentMusic = bl;
	}

	@Environment(EnvType.CLIENT)
	public SoundEvent getEvent() {
		return this.event;
	}

	@Environment(EnvType.CLIENT)
	public int getMinDelay() {
		return this.minDelay;
	}

	@Environment(EnvType.CLIENT)
	public int getMaxDelay() {
		return this.maxDelay;
	}

	@Environment(EnvType.CLIENT)
	public boolean replaceCurrentMusic() {
		return this.replaceCurrentMusic;
	}
}
