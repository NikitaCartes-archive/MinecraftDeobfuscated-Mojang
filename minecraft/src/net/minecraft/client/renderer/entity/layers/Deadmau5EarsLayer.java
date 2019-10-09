package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class Deadmau5EarsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public Deadmau5EarsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		AbstractClientPlayer abstractClientPlayer,
		float f,
		float g,
		float h,
		float j,
		float k,
		float l,
		float m
	) {
		if ("deadmau5".equals(abstractClientPlayer.getName().getString()) && abstractClientPlayer.isSkinLoaded() && !abstractClientPlayer.isInvisible()) {
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(abstractClientPlayer.getSkinTextureLocation()));
			int n = LivingEntityRenderer.getOverlayCoords(abstractClientPlayer, 0.0F);

			for (int o = 0; o < 2; o++) {
				float p = Mth.lerp(h, abstractClientPlayer.yRotO, abstractClientPlayer.yRot) - Mth.lerp(h, abstractClientPlayer.yBodyRotO, abstractClientPlayer.yBodyRot);
				float q = Mth.lerp(h, abstractClientPlayer.xRotO, abstractClientPlayer.xRot);
				poseStack.pushPose();
				poseStack.mulPose(Vector3f.YP.rotationDegrees(p));
				poseStack.mulPose(Vector3f.XP.rotationDegrees(q));
				poseStack.translate((double)(0.375F * (float)(o * 2 - 1)), 0.0, 0.0);
				poseStack.translate(0.0, -0.375, 0.0);
				poseStack.mulPose(Vector3f.XP.rotationDegrees(-q));
				poseStack.mulPose(Vector3f.YP.rotationDegrees(-p));
				float r = 1.3333334F;
				poseStack.scale(1.3333334F, 1.3333334F, 1.3333334F);
				this.getParentModel().renderEars(poseStack, vertexConsumer, 0.0625F, i, n);
				poseStack.popPose();
			}
		}
	}
}
