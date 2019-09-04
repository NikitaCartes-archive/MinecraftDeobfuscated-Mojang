package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

@Environment(EnvType.CLIENT)
public class WitherArmorLayer extends RenderLayer<WitherBoss, WitherBossModel<WitherBoss>> {
	private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
	private final WitherBossModel<WitherBoss> model = new WitherBossModel<>(0.5F);

	public WitherArmorLayer(RenderLayerParent<WitherBoss, WitherBossModel<WitherBoss>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(WitherBoss witherBoss, float f, float g, float h, float i, float j, float k, float l) {
		if (witherBoss.isPowered()) {
			RenderSystem.depthMask(!witherBoss.isInvisible());
			this.bindTexture(WITHER_ARMOR_LOCATION);
			RenderSystem.matrixMode(5890);
			RenderSystem.loadIdentity();
			float m = (float)witherBoss.tickCount + h;
			float n = Mth.cos(m * 0.02F) * 3.0F;
			float o = m * 0.01F;
			RenderSystem.translatef(n, o, 0.0F);
			RenderSystem.matrixMode(5888);
			RenderSystem.enableBlend();
			float p = 0.5F;
			RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
			RenderSystem.disableLighting();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
			this.model.prepareMobModel(witherBoss, f, g, h);
			this.getParentModel().copyPropertiesTo(this.model);
			GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
			gameRenderer.resetFogColor(true);
			this.model.render(witherBoss, f, g, i, j, k, l);
			gameRenderer.resetFogColor(false);
			RenderSystem.matrixMode(5890);
			RenderSystem.loadIdentity();
			RenderSystem.matrixMode(5888);
			RenderSystem.enableLighting();
			RenderSystem.disableBlend();
			RenderSystem.depthMask(true);
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
