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
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PowerableMob;

@Environment(value=EnvType.CLIENT)
public abstract class SpinnyLayer<T extends Entity, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    public SpinnyLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T entity, float f, float g, float h, float j, float k, float l, float m) {
        if (!((PowerableMob)entity).isPowered()) {
            return;
        }
        float n = (float)((Entity)entity).tickCount + h;
        EntityModel<T> entityModel = this.model();
        entityModel.prepareMobModel(entity, f, g, h);
        ((EntityModel)this.getParentModel()).copyPropertiesTo(entityModel);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.POWER_SWIRL(this.getTextureLocation(), this.xOffset(n), n * 0.01f));
        OverlayTexture.setDefault(vertexConsumer);
        entityModel.setupAnim(entity, f, g, j, k, l, m);
        entityModel.renderToBuffer(poseStack, vertexConsumer, i, 0.5f, 0.5f, 0.5f);
        vertexConsumer.unsetDefaultOverlayCoords();
    }

    protected abstract float xOffset(float var1);

    protected abstract ResourceLocation getTextureLocation();

    protected abstract EntityModel<T> model();
}

