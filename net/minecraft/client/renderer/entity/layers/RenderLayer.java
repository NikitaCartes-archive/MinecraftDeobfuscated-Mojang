/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public abstract class RenderLayer<T extends Entity, M extends EntityModel<T>> {
    private final RenderLayerParent<T, M> renderer;

    public RenderLayer(RenderLayerParent<T, M> renderLayerParent) {
        this.renderer = renderLayerParent;
    }

    protected static <T extends LivingEntity> void coloredModelCopyLayerRender(EntityModel<T> entityModel, EntityModel<T> entityModel2, ResourceLocation resourceLocation, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m) {
        RenderLayer.coloredModelCopyLayerRender(entityModel, entityModel2, resourceLocation, poseStack, multiBufferSource, i, livingEntity, f, g, h, j, k, l, m, 1.0f, 1.0f, 1.0f);
    }

    protected static <T extends LivingEntity> void coloredModelCopyLayerRender(EntityModel<T> entityModel, EntityModel<T> entityModel2, ResourceLocation resourceLocation, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m, float n, float o, float p) {
        if (!livingEntity.isInvisible()) {
            entityModel.copyPropertiesTo(entityModel2);
            entityModel2.prepareMobModel(livingEntity, f, g, m);
            entityModel2.setupAnim(livingEntity, f, g, h, j, k, l);
            RenderLayer.renderColoredModel(entityModel2, resourceLocation, poseStack, multiBufferSource, i, livingEntity, n, o, p);
        }
    }

    protected static <T extends LivingEntity> void renderColoredModel(EntityModel<T> entityModel, ResourceLocation resourceLocation, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity) {
        RenderLayer.renderColoredModel(entityModel, resourceLocation, poseStack, multiBufferSource, i, livingEntity, 1.0f, 1.0f, 1.0f);
    }

    protected static <T extends LivingEntity> void renderColoredModel(EntityModel<T> entityModel, ResourceLocation resourceLocation, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(resourceLocation));
        LivingEntityRenderer.setOverlayCoords(livingEntity, vertexConsumer, 0.0f);
        entityModel.renderToBuffer(poseStack, vertexConsumer, i, f, g, h);
        vertexConsumer.unsetDefaultOverlayCoords();
    }

    protected static <T extends LivingEntity> void renderModel(EntityModel<T> entityModel, ResourceLocation resourceLocation, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, float h) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(resourceLocation));
        OverlayTexture.setDefault(vertexConsumer);
        entityModel.renderToBuffer(poseStack, vertexConsumer, i, f, g, h);
        vertexConsumer.unsetDefaultOverlayCoords();
    }

    public M getParentModel() {
        return this.renderer.getModel();
    }

    protected ResourceLocation getTextureLocation(T entity) {
        return this.renderer.getTextureLocation(entity);
    }

    public abstract void render(PoseStack var1, MultiBufferSource var2, int var3, T var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11);
}

