package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface TickableSoundInstance extends SoundInstance {
	boolean isStopped();

	void tick();
}
