package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class PhantomEyesLayer<T extends Entity> extends RenderLayer<T, PhantomModel<T>> {
	private static final ResourceLocation PHANTOM_EYES_LOCATION = new ResourceLocation("textures/entity/phantom_eyes.png");

	public PhantomEyesLayer(RenderLayerParent<T, PhantomModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	public void render(T entity, float f, float g, float h, float i, float j, float k, float l) {
		this.bindTexture(PHANTOM_EYES_LOCATION);
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.disableLighting();
		RenderSystem.depthMask(!entity.isInvisible());
		int m = 61680;
		int n = 61680;
		int o = 0;
		RenderSystem.glMultiTexCoord2f(33985, 61680.0F, 0.0F);
		RenderSystem.enableLighting();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
		gameRenderer.resetFogColor(true);
		this.getParentModel().render(entity, f, g, i, j, k, l);
		gameRenderer.resetFogColor(false);
		this.setLightColor(entity);
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
