package net.minecraft.client.resources.model;

import com.mojang.math.Transformation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ModelState {
	default Transformation getRotation() {
		return Transformation.identity();
	}

	default boolean isUvLocked() {
		return false;
	}
}
