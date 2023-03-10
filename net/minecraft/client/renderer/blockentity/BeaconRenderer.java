/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class BeaconRenderer
implements BlockEntityRenderer<BeaconBlockEntity> {
    public static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");
    public static final int MAX_RENDER_Y = 1024;

    public BeaconRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BeaconBlockEntity beaconBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        long l = beaconBlockEntity.getLevel().getGameTime();
        List<BeaconBlockEntity.BeaconBeamSection> list = beaconBlockEntity.getBeamSections();
        int k = 0;
        for (int m = 0; m < list.size(); ++m) {
            BeaconBlockEntity.BeaconBeamSection beaconBeamSection = list.get(m);
            BeaconRenderer.renderBeaconBeam(poseStack, multiBufferSource, f, l, k, m == list.size() - 1 ? 1024 : beaconBeamSection.getHeight(), beaconBeamSection.getColor());
            k += beaconBeamSection.getHeight();
        }
    }

    private static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource multiBufferSource, float f, long l, int i, int j, float[] fs) {
        BeaconRenderer.renderBeaconBeam(poseStack, multiBufferSource, BEAM_LOCATION, f, 1.0f, l, i, j, fs, 0.2f, 0.25f);
    }

    public static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource multiBufferSource, ResourceLocation resourceLocation, float f, float g, long l, int i, int j, float[] fs, float h, float k) {
        int m = i + j;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float n = (float)Math.floorMod(l, 40) + f;
        float o = j < 0 ? n : -n;
        float p = Mth.frac(o * 0.2f - (float)Mth.floor(o * 0.1f));
        float q = fs[0];
        float r = fs[1];
        float s = fs[2];
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(n * 2.25f - 45.0f));
        float t = 0.0f;
        float u = h;
        float v = h;
        float w = 0.0f;
        float x = -h;
        float y = 0.0f;
        float z = 0.0f;
        float aa = -h;
        float ab = 0.0f;
        float ac = 1.0f;
        float ad = -1.0f + p;
        float ae = (float)j * g * (0.5f / h) + ad;
        BeaconRenderer.renderPart(poseStack, multiBufferSource.getBuffer(RenderType.beaconBeam(resourceLocation, false)), q, r, s, 1.0f, i, m, 0.0f, u, v, 0.0f, x, 0.0f, 0.0f, aa, 0.0f, 1.0f, ae, ad);
        poseStack.popPose();
        t = -k;
        u = -k;
        v = k;
        w = -k;
        x = -k;
        y = k;
        z = k;
        aa = k;
        ab = 0.0f;
        ac = 1.0f;
        ad = -1.0f + p;
        ae = (float)j * g + ad;
        BeaconRenderer.renderPart(poseStack, multiBufferSource.getBuffer(RenderType.beaconBeam(resourceLocation, true)), q, r, s, 0.125f, i, m, t, u, v, w, x, y, z, aa, 0.0f, 1.0f, ae, ad);
        poseStack.popPose();
    }

    private static void renderPart(PoseStack poseStack, VertexConsumer vertexConsumer, float f, float g, float h, float i, int j, int k, float l, float m, float n, float o, float p, float q, float r, float s, float t, float u, float v, float w) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        BeaconRenderer.renderQuad(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, l, m, n, o, t, u, v, w);
        BeaconRenderer.renderQuad(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, r, s, p, q, t, u, v, w);
        BeaconRenderer.renderQuad(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, n, o, r, s, t, u, v, w);
        BeaconRenderer.renderQuad(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, p, q, l, m, t, u, v, w);
    }

    private static void renderQuad(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float f, float g, float h, float i, int j, int k, float l, float m, float n, float o, float p, float q, float r, float s) {
        BeaconRenderer.addVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, k, l, m, q, r);
        BeaconRenderer.addVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, l, m, q, s);
        BeaconRenderer.addVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, n, o, p, s);
        BeaconRenderer.addVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, k, n, o, p, r);
    }

    private static void addVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float f, float g, float h, float i, int j, float k, float l, float m, float n) {
        vertexConsumer.vertex(matrix4f, k, j, l).color(f, g, h, i).uv(m, n).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(BeaconBlockEntity beaconBlockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(BeaconBlockEntity beaconBlockEntity, Vec3 vec3) {
        return Vec3.atCenterOf(beaconBlockEntity.getBlockPos()).multiply(1.0, 0.0, 1.0).closerThan(vec3.multiply(1.0, 0.0, 1.0), this.getViewDistance());
    }
}

