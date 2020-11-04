package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.IronGolemCrackinessLayer;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;

@Environment(EnvType.CLIENT)
public class IronGolemRenderer extends MobRenderer<IronGolem, IronGolemModel<IronGolem>> {
	private static final ResourceLocation GOLEM_LOCATION = new ResourceLocation("textures/entity/iron_golem/iron_golem.png");

	public IronGolemRenderer(EntityRendererProvider.Context context) {
		super(context, new IronGolemModel<>(context.getLayer(ModelLayers.IRON_GOLEM)), 0.7F);
		this.addLayer(new IronGolemCrackinessLayer(this));
		this.addLayer(new IronGolemFlowerLayer(this));
	}

	public ResourceLocation getTextureLocation(IronGolem ironGolem) {
		return GOLEM_LOCATION;
	}

	protected void setupRotations(IronGolem ironGolem, PoseStack poseStack, float f, float g, float h) {
		super.setupRotations(ironGolem, poseStack, f, g, h);
		if (!((double)ironGolem.animationSpeed < 0.01)) {
			float i = 13.0F;
			float j = ironGolem.animationPosition - ironGolem.animationSpeed * (1.0F - h) + 6.0F;
			float k = (Math.abs(j % 13.0F - 6.5F) - 3.25F) / 3.25F;
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(6.5F * k));
		}
	}
}
