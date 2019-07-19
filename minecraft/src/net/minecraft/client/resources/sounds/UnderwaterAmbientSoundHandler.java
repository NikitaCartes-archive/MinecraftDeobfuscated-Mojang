package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;

@Environment(EnvType.CLIENT)
public class UnderwaterAmbientSoundHandler implements AmbientSoundHandler {
	private final LocalPlayer player;
	private final SoundManager soundManager;
	private int tick_delay = 0;

	public UnderwaterAmbientSoundHandler(LocalPlayer localPlayer, SoundManager soundManager) {
		this.player = localPlayer;
		this.soundManager = soundManager;
	}

	@Override
	public void tick() {
		this.tick_delay--;
		if (this.tick_delay <= 0 && this.player.isUnderWater()) {
			float f = this.player.level.random.nextFloat();
			if (f < 1.0E-4F) {
				this.tick_delay = 0;
				this.soundManager.play(new UnderwaterAmbientSoundInstances.SubSound(this.player, SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE));
			} else if (f < 0.001F) {
				this.tick_delay = 0;
				this.soundManager.play(new UnderwaterAmbientSoundInstances.SubSound(this.player, SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_RARE));
			} else if (f < 0.01F) {
				this.tick_delay = 0;
				this.soundManager.play(new UnderwaterAmbientSoundInstances.SubSound(this.player, SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS));
			}
		}
	}
}
