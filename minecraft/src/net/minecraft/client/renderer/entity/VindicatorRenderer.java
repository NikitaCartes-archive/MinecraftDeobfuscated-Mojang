package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;

@Environment(EnvType.CLIENT)
public class VindicatorRenderer extends IllagerRenderer<Vindicator, IllagerRenderState> {
	private static final ResourceLocation VINDICATOR = ResourceLocation.withDefaultNamespace("textures/entity/illager/vindicator.png");

	public VindicatorRenderer(EntityRendererProvider.Context context) {
		super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
		this.addLayer(new ItemInHandLayer<IllagerRenderState, IllagerModel<IllagerRenderState>>(this, context.getItemRenderer()) {
			public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, IllagerRenderState illagerRenderState, float f, float g) {
				if (illagerRenderState.isAggressive) {
					super.render(poseStack, multiBufferSource, i, illagerRenderState, f, g);
				}
			}
		});
	}

	public ResourceLocation getTextureLocation(IllagerRenderState illagerRenderState) {
		return VINDICATOR;
	}

	public IllagerRenderState createRenderState() {
		return new IllagerRenderState();
	}
}
