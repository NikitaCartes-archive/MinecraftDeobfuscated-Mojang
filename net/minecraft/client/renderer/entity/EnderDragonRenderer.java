/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class EnderDragonRenderer
extends EntityRenderer<EnderDragon> {
    public static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
    private static final ResourceLocation DRAGON_EXPLODING_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
    private static final ResourceLocation DRAGON_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation DRAGON_EYES_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(DRAGON_LOCATION);
    private static final RenderType DECAL = RenderType.entityDecal(DRAGON_LOCATION);
    private static final RenderType EYES = RenderType.eyes(DRAGON_EYES_LOCATION);
    private static final RenderType BEAM = RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION);
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    private final DragonModel model;

    public EnderDragonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.model = new DragonModel(context.bakeLayer(ModelLayers.ENDER_DRAGON));
    }

    @Override
    public void render(EnderDragon enderDragon, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        float h = (float)enderDragon.getLatencyPos(7, g)[0];
        float j = (float)(enderDragon.getLatencyPos(5, g)[1] - enderDragon.getLatencyPos(10, g)[1]);
        poseStack.mulPose(Axis.YP.rotationDegrees(-h));
        poseStack.mulPose(Axis.XP.rotationDegrees(j * 10.0f));
        poseStack.translate(0.0f, 0.0f, 1.0f);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        boolean bl = enderDragon.hurtTime > 0;
        this.model.prepareMobModel(enderDragon, 0.0f, 0.0f, g);
        if (enderDragon.dragonDeathTime > 0) {
            float k = (float)enderDragon.dragonDeathTime / 200.0f;
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION));
            this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, k);
            VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(DECAL);
            this.model.renderToBuffer(poseStack, vertexConsumer2, i, OverlayTexture.pack(0.0f, bl), 1.0f, 1.0f, 1.0f, 1.0f);
        } else {
            VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(RENDER_TYPE);
            this.model.renderToBuffer(poseStack, vertexConsumer3, i, OverlayTexture.pack(0.0f, bl), 1.0f, 1.0f, 1.0f, 1.0f);
        }
        VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(EYES);
        this.model.renderToBuffer(poseStack, vertexConsumer3, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        if (enderDragon.dragonDeathTime > 0) {
            float l = ((float)enderDragon.dragonDeathTime + g) / 200.0f;
            float m = Math.min(l > 0.8f ? (l - 0.8f) / 0.2f : 0.0f, 1.0f);
            RandomSource randomSource = RandomSource.create(432L);
            VertexConsumer vertexConsumer4 = multiBufferSource.getBuffer(RenderType.lightning());
            poseStack.pushPose();
            poseStack.translate(0.0f, -1.0f, -2.0f);
            int n = 0;
            while ((float)n < (l + l * l) / 2.0f * 60.0f) {
                poseStack.mulPose(Axis.XP.rotationDegrees(randomSource.nextFloat() * 360.0f));
                poseStack.mulPose(Axis.YP.rotationDegrees(randomSource.nextFloat() * 360.0f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(randomSource.nextFloat() * 360.0f));
                poseStack.mulPose(Axis.XP.rotationDegrees(randomSource.nextFloat() * 360.0f));
                poseStack.mulPose(Axis.YP.rotationDegrees(randomSource.nextFloat() * 360.0f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(randomSource.nextFloat() * 360.0f + l * 90.0f));
                float o = randomSource.nextFloat() * 20.0f + 5.0f + m * 10.0f;
                float p = randomSource.nextFloat() * 2.0f + 1.0f + m * 2.0f;
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
        poseStack.translate(0.0f, 2.0f, 0.0f);
        poseStack.mulPose(Axis.YP.rotation((float)(-Math.atan2(h, f)) - 1.5707964f));
        poseStack.mulPose(Axis.XP.rotation((float)(-Math.atan2(l, g)) - 1.5707964f));
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(BEAM);
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
            float u = Mth.sin((float)t * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float v = Mth.cos((float)t * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float w = (float)t / 8.0f;
            vertexConsumer.vertex(matrix4f, q * 0.2f, r * 0.2f, 0.0f).color(0, 0, 0, 255).uv(s, n).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(k).normal(matrix3f, 0.0f, -1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, q, r, m).color(255, 255, 255, 255).uv(s, o).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(k).normal(matrix3f, 0.0f, -1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, u, v, m).color(255, 255, 255, 255).uv(w, o).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(k).normal(matrix3f, 0.0f, -1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, u * 0.2f, v * 0.2f, 0.0f).color(0, 0, 0, 255).uv(w, n).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(k).normal(matrix3f, 0.0f, -1.0f, 0.0f).endVertex();
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

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = -16.0f;
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().addBox("upperlip", -6.0f, -1.0f, -24.0f, 12, 5, 16, 176, 44).addBox("upperhead", -8.0f, -8.0f, -10.0f, 16, 16, 16, 112, 30).mirror().addBox("scale", -5.0f, -12.0f, -4.0f, 2, 4, 6, 0, 0).addBox("nostril", -5.0f, -3.0f, -22.0f, 2, 2, 4, 112, 0).mirror().addBox("scale", 3.0f, -12.0f, -4.0f, 2, 4, 6, 0, 0).addBox("nostril", 3.0f, -3.0f, -22.0f, 2, 2, 4, 112, 0), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("jaw", CubeListBuilder.create().addBox("jaw", -6.0f, 0.0f, -16.0f, 12, 4, 16, 176, 65), PartPose.offset(0.0f, 4.0f, -8.0f));
        partDefinition.addOrReplaceChild("neck", CubeListBuilder.create().addBox("box", -5.0f, -5.0f, -5.0f, 10, 10, 10, 192, 104).addBox("scale", -1.0f, -9.0f, -3.0f, 2, 4, 6, 48, 0), PartPose.ZERO);
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().addBox("body", -12.0f, 0.0f, -16.0f, 24, 24, 64, 0, 0).addBox("scale", -1.0f, -6.0f, -10.0f, 2, 6, 12, 220, 53).addBox("scale", -1.0f, -6.0f, 10.0f, 2, 6, 12, 220, 53).addBox("scale", -1.0f, -6.0f, 30.0f, 2, 6, 12, 220, 53), PartPose.offset(0.0f, 4.0f, 8.0f));
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().mirror().addBox("bone", 0.0f, -4.0f, -4.0f, 56, 8, 8, 112, 88).addBox("skin", 0.0f, 0.0f, 2.0f, 56, 0, 56, -56, 88), PartPose.offset(12.0f, 5.0f, 2.0f));
        partDefinition3.addOrReplaceChild("left_wing_tip", CubeListBuilder.create().mirror().addBox("bone", 0.0f, -2.0f, -2.0f, 56, 4, 4, 112, 136).addBox("skin", 0.0f, 0.0f, 2.0f, 56, 0, 56, -56, 144), PartPose.offset(56.0f, 0.0f, 0.0f));
        PartDefinition partDefinition4 = partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().addBox("main", -4.0f, -4.0f, -4.0f, 8, 24, 8, 112, 104), PartPose.offset(12.0f, 20.0f, 2.0f));
        PartDefinition partDefinition5 = partDefinition4.addOrReplaceChild("left_front_leg_tip", CubeListBuilder.create().addBox("main", -3.0f, -1.0f, -3.0f, 6, 24, 6, 226, 138), PartPose.offset(0.0f, 20.0f, -1.0f));
        partDefinition5.addOrReplaceChild("left_front_foot", CubeListBuilder.create().addBox("main", -4.0f, 0.0f, -12.0f, 8, 4, 16, 144, 104), PartPose.offset(0.0f, 23.0f, 0.0f));
        PartDefinition partDefinition6 = partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().addBox("main", -8.0f, -4.0f, -8.0f, 16, 32, 16, 0, 0), PartPose.offset(16.0f, 16.0f, 42.0f));
        PartDefinition partDefinition7 = partDefinition6.addOrReplaceChild("left_hind_leg_tip", CubeListBuilder.create().addBox("main", -6.0f, -2.0f, 0.0f, 12, 32, 12, 196, 0), PartPose.offset(0.0f, 32.0f, -4.0f));
        partDefinition7.addOrReplaceChild("left_hind_foot", CubeListBuilder.create().addBox("main", -9.0f, 0.0f, -20.0f, 18, 6, 24, 112, 0), PartPose.offset(0.0f, 31.0f, 4.0f));
        PartDefinition partDefinition8 = partDefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().addBox("bone", -56.0f, -4.0f, -4.0f, 56, 8, 8, 112, 88).addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, -56, 88), PartPose.offset(-12.0f, 5.0f, 2.0f));
        partDefinition8.addOrReplaceChild("right_wing_tip", CubeListBuilder.create().addBox("bone", -56.0f, -2.0f, -2.0f, 56, 4, 4, 112, 136).addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56, -56, 144), PartPose.offset(-56.0f, 0.0f, 0.0f));
        PartDefinition partDefinition9 = partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().addBox("main", -4.0f, -4.0f, -4.0f, 8, 24, 8, 112, 104), PartPose.offset(-12.0f, 20.0f, 2.0f));
        PartDefinition partDefinition10 = partDefinition9.addOrReplaceChild("right_front_leg_tip", CubeListBuilder.create().addBox("main", -3.0f, -1.0f, -3.0f, 6, 24, 6, 226, 138), PartPose.offset(0.0f, 20.0f, -1.0f));
        partDefinition10.addOrReplaceChild("right_front_foot", CubeListBuilder.create().addBox("main", -4.0f, 0.0f, -12.0f, 8, 4, 16, 144, 104), PartPose.offset(0.0f, 23.0f, 0.0f));
        PartDefinition partDefinition11 = partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().addBox("main", -8.0f, -4.0f, -8.0f, 16, 32, 16, 0, 0), PartPose.offset(-16.0f, 16.0f, 42.0f));
        PartDefinition partDefinition12 = partDefinition11.addOrReplaceChild("right_hind_leg_tip", CubeListBuilder.create().addBox("main", -6.0f, -2.0f, 0.0f, 12, 32, 12, 196, 0), PartPose.offset(0.0f, 32.0f, -4.0f));
        partDefinition12.addOrReplaceChild("right_hind_foot", CubeListBuilder.create().addBox("main", -9.0f, 0.0f, -20.0f, 18, 6, 24, 112, 0), PartPose.offset(0.0f, 31.0f, 4.0f));
        return LayerDefinition.create(meshDefinition, 256, 256);
    }

    @Environment(value=EnvType.CLIENT)
    public static class DragonModel
    extends EntityModel<EnderDragon> {
        private final ModelPart head;
        private final ModelPart neck;
        private final ModelPart jaw;
        private final ModelPart body;
        private final ModelPart leftWing;
        private final ModelPart leftWingTip;
        private final ModelPart leftFrontLeg;
        private final ModelPart leftFrontLegTip;
        private final ModelPart leftFrontFoot;
        private final ModelPart leftRearLeg;
        private final ModelPart leftRearLegTip;
        private final ModelPart leftRearFoot;
        private final ModelPart rightWing;
        private final ModelPart rightWingTip;
        private final ModelPart rightFrontLeg;
        private final ModelPart rightFrontLegTip;
        private final ModelPart rightFrontFoot;
        private final ModelPart rightRearLeg;
        private final ModelPart rightRearLegTip;
        private final ModelPart rightRearFoot;
        @Nullable
        private EnderDragon entity;
        private float a;

        public DragonModel(ModelPart modelPart) {
            this.head = modelPart.getChild("head");
            this.jaw = this.head.getChild("jaw");
            this.neck = modelPart.getChild("neck");
            this.body = modelPart.getChild("body");
            this.leftWing = modelPart.getChild("left_wing");
            this.leftWingTip = this.leftWing.getChild("left_wing_tip");
            this.leftFrontLeg = modelPart.getChild("left_front_leg");
            this.leftFrontLegTip = this.leftFrontLeg.getChild("left_front_leg_tip");
            this.leftFrontFoot = this.leftFrontLegTip.getChild("left_front_foot");
            this.leftRearLeg = modelPart.getChild("left_hind_leg");
            this.leftRearLegTip = this.leftRearLeg.getChild("left_hind_leg_tip");
            this.leftRearFoot = this.leftRearLegTip.getChild("left_hind_foot");
            this.rightWing = modelPart.getChild("right_wing");
            this.rightWingTip = this.rightWing.getChild("right_wing_tip");
            this.rightFrontLeg = modelPart.getChild("right_front_leg");
            this.rightFrontLegTip = this.rightFrontLeg.getChild("right_front_leg_tip");
            this.rightFrontFoot = this.rightFrontLegTip.getChild("right_front_foot");
            this.rightRearLeg = modelPart.getChild("right_hind_leg");
            this.rightRearLegTip = this.rightRearLeg.getChild("right_hind_leg_tip");
            this.rightRearFoot = this.rightRearLegTip.getChild("right_hind_foot");
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
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
            float v;
            poseStack.pushPose();
            float l = Mth.lerp(this.a, this.entity.oFlapTime, this.entity.flapTime);
            this.jaw.xRot = (float)(Math.sin(l * ((float)Math.PI * 2)) + 1.0) * 0.2f;
            float m = (float)(Math.sin(l * ((float)Math.PI * 2) - 1.0f) + 1.0);
            m = (m * m + m * 2.0f) * 0.05f;
            poseStack.translate(0.0f, m - 2.0f, -3.0f);
            poseStack.mulPose(Axis.XP.rotationDegrees(m * 2.0f));
            float n = 0.0f;
            float o = 20.0f;
            float p = -12.0f;
            float q = 1.5f;
            double[] ds = this.entity.getLatencyPos(6, this.a);
            float r = Mth.wrapDegrees((float)(this.entity.getLatencyPos(5, this.a)[0] - this.entity.getLatencyPos(10, this.a)[0]));
            float s = Mth.wrapDegrees((float)(this.entity.getLatencyPos(5, this.a)[0] + (double)(r / 2.0f)));
            float t = l * ((float)Math.PI * 2);
            for (int u = 0; u < 5; ++u) {
                double[] es = this.entity.getLatencyPos(5 - u, this.a);
                v = (float)Math.cos((float)u * 0.45f + t) * 0.15f;
                this.neck.yRot = Mth.wrapDegrees((float)(es[0] - ds[0])) * ((float)Math.PI / 180) * 1.5f;
                this.neck.xRot = v + this.entity.getHeadPartYOffset(u, ds, es) * ((float)Math.PI / 180) * 1.5f * 5.0f;
                this.neck.zRot = -Mth.wrapDegrees((float)(es[0] - (double)s)) * ((float)Math.PI / 180) * 1.5f;
                this.neck.y = o;
                this.neck.z = p;
                this.neck.x = n;
                o += Mth.sin(this.neck.xRot) * 10.0f;
                p -= Mth.cos(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0f;
                n -= Mth.sin(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0f;
                this.neck.render(poseStack, vertexConsumer, i, j, 1.0f, 1.0f, 1.0f, k);
            }
            this.head.y = o;
            this.head.z = p;
            this.head.x = n;
            double[] fs = this.entity.getLatencyPos(0, this.a);
            this.head.yRot = Mth.wrapDegrees((float)(fs[0] - ds[0])) * ((float)Math.PI / 180);
            this.head.xRot = Mth.wrapDegrees(this.entity.getHeadPartYOffset(6, ds, fs)) * ((float)Math.PI / 180) * 1.5f * 5.0f;
            this.head.zRot = -Mth.wrapDegrees((float)(fs[0] - (double)s)) * ((float)Math.PI / 180);
            this.head.render(poseStack, vertexConsumer, i, j, 1.0f, 1.0f, 1.0f, k);
            poseStack.pushPose();
            poseStack.translate(0.0f, 1.0f, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-r * 1.5f));
            poseStack.translate(0.0f, -1.0f, 0.0f);
            this.body.zRot = 0.0f;
            this.body.render(poseStack, vertexConsumer, i, j, 1.0f, 1.0f, 1.0f, k);
            float w = l * ((float)Math.PI * 2);
            this.leftWing.xRot = 0.125f - (float)Math.cos(w) * 0.2f;
            this.leftWing.yRot = -0.25f;
            this.leftWing.zRot = -((float)(Math.sin(w) + 0.125)) * 0.8f;
            this.leftWingTip.zRot = (float)(Math.sin(w + 2.0f) + 0.5) * 0.75f;
            this.rightWing.xRot = this.leftWing.xRot;
            this.rightWing.yRot = -this.leftWing.yRot;
            this.rightWing.zRot = -this.leftWing.zRot;
            this.rightWingTip.zRot = -this.leftWingTip.zRot;
            this.renderSide(poseStack, vertexConsumer, i, j, m, this.leftWing, this.leftFrontLeg, this.leftFrontLegTip, this.leftFrontFoot, this.leftRearLeg, this.leftRearLegTip, this.leftRearFoot, k);
            this.renderSide(poseStack, vertexConsumer, i, j, m, this.rightWing, this.rightFrontLeg, this.rightFrontLegTip, this.rightFrontFoot, this.rightRearLeg, this.rightRearLegTip, this.rightRearFoot, k);
            poseStack.popPose();
            v = -Mth.sin(l * ((float)Math.PI * 2)) * 0.0f;
            t = l * ((float)Math.PI * 2);
            o = 10.0f;
            p = 60.0f;
            n = 0.0f;
            ds = this.entity.getLatencyPos(11, this.a);
            for (int x = 0; x < 12; ++x) {
                fs = this.entity.getLatencyPos(12 + x, this.a);
                this.neck.yRot = (Mth.wrapDegrees((float)(fs[0] - ds[0])) * 1.5f + 180.0f) * ((float)Math.PI / 180);
                this.neck.xRot = (v += Mth.sin((float)x * 0.45f + t) * 0.05f) + (float)(fs[1] - ds[1]) * ((float)Math.PI / 180) * 1.5f * 5.0f;
                this.neck.zRot = Mth.wrapDegrees((float)(fs[0] - (double)s)) * ((float)Math.PI / 180) * 1.5f;
                this.neck.y = o;
                this.neck.z = p;
                this.neck.x = n;
                o += Mth.sin(this.neck.xRot) * 10.0f;
                p -= Mth.cos(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0f;
                n -= Mth.sin(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0f;
                this.neck.render(poseStack, vertexConsumer, i, j, 1.0f, 1.0f, 1.0f, k);
            }
            poseStack.popPose();
        }

        private void renderSide(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, ModelPart modelPart4, ModelPart modelPart5, ModelPart modelPart6, ModelPart modelPart7, float g) {
            modelPart5.xRot = 1.0f + f * 0.1f;
            modelPart6.xRot = 0.5f + f * 0.1f;
            modelPart7.xRot = 0.75f + f * 0.1f;
            modelPart2.xRot = 1.3f + f * 0.1f;
            modelPart3.xRot = -0.5f - f * 0.1f;
            modelPart4.xRot = 0.75f + f * 0.1f;
            modelPart.render(poseStack, vertexConsumer, i, j, 1.0f, 1.0f, 1.0f, g);
            modelPart2.render(poseStack, vertexConsumer, i, j, 1.0f, 1.0f, 1.0f, g);
            modelPart5.render(poseStack, vertexConsumer, i, j, 1.0f, 1.0f, 1.0f, g);
        }
    }
}

