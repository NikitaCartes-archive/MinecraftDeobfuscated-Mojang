package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.sniffer.Sniffer;

@Environment(EnvType.CLIENT)
public class SnifferSoundInstance extends AbstractTickableSoundInstance {
	private static final float VOLUME = 1.0F;
	private static final float PITCH = 1.0F;
	private final Sniffer sniffer;

	public SnifferSoundInstance(Sniffer sniffer) {
		super(SoundEvents.SNIFFER_DIGGING, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
		this.sniffer = sniffer;
		this.attenuation = SoundInstance.Attenuation.LINEAR;
		this.looping = false;
		this.delay = 0;
	}

	@Override
	public boolean canPlaySound() {
		return !this.sniffer.isSilent();
	}

	@Override
	public void tick() {
		if (!this.sniffer.isRemoved() && this.sniffer.getTarget() == null && this.sniffer.canPlayDiggingSound()) {
			this.x = (double)((float)this.sniffer.getX());
			this.y = (double)((float)this.sniffer.getY());
			this.z = (double)((float)this.sniffer.getZ());
			this.volume = 1.0F;
			this.pitch = 1.0F;
		} else {
			this.stop();
		}
	}
}
