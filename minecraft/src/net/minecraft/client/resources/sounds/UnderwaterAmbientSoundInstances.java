package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

@Environment(EnvType.CLIENT)
public class UnderwaterAmbientSoundInstances {
	@Environment(EnvType.CLIENT)
	public static class SubSound extends AbstractTickableSoundInstance {
		private final LocalPlayer player;

		protected SubSound(LocalPlayer localPlayer, SoundEvent soundEvent) {
			super(soundEvent, SoundSource.AMBIENT);
			this.player = localPlayer;
			this.looping = false;
			this.delay = 0;
			this.volume = 1.0F;
			this.relative = true;
		}

		@Override
		public void tick() {
			if (this.player.isRemoved() || !this.player.isUnderWater()) {
				this.stop();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class UnderwaterAmbientSoundInstance extends AbstractTickableSoundInstance {
		public static final int FADE_DURATION = 40;
		private final LocalPlayer player;
		private int fade;

		public UnderwaterAmbientSoundInstance(LocalPlayer localPlayer) {
			super(SoundEvents.AMBIENT_UNDERWATER_LOOP, SoundSource.AMBIENT);
			this.player = localPlayer;
			this.looping = true;
			this.delay = 0;
			this.volume = 1.0F;
			this.relative = true;
		}

		@Override
		public void tick() {
			if (!this.player.isRemoved() && this.fade >= 0) {
				if (this.player.isUnderWater()) {
					this.fade++;
				} else {
					this.fade -= 2;
				}

				this.fade = Math.min(this.fade, 40);
				this.volume = Math.max(0.0F, Math.min((float)this.fade / 40.0F, 1.0F));
			} else {
				this.stop();
			}
		}
	}
}
