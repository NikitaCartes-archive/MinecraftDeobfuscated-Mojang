package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.EvokerFangs;

@Environment(EnvType.CLIENT)
public class EvokerFangsRenderer extends EntityRenderer<EvokerFangs> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/evoker_fangs.png");
	private final EvokerFangsModel<EvokerFangs> model;

	public EvokerFangsRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new EvokerFangsModel<>(context.bakeLayer(ModelLayers.EVOKER_FANGS));
	}

	public void render(EvokerFangs evokerFangs, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		float h = evokerFangs.getAnimationProgress(g);
		if (h != 0.0F) {
			float j = 2.0F;
			if (h > 0.9F) {
				j *= (1.0F - h) / 0.1F;
			}

			poseStack.pushPose();
			poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F - evokerFangs.getYRot()));
			poseStack.scale(-j, -j, j);
			float k = 0.03125F;
			poseStack.translate(0.0, -0.626, 0.0);
			poseStack.scale(0.5F, 0.5F, 0.5F);
			this.model.setupAnim(evokerFangs, h, 0.0F, 0.0F, evokerFangs.getYRot(), evokerFangs.getXRot());
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(TEXTURE_LOCATION));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			poseStack.popPose();
			super.render(evokerFangs, f, g, poseStack, multiBufferSource, i);
		}
	}

	public ResourceLocation getTextureLocation(EvokerFangs evokerFangs) {
		return TEXTURE_LOCATION;
	}
}
