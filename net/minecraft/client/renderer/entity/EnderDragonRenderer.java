/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
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
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EnderDragonRenderer
extends EntityRenderer<EnderDragon> {
    public static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
    private static final ResourceLocation DRAGON_EXPLODING_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
    private static final ResourceLocation DRAGON_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation DRAGON_EYES_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    private final DragonModel model = new DragonModel();

    public EnderDragonRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(EnderDragon enderDragon, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        float h = (float)enderDragon.getLatencyPos(7, g)[0];
        float j = (float)(enderDragon.getLatencyPos(5, g)[1] - enderDragon.getLatencyPos(10, g)[1]);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-h));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(j * 10.0f));
        poseStack.translate(0.0, 0.0, 1.0);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0, -1.501f, 0.0);
        boolean bl = enderDragon.hurtTime > 0;
        this.model.prepareMobModel(enderDragon, 0.0f, 0.0f, g);
        if (enderDragon.dragonDeathTime > 0) {
            float k = (float)enderDragon.dragonDeathTime / 200.0f;
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityAlpha(DRAGON_EXPLODING_LOCATION, k));
            this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f);
            VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.entityDecal(DRAGON_LOCATION));
            this.model.renderToBuffer(poseStack, vertexConsumer2, i, OverlayTexture.pack(0.0f, bl), 1.0f, 1.0f, 1.0f);
        } else {
            VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(this.model.renderType(DRAGON_LOCATION));
            this.model.renderToBuffer(poseStack, vertexConsumer3, i, OverlayTexture.pack(0.0f, bl), 1.0f, 1.0f, 1.0f);
        }
        VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(RenderType.eyes(DRAGON_EYES_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer3, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f);
        if (enderDragon.dragonDeathTime > 0) {
            float l = ((float)enderDragon.dragonDeathTime + g) / 200.0f;
            float m = 0.0f;
            if (l > 0.8f) {
                m = (l - 0.8f) / 0.2f;
            }
            Random random = new Random(432L);
            VertexConsumer vertexConsumer4 = multiBufferSource.getBuffer(RenderType.lightning());
            poseStack.pushPose();
            poseStack.translate(0.0, -1.0, -2.0);
            int n = 0;
            while ((float)n < (l + l * l) / 2.0f * 60.0f) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0f));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0f));
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0f));
                poseStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0f));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0f));
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0f + l * 90.0f));
                float o = random.nextFloat() * 20.0f + 5.0f + m * 10.0f;
                float p = random.nextFloat() * 2.0f + 1.0f + m * 2.0f;
                Matrix4f matrix4f = poseStack.last().pose();
                int q = (int)(255.0f * (1.0f - m));
                EnderDragonRenderer.vertex01(vertexConsumer4, matrix4f, q);
                EnderDragonRenderer.vertex2(vertexConsumer4, matrix4f, o, p);
                EnderDragonRenderer.vertex3(vertexConsumer4, matrix4f, o, p);
                EnderDragonRenderer.vertex01(vertexConsumer4, matrix4f, q);
                EnderDragonRenderer.vertex3(vertexConsumer4, matrix4f, o, p);
                EnderDragonRenderer.vertex4(vertexConsumer4, matrix4f, o, p);
                EnderDragonRenderer.vertex01(vertexConsumer4, matrix4f, q);
                EnderDragonRenderer.vertex4(vertexConsumer4, matrix4f, o, p);
                EnderDragonRenderer.vertex2(vertexConsumer4, matrix4f, o, p);
                ++n;
            }
            poseStack.popPose();
        }
        poseStack.popPose();
        if (enderDragon.nearestCrystal != null) {
            poseStack.pushPose();
            float l = (float)(enderDragon.nearestCrystal.getX() - Mth.lerp((double)g, enderDragon.xo, enderDragon.getX()));
            float m = (float)(enderDragon.nearestCrystal.getY() - Mth.lerp((double)g, enderDragon.yo, enderDragon.getY()));
            float r = (float)(enderDragon.nearestCrystal.getZ() - Mth.lerp((double)g, enderDragon.zo, enderDragon.getZ()));
            EnderDragonRenderer.renderCrystalBeams(l, m + EndCrystalRenderer.getY(enderDragon.nearestCrystal, g), r, g, enderDragon.tickCount, poseStack, multiBufferSource, i);
            poseStack.popPose();
        }
        super.render(enderDragon, f, g, poseStack, multiBufferSource, i);
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
        poseStack.mulPose(Vector3f.YP.rotation((float)(-Math.atan2(h, f)) - 1.5707964f));
        poseStack.mulPose(Vector3f.XP.rotation((float)(-Math.atan2(l, g)) - 1.5707964f));
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION));
        float n = 0.0f - ((float)j + i) * 0.01f;
        float o = Mth.sqrt(f * f + g * g + h * h) / 32.0f - ((float)j + i) * 0.01f;
        int p = 8;
        float q = 0.0f;
        float r = 0.75f;
        float s = 0.0f;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        for (int t = 1; t <= 8; ++t) {
            float u = Mth.sin((float)(t % 8) * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float v = Mth.cos((float)(t % 8) * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float w = (float)(t % 8) / 8.0f;
            vertexConsumer.vertex(matrix4f, q * 0.2f, r * 0.2f, 0.0f).color(0, 0, 0, 255).uv(s, n).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(k).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, q, r, m).color(255, 255, 255, 255).uv(s, o).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(k).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, u, v, m).color(255, 255, 255, 255).uv(w, o).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(k).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, u * 0.2f, v * 0.2f, 0.0f).color(0, 0, 0, 255).uv(w, n).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(k).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
            q = u;
            r = v;
            s = w;
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EnderDragon enderDragon) {
        return DRAGON_LOCATION;
    }

    @Environment(value=EnvType.CLIENT)
    public static class DragonModel
    extends EntityModel<EnderDragon> {
        private final ModelPart head;
        private final ModelPart neck;
        private final ModelPart jaw;
        private final ModelPart body;
        private ModelPart leftWing;
        private ModelPart leftWingTip;
        private ModelPart leftFrontLeg;
        private ModelPart leftFrontLegTip;
        private ModelPart leftFrontFoot;
        private ModelPart leftRearLeg;
        private ModelPart leftRearLegTip;
        private ModelPart leftRearFoot;
        private ModelPart rightWing;
        private ModelPart rightWingTip;
        private ModelPart rightFrontLeg;
        private ModelPart rightFrontLegTip;
        private ModelPart rightFrontFoot;
        private ModelPart rightRearLeg;
        private ModelPart rightRearLegTip;
        private ModelPart rightRearFoot;
        @Nullable
        private EnderDragon entity;
        private float a;

        public DragonModel() {
            this.texWidth = 256;
            this.texHeight = 256;
            float f = -16.0f;
            this.head = new ModelPart(this);
            this.head.addBox("upperlip", -6.0f, -1.0f, -24.0f, 12, 5, 16, 0.0f, 176, 44);
            this.head.addBox("upperhead", -8.0f, -8.0f, -10.0f, 16, 16, 16, 0.0f, 112, 30);
            this.head.mirror = true;
            this.head.addBox("scale", -5.0f, -12.0f, -4.0f, 2, 4, 6, 0.0f, 0, 0);
            this.head.addBox("nostril", -5.0f, -3.0f, -22.0f, 2, 2, 4, 0.0f, 112, 0);
            this.head.mirror = false;
            this.head.addBox("scale", 3.0f, -12.0f, -4.0f, 2, 4, 6, 0.0f, 0, 0);
            this.head.addBox("nostril", 3.0f, -3.0f, -22.0f, 2, 2, 4, 0.0f, 112, 0);
            this.jaw = new ModelPart(this);
            this.jaw.setPos(0.0f, 4.0f, -8.0f);
            this.jaw.addBox("jaw", -6.0f, 0.0f, -16.0f, 12, 4, 16, 0.0f, 176, 65);
            this.head.addChild(this.jaw);
            this.neck = new ModelPart(this);
            this.neck.addBox("box", -5.0f, -5.0f, -5.0f, 10, 10, 10, 0.0f, 192, 104);
            this.neck.addBox("scale", -1.0f, -9.0f, -3.0f, 2, 4, 6, 0.0f, 48, 0);
            this.body = new ModelPart(this);
            this.body.setPos(0.0f, 4.0f, 8.0f);
            this.body.addBox("body", -12.0f, 0.0f, -16.0f, 24, 24, 64, 0.0f, 0, 0);
            this.body.addBox("scale", -1.0f, -6.0f, -10.0f, 2, 6, 12, 0.0f, 220, 53);
            this.body.addBox("scale", -1.0f, -6.0f, 10.0f, 2, 6, 12, 0.0f, 220, 53);
            this.body.addBox("scale", -1.0f, -6.0f, 30.0f, 2, 6, 12, 0.0f, 220, 53);
            this.leftWing = new ModelPart(this);
            this.leftWing.mirror = true;
            this.leftWing.setPos(12.0f, 5.0f, 2.0f);
            this.leftWing.addBox("bone", 0.0f, -4.0f, -4.0f, 56, 8, 8, 0.0f, 112, 88);
            this.leftWing.addBox("skin", 0.0f, 0.0f, 2.0f, 56, 0, 56, 0.0f, -56, 88);
            this.leftWingTip = new ModelPart(this);
            this.leftWingTip.mirror = true;
            this.leftWingTip.setPos(56.0f, 0.0f, 0.0f);
            this.leftWingTip.addBox("bone", 0.0f, -2.0f, -2.0f, 56, 4, 4, 0.0f, 112, 136);
            this.leftWingTip.addBox("skin", 0.0f, 0.0f, 2.0f, 56, 0, 56, 0.0f, -56, 144);
            this.leftWing.addChild(this.leftWingTip);
            this.leftFrontLeg = new ModelPart(this);
            this.leftFrontLeg.setPos(12.0f, 20.0f, 2.0f);
            this.leftFrontLeg.addBox("main", -4.0f, -4.0f, -4.0f, 8, 24, 8, 0.0f, 112, 104);
            this.leftFrontLegTip = new ModelPart(this);
            this.leftFrontLegTip.setPos(0.0f, 20.0f, -1.0f);
            this.leftFrontLegTip.addBox("main", -3.0f, -1.0f, -3.0f, 6, 24, 6, 0.0f, 226, 138);
            this.leftFrontLeg.addChild(this.leftFrontLegTip);
            this.leftFrontFoot = new ModelPart(this);
            this.leftFrontFoot.setPos(0.0f, 23.0f, 0.0f);
            this.leftFrontFoot.addBox("main", -4.0f, 0.0f, -12.0f, 8, 4, 16, 0.0f, 144, 104);
            this.leftFrontLegTip.addChild(this.leftFrontFoot);
            this.leftRearLeg = new ModelPart(this);
            this.leftRearLeg.setPos(16.0f, 16.0f, 42.0f);
            this.leftRearLeg.addBox("main", -8.0f, -4.0f, -8.0f, 16, 32, 16, 0.0f, 0, 0);
            this.leftRearLegTip = new ModelPart(this);
            this.leftRearLegTip.setPos(0.0f, 32.0f, -4.0f);
            this.leftRearLegTip.addBox("main", -6.0f, -2.0f, 0.0f, 12, 32, 12, 0.0f, 196, 0);
            this.leftRearLeg.addChild(this.leftRearLegTip);
            this.leftRearFoot = new ModelPart(this);
            this.leftRearFoot.setPos(0.0f, 31.0f, 4.0f);
            this.leftRearFoot.addBox("main", -9.0f, 0.0f, -20.0f, 18, 6, 24, 0.0f, 112, 0);
            this.leftRearLegTip.addChild(this.leftRearFoot);
            this.rightWing = new ModelPart(this);
            this.rightWing.setPos(-12.0f, 5.0f, 2.0f);
            this.rightWing.addBox("bone", -56.0f, -4.0f, -4.0f, 56, 8, 8, 0.0f, 112, 88);
            this.rightWing.addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, 0.0f, -56, 88);
            this.rightWingTip = new ModelPart(this);
            this.rightWingTip.setPos(-56.0f, 0.0f, 0.0f);
            this.rightWingTip.addBox("bone", -56.0f, -2.0f, -2.0f, 56, 4, 4, 0.0f, 112, 136);
            this.rightWingTip.addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, 0.0f, -56, 144);
            this.rightWing.addChild(this.rightWingTip);
            this.rightFrontLeg = new ModelPart(this);
            this.rightFrontLeg.setPos(-12.0f, 20.0f, 2.0f);
            this.rightFrontLeg.addBox("main", -4.0f, -4.0f, -4.0f, 8, 24, 8, 0.0f, 112, 104);
            this.rightFrontLegTip = new ModelPart(this);
            this.rightFrontLegTip.setPos(0.0f, 20.0f, -1.0f);
            this.rightFrontLegTip.addBox("main", -3.0f, -1.0f, -3.0f, 6, 24, 6, 0.0f, 226, 138);
            this.rightFrontLeg.addChild(this.rightFrontLegTip);
            this.rightFrontFoot = new ModelPart(this);
            this.rightFrontFoot.setPos(0.0f, 23.0f, 0.0f);
            this.rightFrontFoot.addBox("main", -4.0f, 0.0f, -12.0f, 8, 4, 16, 0.0f, 144, 104);
            this.rightFrontLegTip.addChild(this.rightFrontFoot);
            this.rightRearLeg = new ModelPart(this);
            this.rightRearLeg.setPos(-16.0f, 16.0f, 42.0f);
            this.rightRearLeg.addBox("main", -8.0f, -4.0f, -8.0f, 16, 32, 16, 0.0f, 0, 0);
            this.rightRearLegTip = new ModelPart(this);
            this.rightRearLegTip.setPos(0.0f, 32.0f, -4.0f);
            this.rightRearLegTip.addBox("main", -6.0f, -2.0f, 0.0f, 12, 32, 12, 0.0f, 196, 0);
            this.rightRearLeg.addChild(this.rightRearLegTip);
            this.rightRearFoot = new ModelPart(this);
            this.rightRearFoot.setPos(0.0f, 31.0f, 4.0f);
            this.rightRearFoot.addBox("main", -9.0f, 0.0f, -20.0f, 18, 6, 24, 0.0f, 112, 0);
            this.rightRearLegTip.addChild(this.rightRearFoot);
        }

        @Override
        public void prepareMobModel(EnderDragon enderDragon, float f, float g, float h) {
            this.entity = enderDragon;
            this.a = h;
        }

        @Override
        public void setupAnim(EnderDragon enderDragon, float f, float g, float h, float i, float j) {
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h) {
            float u;
            poseStack.pushPose();
            float k = Mth.lerp(this.a, this.entity.oFlapTime, this.entity.flapTime);
            this.jaw.xRot = (float)(Math.sin(k * ((float)Math.PI * 2)) + 1.0) * 0.2f;
            float l = (float)(Math.sin(k * ((float)Math.PI * 2) - 1.0f) + 1.0);
            l = (l * l + l * 2.0f) * 0.05f;
            poseStack.translate(0.0, l - 2.0f, -3.0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(l * 2.0f));
            float m = 0.0f;
            float n = 20.0f;
            float o = -12.0f;
            float p = 1.5f;
            double[] ds = this.entity.getLatencyPos(6, this.a);
            float q = Mth.rotWrap(this.entity.getLatencyPos(5, this.a)[0] - this.entity.getLatencyPos(10, this.a)[0]);
            float r = Mth.rotWrap(this.entity.getLatencyPos(5, this.a)[0] + (double)(q / 2.0f));
            float s = k * ((float)Math.PI * 2);
            for (int t = 0; t < 5; ++t) {
                double[] es = this.entity.getLatencyPos(5 - t, this.a);
                u = (float)Math.cos((float)t * 0.45f + s) * 0.15f;
                this.neck.yRot = Mth.rotWrap(es[0] - ds[0]) * ((float)Math.PI / 180) * 1.5f;
                this.neck.xRot = u + this.entity.getHeadPartYOffset(t, ds, es) * ((float)Math.PI / 180) * 1.5f * 5.0f;
                this.neck.zRot = -Mth.rotWrap(es[0] - (double)r) * ((float)Math.PI / 180) * 1.5f;
                this.neck.y = n;
                this.neck.z = o;
                this.neck.x = m;
                n = (float)((double)n + Math.sin(this.neck.xRot) * 10.0);
                o = (float)((double)o - Math.cos(this.neck.yRot) * Math.cos(this.neck.xRot) * 10.0);
                m = (float)((double)m - Math.sin(this.neck.yRot) * Math.cos(this.neck.xRot) * 10.0);
                this.neck.render(poseStack, vertexConsumer, i, j, null);
            }
            this.head.y = n;
            this.head.z = o;
            this.head.x = m;
            double[] fs = this.entity.getLatencyPos(0, this.a);
            this.head.yRot = Mth.rotWrap(fs[0] - ds[0]) * ((float)Math.PI / 180);
            this.head.xRot = Mth.rotWrap(this.entity.getHeadPartYOffset(6, ds, fs)) * ((float)Math.PI / 180) * 1.5f * 5.0f;
            this.head.zRot = -Mth.rotWrap(fs[0] - (double)r) * ((float)Math.PI / 180);
            this.head.render(poseStack, vertexConsumer, i, j, null);
            poseStack.pushPose();
            poseStack.translate(0.0, 1.0, 0.0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-q * 1.5f));
            poseStack.translate(0.0, -1.0, 0.0);
            this.body.zRot = 0.0f;
            this.body.render(poseStack, vertexConsumer, i, j, null);
            float v = k * ((float)Math.PI * 2);
            this.leftWing.xRot = 0.125f - (float)Math.cos(v) * 0.2f;
            this.leftWing.yRot = -0.25f;
            this.leftWing.zRot = -((float)(Math.sin(v) + 0.125)) * 0.8f;
            this.leftWingTip.zRot = (float)(Math.sin(v + 2.0f) + 0.5) * 0.75f;
            this.rightWing.xRot = this.leftWing.xRot;
            this.rightWing.yRot = -this.leftWing.yRot;
            this.rightWing.zRot = -this.leftWing.zRot;
            this.rightWingTip.zRot = -this.leftWingTip.zRot;
            this.renderSide(poseStack, vertexConsumer, i, j, l, this.leftWing, this.leftFrontLeg, this.leftFrontLegTip, this.leftFrontFoot, this.leftRearLeg, this.leftRearLegTip, this.leftRearFoot);
            this.renderSide(poseStack, vertexConsumer, i, j, l, this.rightWing, this.rightFrontLeg, this.rightFrontLegTip, this.rightFrontFoot, this.rightRearLeg, this.rightRearLegTip, this.rightRearFoot);
            poseStack.popPose();
            u = -((float)Math.sin(k * ((float)Math.PI * 2))) * 0.0f;
            s = k * ((float)Math.PI * 2);
            n = 10.0f;
            o = 60.0f;
            m = 0.0f;
            ds = this.entity.getLatencyPos(11, this.a);
            for (int w = 0; w < 12; ++w) {
                fs = this.entity.getLatencyPos(12 + w, this.a);
                u = (float)((double)u + Math.sin((float)w * 0.45f + s) * (double)0.05f);
                this.neck.yRot = (Mth.rotWrap(fs[0] - ds[0]) * 1.5f + 180.0f) * ((float)Math.PI / 180);
                this.neck.xRot = u + (float)(fs[1] - ds[1]) * ((float)Math.PI / 180) * 1.5f * 5.0f;
                this.neck.zRot = Mth.rotWrap(fs[0] - (double)r) * ((float)Math.PI / 180) * 1.5f;
                this.neck.y = n;
                this.neck.z = o;
                this.neck.x = m;
                n = (float)((double)n + Math.sin(this.neck.xRot) * 10.0);
                o = (float)((double)o - Math.cos(this.neck.yRot) * Math.cos(this.neck.xRot) * 10.0);
                m = (float)((double)m - Math.sin(this.neck.yRot) * Math.cos(this.neck.xRot) * 10.0);
                this.neck.render(poseStack, vertexConsumer, i, j, null);
            }
            poseStack.popPose();
        }

        private void renderSide(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, ModelPart modelPart4, ModelPart modelPart5, ModelPart modelPart6, ModelPart modelPart7) {
            modelPart5.xRot = 1.0f + f * 0.1f;
            modelPart6.xRot = 0.5f + f * 0.1f;
            modelPart7.xRot = 0.75f + f * 0.1f;
            modelPart2.xRot = 1.3f + f * 0.1f;
            modelPart3.xRot = -0.5f - f * 0.1f;
            modelPart4.xRot = 0.75f + f * 0.1f;
            modelPart.render(poseStack, vertexConsumer, i, j, null);
            modelPart2.render(poseStack, vertexConsumer, i, j, null);
            modelPart5.render(poseStack, vertexConsumer, i, j, null);
        }
    }
}

