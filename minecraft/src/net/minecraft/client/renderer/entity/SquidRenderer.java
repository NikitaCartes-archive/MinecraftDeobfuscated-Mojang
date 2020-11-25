package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;

@Environment(EnvType.CLIENT)
public class SquidRenderer extends MobRenderer<Squid, SquidModel<Squid>> {
	private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid.png");

	public SquidRenderer(EntityRendererProvider.Context context) {
		super(context, new SquidModel<>(context.bakeLayer(ModelLayers.SQUID)), 0.7F);
	}

	public ResourceLocation getTextureLocation(Squid squid) {
		return SQUID_LOCATION;
	}

	protected void setupRotations(Squid squid, PoseStack poseStack, float f, float g, float h) {
		float i = Mth.lerp(h, squid.xBodyRotO, squid.xBodyRot);
		float j = Mth.lerp(h, squid.zBodyRotO, squid.zBodyRot);
		poseStack.translate(0.0, 0.5, 0.0);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - g));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(i));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(j));
		poseStack.translate(0.0, -1.2F, 0.0);
	}

	protected float getBob(Squid squid, float f) {
		return Mth.lerp(f, squid.oldTentacleAngle, squid.tentacleAngle);
	}
}
