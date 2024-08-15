package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public interface RenderLayerParent<S extends EntityRenderState, M extends EntityModel<? super S>> {
	M getModel();

	ResourceLocation getTextureLocation(S entityRenderState);
}
