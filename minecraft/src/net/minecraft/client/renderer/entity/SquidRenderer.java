package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SquidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;

@Environment(EnvType.CLIENT)
public class SquidRenderer<T extends Squid> extends MobRenderer<T, SquidModel<T>> {
	private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid/squid.png");

	public SquidRenderer(EntityRendererProvider.Context context, SquidModel<T> squidModel) {
		super(context, squidModel, 0.7F);
	}

	public ResourceLocation getTextureLocation(T squid) {
		return SQUID_LOCATION;
	}

	protected void setupRotations(T squid, PoseStack poseStack, float f, float g, float h) {
		float i = Mth.lerp(h, squid.xBodyRotO, squid.xBodyRot);
		float j = Mth.lerp(h, squid.zBodyRotO, squid.zBodyRot);
		poseStack.translate(0.0F, 0.5F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - g));
		poseStack.mulPose(Axis.XP.rotationDegrees(i));
		poseStack.mulPose(Axis.YP.rotationDegrees(j));
		poseStack.translate(0.0F, -1.2F, 0.0F);
	}

	protected float getBob(T squid, float f) {
		return Mth.lerp(f, squid.oldTentacleAngle, squid.tentacleAngle);
	}
}
