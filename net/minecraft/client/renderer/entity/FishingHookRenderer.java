/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class FishingHookRenderer
extends EntityRenderer<FishingHook> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
    private static final double VIEW_BOBBING_SCALE = 960.0;

    public FishingHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FishingHook fishingHook, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        double s;
        float r;
        double q;
        double p;
        double o;
        Player player = fishingHook.getPlayerOwner();
        if (player == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
        FishingHookRenderer.vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0f, 0, 0, 1);
        FishingHookRenderer.vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0f, 0, 1, 1);
        FishingHookRenderer.vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0f, 1, 1, 0);
        FishingHookRenderer.vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0f, 1, 0, 0);
        poseStack.popPose();
        int j = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        ItemStack itemStack = player.getMainHandItem();
        if (!itemStack.is(Items.FISHING_ROD)) {
            j = -j;
        }
        float h = player.getAttackAnim(g);
        float k = Mth.sin(Mth.sqrt(h) * (float)Math.PI);
        float l = Mth.lerp(g, player.yBodyRotO, player.yBodyRot) * ((float)Math.PI / 180);
        double d = Mth.sin(l);
        double e = Mth.cos(l);
        double m = (double)j * 0.35;
        double n = 0.8;
        if (this.entityRenderDispatcher.options != null && !this.entityRenderDispatcher.options.getCameraType().isFirstPerson() || player != Minecraft.getInstance().player) {
            o = Mth.lerp((double)g, player.xo, player.getX()) - e * m - d * 0.8;
            p = player.yo + (double)player.getEyeHeight() + (player.getY() - player.yo) * (double)g - 0.45;
            q = Mth.lerp((double)g, player.zo, player.getZ()) - d * m + e * 0.8;
            r = player.isCrouching() ? -0.1875f : 0.0f;
        } else {
            s = 960.0 / (double)this.entityRenderDispatcher.options.fov().get().intValue();
            Vec3 vec3 = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float)j * 0.525f, -0.1f);
            vec3 = vec3.scale(s);
            vec3 = vec3.yRot(k * 0.5f);
            vec3 = vec3.xRot(-k * 0.7f);
            o = Mth.lerp((double)g, player.xo, player.getX()) + vec3.x;
            p = Mth.lerp((double)g, player.yo, player.getY()) + vec3.y;
            q = Mth.lerp((double)g, player.zo, player.getZ()) + vec3.z;
            r = player.getEyeHeight();
        }
        s = Mth.lerp((double)g, fishingHook.xo, fishingHook.getX());
        double t = Mth.lerp((double)g, fishingHook.yo, fishingHook.getY()) + 0.25;
        double u = Mth.lerp((double)g, fishingHook.zo, fishingHook.getZ());
        float v = (float)(o - s);
        float w = (float)(p - t) + r;
        float x = (float)(q - u);
        VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.lineStrip());
        PoseStack.Pose pose2 = poseStack.last();
        int y = 16;
        for (int z = 0; z <= 16; ++z) {
            FishingHookRenderer.stringVertex(v, w, x, vertexConsumer2, pose2, FishingHookRenderer.fraction(z, 16), FishingHookRenderer.fraction(z + 1, 16));
        }
        poseStack.popPose();
        super.render(fishingHook, f, g, poseStack, multiBufferSource, i);
    }

    private static float fraction(int i, int j) {
        return (float)i / (float)j;
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, int i, float f, int j, int k, int l) {
        vertexConsumer.vertex(matrix4f, f - 0.5f, (float)j - 0.5f, 0.0f).color(255, 255, 255, 255).uv(k, l).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(i).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
    }

    private static void stringVertex(float f, float g, float h, VertexConsumer vertexConsumer, PoseStack.Pose pose, float i, float j) {
        float k = f * i;
        float l = g * (i * i + i) * 0.5f + 0.25f;
        float m = h * i;
        float n = f * j - k;
        float o = g * (j * j + j) * 0.5f + 0.25f - l;
        float p = h * j - m;
        float q = Mth.sqrt(n * n + o * o + p * p);
        vertexConsumer.vertex(pose.pose(), k, l, m).color(0, 0, 0, 255).normal(pose.normal(), n /= q, o /= q, p /= q).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(FishingHook fishingHook) {
        return TEXTURE_LOCATION;
    }
}

