package net.minecraft.world.level.biome;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvent;

public class AmbientAdditionsSettings {
	private SoundEvent soundEvent;
	private double tickChance;

	public AmbientAdditionsSettings(SoundEvent soundEvent, double d) {
		this.soundEvent = soundEvent;
		this.tickChance = d;
	}

	@Environment(EnvType.CLIENT)
	public SoundEvent getSoundEvent() {
		return this.soundEvent;
	}

	@Environment(EnvType.CLIENT)
	public double getTickChance() {
		return this.tickChance;
	}
}
