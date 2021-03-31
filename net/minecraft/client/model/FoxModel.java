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
import net.minecraft.world.entity.animal.Fox;

@Environment(value=EnvType.CLIENT)
public class FoxModel<T extends Fox>
extends AgeableListModel<T> {
    public final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private static final int LEG_SIZE = 6;
    private static final float HEAD_HEIGHT = 16.5f;
    private static final float LEG_POS = 17.5f;
    private float legMotionPos;

    public FoxModel(ModelPart modelPart) {
        super(true, 8.0f, 3.35f);
        this.head = modelPart.getChild("head");
        this.body = modelPart.getChild("body");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(1, 5).addBox(-3.0f, -2.0f, -5.0f, 8.0f, 6.0f, 6.0f), PartPose.offset(-1.0f, 16.5f, -3.0f));
        partDefinition2.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(8, 1).addBox(-3.0f, -4.0f, -4.0f, 2.0f, 2.0f, 1.0f), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(15, 1).addBox(3.0f, -4.0f, -4.0f, 2.0f, 2.0f, 1.0f), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(6, 18).addBox(-1.0f, 2.01f, -8.0f, 4.0f, 2.0f, 3.0f), PartPose.ZERO);
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(24, 15).addBox(-3.0f, 3.999f, -3.5f, 6.0f, 11.0f, 6.0f), PartPose.offsetAndRotation(0.0f, 16.0f, -6.0f, 1.5707964f, 0.0f, 0.0f));
        CubeDeformation cubeDeformation = new CubeDeformation(0.001f);
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(4, 24).addBox(2.0f, 0.5f, -1.0f, 2.0f, 6.0f, 2.0f, cubeDeformation);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(13, 24).addBox(2.0f, 0.5f, -1.0f, 2.0f, 6.0f, 2.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder2, PartPose.offset(-5.0f, 17.5f, 7.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(-1.0f, 17.5f, 7.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder2, PartPose.offset(-5.0f, 17.5f, 0.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(-1.0f, 17.5f, 0.0f));
        partDefinition3.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(30, 0).addBox(2.0f, 0.0f, -1.0f, 4.0f, 9.0f, 5.0f), PartPose.offsetAndRotation(-4.0f, 15.0f, -1.0f, -0.05235988f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 48, 32);
    }

    @Override
    public void prepareMobModel(T fox, float f, float g, float h) {
        this.body.xRot = 1.5707964f;
        this.tail.xRot = -0.05235988f;
        this.rightHindLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        this.leftHindLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.rightFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.leftFrontLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        this.head.setPos(-1.0f, 16.5f, -3.0f);
        this.head.yRot = 0.0f;
        this.head.zRot = ((Fox)fox).getHeadRollAngle(h);
        this.rightHindLeg.visible = true;
        this.leftHindLeg.visible = true;
        this.rightFrontLeg.visible = true;
        this.leftFrontLeg.visible = true;
        this.body.setPos(0.0f, 16.0f, -6.0f);
        this.body.zRot = 0.0f;
        this.rightHindLeg.setPos(-5.0f, 17.5f, 7.0f);
        this.leftHindLeg.setPos(-1.0f, 17.5f, 7.0f);
        if (((Fox)fox).isCrouching()) {
            this.body.xRot = 1.6755161f;
            float i = ((Fox)fox).getCrouchAmount(h);
            this.body.setPos(0.0f, 16.0f + ((Fox)fox).getCrouchAmount(h), -6.0f);
            this.head.setPos(-1.0f, 16.5f + i, -3.0f);
            this.head.yRot = 0.0f;
        } else if (((Fox)fox).isSleeping()) {
            this.body.zRot = -1.5707964f;
            this.body.setPos(0.0f, 21.0f, -6.0f);
            this.tail.xRot = -2.6179938f;
            if (this.young) {
                this.tail.xRot = -2.1816616f;
                this.body.setPos(0.0f, 21.0f, -2.0f);
            }
            this.head.setPos(1.0f, 19.49f, -3.0f);
            this.head.xRot = 0.0f;
            this.head.yRot = -2.0943952f;
            this.head.zRot = 0.0f;
            this.rightHindLeg.visible = false;
            this.leftHindLeg.visible = false;
            this.rightFrontLeg.visible = false;
            this.leftFrontLeg.visible = false;
        } else if (((Fox)fox).isSitting()) {
            this.body.xRot = 0.5235988f;
            this.body.setPos(0.0f, 9.0f, -3.0f);
            this.tail.xRot = 0.7853982f;
            this.tail.setPos(-4.0f, 15.0f, -2.0f);
            this.head.setPos(-1.0f, 10.0f, -0.25f);
            this.head.xRot = 0.0f;
            this.head.yRot = 0.0f;
            if (this.young) {
                this.head.setPos(-1.0f, 13.0f, -3.75f);
            }
            this.rightHindLeg.xRot = -1.3089969f;
            this.rightHindLeg.setPos(-5.0f, 21.5f, 6.75f);
            this.leftHindLeg.xRot = -1.3089969f;
            this.leftHindLeg.setPos(-1.0f, 21.5f, 6.75f);
            this.rightFrontLeg.xRot = -0.2617994f;
            this.leftFrontLeg.xRot = -0.2617994f;
        }
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg);
    }

    @Override
    public void setupAnim(T fox, float f, float g, float h, float i, float j) {
        float k;
        if (!(((Fox)fox).isSleeping() || ((Fox)fox).isFaceplanted() || ((Fox)fox).isCrouching())) {
            this.head.xRot = j * ((float)Math.PI / 180);
            this.head.yRot = i * ((float)Math.PI / 180);
        }
        if (((Fox)fox).isSleeping()) {
            this.head.xRot = 0.0f;
            this.head.yRot = -2.0943952f;
            this.head.zRot = Mth.cos(h * 0.027f) / 22.0f;
        }
        if (((Fox)fox).isCrouching()) {
            this.body.yRot = k = Mth.cos(h) * 0.01f;
            this.rightHindLeg.zRot = k;
            this.leftHindLeg.zRot = k;
            this.rightFrontLeg.zRot = k / 2.0f;
            this.leftFrontLeg.zRot = k / 2.0f;
        }
        if (((Fox)fox).isFaceplanted()) {
            k = 0.1f;
            this.legMotionPos += 0.67f;
            this.rightHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662f) * 0.1f;
            this.leftHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662f + (float)Math.PI) * 0.1f;
            this.rightFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662f + (float)Math.PI) * 0.1f;
            this.leftFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662f) * 0.1f;
        }
    }
}

