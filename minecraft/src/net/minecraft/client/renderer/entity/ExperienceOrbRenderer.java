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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;

@Environment(EnvType.CLIENT)
public class ExperienceOrbRenderer extends EntityRenderer<ExperienceOrb> {
	private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");
	private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

	public ExperienceOrbRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.15F;
		this.shadowStrength = 0.75F;
	}

	protected int getBlockLightLevel(ExperienceOrb experienceOrb, BlockPos blockPos) {
		return Mth.clamp(super.getBlockLightLevel(experienceOrb, blockPos) + 7, 0, 15);
	}

	public void render(ExperienceOrb experienceOrb, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		int j = experienceOrb.getIcon();
		float h = (float)(j % 4 * 16 + 0) / 64.0F;
		float k = (float)(j % 4 * 16 + 16) / 64.0F;
		float l = (float)(j / 4 * 16 + 0) / 64.0F;
		float m = (float)(j / 4 * 16 + 16) / 64.0F;
		float n = 1.0F;
		float o = 0.5F;
		float p = 0.25F;
		float q = 255.0F;
		float r = ((float)experienceOrb.tickCount + g) / 2.0F;
		int s = (int)((Mth.sin(r + 0.0F) + 1.0F) * 0.5F * 255.0F);
		int t = 255;
		int u = (int)((Mth.sin(r + (float) (Math.PI * 4.0 / 3.0)) + 1.0F) * 0.1F * 255.0F);
		poseStack.translate(0.0, 0.1F, 0.0);
		poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
		float v = 0.3F;
		poseStack.scale(0.3F, 0.3F, 0.3F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
		PoseStack.Pose pose = poseStack.last();
		Matrix4f matrix4f = pose.pose();
		Matrix3f matrix3f = pose.normal();
		vertex(vertexConsumer, matrix4f, matrix3f, -0.5F, -0.25F, s, 255, u, h, m, i);
		vertex(vertexConsumer, matrix4f, matrix3f, 0.5F, -0.25F, s, 255, u, k, m, i);
		vertex(vertexConsumer, matrix4f, matrix3f, 0.5F, 0.75F, s, 255, u, k, l, i);
		vertex(vertexConsumer, matrix4f, matrix3f, -0.5F, 0.75F, s, 255, u, h, l, i);
		poseStack.popPose();
		super.render(experienceOrb, f, g, poseStack, multiBufferSource, i);
	}

	private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, float f, float g, int i, int j, int k, float h, float l, int m) {
		vertexConsumer.vertex(matrix4f, f, g, 0.0F)
			.color(i, j, k, 128)
			.uv(h, l)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(m)
			.normal(matrix3f, 0.0F, 1.0F, 0.0F)
			.endVertex();
	}

	public ResourceLocation getTextureLocation(ExperienceOrb experienceOrb) {
		return EXPERIENCE_ORB_LOCATION;
	}
}
