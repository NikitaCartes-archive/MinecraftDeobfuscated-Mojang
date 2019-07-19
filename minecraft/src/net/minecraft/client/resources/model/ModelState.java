package net.minecraft.client.resources.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ModelState {
	default BlockModelRotation getRotation() {
		return BlockModelRotation.X0_Y0;
	}

	default boolean isUvLocked() {
		return false;
	}
}
