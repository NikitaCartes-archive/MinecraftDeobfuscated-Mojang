/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;

@Environment(value=EnvType.CLIENT)
public class LeashKnotRenderer
extends EntityRenderer<LeashFenceKnotEntity> {
    private static final ResourceLocation KNOT_LOCATION = new ResourceLocation("textures/entity/lead_knot.png");
    private final LeashKnotModel<LeashFenceKnotEntity> model;

    public LeashKnotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new LeashKnotModel(context.getLayer(ModelLayers.LEASH_KNOT));
    }

    @Override
    public void render(LeashFenceKnotEntity leashFenceKnotEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.model.setupAnim(leashFenceKnotEntity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(KNOT_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
        super.render(leashFenceKnotEntity, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(LeashFenceKnotEntity leashFenceKnotEntity) {
        return KNOT_LOCATION;
    }
}

