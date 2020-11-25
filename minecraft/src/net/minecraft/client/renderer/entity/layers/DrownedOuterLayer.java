package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Drowned;

@Environment(EnvType.CLIENT)
public class DrownedOuterLayer<T extends Drowned> extends RenderLayer<T, DrownedModel<T>> {
	private static final ResourceLocation DROWNED_OUTER_LAYER_LOCATION = new ResourceLocation("textures/entity/zombie/drowned_outer_layer.png");
	private final DrownedModel<T> model;

	public DrownedOuterLayer(RenderLayerParent<T, DrownedModel<T>> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new DrownedModel<>(entityModelSet.bakeLayer(ModelLayers.DROWNED_OUTER_LAYER));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T drowned, float f, float g, float h, float j, float k, float l) {
		coloredCutoutModelCopyLayerRender(
			this.getParentModel(), this.model, DROWNED_OUTER_LAYER_LOCATION, poseStack, multiBufferSource, i, drowned, f, g, j, k, l, h, 1.0F, 1.0F, 1.0F
		);
	}
}
