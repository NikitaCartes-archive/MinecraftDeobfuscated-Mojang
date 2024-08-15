package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.ExperienceOrbRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;

@Environment(EnvType.CLIENT)
public class ExperienceOrbRenderer extends EntityRenderer<ExperienceOrb, ExperienceOrbRenderState> {
	private static final ResourceLocation EXPERIENCE_ORB_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/experience_orb.png");
	private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

	public ExperienceOrbRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.15F;
		this.shadowStrength = 0.75F;
	}

	protected int getBlockLightLevel(ExperienceOrb experienceOrb, BlockPos blockPos) {
		return Mth.clamp(super.getBlockLightLevel(experienceOrb, blockPos) + 7, 0, 15);
	}

	public void render(ExperienceOrbRenderState experienceOrbRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		int j = experienceOrbRenderState.icon;
		float f = (float)(j % 4 * 16 + 0) / 64.0F;
		float g = (float)(j % 4 * 16 + 16) / 64.0F;
		float h = (float)(j / 4 * 16 + 0) / 64.0F;
		float k = (float)(j / 4 * 16 + 16) / 64.0F;
		float l = 1.0F;
		float m = 0.5F;
		float n = 0.25F;
		float o = 255.0F;
		float p = experienceOrbRenderState.ageInTicks / 2.0F;
		int q = (int)((Mth.sin(p + 0.0F) + 1.0F) * 0.5F * 255.0F);
		int r = 255;
		int s = (int)((Mth.sin(p + (float) (Math.PI * 4.0 / 3.0)) + 1.0F) * 0.1F * 255.0F);
		poseStack.translate(0.0F, 0.1F, 0.0F);
		poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
		float t = 0.3F;
		poseStack.scale(0.3F, 0.3F, 0.3F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
		PoseStack.Pose pose = poseStack.last();
		vertex(vertexConsumer, pose, -0.5F, -0.25F, q, 255, s, f, k, i);
		vertex(vertexConsumer, pose, 0.5F, -0.25F, q, 255, s, g, k, i);
		vertex(vertexConsumer, pose, 0.5F, 0.75F, q, 255, s, g, h, i);
		vertex(vertexConsumer, pose, -0.5F, 0.75F, q, 255, s, f, h, i);
		poseStack.popPose();
		super.render(experienceOrbRenderState, poseStack, multiBufferSource, i);
	}

	private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f, float g, int i, int j, int k, float h, float l, int m) {
		vertexConsumer.addVertex(pose, f, g, 0.0F)
			.setColor(i, j, k, 128)
			.setUv(h, l)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(m)
			.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	public ResourceLocation getTextureLocation(ExperienceOrbRenderState experienceOrbRenderState) {
		return EXPERIENCE_ORB_LOCATION;
	}

	public ExperienceOrbRenderState createRenderState() {
		return new ExperienceOrbRenderState();
	}

	public void extractRenderState(ExperienceOrb experienceOrb, ExperienceOrbRenderState experienceOrbRenderState, float f) {
		super.extractRenderState(experienceOrb, experienceOrbRenderState, f);
		experienceOrbRenderState.icon = experienceOrb.getIcon();
	}
}
