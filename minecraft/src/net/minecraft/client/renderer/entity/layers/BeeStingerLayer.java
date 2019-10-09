package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
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
	private static final ResourceLocation BEE_STINGER_LOCATION = new ResourceLocation("textures/entity/bee/bee_stinger.png");

	public BeeStingerLayer(LivingEntityRenderer<T, M> livingEntityRenderer) {
		super(livingEntityRenderer);
	}

	@Override
	protected int numStuck(T livingEntity) {
		return livingEntity.getStingerCount();
	}

	@Override
	protected void renderStuckItem(PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity, float f, float g, float h, float i) {
		float j = Mth.sqrt(f * f + h * h);
		float k = (float)(Math.atan2((double)f, (double)h) * 180.0F / (float)Math.PI);
		float l = (float)(Math.atan2((double)g, (double)j) * 180.0F / (float)Math.PI);
		poseStack.translate(0.0, 0.0, 0.0);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(k - 90.0F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(l));
		float m = 0.0F;
		float n = 0.125F;
		float o = 0.0F;
		float p = 0.0625F;
		float q = 0.03125F;
		poseStack.mulPose(Vector3f.XP.rotationDegrees(45.0F));
		poseStack.scale(0.03125F, 0.03125F, 0.03125F);
		poseStack.translate(2.5, 0.0, 0.0);
		int r = entity.getLightColor();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(BEE_STINGER_LOCATION));

		for (int s = 0; s < 4; s++) {
			poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
			Matrix4f matrix4f = poseStack.getPose();
			vertex(vertexConsumer, matrix4f, -4.5F, -1, 0.0F, 0.0F, r);
			vertex(vertexConsumer, matrix4f, 4.5F, -1, 0.125F, 0.0F, r);
			vertex(vertexConsumer, matrix4f, 4.5F, 1, 0.125F, 0.0625F, r);
			vertex(vertexConsumer, matrix4f, -4.5F, 1, 0.0F, 0.0625F, r);
		}
	}

	private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, int i, float g, float h, int j) {
		vertexConsumer.vertex(matrix4f, f, (float)i, 0.0F)
			.color(255, 255, 255, 255)
			.uv(g, h)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(j)
			.normal(0.0F, 1.0F, 0.0F)
			.endVertex();
	}
}
