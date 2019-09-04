package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.layers.DolphinCarryingItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Dolphin;

@Environment(EnvType.CLIENT)
public class DolphinRenderer extends MobRenderer<Dolphin, DolphinModel<Dolphin>> {
	private static final ResourceLocation DOLPHIN_LOCATION = new ResourceLocation("textures/entity/dolphin.png");

	public DolphinRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new DolphinModel<>(), 0.7F);
		this.addLayer(new DolphinCarryingItemLayer(this));
	}

	protected ResourceLocation getTextureLocation(Dolphin dolphin) {
		return DOLPHIN_LOCATION;
	}

	protected void scale(Dolphin dolphin, float f) {
		float g = 1.0F;
		RenderSystem.scalef(1.0F, 1.0F, 1.0F);
	}

	protected void setupRotations(Dolphin dolphin, float f, float g, float h) {
		super.setupRotations(dolphin, f, g, h);
	}
}
