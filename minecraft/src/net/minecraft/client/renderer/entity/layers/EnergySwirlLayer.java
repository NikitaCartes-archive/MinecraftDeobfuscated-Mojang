package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class EnergySwirlLayer<S extends EntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
	public EnergySwirlLayer(RenderLayerParent<S, M> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S entityRenderState, float f, float g) {
		if (this.isPowered(entityRenderState)) {
			float h = entityRenderState.ageInTicks;
			M entityModel = this.model();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.energySwirl(this.getTextureLocation(), this.xOffset(h) % 1.0F, h * 0.01F % 1.0F));
			entityModel.setupAnim(entityRenderState);
			entityModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, -8355712);
		}
	}

	protected abstract boolean isPowered(S entityRenderState);

	protected abstract float xOffset(float f);

	protected abstract ResourceLocation getTextureLocation();

	protected abstract M model();
}
