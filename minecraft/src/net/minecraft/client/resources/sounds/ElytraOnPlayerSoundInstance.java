package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ElytraOnPlayerSoundInstance extends AbstractTickableSoundInstance {
	private final LocalPlayer player;
	private int time;

	public ElytraOnPlayerSoundInstance(LocalPlayer localPlayer) {
		super(SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS);
		this.player = localPlayer;
		this.looping = true;
		this.delay = 0;
		this.volume = 0.1F;
	}

	@Override
	public void tick() {
		this.time++;
		if (!this.player.removed && (this.time <= 20 || this.player.isFallFlying())) {
			this.x = (float)this.player.getX();
			this.y = (float)this.player.getY();
			this.z = (float)this.player.getZ();
			float f = (float)this.player.getDeltaMovement().lengthSqr();
			if ((double)f >= 1.0E-7) {
				this.volume = Mth.clamp(f / 4.0F, 0.0F, 1.0F);
			} else {
				this.volume = 0.0F;
			}

			if (this.time < 20) {
				this.volume = 0.0F;
			} else if (this.time < 40) {
				this.volume = (float)((double)this.volume * ((double)(this.time - 20) / 20.0));
			}

			float g = 0.8F;
			if (this.volume > 0.8F) {
				this.pitch = 1.0F + (this.volume - 0.8F);
			} else {
				this.pitch = 1.0F;
			}
		} else {
			this.stopped = true;
		}
	}
}
