package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.VexModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vex;

@Environment(EnvType.CLIENT)
public class VexRenderer extends HumanoidMobRenderer<Vex, VexModel> {
	private static final ResourceLocation VEX_LOCATION = new ResourceLocation("textures/entity/illager/vex.png");
	private static final ResourceLocation VEX_CHARGING_LOCATION = new ResourceLocation("textures/entity/illager/vex_charging.png");

	public VexRenderer(EntityRendererProvider.Context context) {
		super(context, new VexModel(context.bakeLayer(ModelLayers.VEX)), 0.3F);
	}

	protected int getBlockLightLevel(Vex vex, BlockPos blockPos) {
		return 15;
	}

	public ResourceLocation getTextureLocation(Vex vex) {
		return vex.isCharging() ? VEX_CHARGING_LOCATION : VEX_LOCATION;
	}

	protected void scale(Vex vex, PoseStack poseStack, float f) {
		poseStack.scale(0.4F, 0.4F, 0.4F);
	}
}
