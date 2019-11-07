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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.fishing.FishingHook;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class FishingHookRenderer
extends EntityRenderer<FishingHook> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");

    public FishingHookRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(FishingHook fishingHook, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        double t;
        float s;
        double r;
        double q;
        double p;
        Player player = fishingHook.getOwner();
        if (player == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f - this.entityRenderDispatcher.playerRotY));
        float h = (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX;
        poseStack.mulPose(Vector3f.XP.rotationDegrees(h));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(TEXTURE_LOCATION));
        FishingHookRenderer.vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0f, 0, 0, 1);
        FishingHookRenderer.vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0f, 0, 1, 1);
        FishingHookRenderer.vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0f, 1, 1, 0);
        FishingHookRenderer.vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0f, 1, 0, 0);
        poseStack.popPose();
        int j = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() != Items.FISHING_ROD) {
            j = -j;
        }
        float k = player.getAttackAnim(g);
        float l = Mth.sin(Mth.sqrt(k) * (float)Math.PI);
        float m = Mth.lerp(g, player.yBodyRotO, player.yBodyRot) * ((float)Math.PI / 180);
        double d = Mth.sin(m);
        double e = Mth.cos(m);
        double n = (double)j * 0.35;
        double o = 0.8;
        if (this.entityRenderDispatcher.options != null && this.entityRenderDispatcher.options.thirdPersonView > 0 || player != Minecraft.getInstance().player) {
            p = Mth.lerp((double)g, player.xo, player.getX()) - e * n - d * 0.8;
            q = player.yo + (double)player.getEyeHeight() + (player.getY() - player.yo) * (double)g - 0.45;
            r = Mth.lerp((double)g, player.zo, player.getZ()) - d * n + e * 0.8;
            s = player.isCrouching() ? -0.1875f : 0.0f;
        } else {
            t = this.entityRenderDispatcher.options.fov;
            Vec3 vec3 = new Vec3((double)j * -0.36 * (t /= 100.0), -0.045 * t, 0.4);
            vec3 = vec3.xRot(-Mth.lerp(g, player.xRotO, player.xRot) * ((float)Math.PI / 180));
            vec3 = vec3.yRot(-Mth.lerp(g, player.yRotO, player.yRot) * ((float)Math.PI / 180));
            vec3 = vec3.yRot(l * 0.5f);
            vec3 = vec3.xRot(-l * 0.7f);
            p = Mth.lerp((double)g, player.xo, player.getX()) + vec3.x;
            q = Mth.lerp((double)g, player.yo, player.getY()) + vec3.y;
            r = Mth.lerp((double)g, player.zo, player.getZ()) + vec3.z;
            s = player.getEyeHeight();
        }
        t = Mth.lerp((double)g, fishingHook.xo, fishingHook.getX());
        double u = Mth.lerp((double)g, fishingHook.yo, fishingHook.getY()) + 0.25;
        double v = Mth.lerp((double)g, fishingHook.zo, fishingHook.getZ());
        float w = (float)(p - t);
        float x = (float)(q - u) + s;
        float y = (float)(r - v);
        VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix4f2 = poseStack.last().pose();
        int z = 16;
        for (int aa = 0; aa < 16; ++aa) {
            FishingHookRenderer.stringVertex(w, x, y, vertexConsumer2, matrix4f2, aa / 16);
            FishingHookRenderer.stringVertex(w, x, y, vertexConsumer2, matrix4f2, (aa + 1) / 16);
        }
        poseStack.popPose();
        super.render(fishingHook, f, g, poseStack, multiBufferSource, i);
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, int i, float f, int j, int k, int l) {
        vertexConsumer.vertex(matrix4f, f - 0.5f, (float)j - 0.5f, 0.0f).color(255, 255, 255, 255).uv(k, l).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(i).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
    }

    private static void stringVertex(float f, float g, float h, VertexConsumer vertexConsumer, Matrix4f matrix4f, float i) {
        vertexConsumer.vertex(matrix4f, f * i, g * (i * i + i) * 0.5f + 0.25f, h * i).color(0, 0, 0, 255).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(FishingHook fishingHook) {
        return TEXTURE_LOCATION;
    }
}

