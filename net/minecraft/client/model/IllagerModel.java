/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractIllager;

@Environment(value=EnvType.CLIENT)
public class IllagerModel<T extends AbstractIllager>
extends ListModel<T>
implements ArmedModel,
HeadedModel {
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart body;
    private final ModelPart arms;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public IllagerModel(float f, float g, int i, int j) {
        this.head = new ModelPart(this).setTexSize(i, j);
        this.head.setPos(0.0f, 0.0f + g, 0.0f);
        this.head.texOffs(0, 0).addBox(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f, f);
        this.hat = new ModelPart(this, 32, 0).setTexSize(i, j);
        this.hat.addBox(-4.0f, -10.0f, -4.0f, 8.0f, 12.0f, 8.0f, f + 0.45f);
        this.head.addChild(this.hat);
        this.hat.visible = false;
        ModelPart modelPart = new ModelPart(this).setTexSize(i, j);
        modelPart.setPos(0.0f, g - 2.0f, 0.0f);
        modelPart.texOffs(24, 0).addBox(-1.0f, -1.0f, -6.0f, 2.0f, 4.0f, 2.0f, f);
        this.head.addChild(modelPart);
        this.body = new ModelPart(this).setTexSize(i, j);
        this.body.setPos(0.0f, 0.0f + g, 0.0f);
        this.body.texOffs(16, 20).addBox(-4.0f, 0.0f, -3.0f, 8.0f, 12.0f, 6.0f, f);
        this.body.texOffs(0, 38).addBox(-4.0f, 0.0f, -3.0f, 8.0f, 18.0f, 6.0f, f + 0.5f);
        this.arms = new ModelPart(this).setTexSize(i, j);
        this.arms.setPos(0.0f, 0.0f + g + 2.0f, 0.0f);
        this.arms.texOffs(44, 22).addBox(-8.0f, -2.0f, -2.0f, 4.0f, 8.0f, 4.0f, f);
        ModelPart modelPart2 = new ModelPart(this, 44, 22).setTexSize(i, j);
        modelPart2.mirror = true;
        modelPart2.addBox(4.0f, -2.0f, -2.0f, 4.0f, 8.0f, 4.0f, f);
        this.arms.addChild(modelPart2);
        this.arms.texOffs(40, 38).addBox(-4.0f, 2.0f, -2.0f, 8.0f, 4.0f, 4.0f, f);
        this.leftLeg = new ModelPart(this, 0, 22).setTexSize(i, j);
        this.leftLeg.setPos(-2.0f, 12.0f + g, 0.0f);
        this.leftLeg.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.rightLeg = new ModelPart(this, 0, 22).setTexSize(i, j);
        this.rightLeg.mirror = true;
        this.rightLeg.setPos(2.0f, 12.0f + g, 0.0f);
        this.rightLeg.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.rightArm = new ModelPart(this, 40, 46).setTexSize(i, j);
        this.rightArm.addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.rightArm.setPos(-5.0f, 2.0f + g, 0.0f);
        this.leftArm = new ModelPart(this, 40, 46).setTexSize(i, j);
        this.leftArm.mirror = true;
        this.leftArm.addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.leftArm.setPos(5.0f, 2.0f + g, 0.0f);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.head, this.body, this.leftLeg, this.rightLeg, this.arms, this.rightArm, this.leftArm);
    }

    @Override
    public void setupAnim(T abstractIllager, float f, float g, float h, float i, float j) {
        boolean bl;
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        this.arms.y = 3.0f;
        this.arms.z = -1.0f;
        this.arms.xRot = -0.75f;
        if (this.riding) {
            this.rightArm.xRot = -0.62831855f;
            this.rightArm.yRot = 0.0f;
            this.rightArm.zRot = 0.0f;
            this.leftArm.xRot = -0.62831855f;
            this.leftArm.yRot = 0.0f;
            this.leftArm.zRot = 0.0f;
            this.leftLeg.xRot = -1.4137167f;
            this.leftLeg.yRot = 0.31415927f;
            this.leftLeg.zRot = 0.07853982f;
            this.rightLeg.xRot = -1.4137167f;
            this.rightLeg.yRot = -0.31415927f;
            this.rightLeg.zRot = -0.07853982f;
        } else {
            this.rightArm.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 2.0f * g * 0.5f;
            this.rightArm.yRot = 0.0f;
            this.rightArm.zRot = 0.0f;
            this.leftArm.xRot = Mth.cos(f * 0.6662f) * 2.0f * g * 0.5f;
            this.leftArm.yRot = 0.0f;
            this.leftArm.zRot = 0.0f;
            this.leftLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * g * 0.5f;
            this.leftLeg.yRot = 0.0f;
            this.leftLeg.zRot = 0.0f;
            this.rightLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g * 0.5f;
            this.rightLeg.yRot = 0.0f;
            this.rightLeg.zRot = 0.0f;
        }
        AbstractIllager.IllagerArmPose illagerArmPose = ((AbstractIllager)abstractIllager).getArmPose();
        if (illagerArmPose == AbstractIllager.IllagerArmPose.ATTACKING) {
            float k = Mth.sin(this.attackTime * (float)Math.PI);
            float l = Mth.sin((1.0f - (1.0f - this.attackTime) * (1.0f - this.attackTime)) * (float)Math.PI);
            this.rightArm.zRot = 0.0f;
            this.leftArm.zRot = 0.0f;
            this.rightArm.yRot = 0.15707964f;
            this.leftArm.yRot = -0.15707964f;
            if (((Mob)abstractIllager).getMainArm() == HumanoidArm.RIGHT) {
                this.rightArm.xRot = -1.8849558f + Mth.cos(h * 0.09f) * 0.15f;
                this.leftArm.xRot = -0.0f + Mth.cos(h * 0.19f) * 0.5f;
                this.rightArm.xRot += k * 2.2f - l * 0.4f;
                this.leftArm.xRot += k * 1.2f - l * 0.4f;
            } else {
                this.rightArm.xRot = -0.0f + Mth.cos(h * 0.19f) * 0.5f;
                this.leftArm.xRot = -1.8849558f + Mth.cos(h * 0.09f) * 0.15f;
                this.rightArm.xRot += k * 1.2f - l * 0.4f;
                this.leftArm.xRot += k * 2.2f - l * 0.4f;
            }
            this.rightArm.zRot += Mth.cos(h * 0.09f) * 0.05f + 0.05f;
            this.leftArm.zRot -= Mth.cos(h * 0.09f) * 0.05f + 0.05f;
            this.rightArm.xRot += Mth.sin(h * 0.067f) * 0.05f;
            this.leftArm.xRot -= Mth.sin(h * 0.067f) * 0.05f;
        } else if (illagerArmPose == AbstractIllager.IllagerArmPose.SPELLCASTING) {
            this.rightArm.z = 0.0f;
            this.rightArm.x = -5.0f;
            this.leftArm.z = 0.0f;
            this.leftArm.x = 5.0f;
            this.rightArm.xRot = Mth.cos(h * 0.6662f) * 0.25f;
            this.leftArm.xRot = Mth.cos(h * 0.6662f) * 0.25f;
            this.rightArm.zRot = 2.3561945f;
            this.leftArm.zRot = -2.3561945f;
            this.rightArm.yRot = 0.0f;
            this.leftArm.yRot = 0.0f;
        } else if (illagerArmPose == AbstractIllager.IllagerArmPose.BOW_AND_ARROW) {
            this.rightArm.yRot = -0.1f + this.head.yRot;
            this.rightArm.xRot = -1.5707964f + this.head.xRot;
            this.leftArm.xRot = -0.9424779f + this.head.xRot;
            this.leftArm.yRot = this.head.yRot - 0.4f;
            this.leftArm.zRot = 1.5707964f;
        } else if (illagerArmPose == AbstractIllager.IllagerArmPose.CROSSBOW_HOLD) {
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
        } else if (illagerArmPose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE) {
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, abstractIllager, true);
        } else if (illagerArmPose == AbstractIllager.IllagerArmPose.CELEBRATING) {
            this.rightArm.z = 0.0f;
            this.rightArm.x = -5.0f;
            this.rightArm.xRot = Mth.cos(h * 0.6662f) * 0.05f;
            this.rightArm.zRot = 2.670354f;
            this.rightArm.yRot = 0.0f;
            this.leftArm.z = 0.0f;
            this.leftArm.x = 5.0f;
            this.leftArm.xRot = Mth.cos(h * 0.6662f) * 0.05f;
            this.leftArm.zRot = -2.3561945f;
            this.leftArm.yRot = 0.0f;
        }
        this.arms.visible = bl = illagerArmPose == AbstractIllager.IllagerArmPose.CROSSED;
        this.leftArm.visible = !bl;
        this.rightArm.visible = !bl;
    }

    private ModelPart getArm(HumanoidArm humanoidArm) {
        if (humanoidArm == HumanoidArm.LEFT) {
            return this.leftArm;
        }
        return this.rightArm;
    }

    public ModelPart getHat() {
        return this.hat;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
        this.getArm(humanoidArm).translateAndRotate(poseStack);
    }
}

