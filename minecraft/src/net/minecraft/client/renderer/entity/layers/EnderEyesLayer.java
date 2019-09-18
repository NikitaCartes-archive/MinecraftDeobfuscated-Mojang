package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class EnderEyesLayer<T extends LivingEntity> extends RenderLayer<T, EndermanModel<T>> {
	private static final ResourceLocation ENDERMAN_EYES_LOCATION = new ResourceLocation("textures/entity/enderman/enderman_eyes.png");

	public EnderEyesLayer(RenderLayerParent<T, EndermanModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
		this.bindTexture(ENDERMAN_EYES_LOCATION);
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.disableLighting();
		RenderSystem.depthMask(!livingEntity.isInvisible());
		int m = 61680;
		int n = 61680;
		int o = 0;
		RenderSystem.glMultiTexCoord2f(33985, 61680.0F, 0.0F);
		RenderSystem.enableLighting();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		FogRenderer.resetFogColor(true);
		this.getParentModel().render(livingEntity, f, g, i, j, k, l);
		FogRenderer.resetFogColor(false);
		this.setLightColor(livingEntity);
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.enableAlphaTest();
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
