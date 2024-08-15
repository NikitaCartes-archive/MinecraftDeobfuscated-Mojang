package net.minecraft.client.model.geom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public record ModelLayerLocation(ResourceLocation model, String layer) {
	public String toString() {
		return this.model + "#" + this.layer;
	}
}
