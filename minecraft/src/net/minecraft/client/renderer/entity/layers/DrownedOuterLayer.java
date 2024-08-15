package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class DrownedOuterLayer extends RenderLayer<ZombieRenderState, DrownedModel> {
	private static final ResourceLocation DROWNED_OUTER_LAYER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/drowned_outer_layer.png");
	private final DrownedModel model;
	private final DrownedModel babyModel;

	public DrownedOuterLayer(RenderLayerParent<ZombieRenderState, DrownedModel> renderLayerParent, EntityModelSet entityModelSet) {
		super(renderLayerParent);
		this.model = new DrownedModel(entityModelSet.bakeLayer(ModelLayers.DROWNED_OUTER_LAYER));
		this.babyModel = new DrownedModel(entityModelSet.bakeLayer(ModelLayers.DROWNED_BABY_OUTER_LAYER));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ZombieRenderState zombieRenderState, float f, float g) {
		DrownedModel drownedModel = zombieRenderState.isBaby ? this.babyModel : this.model;
		coloredCutoutModelCopyLayerRender(drownedModel, DROWNED_OUTER_LAYER_LOCATION, poseStack, multiBufferSource, i, zombieRenderState, -1);
	}
}
