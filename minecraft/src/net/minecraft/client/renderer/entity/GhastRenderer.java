package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ghast;

@Environment(EnvType.CLIENT)
public class GhastRenderer extends MobRenderer<Ghast, GhastModel<Ghast>> {
	private static final ResourceLocation GHAST_LOCATION = new ResourceLocation("textures/entity/ghast/ghast.png");
	private static final ResourceLocation GHAST_SHOOTING_LOCATION = new ResourceLocation("textures/entity/ghast/ghast_shooting.png");

	public GhastRenderer(EntityRendererProvider.Context context) {
		super(context, new GhastModel<>(context.getLayer(ModelLayers.GHAST)), 1.5F);
	}

	public ResourceLocation getTextureLocation(Ghast ghast) {
		return ghast.isCharging() ? GHAST_SHOOTING_LOCATION : GHAST_LOCATION;
	}

	protected void scale(Ghast ghast, PoseStack poseStack, float f) {
		float g = 1.0F;
		float h = 4.5F;
		float i = 4.5F;
		poseStack.scale(4.5F, 4.5F, 4.5F);
	}
}
