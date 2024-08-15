package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;

@Environment(EnvType.CLIENT)
public class TropicalFishRenderer extends MobRenderer<TropicalFish, TropicalFishRenderState, EntityModel<TropicalFishRenderState>> {
	private final EntityModel<TropicalFishRenderState> modelA = this.getModel();
	private final EntityModel<TropicalFishRenderState> modelB;
	private static final ResourceLocation MODEL_A_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a.png");
	private static final ResourceLocation MODEL_B_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_b.png");

	public TropicalFishRenderer(EntityRendererProvider.Context context) {
		super(context, new TropicalFishModelA(context.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL)), 0.15F);
		this.modelB = new TropicalFishModelB(context.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE));
		this.addLayer(new TropicalFishPatternLayer(this, context.getModelSet()));
	}

	public ResourceLocation getTextureLocation(TropicalFishRenderState tropicalFishRenderState) {
		return switch (tropicalFishRenderState.variant.base()) {
			case SMALL -> MODEL_A_TEXTURE;
			case LARGE -> MODEL_B_TEXTURE;
		};
	}

	public TropicalFishRenderState createRenderState() {
		return new TropicalFishRenderState();
	}

	public void extractRenderState(TropicalFish tropicalFish, TropicalFishRenderState tropicalFishRenderState, float f) {
		super.extractRenderState(tropicalFish, tropicalFishRenderState, f);
		tropicalFishRenderState.variant = tropicalFish.getVariant();
		tropicalFishRenderState.baseColor = tropicalFish.getBaseColor().getTextureDiffuseColor();
		tropicalFishRenderState.patternColor = tropicalFish.getPatternColor().getTextureDiffuseColor();
	}

	public void render(TropicalFishRenderState tropicalFishRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		this.model = switch (tropicalFishRenderState.variant.base()) {
			case SMALL -> this.modelA;
			case LARGE -> this.modelB;
		};
		super.render(tropicalFishRenderState, poseStack, multiBufferSource, i);
	}

	protected int getModelTint(TropicalFishRenderState tropicalFishRenderState) {
		return tropicalFishRenderState.baseColor;
	}

	protected void setupRotations(TropicalFishRenderState tropicalFishRenderState, PoseStack poseStack, float f, float g) {
		super.setupRotations(tropicalFishRenderState, poseStack, f, g);
		float h = 4.3F * Mth.sin(0.6F * tropicalFishRenderState.ageInTicks);
		poseStack.mulPose(Axis.YP.rotationDegrees(h));
		if (!tropicalFishRenderState.isInWater) {
			poseStack.translate(0.2F, 0.1F, 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
		}
	}
}
