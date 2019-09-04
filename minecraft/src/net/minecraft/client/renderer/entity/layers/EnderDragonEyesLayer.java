package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.dragon.DragonModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

@Environment(EnvType.CLIENT)
public class EnderDragonEyesLayer extends RenderLayer<EnderDragon, DragonModel> {
	private static final ResourceLocation DRAGON_EYES_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");

	public EnderDragonEyesLayer(RenderLayerParent<EnderDragon, DragonModel> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(EnderDragon enderDragon, float f, float g, float h, float i, float j, float k, float l) {
		this.bindTexture(DRAGON_EYES_LOCATION);
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.disableLighting();
		RenderSystem.depthFunc(514);
		int m = 61680;
		int n = 61680;
		int o = 0;
		RenderSystem.glMultiTexCoord2f(33985, 61680.0F, 0.0F);
		RenderSystem.enableLighting();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
		gameRenderer.resetFogColor(true);
		this.getParentModel().render(enderDragon, f, g, i, j, k, l);
		gameRenderer.resetFogColor(false);
		this.setLightColor(enderDragon);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.depthFunc(515);
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
