/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;

@Environment(value=EnvType.CLIENT)
public class ShulkerBulletRenderer
extends EntityRenderer<ShulkerBullet> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/shulker/spark.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE_LOCATION);
    private final ShulkerBulletModel<ShulkerBullet> model;

    public ShulkerBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ShulkerBulletModel(context.bakeLayer(ModelLayers.SHULKER_BULLET));
    }

    @Override
    protected int getBlockLightLevel(ShulkerBullet shulkerBullet, BlockPos blockPos) {
        return 15;
    }

    @Override
    public void render(ShulkerBullet shulkerBullet, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        float h = Mth.rotLerp(g, shulkerBullet.yRotO, shulkerBullet.getYRot());
        float j = Mth.lerp(g, shulkerBullet.xRotO, shulkerBullet.getXRot());
        float k = (float)shulkerBullet.tickCount + g;
        poseStack.translate(0.0f, 0.15f, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(k * 0.1f) * 180.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.cos(k * 0.1f) * 180.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(k * 0.15f) * 360.0f));
        poseStack.scale(-0.5f, -0.5f, 0.5f);
        this.model.setupAnim(shulkerBullet, 0.0f, 0.0f, 0.0f, h, j);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(TEXTURE_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.scale(1.5f, 1.5f, 1.5f);
        VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RENDER_TYPE);
        this.model.renderToBuffer(poseStack, vertexConsumer2, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 0.15f);
        poseStack.popPose();
        super.render(shulkerBullet, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(ShulkerBullet shulkerBullet) {
        return TEXTURE_LOCATION;
    }
}

