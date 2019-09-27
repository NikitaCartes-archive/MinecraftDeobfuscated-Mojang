/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
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
    protected void renderStuckItem(PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity, float f, float g, float h, float i) {
        float j = Mth.sqrt(f * f + h * h);
        float k = (float)(Math.atan2(f, h) * 57.2957763671875);
        float l = (float)(Math.atan2(g, j) * 57.2957763671875);
        poseStack.translate(0.0, 0.0, 0.0);
        poseStack.mulPose(Vector3f.YP.rotation(k - 90.0f, true));
        poseStack.mulPose(Vector3f.ZP.rotation(l, true));
        float m = 0.0f;
        float n = 0.125f;
        float o = 0.0f;
        float p = 0.0625f;
        float q = 0.03125f;
        poseStack.mulPose(Vector3f.XP.rotation(45.0f, true));
        poseStack.scale(0.03125f, 0.03125f, 0.03125f);
        poseStack.translate(2.5, 0.0, 0.0);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(BEE_STINGER_LOCATION));
        OverlayTexture.setDefault(vertexConsumer);
        for (int r = 0; r < 4; ++r) {
            poseStack.mulPose(Vector3f.XP.rotation(90.0f, true));
            Matrix4f matrix4f = poseStack.getPose();
            vertexConsumer.vertex(matrix4f, -4.5f, -1.0f, 0.0f).uv(0.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, 4.5f, -1.0f, 0.0f).uv(0.125f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, 4.5f, 1.0f, 0.0f).uv(0.125f, 0.0625f).endVertex();
            vertexConsumer.vertex(matrix4f, -4.5f, 1.0f, 0.0f).uv(0.0f, 0.0625f).endVertex();
        }
        vertexConsumer.unsetDefaultOverlayCoords();
    }
}

