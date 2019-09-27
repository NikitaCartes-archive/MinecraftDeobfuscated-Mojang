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
public abstract class SpinnyLayer<T extends Entity & PowerableMob, M extends EntityModel<T>> extends RenderLayer<T, M> {
	public SpinnyLayer(RenderLayerParent<T, M> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T entity, float f, float g, float h, float j, float k, float l, float m) {
		if (entity.isPowered()) {
			float n = (float)entity.tickCount + h;
			EntityModel<T> entityModel = this.model();
			entityModel.prepareMobModel(entity, f, g, h);
			this.getParentModel().copyPropertiesTo(entityModel);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.POWER_SWIRL(this.getTextureLocation(), this.xOffset(n), n * 0.01F));
			OverlayTexture.setDefault(vertexConsumer);
			entityModel.setupAnim(entity, f, g, j, k, l, m);
			entityModel.renderToBuffer(poseStack, vertexConsumer, i, 0.5F, 0.5F, 0.5F);
			vertexConsumer.unsetDefaultOverlayCoords();
		}
	}

	protected abstract float xOffset(float f);

	protected abstract ResourceLocation getTextureLocation();

	protected abstract EntityModel<T> model();
}
