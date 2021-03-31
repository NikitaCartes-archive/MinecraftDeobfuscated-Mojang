/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

@Environment(value=EnvType.CLIENT)
public class AxolotlModel<T extends Axolotl>
extends AgeableListModel<T> {
    private final ModelPart tail;
    private final ModelPart leftHindLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart topGills;
    private final ModelPart leftGills;
    private final ModelPart rightGills;
    public static final float SWIMMING_LEG_XROT = 1.8849558f;

    public AxolotlModel(ModelPart modelPart) {
        super(true, 8.0f, 3.35f);
        this.body = modelPart.getChild("body");
        this.head = this.body.getChild("head");
        this.rightHindLeg = this.body.getChild("right_hind_leg");
        this.leftHindLeg = this.body.getChild("left_hind_leg");
        this.rightFrontLeg = this.body.getChild("right_front_leg");
        this.leftFrontLeg = this.body.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
        this.topGills = this.head.getChild("top_gills");
        this.leftGills = this.head.getChild("left_gills");
        this.rightGills = this.head.getChild("right_gills");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 11).addBox(-4.0f, -2.0f, -9.0f, 8.0f, 4.0f, 10.0f).texOffs(2, 17).addBox(0.0f, -3.0f, -8.0f, 0.0f, 5.0f, 9.0f), PartPose.offset(0.0f, 20.0f, 5.0f));
        CubeDeformation cubeDeformation = new CubeDeformation(0.001f);
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 1).addBox(-4.0f, -3.0f, -5.0f, 8.0f, 5.0f, 5.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f, -9.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(3, 37).addBox(-4.0f, -3.0f, 0.0f, 8.0f, 3.0f, 0.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(0, 40).addBox(-3.0f, -5.0f, 0.0f, 3.0f, 7.0f, 0.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder3 = CubeListBuilder.create().texOffs(11, 40).addBox(0.0f, -5.0f, 0.0f, 3.0f, 7.0f, 0.0f, cubeDeformation);
        partDefinition3.addOrReplaceChild("top_gills", cubeListBuilder, PartPose.offset(0.0f, -3.0f, -1.0f));
        partDefinition3.addOrReplaceChild("left_gills", cubeListBuilder2, PartPose.offset(-4.0f, 0.0f, -1.0f));
        partDefinition3.addOrReplaceChild("right_gills", cubeListBuilder3, PartPose.offset(4.0f, 0.0f, -1.0f));
        CubeListBuilder cubeListBuilder4 = CubeListBuilder.create().texOffs(2, 13).addBox(-1.0f, 0.0f, 0.0f, 3.0f, 5.0f, 0.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder5 = CubeListBuilder.create().texOffs(2, 13).addBox(-2.0f, 0.0f, 0.0f, 3.0f, 5.0f, 0.0f, cubeDeformation);
        partDefinition2.addOrReplaceChild("right_hind_leg", cubeListBuilder5, PartPose.offset(-3.5f, 1.0f, -1.0f));
        partDefinition2.addOrReplaceChild("left_hind_leg", cubeListBuilder4, PartPose.offset(3.5f, 1.0f, -1.0f));
        partDefinition2.addOrReplaceChild("right_front_leg", cubeListBuilder5, PartPose.offset(-3.5f, 1.0f, -8.0f));
        partDefinition2.addOrReplaceChild("left_front_leg", cubeListBuilder4, PartPose.offset(3.5f, 1.0f, -8.0f));
        partDefinition2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(2, 19).addBox(0.0f, -3.0f, 0.0f, 0.0f, 5.0f, 12.0f), PartPose.offset(0.0f, 0.0f, 1.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body);
    }

    @Override
    public void setupAnim(T axolotl, float f, float g, float h, float i, float j) {
        boolean bl;
        this.setupInitialAnimationValues(i, j);
        if (((Axolotl)axolotl).isPlayingDead()) {
            this.setupPlayDeadAnimation();
            return;
        }
        boolean bl2 = bl = Entity.getHorizontalDistanceSqr(((Entity)axolotl).getDeltaMovement()) > 1.0E-7;
        if (((Entity)axolotl).isInWaterOrBubble()) {
            if (bl) {
                this.setupSwimmingAnimation(h, j);
            } else {
                this.setupWaterHoveringAnimation(h);
            }
            return;
        }
        if (((Entity)axolotl).isOnGround()) {
            if (bl) {
                this.setupGroundCrawlingAnimation(h);
            } else {
                this.setupLayStillOnGroundAnimation(h);
            }
        }
    }

    private void setupInitialAnimationValues(float f, float g) {
        this.body.x = 0.0f;
        this.head.y = 0.0f;
        this.body.y = 20.0f;
        this.body.setRotation(g * ((float)Math.PI / 180), f * ((float)Math.PI / 180), 0.0f);
        this.head.setRotation(0.0f, 0.0f, 0.0f);
        this.leftHindLeg.setRotation(0.0f, 0.0f, 0.0f);
        this.rightHindLeg.setRotation(0.0f, 0.0f, 0.0f);
        this.leftFrontLeg.setRotation(0.0f, 0.0f, 0.0f);
        this.rightFrontLeg.setRotation(0.0f, 0.0f, 0.0f);
        this.leftGills.setRotation(0.0f, 0.0f, 0.0f);
        this.rightGills.setRotation(0.0f, 0.0f, 0.0f);
        this.topGills.setRotation(0.0f, 0.0f, 0.0f);
        this.tail.setRotation(0.0f, 0.0f, 0.0f);
    }

    private void setupLayStillOnGroundAnimation(float f) {
        float g = f * 0.09f;
        float h = Mth.sin(g);
        float i = Mth.cos(g);
        float j = h * h - 2.0f * h;
        float k = i * i - 3.0f * h;
        this.head.xRot = -0.09f * j;
        this.head.zRot = -0.2f;
        this.tail.yRot = -0.1f + 0.1f * j;
        this.topGills.xRot = 0.6f + 0.05f * k;
        this.leftGills.yRot = -this.topGills.xRot;
        this.rightGills.yRot = -this.leftGills.yRot;
        this.leftHindLeg.setRotation(1.1f, 1.0f, 0.0f);
        this.leftFrontLeg.setRotation(0.8f, 2.3f, -0.5f);
        this.applyMirrorLegRotations();
    }

    private void setupGroundCrawlingAnimation(float f) {
        float g = f * 0.11f;
        float h = Mth.cos(g);
        float i = (h * h - 2.0f * h) / 5.0f;
        float j = 0.7f * h;
        this.tail.yRot = this.head.yRot = 0.09f * h;
        this.topGills.xRot = 0.6f - 0.08f * (h * h + 2.0f * Mth.sin(g));
        this.leftGills.yRot = -this.topGills.xRot;
        this.rightGills.yRot = -this.leftGills.yRot;
        this.leftHindLeg.setRotation(0.9424779f, 1.5f - i, -0.1f);
        this.leftFrontLeg.setRotation(1.0995574f, 1.5707964f - j, 0.0f);
        this.rightHindLeg.setRotation(this.leftHindLeg.xRot, -1.0f - i, 0.0f);
        this.rightFrontLeg.setRotation(this.leftFrontLeg.xRot, -1.5707964f - j, 0.0f);
    }

    private void setupWaterHoveringAnimation(float f) {
        float g = f * 0.075f;
        float h = Mth.cos(g);
        float i = Mth.sin(g) * 0.15f;
        this.body.xRot = -0.15f + 0.075f * h;
        this.body.y -= i;
        this.head.xRot = -this.body.xRot;
        this.topGills.xRot = 0.2f * h;
        this.leftGills.yRot = -0.3f * h - 0.19f;
        this.rightGills.yRot = -this.leftGills.yRot;
        this.leftHindLeg.setRotation(2.3561945f - h * 0.11f, 0.47123894f, 1.7278761f);
        this.leftFrontLeg.setRotation(0.7853982f - h * 0.2f, 2.042035f, 0.0f);
        this.applyMirrorLegRotations();
        this.tail.yRot = 0.5f * h;
    }

    private void setupSwimmingAnimation(float f, float g) {
        float h = f * 0.33f;
        float i = Mth.sin(h);
        float j = Mth.cos(h);
        float k = 0.13f * i;
        this.body.xRot = g * ((float)Math.PI / 180) + k;
        this.head.xRot = -k * 1.8f;
        this.body.y -= 0.45f * j;
        this.topGills.xRot = -0.5f * i - 0.8f;
        this.leftGills.yRot = 0.3f * i + 0.9f;
        this.rightGills.yRot = -this.leftGills.yRot;
        this.tail.yRot = 0.3f * Mth.cos(h * 0.9f);
        this.leftHindLeg.setRotation(1.8849558f, -0.4f * i, 1.5707964f);
        this.leftFrontLeg.setRotation(1.8849558f, -0.2f * j - 0.1f, 1.5707964f);
        this.applyMirrorLegRotations();
    }

    private void setupPlayDeadAnimation() {
        this.leftHindLeg.setRotation(1.4137167f, 1.0995574f, 0.7853982f);
        this.leftFrontLeg.setRotation(0.7853982f, 2.042035f, 0.0f);
        this.body.xRot = -0.15f;
        this.body.zRot = 0.35f;
        this.applyMirrorLegRotations();
    }

    private void applyMirrorLegRotations() {
        this.rightHindLeg.setRotation(this.leftHindLeg.xRot, -this.leftHindLeg.yRot, -this.leftHindLeg.zRot);
        this.rightFrontLeg.setRotation(this.leftFrontLeg.xRot, -this.leftFrontLeg.yRot, -this.leftFrontLeg.zRot);
    }
}

