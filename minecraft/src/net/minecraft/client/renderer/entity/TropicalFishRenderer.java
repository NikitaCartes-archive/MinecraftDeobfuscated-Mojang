package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;

@Environment(EnvType.CLIENT)
public class TropicalFishRenderer extends MobRenderer<TropicalFish, ColorableHierarchicalModel<TropicalFish>> {
	private final ColorableHierarchicalModel<TropicalFish> modelA = this.getModel();
	private final ColorableHierarchicalModel<TropicalFish> modelB;
	private static final ResourceLocation MODEL_A_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a.png");
	private static final ResourceLocation MODEL_B_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b.png");

	public TropicalFishRenderer(EntityRendererProvider.Context context) {
		super(context, new TropicalFishModelA<>(context.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL)), 0.15F);
		this.modelB = new TropicalFishModelB<>(context.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE));
		this.addLayer(new TropicalFishPatternLayer(this, context.getModelSet()));
	}

	public ResourceLocation getTextureLocation(TropicalFish tropicalFish) {
		return switch (tropicalFish.getVariant().base()) {
			case SMALL -> MODEL_A_TEXTURE;
			case LARGE -> MODEL_B_TEXTURE;
		};
	}

	public void render(TropicalFish tropicalFish, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		ColorableHierarchicalModel<TropicalFish> colorableHierarchicalModel = switch (tropicalFish.getVariant().base()) {
			case SMALL -> this.modelA;
			case LARGE -> this.modelB;
		};
		this.model = colorableHierarchicalModel;
		float[] fs = tropicalFish.getBaseColor().getTextureDiffuseColors();
		colorableHierarchicalModel.setColor(fs[0], fs[1], fs[2]);
		super.render(tropicalFish, f, g, poseStack, multiBufferSource, i);
		colorableHierarchicalModel.setColor(1.0F, 1.0F, 1.0F);
	}

	protected void setupRotations(TropicalFish tropicalFish, PoseStack poseStack, float f, float g, float h, float i) {
		super.setupRotations(tropicalFish, poseStack, f, g, h, i);
		float j = 4.3F * Mth.sin(0.6F * f);
		poseStack.mulPose(Axis.YP.rotationDegrees(j));
		if (!tropicalFish.isInWater()) {
			poseStack.translate(0.2F, 0.1F, 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
		}
	}
}
