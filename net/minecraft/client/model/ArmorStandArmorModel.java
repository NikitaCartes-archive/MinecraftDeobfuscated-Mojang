/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.decoration.ArmorStand;

@Environment(value=EnvType.CLIENT)
public class ArmorStandArmorModel
extends HumanoidModel<ArmorStand> {
    public ArmorStandArmorModel(float f) {
        this(f, 64, 32);
    }

    protected ArmorStandArmorModel(float f, int i, int j) {
        super(f, 0.0f, i, j);
    }

    @Override
    public void setupAnim(ArmorStand armorStand, float f, float g, float h, float i, float j) {
        this.head.xRot = (float)Math.PI / 180 * armorStand.getHeadPose().getX();
        this.head.yRot = (float)Math.PI / 180 * armorStand.getHeadPose().getY();
        this.head.zRot = (float)Math.PI / 180 * armorStand.getHeadPose().getZ();
        this.head.setPos(0.0f, 1.0f, 0.0f);
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
        this.leftLeg.setPos(1.9f, 11.0f, 0.0f);
        this.rightLeg.xRot = (float)Math.PI / 180 * armorStand.getRightLegPose().getX();
        this.rightLeg.yRot = (float)Math.PI / 180 * armorStand.getRightLegPose().getY();
        this.rightLeg.zRot = (float)Math.PI / 180 * armorStand.getRightLegPose().getZ();
        this.rightLeg.setPos(-1.9f, 11.0f, 0.0f);
        this.hat.copyFrom(this.head);
    }
}

