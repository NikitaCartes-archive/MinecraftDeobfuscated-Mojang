package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemSteerableMount;

@Environment(EnvType.CLIENT)
public class SaddleLayer<T extends Entity & ItemSteerableMount, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private final ResourceLocation textureLocation;
	private final M model;

	public SaddleLayer(RenderLayerParent<T, M> renderLayerParent, M entityModel, ResourceLocation resourceLocation) {
		super(renderLayerParent);
		this.model = entityModel;
		this.textureLocation = resourceLocation;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T entity, float f, float g, float h, float j, float k, float l) {
		if (entity.hasSaddle()) {
			this.getParentModel().copyPropertiesTo(this.model);
			this.model.prepareMobModel(entity, f, g, h);
			this.model.setupAnim(entity, f, g, j, k, l);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(this.textureLocation));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}
