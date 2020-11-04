package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

@Environment(EnvType.CLIENT)
public class WolfRenderer extends MobRenderer<Wolf, WolfModel<Wolf>> {
	private static final ResourceLocation WOLF_LOCATION = new ResourceLocation("textures/entity/wolf/wolf.png");
	private static final ResourceLocation WOLF_TAME_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_tame.png");
	private static final ResourceLocation WOLF_ANGRY_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_angry.png");

	public WolfRenderer(EntityRendererProvider.Context context) {
		super(context, new WolfModel<>(context.getLayer(ModelLayers.WOLF)), 0.5F);
		this.addLayer(new WolfCollarLayer(this));
	}

	protected float getBob(Wolf wolf, float f) {
		return wolf.getTailAngle();
	}

	public void render(Wolf wolf, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		if (wolf.isWet()) {
			float h = wolf.getWetShade(g);
			this.model.setColor(h, h, h);
		}

		super.render(wolf, f, g, poseStack, multiBufferSource, i);
		if (wolf.isWet()) {
			this.model.setColor(1.0F, 1.0F, 1.0F);
		}
	}

	public ResourceLocation getTextureLocation(Wolf wolf) {
		if (wolf.isTame()) {
			return WOLF_TAME_LOCATION;
		} else {
			return wolf.isAngry() ? WOLF_ANGRY_LOCATION : WOLF_LOCATION;
		}
	}
}
