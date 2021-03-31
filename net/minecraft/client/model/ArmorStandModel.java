/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.decoration.ArmorStand;

@Environment(value=EnvType.CLIENT)
public class ArmorStandModel
extends ArmorStandArmorModel {
    private static final String RIGHT_BODY_STICK = "right_body_stick";
    private static final String LEFT_BODY_STICK = "left_body_stick";
    private static final String SHOULDER_STICK = "shoulder_stick";
    private static final String BASE_PLATE = "base_plate";
    private final ModelPart rightBodyStick;
    private final ModelPart leftBodyStick;
    private final ModelPart shoulderStick;
    private final ModelPart basePlate;

    public ArmorStandModel(ModelPart modelPart) {
        super(modelPart);
        this.rightBodyStick = modelPart.getChild(RIGHT_BODY_STICK);
        this.leftBodyStick = modelPart.getChild(LEFT_BODY_STICK);
        this.shoulderStick = modelPart.getChild(SHOULDER_STICK);
        this.basePlate = modelPart.getChild(BASE_PLATE);
        this.hat.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -7.0f, -1.0f, 2.0f, 7.0f, 2.0f), PartPose.offset(0.0f, 1.0f, 0.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 26).addBox(-6.0f, 0.0f, -1.5f, 12.0f, 3.0f, 3.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 0).addBox(-2.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 16).mirror().addBox(0.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 11.0f, 2.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0f, 0.0f, -1.0f, 2.0f, 11.0f, 2.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild(RIGHT_BODY_STICK, CubeListBuilder.create().texOffs(16, 0).addBox(-3.0f, 3.0f, -1.0f, 2.0f, 7.0f, 2.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(LEFT_BODY_STICK, CubeListBuilder.create().texOffs(48, 16).addBox(1.0f, 3.0f, -1.0f, 2.0f, 7.0f, 2.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(SHOULDER_STICK, CubeListBuilder.create().texOffs(0, 48).addBox(-4.0f, 10.0f, -1.0f, 8.0f, 2.0f, 2.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild(BASE_PLATE, CubeListBuilder.create().texOffs(0, 32).addBox(-6.0f, 11.0f, -6.0f, 12.0f, 1.0f, 12.0f), PartPose.offset(0.0f, 12.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void prepareMobModel(ArmorStand armorStand, float f, float g, float h) {
        this.basePlate.xRot = 0.0f;
        this.basePlate.yRot = (float)Math.PI / 180 * -Mth.rotLerp(h, armorStand.yRotO, armorStand.yRot);
        this.basePlate.zRot = 0.0f;
    }

    @Override
    public void setupAnim(ArmorStand armorStand, float f, float g, float h, float i, float j) {
        super.setupAnim(armorStand, f, g, h, i, j);
        this.leftArm.visible = armorStand.isShowArms();
        this.rightArm.visible = armorStand.isShowArms();
        this.basePlate.visible = !armorStand.isNoBasePlate();
        this.rightBodyStick.xRot = (float)Math.PI / 180 * armorStand.getBodyPose().getX();
        this.rightBodyStick.yRot = (float)Math.PI / 180 * armorStand.getBodyPose().getY();
        this.rightBodyStick.zRot = (float)Math.PI / 180 * armorStand.getBodyPose().getZ();
        this.leftBodyStick.xRot = (float)Math.PI / 180 * armorStand.getBodyPose().getX();
        this.leftBodyStick.yRot = (float)Math.PI / 180 * armorStand.getBodyPose().getY();
        this.leftBodyStick.zRot = (float)Math.PI / 180 * armorStand.getBodyPose().getZ();
        this.shoulderStick.xRot = (float)Math.PI / 180 * armorStand.getBodyPose().getX();
        this.shoulderStick.yRot = (float)Math.PI / 180 * armorStand.getBodyPose().getY();
        this.shoulderStick.zRot = (float)Math.PI / 180 * armorStand.getBodyPose().getZ();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.rightBodyStick, this.leftBodyStick, this.shoulderStick, this.basePlate));
    }

    @Override
    public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
        ModelPart modelPart = this.getArm(humanoidArm);
        boolean bl = modelPart.visible;
        modelPart.visible = true;
        super.translateToHand(humanoidArm, poseStack);
        modelPart.visible = bl;
    }
}

