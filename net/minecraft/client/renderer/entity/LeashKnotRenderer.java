/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;

@Environment(value=EnvType.CLIENT)
public class LeashKnotRenderer
extends EntityRenderer<LeashFenceKnotEntity> {
    private static final ResourceLocation KNOT_LOCATION = new ResourceLocation("textures/entity/lead_knot.png");
    private final LeashKnotModel<LeashFenceKnotEntity> model = new LeashKnotModel();

    public LeashKnotRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(LeashFenceKnotEntity leashFenceKnotEntity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        poseStack.pushPose();
        float i = 0.0625f;
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        int j = leashFenceKnotEntity.getLightColor();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(KNOT_LOCATION));
        OverlayTexture.setDefault(vertexConsumer);
        this.model.setupAnim(leashFenceKnotEntity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f);
        this.model.renderToBuffer(poseStack, vertexConsumer, j);
        vertexConsumer.unsetDefaultOverlayCoords();
        poseStack.popPose();
        super.render(leashFenceKnotEntity, d, e, f, g, h, poseStack, multiBufferSource);
    }

    @Override
    public ResourceLocation getTextureLocation(LeashFenceKnotEntity leashFenceKnotEntity) {
        return KNOT_LOCATION;
    }
}

