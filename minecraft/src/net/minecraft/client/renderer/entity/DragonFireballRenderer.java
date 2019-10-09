package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

	public void render(DragonFireball dragonFireball, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		poseStack.pushPose();
		poseStack.scale(2.0F, 2.0F, 2.0F);
		float i = 1.0F;
		float j = 0.5F;
		float k = 0.25F;
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - this.entityRenderDispatcher.playerRotY));
		float l = (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX;
		poseStack.mulPose(Vector3f.XP.rotationDegrees(l));
		Matrix4f matrix4f = poseStack.getPose();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE_LOCATION));
		int m = dragonFireball.getLightColor();
		vertexConsumer.vertex(matrix4f, -0.5F, -0.25F, 0.0F)
			.color(255, 255, 255, 255)
			.uv(0.0F, 1.0F)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(m)
			.normal(0.0F, 1.0F, 0.0F)
			.endVertex();
		vertexConsumer.vertex(matrix4f, 0.5F, -0.25F, 0.0F)
			.color(255, 255, 255, 255)
			.uv(1.0F, 1.0F)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(m)
			.normal(0.0F, 1.0F, 0.0F)
			.endVertex();
		vertexConsumer.vertex(matrix4f, 0.5F, 0.75F, 0.0F)
			.color(255, 255, 255, 255)
			.uv(1.0F, 0.0F)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(m)
			.normal(0.0F, 1.0F, 0.0F)
			.endVertex();
		vertexConsumer.vertex(matrix4f, -0.5F, 0.75F, 0.0F)
			.color(255, 255, 255, 255)
			.uv(0.0F, 0.0F)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(m)
			.normal(0.0F, 1.0F, 0.0F)
			.endVertex();
		poseStack.popPose();
		super.render(dragonFireball, d, e, f, g, h, poseStack, multiBufferSource);
	}

	public ResourceLocation getTextureLocation(DragonFireball dragonFireball) {
		return TEXTURE_LOCATION;
	}
}
