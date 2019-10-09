package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public abstract class RenderLayer<T extends Entity, M extends EntityModel<T>> {
	private final RenderLayerParent<T, M> renderer;

	public RenderLayer(RenderLayerParent<T, M> renderLayerParent) {
		this.renderer = renderLayerParent;
	}

	protected static <T extends LivingEntity> void coloredCutoutModelCopyLayerRender(
		EntityModel<T> entityModel,
		EntityModel<T> entityModel2,
		ResourceLocation resourceLocation,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		T livingEntity,
		float f,
		float g,
		float h,
		float j,
		float k,
		float l,
		float m,
		float n,
		float o,
		float p
	) {
		if (!livingEntity.isInvisible()) {
			entityModel.copyPropertiesTo(entityModel2);
			entityModel2.prepareMobModel(livingEntity, f, g, m);
			entityModel2.setupAnim(livingEntity, f, g, h, j, k, l);
			renderColoredCutoutModel(entityModel2, resourceLocation, poseStack, multiBufferSource, i, livingEntity, n, o, p);
		}
	}

	protected static <T extends LivingEntity> void renderColoredCutoutModel(
		EntityModel<T> entityModel,
		ResourceLocation resourceLocation,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		T livingEntity,
		float f,
		float g,
		float h
	) {
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(resourceLocation));
		entityModel.renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F), f, g, h);
	}

	public M getParentModel() {
		return this.renderer.getModel();
	}

	protected ResourceLocation getTextureLocation(T entity) {
		return this.renderer.getTextureLocation(entity);
	}

	public abstract void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T entity, float f, float g, float h, float j, float k, float l, float m
	);
}
