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
import net.minecraft.world.entity.PowerableMob;

@Environment(EnvType.CLIENT)
public abstract class EnergySwirlLayer<T extends Entity & PowerableMob, M extends EntityModel<T>> extends RenderLayer<T, M> {
	public EnergySwirlLayer(RenderLayerParent<T, M> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T entity, float f, float g, float h, float j, float k, float l) {
		if (entity.isPowered()) {
			float m = (float)entity.tickCount + h;
			EntityModel<T> entityModel = this.model();
			entityModel.prepareMobModel(entity, f, g, h);
			this.getParentModel().copyPropertiesTo(entityModel);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(
				RenderType.energySwirl(this.getTextureLocation(entity), this.xOffset(m) % 1.0F, this.yOffset(m) % 1.0F)
			);
			entityModel.setupAnim(entity, f, g, j, k, l);
			entityModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
		}
	}

	protected abstract float xOffset(float f);

	protected float yOffset(float f) {
		return f * 0.01F;
	}

	protected abstract ResourceLocation getTextureLocation();

	protected abstract EntityModel<T> model();
}
