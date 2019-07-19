package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class RenderLayer<T extends Entity, M extends EntityModel<T>> {
	private final RenderLayerParent<T, M> renderer;

	public RenderLayer(RenderLayerParent<T, M> renderLayerParent) {
		this.renderer = renderLayerParent;
	}

	public M getParentModel() {
		return this.renderer.getModel();
	}

	public void bindTexture(ResourceLocation resourceLocation) {
		this.renderer.bindTexture(resourceLocation);
	}

	public void setLightColor(T entity) {
		this.renderer.setLightColor(entity);
	}

	public abstract void render(T entity, float f, float g, float h, float i, float j, float k, float l);

	public abstract boolean colorsOnDamage();
}
