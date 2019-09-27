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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;

@Environment(EnvType.CLIENT)
public class ExperienceOrbRenderer extends EntityRenderer<ExperienceOrb> {
	private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");

	public ExperienceOrbRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.15F;
		this.shadowStrength = 0.75F;
	}

	public void render(ExperienceOrb experienceOrb, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		poseStack.pushPose();
		int i = experienceOrb.getIcon();
		float j = (float)(i % 4 * 16 + 0) / 64.0F;
		float k = (float)(i % 4 * 16 + 16) / 64.0F;
		float l = (float)(i / 4 * 16 + 0) / 64.0F;
		float m = (float)(i / 4 * 16 + 16) / 64.0F;
		float n = 1.0F;
		float o = 0.5F;
		float p = 0.25F;
		float q = 255.0F;
		float r = ((float)experienceOrb.tickCount + h) / 2.0F;
		int s = (int)((Mth.sin(r + 0.0F) + 1.0F) * 0.5F * 255.0F);
		int t = 255;
		int u = (int)((Mth.sin(r + (float) (Math.PI * 4.0 / 3.0)) + 1.0F) * 0.1F * 255.0F);
		poseStack.translate(0.0, 0.1F, 0.0);
		poseStack.mulPose(Vector3f.YP.rotation(180.0F - this.entityRenderDispatcher.playerRotY, true));
		poseStack.mulPose(
			Vector3f.XP.rotation((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX, true)
		);
		float v = 0.3F;
		poseStack.scale(0.3F, 0.3F, 0.3F);
		int w = experienceOrb.getLightColor();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(EXPERIENCE_ORB_LOCATION));
		OverlayTexture.setDefault(vertexConsumer);
		Matrix4f matrix4f = poseStack.getPose();
		vertex(vertexConsumer, matrix4f, -0.5F, -0.25F, s, 255, u, j, m, w);
		vertex(vertexConsumer, matrix4f, 0.5F, -0.25F, s, 255, u, k, m, w);
		vertex(vertexConsumer, matrix4f, 0.5F, 0.75F, s, 255, u, k, l, w);
		vertex(vertexConsumer, matrix4f, -0.5F, 0.75F, s, 255, u, j, l, w);
		vertexConsumer.unsetDefaultOverlayCoords();
		poseStack.popPose();
		super.render(experienceOrb, d, e, f, g, h, poseStack, multiBufferSource);
	}

	private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, int i, int j, int k, float h, float l, int m) {
		vertexConsumer.vertex(matrix4f, f, g, 0.0F).color(i, j, k, 128).uv(h, l).uv2(m).normal(0.0F, 1.0F, 0.0F).endVertex();
	}

	public ResourceLocation getTextureLocation(ExperienceOrb experienceOrb) {
		return EXPERIENCE_ORB_LOCATION;
	}
}
