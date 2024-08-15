package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.TropicalFish;

@Environment(EnvType.CLIENT)
public class TropicalFishPatternLayer extends RenderLayer<TropicalFishRenderState, EntityModel<TropicalFishRenderState>> {
	private static final ResourceLocation KOB_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a_pattern_1.png");
	private static final ResourceLocation SUNSTREAK_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a_pattern_2.png");
	private static final ResourceLocation SNOOPER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a_pattern_3.png");
	private static final ResourceLocation DASHER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a_pattern_4.png");
	private static final ResourceLocation BRINELY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a_pattern_5.png");
	private static final ResourceLocation SPOTTY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a_pattern_6.png");
	private static final ResourceLocation FLOPPER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_b_pattern_1.png");
	private static final ResourceLocation STRIPEY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_b_pattern_2.png");
	private static final ResourceLocation GLITTER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_b_pattern_3.png");
	private static final ResourceLocation BLOCKFISH_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_b_pattern_4.png");
	private static final ResourceLocation BETTY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_b_pattern_5.png");
	private static final ResourceLocation CLAYFISH_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_b_pattern_6.png");
	private final TropicalFishModelA modelA;
	private final TropicalFishModelB modelB;

	public TropicalFishPatternLayer(
		RenderLayerParent<TropicalFishRenderState, EntityModel<TropicalFishRenderState>> renderLayerParent, EntityModelSet entityModelSet
	) {
		super(renderLayerParent);
		this.modelA = new TropicalFishModelA(entityModelSet.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL_PATTERN));
		this.modelB = new TropicalFishModelB(entityModelSet.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE_PATTERN));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, TropicalFishRenderState tropicalFishRenderState, float f, float g) {
		TropicalFish.Pattern pattern = tropicalFishRenderState.variant;

		EntityModel<TropicalFishRenderState> entityModel = (EntityModel<TropicalFishRenderState>)(switch (pattern.base()) {
			case SMALL -> this.modelA;
			case LARGE -> this.modelB;
		});

		ResourceLocation resourceLocation = switch (pattern) {
			case KOB -> KOB_TEXTURE;
			case SUNSTREAK -> SUNSTREAK_TEXTURE;
			case SNOOPER -> SNOOPER_TEXTURE;
			case DASHER -> DASHER_TEXTURE;
			case BRINELY -> BRINELY_TEXTURE;
			case SPOTTY -> SPOTTY_TEXTURE;
			case FLOPPER -> FLOPPER_TEXTURE;
			case STRIPEY -> STRIPEY_TEXTURE;
			case GLITTER -> GLITTER_TEXTURE;
			case BLOCKFISH -> BLOCKFISH_TEXTURE;
			case BETTY -> BETTY_TEXTURE;
			case CLAYFISH -> CLAYFISH_TEXTURE;
		};
		coloredCutoutModelCopyLayerRender(
			entityModel, resourceLocation, poseStack, multiBufferSource, i, tropicalFishRenderState, tropicalFishRenderState.patternColor
		);
	}
}
