/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class BeeStingerLayer<T extends LivingEntity, M extends PlayerModel<T>>
extends StuckInBodyLayer<T, M> {
    private static final ResourceLocation BEE_STINGER_LOCATION = new ResourceLocation("textures/entity/bee/bee_stinger.png");

    public BeeStingerLayer(LivingEntityRenderer<T, M> livingEntityRenderer) {
        super(livingEntityRenderer);
    }

    @Override
    protected int numStuck(T livingEntity) {
        return ((LivingEntity)livingEntity).getStingerCount();
    }

    @Override
    protected void renderStuckItem(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Entity entity, float f, float g, float h, float j) {
        float k = Mth.sqrt(f * f + h * h);
        float l = (float)(Math.atan2(f, h) * 57.2957763671875);
        float m = (float)(Math.atan2(g, k) * 57.2957763671875);
        poseStack.translate(0.0f, 0.0f, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(l - 90.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(m));
        float n = 0.0f;
        float o = 0.125f;
        float p = 0.0f;
        float q = 0.0625f;
        float r = 0.03125f;
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0f));
        poseStack.scale(0.03125f, 0.03125f, 0.03125f);
        poseStack.translate(2.5f, 0.0f, 0.0f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(BEE_STINGER_LOCATION));
        for (int s = 0; s < 4; ++s) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix4f = pose.pose();
            Matrix3f matrix3f = pose.normal();
            BeeStingerLayer.vertex(vertexConsumer, matrix4f, matrix3f, -4.5f, -1, 0.0f, 0.0f, i);
            BeeStingerLayer.vertex(vertexConsumer, matrix4f, matrix3f, 4.5f, -1, 0.125f, 0.0f, i);
            BeeStingerLayer.vertex(vertexConsumer, matrix4f, matrix3f, 4.5f, 1, 0.125f, 0.0625f, i);
            BeeStingerLayer.vertex(vertexConsumer, matrix4f, matrix3f, -4.5f, 1, 0.0f, 0.0625f, i);
        }
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, float f, int i, float g, float h, int j) {
        vertexConsumer.vertex(matrix4f, f, i, 0.0f).color(255, 255, 255, 255).uv(g, h).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(j).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
    }
}

