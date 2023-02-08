/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.decoration.ArmorStand;

@Environment(value=EnvType.CLIENT)
public class ArmorStandArmorModel
extends HumanoidModel<ArmorStand> {
    public ArmorStandArmorModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(cubeDeformation, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation), PartPose.offset(0.0f, 1.0f, 0.0f));
        partDefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation.extend(0.5f)), PartPose.offset(0.0f, 1.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(-0.1f)), PartPose.offset(-1.9f, 11.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(-0.1f)), PartPose.offset(1.9f, 11.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(ArmorStand armorStand, float f, float g, float h, float i, float j) {
        this.head.xRot = (float)Math.PI / 180 * armorStand.getHeadPose().getX();
        this.head.yRot = (float)Math.PI / 180 * armorStand.getHeadPose().getY();
        this.head.zRot = (float)Math.PI / 180 * armorStand.getHeadPose().getZ();
        this.body.xRot = (float)Math.PI / 180 * armorStand.getBodyPose().getX();
        this.body.yRot = (float)Math.PI / 180 * armorStand.getBodyPose().getY();
        this.body.zRot = (float)Math.PI / 180 * armorStand.getBodyPose().getZ();
        this.leftArm.xRot = (float)Math.PI / 180 * armorStand.getLeftArmPose().getX();
        this.leftArm.yRot = (float)Math.PI / 180 * armorStand.getLeftArmPose().getY();
        this.leftArm.zRot = (float)Math.PI / 180 * armorStand.getLeftArmPose().getZ();
        this.rightArm.xRot = (float)Math.PI / 180 * armorStand.getRightArmPose().getX();
        this.rightArm.yRot = (float)Math.PI / 180 * armorStand.getRightArmPose().getY();
        this.rightArm.zRot = (float)Math.PI / 180 * armorStand.getRightArmPose().getZ();
        this.leftLeg.xRot = (float)Math.PI / 180 * armorStand.getLeftLegPose().getX();
        this.leftLeg.yRot = (float)Math.PI / 180 * armorStand.getLeftLegPose().getY();
        this.leftLeg.zRot = (float)Math.PI / 180 * armorStand.getLeftLegPose().getZ();
        this.rightLeg.xRot = (float)Math.PI / 180 * armorStand.getRightLegPose().getX();
        this.rightLeg.yRot = (float)Math.PI / 180 * armorStand.getRightLegPose().getY();
        this.rightLeg.zRot = (float)Math.PI / 180 * armorStand.getRightLegPose().getZ();
        this.hat.copyFrom(this.head);
    }
}

