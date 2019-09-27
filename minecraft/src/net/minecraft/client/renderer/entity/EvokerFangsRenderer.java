package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.EvokerFangs;

@Environment(EnvType.CLIENT)
public class EvokerFangsRenderer extends EntityRenderer<EvokerFangs> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/evoker_fangs.png");
	private final EvokerFangsModel<EvokerFangs> model = new EvokerFangsModel<>();

	public EvokerFangsRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(EvokerFangs evokerFangs, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		float i = evokerFangs.getAnimationProgress(h);
		if (i != 0.0F) {
			float j = 2.0F;
			if (i > 0.9F) {
				j = (float)((double)j * ((1.0 - (double)i) / 0.1F));
			}

			poseStack.pushPose();
			poseStack.mulPose(Vector3f.YP.rotation(90.0F - evokerFangs.yRot, true));
			poseStack.scale(-j, -j, j);
			float k = 0.03125F;
			poseStack.translate(0.0, -0.626F, 0.0);
			int l = evokerFangs.getLightColor();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(TEXTURE_LOCATION));
			OverlayTexture.setDefault(vertexConsumer);
			this.model.setupAnim(evokerFangs, i, 0.0F, 0.0F, evokerFangs.yRot, evokerFangs.xRot, 0.03125F);
			this.model.renderToBuffer(poseStack, vertexConsumer, l);
			vertexConsumer.unsetDefaultOverlayCoords();
			poseStack.popPose();
			super.render(evokerFangs, d, e, f, g, h, poseStack, multiBufferSource);
		}
	}

	public ResourceLocation getTextureLocation(EvokerFangs evokerFangs) {
		return TEXTURE_LOCATION;
	}
}
