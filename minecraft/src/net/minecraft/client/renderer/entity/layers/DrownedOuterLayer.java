package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public class DrownedOuterLayer<T extends Zombie> extends RenderLayer<T, DrownedModel<T>> {
	private static final ResourceLocation DROWNED_OUTER_LAYER_LOCATION = new ResourceLocation("textures/entity/zombie/drowned_outer_layer.png");
	private final DrownedModel<T> model = new DrownedModel<>(0.25F, 0.0F, 64, 64);

	public DrownedOuterLayer(RenderLayerParent<T, DrownedModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T zombie, float f, float g, float h, float j, float k, float l, float m) {
		coloredCutoutModelCopyLayerRender(
			this.getParentModel(), this.model, DROWNED_OUTER_LAYER_LOCATION, poseStack, multiBufferSource, i, zombie, f, g, j, k, l, m, h, 1.0F, 1.0F, 1.0F
		);
	}
}
