package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.StriderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Strider;

@Environment(EnvType.CLIENT)
public class StriderRenderer extends MobRenderer<Strider, StriderModel<Strider>> {
	private static final ResourceLocation STRIDER_LOCATION = new ResourceLocation("textures/entity/strider/strider.png");
	private static final ResourceLocation COLD_LOCATION = new ResourceLocation("textures/entity/strider/strider_cold.png");
	private static final float SHADOW_RADIUS = 0.5F;

	public StriderRenderer(EntityRendererProvider.Context context) {
		super(context, new StriderModel<>(context.bakeLayer(ModelLayers.STRIDER)), 0.5F);
		this.addLayer(
			new SaddleLayer<>(
				this, new StriderModel<>(context.bakeLayer(ModelLayers.STRIDER_SADDLE)), new ResourceLocation("textures/entity/strider/strider_saddle.png")
			)
		);
	}

	public ResourceLocation getTextureLocation(Strider strider) {
		return strider.isSuffocating() ? COLD_LOCATION : STRIDER_LOCATION;
	}

	protected float getShadowRadius(Strider strider) {
		float f = super.getShadowRadius(strider);
		return strider.isBaby() ? f * 0.5F : f;
	}

	protected boolean isShaking(Strider strider) {
		return super.isShaking(strider) || strider.isSuffocating();
	}
}
