package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Music {
	public static final Codec<Music> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					SoundEvent.CODEC.fieldOf("sound").forGetter(music -> music.event),
					Codec.INT.fieldOf("min_delay").forGetter(music -> music.minDelay),
					Codec.INT.fieldOf("max_delay").forGetter(music -> music.maxDelay),
					Codec.BOOL.fieldOf("replace_current_music").forGetter(music -> music.replaceCurrentMusic)
				)
				.apply(instance, Music::new)
	);
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

	public SoundEvent getEvent() {
		return this.event;
	}

	public int getMinDelay() {
		return this.minDelay;
	}

	public int getMaxDelay() {
		return this.maxDelay;
	}

	public boolean replaceCurrentMusic() {
		return this.replaceCurrentMusic;
	}
}
