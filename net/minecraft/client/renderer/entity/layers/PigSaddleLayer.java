/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

@Environment(value=EnvType.CLIENT)
public class PigSaddleLayer
extends RenderLayer<Pig, PigModel<Pig>> {
    private static final ResourceLocation SADDLE_LOCATION = new ResourceLocation("textures/entity/pig/pig_saddle.png");
    private final PigModel<Pig> model = new PigModel(0.5f);

    public PigSaddleLayer(RenderLayerParent<Pig, PigModel<Pig>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Pig pig, float f, float g, float h, float j, float k, float l, float m) {
        if (!pig.hasSaddle()) {
            return;
        }
        ((PigModel)this.getParentModel()).copyPropertiesTo(this.model);
        this.model.prepareMobModel(pig, f, g, h);
        this.model.setupAnim(pig, f, g, j, k, l, m);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(SADDLE_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f);
    }
}

