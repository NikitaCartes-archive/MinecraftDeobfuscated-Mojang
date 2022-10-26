/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Rabbit;

@Environment(value=EnvType.CLIENT)
public class RabbitModel<T extends Rabbit>
extends EntityModel<T> {
    private static final float REAR_JUMP_ANGLE = 50.0f;
    private static final float FRONT_JUMP_ANGLE = -40.0f;
    private static final String LEFT_HAUNCH = "left_haunch";
    private static final String RIGHT_HAUNCH = "right_haunch";
    private final ModelPart leftRearFoot;
    private final ModelPart rightRearFoot;
    private final ModelPart leftHaunch;
    private final ModelPart rightHaunch;
    private final ModelPart body;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart head;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart tail;
    private final ModelPart nose;
    private float jumpRotation;
    private static final float NEW_SCALE = 0.6f;

    public RabbitModel(ModelPart modelPart) {
        this.leftRearFoot = modelPart.getChild("left_hind_foot");
        this.rightRearFoot = modelPart.getChild("right_hind_foot");
        this.leftHaunch = modelPart.getChild(LEFT_HAUNCH);
        this.rightHaunch = modelPart.getChild(RIGHT_HAUNCH);
        this.body = modelPart.getChild("body");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.head = modelPart.getChild("head");
        this.rightEar = modelPart.getChild("right_ear");
        this.leftEar = modelPart.getChild("left_ear");
        this.tail = modelPart.getChild("tail");
        this.nose = modelPart.getChild("nose");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("left_hind_foot", CubeListBuilder.create().texOffs(26, 24).addBox(-1.0f, 5.5f, -3.7f, 2.0f, 1.0f, 7.0f), PartPose.offset(3.0f, 17.5f, 3.7f));
        partDefinition.addOrReplaceChild("right_hind_foot", CubeListBuilder.create().texOffs(8, 24).addBox(-1.0f, 5.5f, -3.7f, 2.0f, 1.0f, 7.0f), PartPose.offset(-3.0f, 17.5f, 3.7f));
        partDefinition.addOrReplaceChild(LEFT_HAUNCH, CubeListBuilder.create().texOffs(30, 15).addBox(-1.0f, 0.0f, 0.0f, 2.0f, 4.0f, 5.0f), PartPose.offsetAndRotation(3.0f, 17.5f, 3.7f, -0.34906584f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(RIGHT_HAUNCH, CubeListBuilder.create().texOffs(16, 15).addBox(-1.0f, 0.0f, 0.0f, 2.0f, 4.0f, 5.0f), PartPose.offsetAndRotation(-3.0f, 17.5f, 3.7f, -0.34906584f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -2.0f, -10.0f, 6.0f, 5.0f, 10.0f), PartPose.offsetAndRotation(0.0f, 19.0f, 8.0f, -0.34906584f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(8, 15).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f), PartPose.offsetAndRotation(3.0f, 17.0f, -1.0f, -0.17453292f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 15).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f), PartPose.offsetAndRotation(-3.0f, 17.0f, -1.0f, -0.17453292f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 0).addBox(-2.5f, -4.0f, -5.0f, 5.0f, 4.0f, 5.0f), PartPose.offset(0.0f, 16.0f, -1.0f));
        partDefinition.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(52, 0).addBox(-2.5f, -9.0f, -1.0f, 2.0f, 5.0f, 1.0f), PartPose.offsetAndRotation(0.0f, 16.0f, -1.0f, 0.0f, -0.2617994f, 0.0f));
        partDefinition.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(58, 0).addBox(0.5f, -9.0f, -1.0f, 2.0f, 5.0f, 1.0f), PartPose.offsetAndRotation(0.0f, 16.0f, -1.0f, 0.0f, 0.2617994f, 0.0f));
        partDefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(52, 6).addBox(-1.5f, -1.5f, 0.0f, 3.0f, 3.0f, 2.0f), PartPose.offsetAndRotation(0.0f, 20.0f, 7.0f, -0.3490659f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(32, 9).addBox(-0.5f, -2.5f, -5.5f, 1.0f, 1.0f, 1.0f), PartPose.offset(0.0f, 16.0f, -1.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        if (this.young) {
            float l = 1.5f;
            poseStack.pushPose();
            poseStack.scale(0.56666666f, 0.56666666f, 0.56666666f);
            poseStack.translate(0.0f, 1.375f, 0.125f);
            ImmutableList.of(this.head, this.leftEar, this.rightEar, this.nose).forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.scale(0.4f, 0.4f, 0.4f);
            poseStack.translate(0.0f, 2.25f, 0.0f);
            ImmutableList.of(this.leftRearFoot, this.rightRearFoot, this.leftHaunch, this.rightHaunch, this.body, this.leftFrontLeg, this.rightFrontLeg, this.tail).forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            poseStack.scale(0.6f, 0.6f, 0.6f);
            poseStack.translate(0.0f, 1.0f, 0.0f);
            ImmutableList.of(this.leftRearFoot, this.rightRearFoot, this.leftHaunch, this.rightHaunch, this.body, this.leftFrontLeg, this.rightFrontLeg, this.head, this.rightEar, this.leftEar, this.tail, this.nose, new ModelPart[0]).forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
            poseStack.popPose();
        }
    }

    @Override
    public void setupAnim(T rabbit, float f, float g, float h, float i, float j) {
        float k = h - (float)((Rabbit)rabbit).tickCount;
        this.nose.xRot = j * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        this.rightEar.xRot = j * ((float)Math.PI / 180);
        this.leftEar.xRot = j * ((float)Math.PI / 180);
        this.nose.yRot = i * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        this.rightEar.yRot = this.nose.yRot - 0.2617994f;
        this.leftEar.yRot = this.nose.yRot + 0.2617994f;
        this.jumpRotation = Mth.sin(((Rabbit)rabbit).getJumpCompletion(k) * (float)Math.PI);
        this.leftHaunch.xRot = (this.jumpRotation * 50.0f - 21.0f) * ((float)Math.PI / 180);
        this.rightHaunch.xRot = (this.jumpRotation * 50.0f - 21.0f) * ((float)Math.PI / 180);
        this.leftRearFoot.xRot = this.jumpRotation * 50.0f * ((float)Math.PI / 180);
        this.rightRearFoot.xRot = this.jumpRotation * 50.0f * ((float)Math.PI / 180);
        this.leftFrontLeg.xRot = (this.jumpRotation * -40.0f - 11.0f) * ((float)Math.PI / 180);
        this.rightFrontLeg.xRot = (this.jumpRotation * -40.0f - 11.0f) * ((float)Math.PI / 180);
    }

    @Override
    public void prepareMobModel(T rabbit, float f, float g, float h) {
        super.prepareMobModel(rabbit, f, g, h);
        this.jumpRotation = Mth.sin(((Rabbit)rabbit).getJumpCompletion(h) * (float)Math.PI);
    }
}

