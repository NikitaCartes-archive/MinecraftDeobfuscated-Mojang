package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;

@Environment(EnvType.CLIENT)
public interface HeadedModel {
	ModelPart getHead();

	default void translateToHead(float f) {
		this.getHead().translateTo(f);
	}
}
