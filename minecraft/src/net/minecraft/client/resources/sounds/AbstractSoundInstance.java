package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

@Environment(EnvType.CLIENT)
public abstract class AbstractSoundInstance implements SoundInstance {
	protected Sound sound;
	protected final SoundSource source;
	protected final ResourceLocation location;
	protected float volume = 1.0F;
	protected float pitch = 1.0F;
	protected double x;
	protected double y;
	protected double z;
	protected boolean looping;
	protected int delay;
	protected SoundInstance.Attenuation attenuation = SoundInstance.Attenuation.LINEAR;
	protected boolean relative;

	protected AbstractSoundInstance(SoundEvent soundEvent, SoundSource soundSource) {
		this(soundEvent.getLocation(), soundSource);
	}

	protected AbstractSoundInstance(ResourceLocation resourceLocation, SoundSource soundSource) {
		this.location = resourceLocation;
		this.source = soundSource;
	}

	@Override
	public ResourceLocation getLocation() {
		return this.location;
	}

	@Override
	public WeighedSoundEvents resolve(SoundManager soundManager) {
		WeighedSoundEvents weighedSoundEvents = soundManager.getSoundEvent(this.location);
		if (weighedSoundEvents == null) {
			this.sound = SoundManager.EMPTY_SOUND;
		} else {
			this.sound = weighedSoundEvents.getSound();
		}

		return weighedSoundEvents;
	}

	@Override
	public Sound getSound() {
		return this.sound;
	}

	@Override
	public SoundSource getSource() {
		return this.source;
	}

	@Override
	public boolean isLooping() {
		return this.looping;
	}

	@Override
	public int getDelay() {
		return this.delay;
	}

	@Override
	public float getVolume() {
		return this.volume * this.sound.getVolume();
	}

	@Override
	public float getPitch() {
		return this.pitch * this.sound.getPitch();
	}

	@Override
	public double getX() {
		return this.x;
	}

	@Override
	public double getY() {
		return this.y;
	}

	@Override
	public double getZ() {
		return this.z;
	}

	@Override
	public SoundInstance.Attenuation getAttenuation() {
		return this.attenuation;
	}

	@Override
	public boolean isRelative() {
		return this.relative;
	}

	public String toString() {
		return "SoundInstance[" + this.location + "]";
	}
}
