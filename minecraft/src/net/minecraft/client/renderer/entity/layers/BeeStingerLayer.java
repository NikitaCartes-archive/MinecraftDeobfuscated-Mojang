package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class BeeStingerLayer<T extends LivingEntity, M extends PlayerModel<T>> extends StuckInBodyLayer<T, M> {
	private static final ResourceLocation BEE_STINGER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_stinger.png");

	public BeeStingerLayer(LivingEntityRenderer<T, M> livingEntityRenderer) {
		super(livingEntityRenderer);
	}

	@Override
	protected int numStuck(T livingEntity) {
		return livingEntity.getStingerCount();
	}

	@Override
	protected void renderStuckItem(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Entity entity, float f, float g, float h, float j) {
		float k = Mth.sqrt(f * f + h * h);
		float l = (float)(Math.atan2((double)f, (double)h) * 180.0F / (float)Math.PI);
		float m = (float)(Math.atan2((double)g, (double)k) * 180.0F / (float)Math.PI);
		poseStack.translate(0.0F, 0.0F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(l - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(m));
		float n = 0.0F;
		float o = 0.125F;
		float p = 0.0F;
		float q = 0.0625F;
		float r = 0.03125F;
		poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
		poseStack.scale(0.03125F, 0.03125F, 0.03125F);
		poseStack.translate(2.5F, 0.0F, 0.0F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(BEE_STINGER_LOCATION));

		for (int s = 0; s < 4; s++) {
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			PoseStack.Pose pose = poseStack.last();
			vertex(vertexConsumer, pose, -4.5F, -1, 0.0F, 0.0F, i);
			vertex(vertexConsumer, pose, 4.5F, -1, 0.125F, 0.0F, i);
			vertex(vertexConsumer, pose, 4.5F, 1, 0.125F, 0.0625F, i);
			vertex(vertexConsumer, pose, -4.5F, 1, 0.0F, 0.0625F, i);
		}
	}

	private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f, int i, float g, float h, int j) {
		vertexConsumer.addVertex(pose, f, (float)i, 0.0F)
			.setColor(-1)
			.setUv(g, h)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(j)
			.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}
}
