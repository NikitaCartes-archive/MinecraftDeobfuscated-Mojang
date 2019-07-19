package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class Deadmau5EarsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public Deadmau5EarsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(AbstractClientPlayer abstractClientPlayer, float f, float g, float h, float i, float j, float k, float l) {
		if ("deadmau5".equals(abstractClientPlayer.getName().getString()) && abstractClientPlayer.isSkinLoaded() && !abstractClientPlayer.isInvisible()) {
			this.bindTexture(abstractClientPlayer.getSkinTextureLocation());

			for (int m = 0; m < 2; m++) {
				float n = Mth.lerp(h, abstractClientPlayer.yRotO, abstractClientPlayer.yRot) - Mth.lerp(h, abstractClientPlayer.yBodyRotO, abstractClientPlayer.yBodyRot);
				float o = Mth.lerp(h, abstractClientPlayer.xRotO, abstractClientPlayer.xRot);
				GlStateManager.pushMatrix();
				GlStateManager.rotatef(n, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotatef(o, 1.0F, 0.0F, 0.0F);
				GlStateManager.translatef(0.375F * (float)(m * 2 - 1), 0.0F, 0.0F);
				GlStateManager.translatef(0.0F, -0.375F, 0.0F);
				GlStateManager.rotatef(-o, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(-n, 0.0F, 1.0F, 0.0F);
				float p = 1.3333334F;
				GlStateManager.scalef(1.3333334F, 1.3333334F, 1.3333334F);
				this.getParentModel().renderEars(0.0625F);
				GlStateManager.popMatrix();
			}
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return true;
	}
}
