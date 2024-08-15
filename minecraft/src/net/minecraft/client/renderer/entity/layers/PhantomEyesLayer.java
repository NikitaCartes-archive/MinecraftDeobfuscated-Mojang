package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class PhantomEyesLayer extends EyesLayer<PhantomRenderState, PhantomModel> {
	private static final RenderType PHANTOM_EYES = RenderType.eyes(ResourceLocation.withDefaultNamespace("textures/entity/phantom_eyes.png"));

	public PhantomEyesLayer(RenderLayerParent<PhantomRenderState, PhantomModel> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	public RenderType renderType() {
		return PHANTOM_EYES;
	}
}
