/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.DragonFireball;

@Environment(value=EnvType.CLIENT)
public class DragonFireballRenderer
extends EntityRenderer<DragonFireball> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);

    public DragonFireballRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(DragonFireball dragonFireball, BlockPos blockPos) {
        return 15;
    }

    @Override
    public void render(DragonFireball dragonFireball, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        poseStack.scale(2.0f, 2.0f, 2.0f);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
        DragonFireballRenderer.vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0f, 0, 0, 1);
        DragonFireballRenderer.vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0f, 0, 1, 1);
        DragonFireballRenderer.vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0f, 1, 1, 0);
        DragonFireballRenderer.vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0f, 1, 0, 0);
        poseStack.popPose();
        super.render(dragonFireball, f, g, poseStack, multiBufferSource, i);
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, int i, float f, int j, int k, int l) {
        vertexConsumer.vertex(matrix4f, f - 0.5f, (float)j - 0.25f, 0.0f).color(255, 255, 255, 255).uv(k, l).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(i).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(DragonFireball dragonFireball) {
        return TEXTURE_LOCATION;
    }
}

