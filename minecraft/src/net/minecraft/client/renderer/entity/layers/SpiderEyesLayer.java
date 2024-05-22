package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class SpiderEyesLayer<T extends Entity, M extends SpiderModel<T>> extends EyesLayer<T, M> {
	private static final RenderType SPIDER_EYES = RenderType.eyes(ResourceLocation.withDefaultNamespace("textures/entity/spider_eyes.png"));

	public SpiderEyesLayer(RenderLayerParent<T, M> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	public RenderType renderType() {
		return SPIDER_EYES;
	}
}
