package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.DragonFireball;

@Environment(EnvType.CLIENT)
public class DragonFireballRenderer extends EntityRenderer<DragonFireball> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");

	public DragonFireballRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(DragonFireball dragonFireball, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.scale(2.0F, 2.0F, 2.0F);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - this.entityRenderDispatcher.playerRotY));
		float h = (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX;
		poseStack.mulPose(Vector3f.XP.rotationDegrees(h));
		PoseStack.Pose pose = poseStack.last();
		Matrix4f matrix4f = pose.pose();
		Matrix3f matrix3f = pose.normal();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE_LOCATION));
		vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0F, 0, 0, 1);
		vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0F, 0, 1, 1);
		vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0F, 1, 1, 0);
		vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0F, 1, 0, 0);
		poseStack.popPose();
		super.render(dragonFireball, f, g, poseStack, multiBufferSource, i);
	}

	private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, int i, float f, int j, int k, int l) {
		vertexConsumer.vertex(matrix4f, f - 0.5F, (float)j - 0.25F, 0.0F)
			.color(255, 255, 255, 255)
			.uv((float)k, (float)l)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(i)
			.normal(matrix3f, 0.0F, 1.0F, 0.0F)
			.endVertex();
	}

	public ResourceLocation getTextureLocation(DragonFireball dragonFireball) {
		return TEXTURE_LOCATION;
	}
}
