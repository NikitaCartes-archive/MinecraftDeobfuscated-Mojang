package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public interface RenderLayerParent<T extends Entity, M extends EntityModel<T>> {
	M getModel();

	void bindTexture(ResourceLocation resourceLocation);

	void setLightColor(T entity);
}
