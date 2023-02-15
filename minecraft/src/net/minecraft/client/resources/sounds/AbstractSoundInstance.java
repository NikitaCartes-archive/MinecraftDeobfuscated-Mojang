package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

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
	protected RandomSource random;

	protected AbstractSoundInstance(SoundEvent soundEvent, SoundSource soundSource, RandomSource randomSource) {
		this(soundEvent.getLocation(), soundSource, randomSource);
	}

	protected AbstractSoundInstance(ResourceLocation resourceLocation, SoundSource soundSource, RandomSource randomSource) {
		this.location = resourceLocation;
		this.source = soundSource;
		this.random = randomSource;
	}

	@Override
	public ResourceLocation getLocation() {
		return this.location;
	}

	@Override
	public WeighedSoundEvents resolve(SoundManager soundManager) {
		if (this.location.equals(SoundManager.INTENTIONALLY_EMPTY_SOUND_LOCATION)) {
			this.sound = SoundManager.INTENTIONALLY_EMPTY_SOUND;
			return SoundManager.INTENTIONALLY_EMPTY_SOUND_EVENT;
		} else {
			WeighedSoundEvents weighedSoundEvents = soundManager.getSoundEvent(this.location);
			if (weighedSoundEvents == null) {
				this.sound = SoundManager.EMPTY_SOUND;
			} else {
				this.sound = weighedSoundEvents.getSound(this.random);
			}

			return weighedSoundEvents;
		}
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
		return this.volume * this.sound.getVolume().sample(this.random);
	}

	@Override
	public float getPitch() {
		return this.pitch * this.sound.getPitch().sample(this.random);
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
