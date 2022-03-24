/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;

@Environment(value=EnvType.CLIENT)
public class WardenEmissiveLayer<T extends Warden, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    private final ResourceLocation texture;
    private final AlphaFunction<T> alphaFunction;

    public WardenEmissiveLayer(RenderLayerParent<T, M> renderLayerParent, ResourceLocation resourceLocation, AlphaFunction<T> alphaFunction) {
        super(renderLayerParent);
        this.texture = resourceLocation;
        this.alphaFunction = alphaFunction;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T warden, float f, float g, float h, float j, float k, float l) {
        if (((Entity)warden).isInvisible()) {
            return;
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucentEmissive(this.texture));
        ((Model)this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(warden, 0.0f), 1.0f, 1.0f, 1.0f, this.alphaFunction.apply(warden, h, j));
    }

    @Environment(value=EnvType.CLIENT)
    public static interface AlphaFunction<T extends Warden> {
        public float apply(T var1, float var2, float var3);
    }
}

