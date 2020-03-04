/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;

@Environment(value=EnvType.CLIENT)
public class PiglinModel<T extends Mob>
extends HumanoidModel<T> {
    public final ModelPart earRight;
    public final ModelPart earLeft;

    public PiglinModel(float f, int i, int j) {
        super(f, 0.0f, i, j);
        this.body = new ModelPart(this, 16, 16);
        this.body.addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, f);
        this.head = new ModelPart(this);
        this.head.texOffs(0, 0).addBox(-5.0f, -8.0f, -4.0f, 10.0f, 8.0f, 8.0f, f);
        this.head.texOffs(31, 1).addBox(-2.0f, -4.0f, -5.0f, 4.0f, 4.0f, 1.0f, f);
        this.head.texOffs(2, 4).addBox(2.0f, -2.0f, -5.0f, 1.0f, 2.0f, 1.0f, f);
        this.head.texOffs(2, 0).addBox(-3.0f, -2.0f, -5.0f, 1.0f, 2.0f, 1.0f, f);
        this.earRight = new ModelPart(this);
        this.earRight.setPos(4.5f, -6.0f, 0.0f);
        this.earRight.texOffs(57, 38).addBox(0.0f, 0.0f, -2.0f, 1.0f, 5.0f, 4.0f, f);
        this.head.addChild(this.earRight);
        this.earLeft = new ModelPart(this);
        this.earLeft.setPos(-4.5f, -6.0f, 0.0f);
        this.head.addChild(this.earLeft);
        this.earLeft.texOffs(57, 22).addBox(-1.0f, 0.0f, -2.0f, 1.0f, 5.0f, 4.0f, f);
        this.hat = new ModelPart(this);
        this.rightArm = new ModelPart(this);
        this.rightArm.setPos(-5.0f, 2.0f, 0.0f);
        this.rightArm.texOffs(40, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.leftArm = new ModelPart(this);
        this.leftArm.setPos(5.0f, 2.0f, 0.0f);
        this.leftArm.texOffs(40, 16).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.rightLeg = new ModelPart(this);
        this.rightLeg.setPos(-1.9f, 12.0f, 0.0f);
        this.rightLeg.texOffs(0, 16).addBox(-2.1f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.leftLeg = new ModelPart(this);
        this.leftLeg.setPos(1.9f, 12.0f, 0.0f);
        this.leftLeg.texOffs(0, 16).addBox(-1.9f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
    }

    @Override
    public void setupAnim(T mob, float f, float g, float h, float i, float j) {
        super.setupAnim(mob, f, g, h, i, j);
        float k = 0.5235988f;
        float l = h * 0.1f + f * 0.5f;
        float m = 0.08f + g * 0.4f;
        this.earRight.zRot = -0.5235988f - Mth.cos(l * 1.2f) * m;
        this.earLeft.zRot = 0.5235988f + Mth.cos(l) * m;
        if (mob instanceof Piglin) {
            Piglin piglin = (Piglin)mob;
            Piglin.PiglinArmPose piglinArmPose = piglin.getArmPose();
            if (piglinArmPose == Piglin.PiglinArmPose.CROSSBOW_HOLD) {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
            } else if (piglinArmPose == Piglin.PiglinArmPose.CROSSBOW_CHARGE) {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, mob, true);
            } else if (piglinArmPose == Piglin.PiglinArmPose.ADMIRING_ITEM) {
                this.leftArm.yRot = 0.5f;
                this.leftArm.xRot = -0.9f;
                this.head.xRot = 0.5f;
                this.head.yRot = 0.0f;
            }
        }
    }
}

