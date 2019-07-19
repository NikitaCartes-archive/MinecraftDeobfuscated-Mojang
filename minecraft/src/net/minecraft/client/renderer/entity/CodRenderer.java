package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CodModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cod;

@Environment(EnvType.CLIENT)
public class CodRenderer extends MobRenderer<Cod, CodModel<Cod>> {
	private static final ResourceLocation COD_LOCATION = new ResourceLocation("textures/entity/fish/cod.png");

	public CodRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new CodModel<>(), 0.3F);
	}

	@Nullable
	protected ResourceLocation getTextureLocation(Cod cod) {
		return COD_LOCATION;
	}

	protected void setupRotations(Cod cod, float f, float g, float h) {
		super.setupRotations(cod, f, g, h);
		float i = 4.3F * Mth.sin(0.6F * f);
		GlStateManager.rotatef(i, 0.0F, 1.0F, 0.0F);
		if (!cod.isInWater()) {
			GlStateManager.translatef(0.1F, 0.1F, -0.1F);
			GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
		}
	}
}
