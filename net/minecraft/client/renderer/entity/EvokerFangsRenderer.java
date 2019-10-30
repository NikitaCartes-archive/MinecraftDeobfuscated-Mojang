/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.EvokerFangs;

@Environment(value=EnvType.CLIENT)
public class EvokerFangsRenderer
extends EntityRenderer<EvokerFangs> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/evoker_fangs.png");
    private final EvokerFangsModel<EvokerFangs> model = new EvokerFangsModel();

    public EvokerFangsRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(EvokerFangs evokerFangs, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        float i = evokerFangs.getAnimationProgress(h);
        if (i == 0.0f) {
            return;
        }
        float j = 2.0f;
        if (i > 0.9f) {
            j = (float)((double)j * ((1.0 - (double)i) / (double)0.1f));
        }
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0f - evokerFangs.yRot));
        poseStack.scale(-j, -j, j);
        float k = 0.03125f;
        poseStack.translate(0.0, -0.626f, 0.0);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        int l = evokerFangs.getLightColor();
        this.model.setupAnim(evokerFangs, i, 0.0f, 0.0f, evokerFangs.yRot, evokerFangs.xRot, 0.03125f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(TEXTURE_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer, l, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
        super.render(evokerFangs, d, e, f, g, h, poseStack, multiBufferSource);
    }

    @Override
    public ResourceLocation getTextureLocation(EvokerFangs evokerFangs) {
        return TEXTURE_LOCATION;
    }
}

