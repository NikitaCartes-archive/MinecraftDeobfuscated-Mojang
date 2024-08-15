package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.state.SquidRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;

@Environment(EnvType.CLIENT)
public class SquidRenderer<T extends Squid> extends AgeableMobRenderer<T, SquidRenderState, SquidModel> {
	private static final ResourceLocation SQUID_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/squid/squid.png");

	public SquidRenderer(EntityRendererProvider.Context context, SquidModel squidModel, SquidModel squidModel2) {
		super(context, squidModel, squidModel2, 0.7F);
	}

	public ResourceLocation getTextureLocation(SquidRenderState squidRenderState) {
		return SQUID_LOCATION;
	}

	public SquidRenderState createRenderState() {
		return new SquidRenderState();
	}

	public void extractRenderState(T squid, SquidRenderState squidRenderState, float f) {
		super.extractRenderState(squid, squidRenderState, f);
		squidRenderState.tentacleAngle = Mth.lerp(f, squid.oldTentacleAngle, squid.tentacleAngle);
		squidRenderState.xBodyRot = Mth.lerp(f, squid.xBodyRotO, squid.xBodyRot);
		squidRenderState.zBodyRot = Mth.lerp(f, squid.zBodyRotO, squid.zBodyRot);
	}

	protected void setupRotations(SquidRenderState squidRenderState, PoseStack poseStack, float f, float g) {
		poseStack.translate(0.0F, squidRenderState.isBaby ? 0.25F : 0.5F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
		poseStack.mulPose(Axis.XP.rotationDegrees(squidRenderState.xBodyRot));
		poseStack.mulPose(Axis.YP.rotationDegrees(squidRenderState.zBodyRot));
		poseStack.translate(0.0F, squidRenderState.isBaby ? -0.6F : -1.2F, 0.0F);
	}
}
