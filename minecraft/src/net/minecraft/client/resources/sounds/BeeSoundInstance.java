package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;

@Environment(EnvType.CLIENT)
public abstract class BeeSoundInstance extends AbstractTickableSoundInstance {
	protected final Bee bee;
	private boolean hasSwitched;

	public BeeSoundInstance(Bee bee, SoundEvent soundEvent, SoundSource soundSource) {
		super(soundEvent, soundSource);
		this.bee = bee;
		this.x = (float)bee.getX();
		this.y = (float)bee.getY();
		this.z = (float)bee.getZ();
		this.looping = true;
		this.delay = 0;
		this.volume = 0.0F;
	}

	@Override
	public void tick() {
		boolean bl = this.shouldSwitchSounds();
		if (bl && !this.isStopped()) {
			Minecraft.getInstance().getSoundManager().queueTickingSound(this.getAlternativeSoundInstance());
			this.hasSwitched = true;
		}

		if (!this.bee.removed && !this.hasSwitched) {
			this.x = (float)this.bee.getX();
			this.y = (float)this.bee.getY();
			this.z = (float)this.bee.getZ();
			float f = Mth.sqrt(Entity.getHorizontalDistanceSqr(this.bee.getDeltaMovement()));
			if ((double)f >= 0.01) {
				this.pitch = Mth.lerp(Mth.clamp(f, this.getMinPitch(), this.getMaxPitch()), this.getMinPitch(), this.getMaxPitch());
				this.volume = Mth.lerp(Mth.clamp(f, 0.0F, 0.5F), 0.0F, 1.2F);
			} else {
				this.pitch = 0.0F;
				this.volume = 0.0F;
			}
		} else {
			this.stop();
		}
	}

	private float getMinPitch() {
		return this.bee.isBaby() ? 1.1F : 0.7F;
	}

	private float getMaxPitch() {
		return this.bee.isBaby() ? 1.5F : 1.1F;
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	protected abstract AbstractTickableSoundInstance getAlternativeSoundInstance();

	protected abstract boolean shouldSwitchSounds();
}
