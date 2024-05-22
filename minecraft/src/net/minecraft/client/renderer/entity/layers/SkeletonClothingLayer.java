package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;

@Environment(EnvType.CLIENT)
public class SkeletonClothingLayer<T extends Mob & RangedAttackMob, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private final SkeletonModel<T> layerModel;
	private final ResourceLocation clothesLocation;

	public SkeletonClothingLayer(
		RenderLayerParent<T, M> renderLayerParent, EntityModelSet entityModelSet, ModelLayerLocation modelLayerLocation, ResourceLocation resourceLocation
	) {
		super(renderLayerParent);
		this.clothesLocation = resourceLocation;
		this.layerModel = new SkeletonModel<>(entityModelSet.bakeLayer(modelLayerLocation));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T mob, float f, float g, float h, float j, float k, float l) {
		coloredCutoutModelCopyLayerRender(this.getParentModel(), this.layerModel, this.clothesLocation, poseStack, multiBufferSource, i, mob, f, g, j, k, l, h, -1);
	}
}
