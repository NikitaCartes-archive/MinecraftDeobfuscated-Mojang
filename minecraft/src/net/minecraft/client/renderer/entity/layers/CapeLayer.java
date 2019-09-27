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
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
		float l,
		float m
	) {
		if (abstractClientPlayer.isCapeLoaded()
			&& !abstractClientPlayer.isInvisible()
			&& abstractClientPlayer.isModelPartShown(PlayerModelPart.CAPE)
			&& abstractClientPlayer.getCloakTextureLocation() != null) {
			ItemStack itemStack = abstractClientPlayer.getItemBySlot(EquipmentSlot.CHEST);
			if (itemStack.getItem() != Items.ELYTRA) {
				poseStack.pushPose();
				poseStack.translate(0.0, 0.0, 0.125);
				double d = Mth.lerp((double)h, abstractClientPlayer.xCloakO, abstractClientPlayer.xCloak)
					- Mth.lerp((double)h, abstractClientPlayer.xo, abstractClientPlayer.x);
				double e = Mth.lerp((double)h, abstractClientPlayer.yCloakO, abstractClientPlayer.yCloak)
					- Mth.lerp((double)h, abstractClientPlayer.yo, abstractClientPlayer.y);
				double n = Mth.lerp((double)h, abstractClientPlayer.zCloakO, abstractClientPlayer.zCloak)
					- Mth.lerp((double)h, abstractClientPlayer.zo, abstractClientPlayer.z);
				float o = abstractClientPlayer.yBodyRotO + (abstractClientPlayer.yBodyRot - abstractClientPlayer.yBodyRotO);
				double p = (double)Mth.sin(o * (float) (Math.PI / 180.0));
				double q = (double)(-Mth.cos(o * (float) (Math.PI / 180.0)));
				float r = (float)e * 10.0F;
				r = Mth.clamp(r, -6.0F, 32.0F);
				float s = (float)(d * p + n * q) * 100.0F;
				s = Mth.clamp(s, 0.0F, 150.0F);
				float t = (float)(d * q - n * p) * 100.0F;
				t = Mth.clamp(t, -20.0F, 20.0F);
				if (s < 0.0F) {
					s = 0.0F;
				}

				float u = Mth.lerp(h, abstractClientPlayer.oBob, abstractClientPlayer.bob);
				r += Mth.sin(Mth.lerp(h, abstractClientPlayer.walkDistO, abstractClientPlayer.walkDist) * 6.0F) * 32.0F * u;
				if (abstractClientPlayer.isCrouching()) {
					r += 25.0F;
				}

				poseStack.mulPose(Vector3f.XP.rotation(6.0F + s / 2.0F + r, true));
				poseStack.mulPose(Vector3f.ZP.rotation(t / 2.0F, true));
				poseStack.mulPose(Vector3f.YP.rotation(180.0F - t / 2.0F, true));
				VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(abstractClientPlayer.getCloakTextureLocation()));
				OverlayTexture.setDefault(vertexConsumer);
				this.getParentModel().renderCloak(poseStack, vertexConsumer, 0.0625F, i);
				vertexConsumer.unsetDefaultOverlayCoords();
				poseStack.popPose();
			}
		}
	}
}
