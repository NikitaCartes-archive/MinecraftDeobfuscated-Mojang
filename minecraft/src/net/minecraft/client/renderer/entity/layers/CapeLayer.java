package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class CapeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public CapeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent) {
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
		float l
	) {
		if (!abstractClientPlayer.isInvisible() && abstractClientPlayer.isModelPartShown(PlayerModelPart.CAPE)) {
			PlayerSkin playerSkin = abstractClientPlayer.getSkin();
			if (playerSkin.capeTexture() != null) {
				ItemStack itemStack = abstractClientPlayer.getItemBySlot(EquipmentSlot.CHEST);
				if (!itemStack.is(Items.ELYTRA)) {
					poseStack.pushPose();
					poseStack.translate(0.0F, 0.0F, 0.125F);
					double d = Mth.lerp((double)h, abstractClientPlayer.xCloakO, abstractClientPlayer.xCloak)
						- Mth.lerp((double)h, abstractClientPlayer.xo, abstractClientPlayer.getX());
					double e = Mth.lerp((double)h, abstractClientPlayer.yCloakO, abstractClientPlayer.yCloak)
						- Mth.lerp((double)h, abstractClientPlayer.yo, abstractClientPlayer.getY());
					double m = Mth.lerp((double)h, abstractClientPlayer.zCloakO, abstractClientPlayer.zCloak)
						- Mth.lerp((double)h, abstractClientPlayer.zo, abstractClientPlayer.getZ());
					float n = Mth.rotLerp(h, abstractClientPlayer.yBodyRotO, abstractClientPlayer.yBodyRot);
					double o = (double)Mth.sin(n * (float) (Math.PI / 180.0));
					double p = (double)(-Mth.cos(n * (float) (Math.PI / 180.0)));
					float q = (float)e * 10.0F;
					q = Mth.clamp(q, -6.0F, 32.0F);
					float r = (float)(d * o + m * p) * 100.0F;
					r = Mth.clamp(r, 0.0F, 150.0F);
					float s = (float)(d * p - m * o) * 100.0F;
					s = Mth.clamp(s, -20.0F, 20.0F);
					if (r < 0.0F) {
						r = 0.0F;
					}

					float t = Mth.lerp(h, abstractClientPlayer.oBob, abstractClientPlayer.bob);
					q += Mth.sin(Mth.lerp(h, abstractClientPlayer.walkDistO, abstractClientPlayer.walkDist) * 6.0F) * 32.0F * t;
					if (abstractClientPlayer.isCrouching()) {
						q += 25.0F;
					}

					poseStack.mulPose(Axis.XP.rotationDegrees(6.0F + r / 2.0F + q));
					poseStack.mulPose(Axis.ZP.rotationDegrees(s / 2.0F));
					poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - s / 2.0F));
					VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(playerSkin.capeTexture()));
					this.getParentModel().renderCloak(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
					poseStack.popPose();
				}
			}
		}
	}
}
