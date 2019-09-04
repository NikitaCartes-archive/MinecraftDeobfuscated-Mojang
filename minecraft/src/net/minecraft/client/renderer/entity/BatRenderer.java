package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BatModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;

@Environment(EnvType.CLIENT)
public class BatRenderer extends MobRenderer<Bat, BatModel> {
	private static final ResourceLocation BAT_LOCATION = new ResourceLocation("textures/entity/bat.png");

	public BatRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new BatModel(), 0.25F);
	}

	protected ResourceLocation getTextureLocation(Bat bat) {
		return BAT_LOCATION;
	}

	protected void scale(Bat bat, float f) {
		RenderSystem.scalef(0.35F, 0.35F, 0.35F);
	}

	protected void setupRotations(Bat bat, float f, float g, float h) {
		if (bat.isResting()) {
			RenderSystem.translatef(0.0F, -0.1F, 0.0F);
		} else {
			RenderSystem.translatef(0.0F, Mth.cos(f * 0.3F) * 0.1F, 0.0F);
		}

		super.setupRotations(bat, f, g, h);
	}
}
