/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(value=EnvType.CLIENT)
public class SkeletonModel<T extends Mob>
extends HumanoidModel<T> {
    public SkeletonModel() {
        this(0.0f, false);
    }

    public SkeletonModel(float f, boolean bl) {
        super(f);
        if (!bl) {
            this.rightArm = new ModelPart(this, 40, 16);
            this.rightArm.addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f, f);
            this.rightArm.setPos(-5.0f, 2.0f, 0.0f);
            this.leftArm = new ModelPart(this, 40, 16);
            this.leftArm.mirror = true;
            this.leftArm.addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f, f);
            this.leftArm.setPos(5.0f, 2.0f, 0.0f);
            this.rightLeg = new ModelPart(this, 0, 16);
            this.rightLeg.addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f, f);
            this.rightLeg.setPos(-2.0f, 12.0f, 0.0f);
            this.leftLeg = new ModelPart(this, 0, 16);
            this.leftLeg.mirror = true;
            this.leftLeg.addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f, f);
            this.leftLeg.setPos(2.0f, 12.0f, 0.0f);
        }
    }

    @Override
    public void prepareMobModel(T mob, float f, float g, float h) {
        this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        ItemStack itemStack = ((LivingEntity)mob).getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.getItem() == Items.BOW && ((Mob)mob).isAggressive()) {
            if (((Mob)mob).getMainArm() == HumanoidArm.RIGHT) {
                this.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            } else {
                this.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        }
        super.prepareMobModel(mob, f, g, h);
    }

    @Override
    public void setupAnim(T mob, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(mob, f, g, h, i, j, k);
        ItemStack itemStack = ((LivingEntity)mob).getMainHandItem();
        if (((Mob)mob).isAggressive() && (itemStack.isEmpty() || itemStack.getItem() != Items.BOW)) {
            float l = Mth.sin(this.attackTime * (float)Math.PI);
            float m = Mth.sin((1.0f - (1.0f - this.attackTime) * (1.0f - this.attackTime)) * (float)Math.PI);
            this.rightArm.zRot = 0.0f;
            this.leftArm.zRot = 0.0f;
            this.rightArm.yRot = -(0.1f - l * 0.6f);
            this.leftArm.yRot = 0.1f - l * 0.6f;
            this.rightArm.xRot = -1.5707964f;
            this.leftArm.xRot = -1.5707964f;
            this.rightArm.xRot -= l * 1.2f - m * 0.4f;
            this.leftArm.xRot -= l * 1.2f - m * 0.4f;
            this.rightArm.zRot += Mth.cos(h * 0.09f) * 0.05f + 0.05f;
            this.leftArm.zRot -= Mth.cos(h * 0.09f) * 0.05f + 0.05f;
            this.rightArm.xRot += Mth.sin(h * 0.067f) * 0.05f;
            this.leftArm.xRot -= Mth.sin(h * 0.067f) * 0.05f;
        }
    }

    @Override
    public void translateToHand(float f, HumanoidArm humanoidArm, PoseStack poseStack) {
        float g = humanoidArm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        ModelPart modelPart = this.getArm(humanoidArm);
        modelPart.x += g;
        modelPart.translateAndRotate(poseStack, f);
        modelPart.x -= g;
    }
}

