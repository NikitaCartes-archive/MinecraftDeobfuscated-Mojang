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
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(abstractClientPlayer.getSkinTextureLocation()));
			LivingEntityRenderer.setOverlayCoords(abstractClientPlayer, vertexConsumer, 0.0F);

			for (int n = 0; n < 2; n++) {
				float o = Mth.lerp(h, abstractClientPlayer.yRotO, abstractClientPlayer.yRot) - Mth.lerp(h, abstractClientPlayer.yBodyRotO, abstractClientPlayer.yBodyRot);
				float p = Mth.lerp(h, abstractClientPlayer.xRotO, abstractClientPlayer.xRot);
				poseStack.pushPose();
				poseStack.mulPose(Vector3f.YP.rotation(o, true));
				poseStack.mulPose(Vector3f.XP.rotation(p, true));
				poseStack.translate((double)(0.375F * (float)(n * 2 - 1)), 0.0, 0.0);
				poseStack.translate(0.0, -0.375, 0.0);
				poseStack.mulPose(Vector3f.XP.rotation(-p, true));
				poseStack.mulPose(Vector3f.YP.rotation(-o, true));
				float q = 1.3333334F;
				poseStack.scale(1.3333334F, 1.3333334F, 1.3333334F);
				this.getParentModel().renderEars(poseStack, vertexConsumer, 0.0625F, i);
				poseStack.popPose();
			}

			vertexConsumer.unsetDefaultOverlayCoords();
		}
	}
}
