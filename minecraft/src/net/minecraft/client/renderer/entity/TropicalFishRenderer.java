package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ColorableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;

@Environment(EnvType.CLIENT)
public class TropicalFishRenderer extends MobRenderer<TropicalFish, EntityModel<TropicalFish>> {
	private final TropicalFishModelA<TropicalFish> modelA = new TropicalFishModelA<>(0.0F);
	private final TropicalFishModelB<TropicalFish> modelB = new TropicalFishModelB<>(0.0F);

	public TropicalFishRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new TropicalFishModelA<>(0.0F), 0.15F);
		this.addLayer(new TropicalFishPatternLayer(this));
	}

	public ResourceLocation getTextureLocation(TropicalFish tropicalFish) {
		return tropicalFish.getBaseTextureLocation();
	}

	public void render(TropicalFish tropicalFish, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		ColorableListModel<TropicalFish> colorableListModel = (ColorableListModel<TropicalFish>)(tropicalFish.getBaseVariant() == 0 ? this.modelA : this.modelB);
		this.model = colorableListModel;
		float[] fs = tropicalFish.getBaseColor();
		colorableListModel.setColor(fs[0], fs[1], fs[2]);
		super.render(tropicalFish, f, g, poseStack, multiBufferSource, i);
		colorableListModel.setColor(1.0F, 1.0F, 1.0F);
	}

	protected void setupRotations(TropicalFish tropicalFish, PoseStack poseStack, float f, float g, float h) {
		super.setupRotations(tropicalFish, poseStack, f, g, h);
		float i = 4.3F * Mth.sin(0.6F * f);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(i));
		if (!tropicalFish.isInWater()) {
			poseStack.translate(0.2F, 0.1F, 0.0);
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
		}
	}
}
