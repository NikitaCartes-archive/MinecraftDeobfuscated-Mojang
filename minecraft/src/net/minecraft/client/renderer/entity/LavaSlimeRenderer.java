package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.MagmaCube;

@Environment(EnvType.CLIENT)
public class LavaSlimeRenderer extends MobRenderer<MagmaCube, LavaSlimeModel<MagmaCube>> {
	private static final ResourceLocation MAGMACUBE_LOCATION = new ResourceLocation("textures/entity/slime/magmacube.png");

	public LavaSlimeRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new LavaSlimeModel<>(), 0.25F);
	}

	protected ResourceLocation getTextureLocation(MagmaCube magmaCube) {
		return MAGMACUBE_LOCATION;
	}

	protected void scale(MagmaCube magmaCube, float f) {
		int i = magmaCube.getSize();
		float g = Mth.lerp(f, magmaCube.oSquish, magmaCube.squish) / ((float)i * 0.5F + 1.0F);
		float h = 1.0F / (g + 1.0F);
		RenderSystem.scalef(h * (float)i, 1.0F / h * (float)i, h * (float)i);
	}
}
