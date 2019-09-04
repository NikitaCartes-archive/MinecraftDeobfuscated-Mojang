package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
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

	public void render(AbstractClientPlayer abstractClientPlayer, float f, float g, float h, float i, float j, float k, float l) {
		if (abstractClientPlayer.isCapeLoaded()
			&& !abstractClientPlayer.isInvisible()
			&& abstractClientPlayer.isModelPartShown(PlayerModelPart.CAPE)
			&& abstractClientPlayer.getCloakTextureLocation() != null) {
			ItemStack itemStack = abstractClientPlayer.getItemBySlot(EquipmentSlot.CHEST);
			if (itemStack.getItem() != Items.ELYTRA) {
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.bindTexture(abstractClientPlayer.getCloakTextureLocation());
				RenderSystem.pushMatrix();
				RenderSystem.translatef(0.0F, 0.0F, 0.125F);
				double d = Mth.lerp((double)h, abstractClientPlayer.xCloakO, abstractClientPlayer.xCloak)
					- Mth.lerp((double)h, abstractClientPlayer.xo, abstractClientPlayer.x);
				double e = Mth.lerp((double)h, abstractClientPlayer.yCloakO, abstractClientPlayer.yCloak)
					- Mth.lerp((double)h, abstractClientPlayer.yo, abstractClientPlayer.y);
				double m = Mth.lerp((double)h, abstractClientPlayer.zCloakO, abstractClientPlayer.zCloak)
					- Mth.lerp((double)h, abstractClientPlayer.zo, abstractClientPlayer.z);
				float n = abstractClientPlayer.yBodyRotO + (abstractClientPlayer.yBodyRot - abstractClientPlayer.yBodyRotO);
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

				RenderSystem.rotatef(6.0F + r / 2.0F + q, 1.0F, 0.0F, 0.0F);
				RenderSystem.rotatef(s / 2.0F, 0.0F, 0.0F, 1.0F);
				RenderSystem.rotatef(-s / 2.0F, 0.0F, 1.0F, 0.0F);
				RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
				this.getParentModel().renderCloak(0.0625F);
				RenderSystem.popMatrix();
			}
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
