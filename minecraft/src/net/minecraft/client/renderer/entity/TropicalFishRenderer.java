package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;

@Environment(EnvType.CLIENT)
public class TropicalFishRenderer extends MobRenderer<TropicalFish, EntityModel<TropicalFish>> {
	private final TropicalFishModelA<TropicalFish> modelA = new TropicalFishModelA<>();
	private final TropicalFishModelB<TropicalFish> modelB = new TropicalFishModelB<>();

	public TropicalFishRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new TropicalFishModelA<>(), 0.15F);
		this.addLayer(new TropicalFishPatternLayer(this));
	}

	@Nullable
	protected ResourceLocation getTextureLocation(TropicalFish tropicalFish) {
		return tropicalFish.getBaseTextureLocation();
	}

	public void render(TropicalFish tropicalFish, double d, double e, double f, float g, float h) {
		this.model = (EntityModel<TropicalFish>)(tropicalFish.getBaseVariant() == 0 ? this.modelA : this.modelB);
		float[] fs = tropicalFish.getBaseColor();
		RenderSystem.color3f(fs[0], fs[1], fs[2]);
		super.render(tropicalFish, d, e, f, g, h);
	}

	protected void setupRotations(TropicalFish tropicalFish, float f, float g, float h) {
		super.setupRotations(tropicalFish, f, g, h);
		float i = 4.3F * Mth.sin(0.6F * f);
		RenderSystem.rotatef(i, 0.0F, 1.0F, 0.0F);
		if (!tropicalFish.isInWater()) {
			RenderSystem.translatef(0.2F, 0.1F, 0.0F);
			RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
		}
	}
}
