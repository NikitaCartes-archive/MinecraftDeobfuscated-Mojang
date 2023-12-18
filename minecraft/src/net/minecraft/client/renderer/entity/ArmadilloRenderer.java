package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmadilloModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

@Environment(EnvType.CLIENT)
public class ArmadilloRenderer extends MobRenderer<Armadillo, ArmadilloModel> {
	private static final ResourceLocation ARMADILLO_LOCATION = new ResourceLocation("textures/entity/armadillo.png");

	public ArmadilloRenderer(EntityRendererProvider.Context context) {
		super(context, new ArmadilloModel(context.bakeLayer(ModelLayers.ARMADILLO)), 0.4F);
	}

	public ResourceLocation getTextureLocation(Armadillo armadillo) {
		return ARMADILLO_LOCATION;
	}

	protected void setupRotations(Armadillo armadillo, PoseStack poseStack, float f, float g, float h) {
		super.setupRotations(armadillo, poseStack, f, g, h);
	}
}
