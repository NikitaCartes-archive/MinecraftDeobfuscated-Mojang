package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.animal.TropicalFish;

@Environment(EnvType.CLIENT)
public class TropicalFishPatternLayer extends RenderLayer<TropicalFish, ColorableHierarchicalModel<TropicalFish>> {
	private final TropicalFishModelA<TropicalFish> modelA;
	private final TropicalFishModelB<TropicalFish> modelB;

	public TropicalFishPatternLayer(RenderLayerParent<TropicalFish, ColorableHierarchicalModel<TropicalFish>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.modelA = new TropicalFishModelA<>(entityModelSet.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL_PATTERN));
		this.modelB = new TropicalFishModelB<>(entityModelSet.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE_PATTERN));
	}

	public void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, TropicalFish tropicalFish, float f, float g, float h, float j, float k, float l
	) {
		EntityModel<TropicalFish> entityModel = (EntityModel<TropicalFish>)(tropicalFish.getBaseVariant() == 0 ? this.modelA : this.modelB);
		float[] fs = tropicalFish.getPatternColor();
		coloredCutoutModelCopyLayerRender(
			this.getParentModel(),
			entityModel,
			tropicalFish.getPatternTextureLocation(),
			poseStack,
			multiBufferSource,
			i,
			tropicalFish,
			f,
			g,
			j,
			k,
			l,
			h,
			fs[0],
			fs[1],
			fs[2]
		);
	}
}
