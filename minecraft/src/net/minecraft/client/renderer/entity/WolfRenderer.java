package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

@Environment(EnvType.CLIENT)
public class WolfRenderer extends MobRenderer<Wolf, WolfModel<Wolf>> {
	private static final ResourceLocation WOLF_LOCATION = new ResourceLocation("textures/entity/wolf/wolf.png");
	private static final ResourceLocation WOLF_TAME_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_tame.png");
	private static final ResourceLocation WOLF_ANGRY_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_angry.png");

	public WolfRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new WolfModel<>(), 0.5F);
		this.addLayer(new WolfCollarLayer(this));
	}

	protected float getBob(Wolf wolf, float f) {
		return wolf.getTailAngle();
	}

	public void render(Wolf wolf, double d, double e, double f, float g, float h) {
		if (wolf.isWet()) {
			float i = wolf.getBrightness() * wolf.getWetShade(h);
			RenderSystem.color3f(i, i, i);
		}

		super.render(wolf, d, e, f, g, h);
	}

	protected ResourceLocation getTextureLocation(Wolf wolf) {
		if (wolf.isTame()) {
			return WOLF_TAME_LOCATION;
		} else {
			return wolf.isAngry() ? WOLF_ANGRY_LOCATION : WOLF_LOCATION;
		}
	}
}
