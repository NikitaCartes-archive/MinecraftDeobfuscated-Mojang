package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Guardian;

@Environment(EnvType.CLIENT)
public class GuardianAttackSoundInstance extends AbstractTickableSoundInstance {
	private final Guardian guardian;

	public GuardianAttackSoundInstance(Guardian guardian) {
		super(SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE);
		this.guardian = guardian;
		this.attenuation = SoundInstance.Attenuation.NONE;
		this.looping = true;
		this.delay = 0;
	}

	@Override
	public boolean canPlaySound() {
		return !this.guardian.isSilent();
	}

	@Override
	public void tick() {
		if (!this.guardian.isRemoved() && this.guardian.getTarget() == null) {
			this.x = (double)((float)this.guardian.getX());
			this.y = (double)((float)this.guardian.getY());
			this.z = (double)((float)this.guardian.getZ());
			float f = this.guardian.getAttackAnimationScale(0.0F);
			this.volume = 0.0F + 1.0F * f * f;
			this.pitch = 0.7F + 0.5F * f;
		} else {
			this.stop();
		}
	}
}
