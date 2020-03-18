package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class EntityBoundSoundInstance extends AbstractTickableSoundInstance {
	private final Entity entity;

	public EntityBoundSoundInstance(SoundEvent soundEvent, SoundSource soundSource, Entity entity) {
		this(soundEvent, soundSource, 1.0F, 1.0F, entity);
	}

	public EntityBoundSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float g, Entity entity) {
		super(soundEvent, soundSource);
		this.volume = f;
		this.pitch = g;
		this.entity = entity;
		this.x = (float)this.entity.getX();
		this.y = (float)this.entity.getY();
		this.z = (float)this.entity.getZ();
	}

	@Override
	public boolean canPlaySound() {
		return !this.entity.isSilent();
	}

	@Override
	public void tick() {
		if (this.entity.removed) {
			this.stop();
		} else {
			this.x = (float)this.entity.getX();
			this.y = (float)this.entity.getY();
			this.z = (float)this.entity.getZ();
		}
	}
}
