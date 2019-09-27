/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

@Environment(value=EnvType.CLIENT)
public class EnderDragonRenderer
extends EntityRenderer<EnderDragon> {
    public static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
    private static final ResourceLocation DRAGON_EXPLODING_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
    private static final ResourceLocation DRAGON_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation DRAGON_EYES_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    private final DragonModel model = new DragonModel(0.0f);

    public EnderDragonRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(EnderDragon enderDragon, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        poseStack.pushPose();
        float i = (float)enderDragon.getLatencyPos(7, h)[0];
        float j = (float)(enderDragon.getLatencyPos(5, h)[1] - enderDragon.getLatencyPos(10, h)[1]);
        poseStack.mulPose(Vector3f.YP.rotation(-i, true));
        poseStack.mulPose(Vector3f.XP.rotation(j * 10.0f, true));
        poseStack.translate(0.0, 0.0, 1.0);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        float k = 0.0625f;
        poseStack.translate(0.0, -1.501f, 0.0);
        boolean bl = enderDragon.hurtTime > 0;
        int l = enderDragon.getLightColor();
        if (enderDragon.dragonDeathTime > 0) {
            float m = (float)enderDragon.dragonDeathTime / 200.0f;
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(DRAGON_EXPLODING_LOCATION, false, true, true, m, false));
            OverlayTexture.setDefault(vertexConsumer);
            this.model.render(poseStack, vertexConsumer, enderDragon, 0.0625f, h, l);
            vertexConsumer.unsetDefaultOverlayCoords();
            VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(DRAGON_LOCATION, false, true, true, 0.1f, true));
            vertexConsumer2.defaultOverlayCoords(OverlayTexture.u(0.0f), OverlayTexture.v(bl));
            this.model.render(poseStack, vertexConsumer2, enderDragon, 0.0625f, h, l);
            vertexConsumer2.unsetDefaultOverlayCoords();
        } else {
            VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(DRAGON_LOCATION, false, true, true));
            vertexConsumer3.defaultOverlayCoords(OverlayTexture.u(0.0f), OverlayTexture.v(bl));
            this.model.render(poseStack, vertexConsumer3, enderDragon, 0.0625f, h, l);
            vertexConsumer3.unsetDefaultOverlayCoords();
        }
        VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(RenderType.EYES(DRAGON_EYES_LOCATION));
        OverlayTexture.setDefault(vertexConsumer3);
        this.model.render(poseStack, vertexConsumer3, enderDragon, 0.0625f, h, l);
        vertexConsumer3.unsetDefaultOverlayCoords();
        if (enderDragon.dragonDeathTime > 0) {
            float n = ((float)enderDragon.dragonDeathTime + h) / 200.0f;
            float o = 0.0f;
            if (n > 0.8f) {
                o = (n - 0.8f) / 0.2f;
            }
            Random random = new Random(432L);
            VertexConsumer vertexConsumer4 = multiBufferSource.getBuffer(RenderType.LIGHTNING);
            poseStack.pushPose();
            poseStack.translate(0.0, -1.0, -2.0);
            int p = 0;
            while ((float)p < (n + n * n) / 2.0f * 60.0f) {
                poseStack.mulPose(Vector3f.XP.rotation(random.nextFloat() * 360.0f, true));
                poseStack.mulPose(Vector3f.YP.rotation(random.nextFloat() * 360.0f, true));
                poseStack.mulPose(Vector3f.ZP.rotation(random.nextFloat() * 360.0f, true));
                poseStack.mulPose(Vector3f.XP.rotation(random.nextFloat() * 360.0f, true));
                poseStack.mulPose(Vector3f.YP.rotation(random.nextFloat() * 360.0f, true));
                poseStack.mulPose(Vector3f.ZP.rotation(random.nextFloat() * 360.0f + n * 90.0f, true));
                float q = random.nextFloat() * 20.0f + 5.0f + o * 10.0f;
                float r = random.nextFloat() * 2.0f + 1.0f + o * 2.0f;
                Matrix4f matrix4f = poseStack.getPose();
                int s = (int)(255.0f * (1.0f - o));
                EnderDragonRenderer.vertex01(vertexConsumer4, matrix4f, s);
                EnderDragonRenderer.vertex2(vertexConsumer4, matrix4f, q, r);
                EnderDragonRenderer.vertex3(vertexConsumer4, matrix4f, q, r);
                EnderDragonRenderer.vertex01(vertexConsumer4, matrix4f, s);
                EnderDragonRenderer.vertex3(vertexConsumer4, matrix4f, q, r);
                EnderDragonRenderer.vertex4(vertexConsumer4, matrix4f, q, r);
                EnderDragonRenderer.vertex01(vertexConsumer4, matrix4f, s);
                EnderDragonRenderer.vertex4(vertexConsumer4, matrix4f, q, r);
                EnderDragonRenderer.vertex2(vertexConsumer4, matrix4f, q, r);
                ++p;
            }
            poseStack.popPose();
        }
        poseStack.popPose();
        if (enderDragon.nearestCrystal != null) {
            poseStack.pushPose();
            float n = (float)(enderDragon.nearestCrystal.x - Mth.lerp((double)h, enderDragon.xo, enderDragon.x));
            float o = (float)(enderDragon.nearestCrystal.y - Mth.lerp((double)h, enderDragon.yo, enderDragon.y));
            float t = (float)(enderDragon.nearestCrystal.z - Mth.lerp((double)h, enderDragon.zo, enderDragon.z));
            EnderDragonRenderer.renderCrystalBeams(n, o + EndCrystalRenderer.getY(enderDragon.nearestCrystal, h), t, h, enderDragon.tickCount, poseStack, multiBufferSource, l);
            poseStack.popPose();
        }
        super.render(enderDragon, d, e, f, g, h, poseStack, multiBufferSource);
    }

    private static void vertex01(VertexConsumer vertexConsumer, Matrix4f matrix4f, int i) {
        vertexConsumer.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color(255, 255, 255, i).endVertex();
        vertexConsumer.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color(255, 255, 255, i).endVertex();
    }

    private static void vertex2(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g) {
        vertexConsumer.vertex(matrix4f, -HALF_SQRT_3 * g, f, -0.5f * g).color(255, 0, 255, 0).endVertex();
    }

    private static void vertex3(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g) {
        vertexConsumer.vertex(matrix4f, HALF_SQRT_3 * g, f, -0.5f * g).color(255, 0, 255, 0).endVertex();
    }

    private static void vertex4(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g) {
        vertexConsumer.vertex(matrix4f, 0.0f, f, 1.0f * g).color(255, 0, 255, 0).endVertex();
    }

    public static void renderCrystalBeams(float f, float g, float h, float i, int j, PoseStack poseStack, MultiBufferSource multiBufferSource, int k) {
        float l = Mth.sqrt(f * f + h * h);
        float m = Mth.sqrt(f * f + g * g + h * h);
        poseStack.pushPose();
        poseStack.translate(0.0, 2.0, 0.0);
        poseStack.mulPose(Vector3f.YP.rotation((float)(-Math.atan2(h, f)) - 1.5707964f, false));
        poseStack.mulPose(Vector3f.XP.rotation((float)(-Math.atan2(l, g)) - 1.5707964f, false));
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(CRYSTAL_BEAM_LOCATION, false, true, true));
        OverlayTexture.setDefault(vertexConsumer);
        float n = 0.0f - ((float)j + i) * 0.01f;
        float o = Mth.sqrt(f * f + g * g + h * h) / 32.0f - ((float)j + i) * 0.01f;
        int p = 8;
        float q = 0.0f;
        float r = 0.75f;
        float s = 0.0f;
        Matrix4f matrix4f = poseStack.getPose();
        for (int t = 1; t <= 8; ++t) {
            float u = Mth.sin((float)(t % 8) * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float v = Mth.cos((float)(t % 8) * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float w = (float)(t % 8) / 8.0f;
            vertexConsumer.vertex(matrix4f, q * 0.2f, r * 0.2f, 0.0f).color(0, 0, 0, 255).uv(s, n).uv2(k).normal(0.0f, 1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, q, r, m).color(255, 255, 255, 255).uv(s, o).uv2(k).normal(0.0f, 1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, u, v, m).color(255, 255, 255, 255).uv(w, o).uv2(k).normal(0.0f, 1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, u * 0.2f, v * 0.2f, 0.0f).color(0, 0, 0, 255).uv(w, n).uv2(k).normal(0.0f, 1.0f, 0.0f).endVertex();
            q = u;
            r = v;
            s = w;
        }
        poseStack.popPose();
        vertexConsumer.unsetDefaultOverlayCoords();
    }

    @Override
    public ResourceLocation getTextureLocation(EnderDragon enderDragon) {
        return DRAGON_LOCATION;
    }

    @Environment(value=EnvType.CLIENT)
    public static class DragonModel
    extends Model {
        private final ModelPart head;
        private final ModelPart neck;
        private final ModelPart jaw;
        private final ModelPart body;
        private final ModelPart rearLeg;
        private final ModelPart frontLeg;
        private final ModelPart rearLegTip;
        private final ModelPart frontLegTip;
        private final ModelPart rearFoot;
        private final ModelPart frontFoot;
        private final ModelPart wing;
        private final ModelPart wingTip;

        public DragonModel(float f) {
            this.texWidth = 256;
            this.texHeight = 256;
            float g = -16.0f;
            this.head = new ModelPart(this);
            this.head.addBox("upperlip", -6.0f, -1.0f, -24.0f, 12, 5, 16, f, 176, 44);
            this.head.addBox("upperhead", -8.0f, -8.0f, -10.0f, 16, 16, 16, f, 112, 30);
            this.head.mirror = true;
            this.head.addBox("scale", -5.0f, -12.0f, -4.0f, 2, 4, 6, f, 0, 0);
            this.head.addBox("nostril", -5.0f, -3.0f, -22.0f, 2, 2, 4, f, 112, 0);
            this.head.mirror = false;
            this.head.addBox("scale", 3.0f, -12.0f, -4.0f, 2, 4, 6, f, 0, 0);
            this.head.addBox("nostril", 3.0f, -3.0f, -22.0f, 2, 2, 4, f, 112, 0);
            this.jaw = new ModelPart(this);
            this.jaw.setPos(0.0f, 4.0f, -8.0f);
            this.jaw.addBox("jaw", -6.0f, 0.0f, -16.0f, 12, 4, 16, f, 176, 65);
            this.head.addChild(this.jaw);
            this.neck = new ModelPart(this);
            this.neck.addBox("box", -5.0f, -5.0f, -5.0f, 10, 10, 10, f, 192, 104);
            this.neck.addBox("scale", -1.0f, -9.0f, -3.0f, 2, 4, 6, f, 48, 0);
            this.body = new ModelPart(this);
            this.body.setPos(0.0f, 4.0f, 8.0f);
            this.body.addBox("body", -12.0f, 0.0f, -16.0f, 24, 24, 64, f, 0, 0);
            this.body.addBox("scale", -1.0f, -6.0f, -10.0f, 2, 6, 12, f, 220, 53);
            this.body.addBox("scale", -1.0f, -6.0f, 10.0f, 2, 6, 12, f, 220, 53);
            this.body.addBox("scale", -1.0f, -6.0f, 30.0f, 2, 6, 12, f, 220, 53);
            this.wing = new ModelPart(this);
            this.wing.setPos(-12.0f, 5.0f, 2.0f);
            this.wing.addBox("bone", -56.0f, -4.0f, -4.0f, 56, 8, 8, f, 112, 88);
            this.wing.addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, f, -56, 88);
            this.wingTip = new ModelPart(this);
            this.wingTip.setPos(-56.0f, 0.0f, 0.0f);
            this.wingTip.addBox("bone", -56.0f, -2.0f, -2.0f, 56, 4, 4, f, 112, 136);
            this.wingTip.addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, f, -56, 144);
            this.wing.addChild(this.wingTip);
            this.frontLeg = new ModelPart(this);
            this.frontLeg.setPos(-12.0f, 20.0f, 2.0f);
            this.frontLeg.addBox("main", -4.0f, -4.0f, -4.0f, 8, 24, 8, f, 112, 104);
            this.frontLegTip = new ModelPart(this);
            this.frontLegTip.setPos(0.0f, 20.0f, -1.0f);
            this.frontLegTip.addBox("main", -3.0f, -1.0f, -3.0f, 6, 24, 6, f, 226, 138);
            this.frontLeg.addChild(this.frontLegTip);
            this.frontFoot = new ModelPart(this);
            this.frontFoot.setPos(0.0f, 23.0f, 0.0f);
            this.frontFoot.addBox("main", -4.0f, 0.0f, -12.0f, 8, 4, 16, f, 144, 104);
            this.frontLegTip.addChild(this.frontFoot);
            this.rearLeg = new ModelPart(this);
            this.rearLeg.setPos(-16.0f, 16.0f, 42.0f);
            this.rearLeg.addBox("main", -8.0f, -4.0f, -8.0f, 16, 32, 16, f, 0, 0);
            this.rearLegTip = new ModelPart(this);
            this.rearLegTip.setPos(0.0f, 32.0f, -4.0f);
            this.rearLegTip.addBox("main", -6.0f, -2.0f, 0.0f, 12, 32, 12, f, 196, 0);
            this.rearLeg.addChild(this.rearLegTip);
            this.rearFoot = new ModelPart(this);
            this.rearFoot.setPos(0.0f, 31.0f, 4.0f);
            this.rearFoot.addBox("main", -9.0f, 0.0f, -20.0f, 18, 6, 24, f, 112, 0);
            this.rearLegTip.addChild(this.rearFoot);
        }

        public void render(PoseStack poseStack, VertexConsumer vertexConsumer, EnderDragon enderDragon, float f, float g, int i) {
            float s;
            poseStack.pushPose();
            float h = Mth.lerp(g, enderDragon.oFlapTime, enderDragon.flapTime);
            this.jaw.xRot = (float)(Math.sin(h * ((float)Math.PI * 2)) + 1.0) * 0.2f;
            float j = (float)(Math.sin(h * ((float)Math.PI * 2) - 1.0f) + 1.0);
            j = (j * j + j * 2.0f) * 0.05f;
            poseStack.translate(0.0, j - 2.0f, -3.0);
            poseStack.mulPose(Vector3f.XP.rotation(j * 2.0f, true));
            float k = 0.0f;
            float l = 20.0f;
            float m = -12.0f;
            float n = 1.5f;
            double[] ds = enderDragon.getLatencyPos(6, g);
            float o = Mth.rotWrap(enderDragon.getLatencyPos(5, g)[0] - enderDragon.getLatencyPos(10, g)[0]);
            float p = Mth.rotWrap(enderDragon.getLatencyPos(5, g)[0] + (double)(o / 2.0f));
            float q = h * ((float)Math.PI * 2);
            for (int r = 0; r < 5; ++r) {
                double[] es = enderDragon.getLatencyPos(5 - r, g);
                s = (float)Math.cos((float)r * 0.45f + q) * 0.15f;
                this.neck.yRot = Mth.rotWrap(es[0] - ds[0]) * ((float)Math.PI / 180) * 1.5f;
                this.neck.xRot = s + enderDragon.getHeadPartYOffset(r, ds, es) * ((float)Math.PI / 180) * 1.5f * 5.0f;
                this.neck.zRot = -Mth.rotWrap(es[0] - (double)p) * ((float)Math.PI / 180) * 1.5f;
                this.neck.y = l;
                this.neck.z = m;
                this.neck.x = k;
                l = (float)((double)l + Math.sin(this.neck.xRot) * 10.0);
                m = (float)((double)m - Math.cos(this.neck.yRot) * Math.cos(this.neck.xRot) * 10.0);
                k = (float)((double)k - Math.sin(this.neck.yRot) * Math.cos(this.neck.xRot) * 10.0);
                this.neck.render(poseStack, vertexConsumer, f, i, null);
            }
            this.head.y = l;
            this.head.z = m;
            this.head.x = k;
            double[] fs = enderDragon.getLatencyPos(0, g);
            this.head.yRot = Mth.rotWrap(fs[0] - ds[0]) * ((float)Math.PI / 180);
            this.head.xRot = Mth.rotWrap(enderDragon.getHeadPartYOffset(6, ds, fs)) * ((float)Math.PI / 180) * 1.5f * 5.0f;
            this.head.zRot = -Mth.rotWrap(fs[0] - (double)p) * ((float)Math.PI / 180);
            this.head.render(poseStack, vertexConsumer, f, i, null);
            poseStack.pushPose();
            poseStack.translate(0.0, 1.0, 0.0);
            poseStack.mulPose(Vector3f.ZP.rotation(-o * 1.5f, true));
            poseStack.translate(0.0, -1.0, 0.0);
            this.body.zRot = 0.0f;
            this.body.render(poseStack, vertexConsumer, f, i, null);
            for (int t = 0; t < 2; ++t) {
                s = h * ((float)Math.PI * 2);
                this.wing.xRot = 0.125f - (float)Math.cos(s) * 0.2f;
                this.wing.yRot = 0.25f;
                this.wing.zRot = (float)(Math.sin(s) + 0.125) * 0.8f;
                this.wingTip.zRot = -((float)(Math.sin(s + 2.0f) + 0.5)) * 0.75f;
                this.rearLeg.xRot = 1.0f + j * 0.1f;
                this.rearLegTip.xRot = 0.5f + j * 0.1f;
                this.rearFoot.xRot = 0.75f + j * 0.1f;
                this.frontLeg.xRot = 1.3f + j * 0.1f;
                this.frontLegTip.xRot = -0.5f - j * 0.1f;
                this.frontFoot.xRot = 0.75f + j * 0.1f;
                this.wing.render(poseStack, vertexConsumer, f, i, null);
                this.frontLeg.render(poseStack, vertexConsumer, f, i, null);
                this.rearLeg.render(poseStack, vertexConsumer, f, i, null);
                poseStack.scale(-1.0f, 1.0f, 1.0f);
            }
            poseStack.popPose();
            float u = -((float)Math.sin(h * ((float)Math.PI * 2))) * 0.0f;
            q = h * ((float)Math.PI * 2);
            l = 10.0f;
            m = 60.0f;
            k = 0.0f;
            ds = enderDragon.getLatencyPos(11, g);
            for (int v = 0; v < 12; ++v) {
                fs = enderDragon.getLatencyPos(12 + v, g);
                u = (float)((double)u + Math.sin((float)v * 0.45f + q) * (double)0.05f);
                this.neck.yRot = (Mth.rotWrap(fs[0] - ds[0]) * 1.5f + 180.0f) * ((float)Math.PI / 180);
                this.neck.xRot = u + (float)(fs[1] - ds[1]) * ((float)Math.PI / 180) * 1.5f * 5.0f;
                this.neck.zRot = Mth.rotWrap(fs[0] - (double)p) * ((float)Math.PI / 180) * 1.5f;
                this.neck.y = l;
                this.neck.z = m;
                this.neck.x = k;
                l = (float)((double)l + Math.sin(this.neck.xRot) * 10.0);
                m = (float)((double)m - Math.cos(this.neck.yRot) * Math.cos(this.neck.xRot) * 10.0);
                k = (float)((double)k - Math.sin(this.neck.yRot) * Math.cos(this.neck.xRot) * 10.0);
                this.neck.render(poseStack, vertexConsumer, f, i, null);
            }
            poseStack.popPose();
        }
    }
}

