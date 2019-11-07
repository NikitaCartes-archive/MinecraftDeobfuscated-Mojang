/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;

@Environment(value=EnvType.CLIENT)
public class ShulkerBulletRenderer
extends EntityRenderer<ShulkerBullet> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/shulker/spark.png");
    private final ShulkerBulletModel<ShulkerBullet> model = new ShulkerBulletModel();

    public ShulkerBulletRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(ShulkerBullet shulkerBullet, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        float h = Mth.rotlerp(shulkerBullet.yRotO, shulkerBullet.yRot, g);
        float j = Mth.lerp(g, shulkerBullet.xRotO, shulkerBullet.xRot);
        float k = (float)shulkerBullet.tickCount + g;
        poseStack.translate(0.0, 0.15f, 0.0);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.sin(k * 0.1f) * 180.0f));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.cos(k * 0.1f) * 180.0f));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.sin(k * 0.15f) * 360.0f));
        poseStack.scale(-0.5f, -0.5f, 0.5f);
        this.model.setupAnim(shulkerBullet, 0.0f, 0.0f, 0.0f, h, j);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(TEXTURE_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f);
        poseStack.scale(1.5f, 1.5f, 1.5f);
        VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.entityForceTranslucent(TEXTURE_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer2, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
        super.render(shulkerBullet, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(ShulkerBullet shulkerBullet) {
        return TEXTURE_LOCATION;
    }
}

