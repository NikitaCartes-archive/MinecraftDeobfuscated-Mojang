package net.minecraft.client.model.geom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class ModelLayerLocation {
	private final ResourceLocation model;
	private final String layer;

	public ModelLayerLocation(ResourceLocation resourceLocation, String string) {
		this.model = resourceLocation;
		this.layer = string;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof ModelLayerLocation)) {
			return false;
		} else {
			ModelLayerLocation modelLayerLocation = (ModelLayerLocation)object;
			return this.model.equals(modelLayerLocation.model) && this.layer.equals(modelLayerLocation.layer);
		}
	}

	public int hashCode() {
		int i = this.model.hashCode();
		return 31 * i + this.layer.hashCode();
	}

	public String toString() {
		return this.model + "#" + this.layer;
	}
}
