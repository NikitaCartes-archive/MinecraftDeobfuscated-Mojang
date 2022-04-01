package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PolarBearModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.PolarBear;

@Environment(EnvType.CLIENT)
public class PolarBearRenderer extends MobRenderer<PolarBear, PolarBearModel<PolarBear>> {
	private static final ResourceLocation BEAR_LOCATION = new ResourceLocation("textures/entity/bear/polarbear.png");

	public PolarBearRenderer(EntityRendererProvider.Context context) {
		super(context, new PolarBearModel<>(context.bakeLayer(ModelLayers.POLAR_BEAR)), 0.9F);
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet()));
	}

	public ResourceLocation getTextureLocation(PolarBear polarBear) {
		return BEAR_LOCATION;
	}

	protected void scale(PolarBear polarBear, PoseStack poseStack, float f) {
		poseStack.scale(1.2F, 1.2F, 1.2F);
		super.scale(polarBear, poseStack, f);
	}
}
