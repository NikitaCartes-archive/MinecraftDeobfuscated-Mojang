package net.minecraft.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
	private final ResourceLocation location;

	public SoundEvent(ResourceLocation resourceLocation) {
		this.location = resourceLocation;
	}

	@Environment(EnvType.CLIENT)
	public ResourceLocation getLocation() {
		return this.location;
	}
}
