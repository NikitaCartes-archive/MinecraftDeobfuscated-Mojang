package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;

@Environment(EnvType.CLIENT)
public class IronGolemRenderer extends MobRenderer<IronGolem, IronGolemModel<IronGolem>> {
	private static final ResourceLocation GOLEM_LOCATION = new ResourceLocation("textures/entity/iron_golem.png");

	public IronGolemRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new IronGolemModel<>(), 0.7F);
		this.addLayer(new IronGolemFlowerLayer(this));
	}

	protected ResourceLocation getTextureLocation(IronGolem ironGolem) {
		return GOLEM_LOCATION;
	}

	protected void setupRotations(IronGolem ironGolem, float f, float g, float h) {
		super.setupRotations(ironGolem, f, g, h);
		if (!((double)ironGolem.animationSpeed < 0.01)) {
			float i = 13.0F;
			float j = ironGolem.animationPosition - ironGolem.animationSpeed * (1.0F - h) + 6.0F;
			float k = (Math.abs(j % 13.0F - 6.5F) - 3.25F) / 3.25F;
			GlStateManager.rotatef(6.5F * k, 0.0F, 0.0F, 1.0F);
		}
	}
}
