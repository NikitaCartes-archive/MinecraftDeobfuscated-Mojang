package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;

@Environment(EnvType.CLIENT)
public class CreeperRenderer extends MobRenderer<Creeper, CreeperModel<Creeper>> {
	private static final ResourceLocation CREEPER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper.png");

	public CreeperRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new CreeperModel<>(), 0.5F);
		this.addLayer(new CreeperPowerLayer(this));
	}

	protected void scale(Creeper creeper, PoseStack poseStack, float f) {
		float g = creeper.getSwelling(f);
		float h = 1.0F + Mth.sin(g * 100.0F) * g * 0.01F;
		g = Mth.clamp(g, 0.0F, 1.0F);
		g *= g;
		g *= g;
		float i = (1.0F + g * 0.4F) * h;
		float j = (1.0F + g * 0.1F) / h;
		poseStack.scale(i, j, i);
	}

	protected float getWhiteOverlayProgress(Creeper creeper, float f) {
		float g = creeper.getSwelling(f);
		return (int)(g * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(g, 0.5F, 1.0F);
	}

	public ResourceLocation getTextureLocation(Creeper creeper) {
		return CREEPER_LOCATION;
	}
}
