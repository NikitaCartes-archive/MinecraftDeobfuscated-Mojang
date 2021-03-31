/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ColorableAgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;

@Environment(value=EnvType.CLIENT)
public class WolfModel<T extends Wolf>
extends ColorableAgeableListModel<T> {
    private static final String REAL_HEAD = "real_head";
    private static final String UPPER_BODY = "upper_body";
    private static final String REAL_TAIL = "real_tail";
    private final ModelPart head;
    private final ModelPart realHead;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private final ModelPart realTail;
    private final ModelPart upperBody;
    private static final int LEG_SIZE = 8;

    public WolfModel(ModelPart modelPart) {
        this.head = modelPart.getChild("head");
        this.realHead = this.head.getChild(REAL_HEAD);
        this.body = modelPart.getChild("body");
        this.upperBody = modelPart.getChild(UPPER_BODY);
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.tail = modelPart.getChild("tail");
        this.realTail = this.tail.getChild(REAL_TAIL);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = 13.5f;
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(-1.0f, 13.5f, -7.0f));
        partDefinition2.addOrReplaceChild(REAL_HEAD, CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -3.0f, -2.0f, 6.0f, 6.0f, 4.0f).texOffs(16, 14).addBox(-2.0f, -5.0f, 0.0f, 2.0f, 2.0f, 1.0f).texOffs(16, 14).addBox(2.0f, -5.0f, 0.0f, 2.0f, 2.0f, 1.0f).texOffs(0, 10).addBox(-0.5f, 0.0f, -5.0f, 3.0f, 3.0f, 4.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 14).addBox(-3.0f, -2.0f, -3.0f, 6.0f, 9.0f, 6.0f), PartPose.offsetAndRotation(0.0f, 14.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(UPPER_BODY, CubeListBuilder.create().texOffs(21, 0).addBox(-3.0f, -3.0f, -3.0f, 8.0f, 6.0f, 7.0f), PartPose.offsetAndRotation(-1.0f, 14.0f, -3.0f, 1.5707964f, 0.0f, 0.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 18).addBox(0.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-2.5f, 16.0f, 7.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(0.5f, 16.0f, 7.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-2.5f, 16.0f, -4.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(0.5f, 16.0f, -4.0f));
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0f, 12.0f, 8.0f, 0.62831855f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild(REAL_TAIL, CubeListBuilder.create().texOffs(9, 18).addBox(0.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.tail, this.upperBody);
    }

    @Override
    public void prepareMobModel(T wolf, float f, float g, float h) {
        this.tail.yRot = wolf.isAngry() ? 0.0f : Mth.cos(f * 0.6662f) * 1.4f * g;
        if (((TamableAnimal)wolf).isInSittingPose()) {
            this.upperBody.setPos(-1.0f, 16.0f, -3.0f);
            this.upperBody.xRot = 1.2566371f;
            this.upperBody.yRot = 0.0f;
            this.body.setPos(0.0f, 18.0f, 0.0f);
            this.body.xRot = 0.7853982f;
            this.tail.setPos(-1.0f, 21.0f, 6.0f);
            this.rightHindLeg.setPos(-2.5f, 22.7f, 2.0f);
            this.rightHindLeg.xRot = 4.712389f;
            this.leftHindLeg.setPos(0.5f, 22.7f, 2.0f);
            this.leftHindLeg.xRot = 4.712389f;
            this.rightFrontLeg.xRot = 5.811947f;
            this.rightFrontLeg.setPos(-2.49f, 17.0f, -4.0f);
            this.leftFrontLeg.xRot = 5.811947f;
            this.leftFrontLeg.setPos(0.51f, 17.0f, -4.0f);
        } else {
            this.body.setPos(0.0f, 14.0f, 2.0f);
            this.body.xRot = 1.5707964f;
            this.upperBody.setPos(-1.0f, 14.0f, -3.0f);
            this.upperBody.xRot = this.body.xRot;
            this.tail.setPos(-1.0f, 12.0f, 8.0f);
            this.rightHindLeg.setPos(-2.5f, 16.0f, 7.0f);
            this.leftHindLeg.setPos(0.5f, 16.0f, 7.0f);
            this.rightFrontLeg.setPos(-2.5f, 16.0f, -4.0f);
            this.leftFrontLeg.setPos(0.5f, 16.0f, -4.0f);
            this.rightHindLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
            this.leftHindLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
            this.rightFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
            this.leftFrontLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        }
        this.realHead.zRot = ((Wolf)wolf).getHeadRollAngle(h) + ((Wolf)wolf).getBodyRollAngle(h, 0.0f);
        this.upperBody.zRot = ((Wolf)wolf).getBodyRollAngle(h, -0.08f);
        this.body.zRot = ((Wolf)wolf).getBodyRollAngle(h, -0.16f);
        this.realTail.zRot = ((Wolf)wolf).getBodyRollAngle(h, -0.2f);
    }

    @Override
    public void setupAnim(T wolf, float f, float g, float h, float i, float j) {
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        this.tail.xRot = h;
    }
}

