package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class EnderEyesLayer<T extends LivingEntity> extends EyesLayer<T, EndermanModel<T>> {
	private static final RenderType ENDERMAN_EYES = RenderType.eyes(new ResourceLocation("textures/entity/enderman/enderman_eyes.png"));

	public EnderEyesLayer(RenderLayerParent<T, EndermanModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	public RenderType renderType() {
		return ENDERMAN_EYES;
	}
}
