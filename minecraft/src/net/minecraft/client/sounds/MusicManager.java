package net.minecraft.client.sounds;

import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class MusicManager {
	private static final int STARTING_DELAY = 100;
	private final Random random = new Random();
	private final Minecraft minecraft;
	@Nullable
	private SoundInstance currentMusic;
	private int nextSongDelay = 100;

	public MusicManager(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void tick() {
		Music music = this.minecraft.getSituationalMusic();
		if (this.currentMusic != null) {
			if (!music.getEvent().getLocation().equals(this.currentMusic.getLocation()) && music.replaceCurrentMusic()) {
				this.minecraft.getSoundManager().stop(this.currentMusic);
				this.nextSongDelay = Mth.nextInt(this.random, 0, music.getMinDelay() / 2);
			}

			if (!this.minecraft.getSoundManager().isActive(this.currentMusic)) {
				this.currentMusic = null;
				this.nextSongDelay = Math.min(this.nextSongDelay, Mth.nextInt(this.random, music.getMinDelay(), music.getMaxDelay()));
			}
		}

		this.nextSongDelay = Math.min(this.nextSongDelay, music.getMaxDelay());
		if (this.currentMusic == null && this.nextSongDelay-- <= 0) {
			this.startPlaying(music);
		}
	}

	public void startPlaying(Music music) {
		this.currentMusic = SimpleSoundInstance.forMusic(music.getEvent());
		if (this.currentMusic.getSound() != SoundManager.EMPTY_SOUND) {
			this.minecraft.getSoundManager().play(this.currentMusic);
		}

		this.nextSongDelay = Integer.MAX_VALUE;
	}

	public void stopPlaying() {
		if (this.currentMusic != null) {
			this.minecraft.getSoundManager().stop(this.currentMusic);
			this.currentMusic = null;
		}

		this.nextSongDelay += 100;
	}

	public boolean isPlayingMusic(Music music) {
		return this.currentMusic == null ? false : music.getEvent().getLocation().equals(this.currentMusic.getLocation());
	}
}
