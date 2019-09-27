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
    public void render(ShulkerBullet shulkerBullet, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        poseStack.pushPose();
        float i = Mth.rotlerp(shulkerBullet.yRotO, shulkerBullet.yRot, h);
        float j = Mth.lerp(h, shulkerBullet.xRotO, shulkerBullet.xRot);
        float k = (float)shulkerBullet.tickCount + h;
        poseStack.translate(0.0, 0.15f, 0.0);
        poseStack.mulPose(Vector3f.YP.rotation(Mth.sin(k * 0.1f) * 180.0f, true));
        poseStack.mulPose(Vector3f.XP.rotation(Mth.cos(k * 0.1f) * 180.0f, true));
        poseStack.mulPose(Vector3f.ZP.rotation(Mth.sin(k * 0.15f) * 360.0f, true));
        float l = 0.03125f;
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        int m = shulkerBullet.getLightColor();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(TEXTURE_LOCATION));
        OverlayTexture.setDefault(vertexConsumer);
        this.model.setupAnim(shulkerBullet, 0.0f, 0.0f, 0.0f, i, j, 0.03125f);
        this.model.renderToBuffer(poseStack, vertexConsumer, m);
        vertexConsumer.unsetDefaultOverlayCoords();
        poseStack.scale(1.5f, 1.5f, 1.5f);
        VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(TEXTURE_LOCATION, true, true, false));
        OverlayTexture.setDefault(vertexConsumer);
        this.model.renderToBuffer(poseStack, vertexConsumer2, m);
        vertexConsumer.unsetDefaultOverlayCoords();
        poseStack.popPose();
        super.render(shulkerBullet, d, e, f, g, h, poseStack, multiBufferSource);
    }

    @Override
    public ResourceLocation getTextureLocation(ShulkerBullet shulkerBullet) {
        return TEXTURE_LOCATION;
    }
}

