/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public class HumanoidModel<T extends LivingEntity>
extends AgeableListModel<T>
implements ArmedModel,
HeadedModel {
    public ModelPart head;
    public ModelPart hat;
    public ModelPart body;
    public ModelPart rightArm;
    public ModelPart leftArm;
    public ModelPart rightLeg;
    public ModelPart leftLeg;
    public ArmPose leftArmPose = ArmPose.EMPTY;
    public ArmPose rightArmPose = ArmPose.EMPTY;
    public boolean crouching;
    public float swimAmount;

    public HumanoidModel(float f) {
        this(RenderType::entityCutoutNoCull, f, 0.0f, 64, 32);
    }

    protected HumanoidModel(float f, float g, int i, int j) {
        this(RenderType::entityCutoutNoCull, f, g, i, j);
    }

    public HumanoidModel(Function<ResourceLocation, RenderType> function, float f, float g, int i, int j) {
        super(function, true, 16.0f, 0.0f, 2.0f, 2.0f, 24.0f);
        this.texWidth = i;
        this.texHeight = j;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, f);
        this.head.setPos(0.0f, 0.0f + g, 0.0f);
        this.hat = new ModelPart(this, 32, 0);
        this.hat.addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, f + 0.5f);
        this.hat.setPos(0.0f, 0.0f + g, 0.0f);
        this.body = new ModelPart(this, 16, 16);
        this.body.addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, f);
        this.body.setPos(0.0f, 0.0f + g, 0.0f);
        this.rightArm = new ModelPart(this, 40, 16);
        this.rightArm.addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.rightArm.setPos(-5.0f, 2.0f + g, 0.0f);
        this.leftArm = new ModelPart(this, 40, 16);
        this.leftArm.mirror = true;
        this.leftArm.addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.leftArm.setPos(5.0f, 2.0f + g, 0.0f);
        this.rightLeg = new ModelPart(this, 0, 16);
        this.rightLeg.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.rightLeg.setPos(-1.9f, 12.0f + g, 0.0f);
        this.leftLeg = new ModelPart(this, 0, 16);
        this.leftLeg.mirror = true;
        this.leftLeg.addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, f);
        this.leftLeg.setPos(1.9f, 12.0f + g, 0.0f);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
    }

    @Override
    public void prepareMobModel(T livingEntity, float f, float g, float h) {
        this.swimAmount = ((LivingEntity)livingEntity).getSwimAmount(h);
        super.prepareMobModel(livingEntity, f, g, h);
    }

    @Override
    public void setupAnim(T livingEntity, float f, float g, float h, float i, float j) {
        boolean bl = ((LivingEntity)livingEntity).getFallFlyingTicks() > 4;
        boolean bl2 = ((LivingEntity)livingEntity).isVisuallySwimming();
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = bl ? -0.7853982f : (this.swimAmount > 0.0f ? (bl2 ? this.rotlerpRad(this.head.xRot, -0.7853982f, this.swimAmount) : this.rotlerpRad(this.head.xRot, j * ((float)Math.PI / 180), this.swimAmount)) : j * ((float)Math.PI / 180));
        this.body.yRot = 0.0f;
        this.rightArm.z = 0.0f;
        this.rightArm.x = -5.0f;
        this.leftArm.z = 0.0f;
        this.leftArm.x = 5.0f;
        float k = 1.0f;
        if (bl) {
            k = (float)((Entity)livingEntity).getDeltaMovement().lengthSqr();
            k /= 0.2f;
            k *= k * k;
        }
        if (k < 1.0f) {
            k = 1.0f;
        }
        this.rightArm.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 2.0f * g * 0.5f / k;
        this.leftArm.xRot = Mth.cos(f * 0.6662f) * 2.0f * g * 0.5f / k;
        this.rightArm.zRot = 0.0f;
        this.leftArm.zRot = 0.0f;
        this.rightLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * g / k;
        this.leftLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g / k;
        this.rightLeg.yRot = 0.0f;
        this.leftLeg.yRot = 0.0f;
        this.rightLeg.zRot = 0.0f;
        this.leftLeg.zRot = 0.0f;
        if (this.riding) {
            this.rightArm.xRot += -0.62831855f;
            this.leftArm.xRot += -0.62831855f;
            this.rightLeg.xRot = -1.4137167f;
            this.rightLeg.yRot = 0.31415927f;
            this.rightLeg.zRot = 0.07853982f;
            this.leftLeg.xRot = -1.4137167f;
            this.leftLeg.yRot = -0.31415927f;
            this.leftLeg.zRot = -0.07853982f;
        }
        this.rightArm.yRot = 0.0f;
        this.rightArm.zRot = 0.0f;
        switch (this.leftArmPose) {
            case EMPTY: {
                this.leftArm.yRot = 0.0f;
                break;
            }
            case BLOCK: {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - 0.9424779f;
                this.leftArm.yRot = 0.5235988f;
                break;
            }
            case ITEM: {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - 0.31415927f;
                this.leftArm.yRot = 0.0f;
            }
        }
        switch (this.rightArmPose) {
            case EMPTY: {
                this.rightArm.yRot = 0.0f;
                break;
            }
            case BLOCK: {
                this.rightArm.xRot = this.rightArm.xRot * 0.5f - 0.9424779f;
                this.rightArm.yRot = -0.5235988f;
                break;
            }
            case ITEM: {
                this.rightArm.xRot = this.rightArm.xRot * 0.5f - 0.31415927f;
                this.rightArm.yRot = 0.0f;
                break;
            }
            case THROW_SPEAR: {
                this.rightArm.xRot = this.rightArm.xRot * 0.5f - (float)Math.PI;
                this.rightArm.yRot = 0.0f;
            }
        }
        if (this.leftArmPose == ArmPose.THROW_SPEAR && this.rightArmPose != ArmPose.BLOCK && this.rightArmPose != ArmPose.THROW_SPEAR && this.rightArmPose != ArmPose.BOW_AND_ARROW) {
            this.leftArm.xRot = this.leftArm.xRot * 0.5f - (float)Math.PI;
            this.leftArm.yRot = 0.0f;
        }
        this.setupAttackAnimation(livingEntity, h);
        if (this.crouching) {
            this.body.xRot = 0.5f;
            this.rightArm.xRot += 0.4f;
            this.leftArm.xRot += 0.4f;
            this.rightLeg.z = 4.0f;
            this.leftLeg.z = 4.0f;
            this.rightLeg.y = 12.2f;
            this.leftLeg.y = 12.2f;
            this.head.y = 4.2f;
            this.body.y = 3.2f;
            this.leftArm.y = 5.2f;
            this.rightArm.y = 5.2f;
        } else {
            this.body.xRot = 0.0f;
            this.rightLeg.z = 0.1f;
            this.leftLeg.z = 0.1f;
            this.rightLeg.y = 12.0f;
            this.leftLeg.y = 12.0f;
            this.head.y = 0.0f;
            this.body.y = 0.0f;
            this.leftArm.y = 2.0f;
            this.rightArm.y = 2.0f;
        }
        AnimationUtils.bobArms(this.rightArm, this.leftArm, h);
        if (this.rightArmPose == ArmPose.BOW_AND_ARROW) {
            this.rightArm.yRot = -0.1f + this.head.yRot;
            this.leftArm.yRot = 0.1f + this.head.yRot + 0.4f;
            this.rightArm.xRot = -1.5707964f + this.head.xRot;
            this.leftArm.xRot = -1.5707964f + this.head.xRot;
        } else if (this.leftArmPose == ArmPose.BOW_AND_ARROW && this.rightArmPose != ArmPose.THROW_SPEAR && this.rightArmPose != ArmPose.BLOCK) {
            this.rightArm.yRot = -0.1f + this.head.yRot - 0.4f;
            this.leftArm.yRot = 0.1f + this.head.yRot;
            this.rightArm.xRot = -1.5707964f + this.head.xRot;
            this.leftArm.xRot = -1.5707964f + this.head.xRot;
        }
        if (this.rightArmPose == ArmPose.CROSSBOW_CHARGE) {
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, livingEntity, true);
        } else if (this.leftArmPose == ArmPose.CROSSBOW_CHARGE) {
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, livingEntity, false);
        }
        if (this.rightArmPose == ArmPose.CROSSBOW_HOLD && this.attackTime <= 0.0f) {
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
        } else if (this.leftArmPose == ArmPose.CROSSBOW_HOLD) {
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
        }
        if (this.swimAmount > 0.0f) {
            float n;
            float m;
            float l = f % 26.0f;
            float f2 = m = this.attackTime > 0.0f ? 0.0f : this.swimAmount;
            if (l < 14.0f) {
                this.leftArm.xRot = this.rotlerpRad(this.leftArm.xRot, 0.0f, this.swimAmount);
                this.rightArm.xRot = Mth.lerp(m, this.rightArm.xRot, 0.0f);
                this.leftArm.yRot = this.rotlerpRad(this.leftArm.yRot, (float)Math.PI, this.swimAmount);
                this.rightArm.yRot = Mth.lerp(m, this.rightArm.yRot, (float)Math.PI);
                this.leftArm.zRot = this.rotlerpRad(this.leftArm.zRot, (float)Math.PI + 1.8707964f * this.quadraticArmUpdate(l) / this.quadraticArmUpdate(14.0f), this.swimAmount);
                this.rightArm.zRot = Mth.lerp(m, this.rightArm.zRot, (float)Math.PI - 1.8707964f * this.quadraticArmUpdate(l) / this.quadraticArmUpdate(14.0f));
            } else if (l >= 14.0f && l < 22.0f) {
                n = (l - 14.0f) / 8.0f;
                this.leftArm.xRot = this.rotlerpRad(this.leftArm.xRot, 1.5707964f * n, this.swimAmount);
                this.rightArm.xRot = Mth.lerp(m, this.rightArm.xRot, 1.5707964f * n);
                this.leftArm.yRot = this.rotlerpRad(this.leftArm.yRot, (float)Math.PI, this.swimAmount);
                this.rightArm.yRot = Mth.lerp(m, this.rightArm.yRot, (float)Math.PI);
                this.leftArm.zRot = this.rotlerpRad(this.leftArm.zRot, 5.012389f - 1.8707964f * n, this.swimAmount);
                this.rightArm.zRot = Mth.lerp(m, this.rightArm.zRot, 1.2707963f + 1.8707964f * n);
            } else if (l >= 22.0f && l < 26.0f) {
                n = (l - 22.0f) / 4.0f;
                this.leftArm.xRot = this.rotlerpRad(this.leftArm.xRot, 1.5707964f - 1.5707964f * n, this.swimAmount);
                this.rightArm.xRot = Mth.lerp(m, this.rightArm.xRot, 1.5707964f - 1.5707964f * n);
                this.leftArm.yRot = this.rotlerpRad(this.leftArm.yRot, (float)Math.PI, this.swimAmount);
                this.rightArm.yRot = Mth.lerp(m, this.rightArm.yRot, (float)Math.PI);
                this.leftArm.zRot = this.rotlerpRad(this.leftArm.zRot, (float)Math.PI, this.swimAmount);
                this.rightArm.zRot = Mth.lerp(m, this.rightArm.zRot, (float)Math.PI);
            }
            n = 0.3f;
            float o = 0.33333334f;
            this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3f * Mth.cos(f * 0.33333334f + (float)Math.PI));
            this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3f * Mth.cos(f * 0.33333334f));
        }
        this.hat.copyFrom(this.head);
    }

    protected void setupAttackAnimation(T livingEntity, float f) {
        if (this.attackTime <= 0.0f) {
            return;
        }
        HumanoidArm humanoidArm = this.getAttackArm(livingEntity);
        ModelPart modelPart = this.getArm(humanoidArm);
        float g = this.attackTime;
        this.body.yRot = Mth.sin(Mth.sqrt(g) * ((float)Math.PI * 2)) * 0.2f;
        if (humanoidArm == HumanoidArm.LEFT) {
            this.body.yRot *= -1.0f;
        }
        this.rightArm.z = Mth.sin(this.body.yRot) * 5.0f;
        this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0f;
        this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0f;
        this.leftArm.x = Mth.cos(this.body.yRot) * 5.0f;
        this.rightArm.yRot += this.body.yRot;
        this.leftArm.yRot += this.body.yRot;
        this.leftArm.xRot += this.body.yRot;
        g = 1.0f - this.attackTime;
        g *= g;
        g *= g;
        g = 1.0f - g;
        float h = Mth.sin(g * (float)Math.PI);
        float i = Mth.sin(this.attackTime * (float)Math.PI) * -(this.head.xRot - 0.7f) * 0.75f;
        modelPart.xRot = (float)((double)modelPart.xRot - ((double)h * 1.2 + (double)i));
        modelPart.yRot += this.body.yRot * 2.0f;
        modelPart.zRot += Mth.sin(this.attackTime * (float)Math.PI) * -0.4f;
    }

    protected float rotlerpRad(float f, float g, float h) {
        float i = (g - f) % ((float)Math.PI * 2);
        if (i < (float)(-Math.PI)) {
            i += (float)Math.PI * 2;
        }
        if (i >= (float)Math.PI) {
            i -= (float)Math.PI * 2;
        }
        return f + h * i;
    }

    private float quadraticArmUpdate(float f) {
        return -65.0f * f + f * f;
    }

    @Override
    public void copyPropertiesTo(HumanoidModel<T> humanoidModel) {
        super.copyPropertiesTo(humanoidModel);
        humanoidModel.leftArmPose = this.leftArmPose;
        humanoidModel.rightArmPose = this.rightArmPose;
        humanoidModel.crouching = this.crouching;
    }

    public void setAllVisible(boolean bl) {
        this.head.visible = bl;
        this.hat.visible = bl;
        this.body.visible = bl;
        this.rightArm.visible = bl;
        this.leftArm.visible = bl;
        this.rightLeg.visible = bl;
        this.leftLeg.visible = bl;
    }

    @Override
    public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
        this.getArm(humanoidArm).translateAndRotate(poseStack);
    }

    protected ModelPart getArm(HumanoidArm humanoidArm) {
        if (humanoidArm == HumanoidArm.LEFT) {
            return this.leftArm;
        }
        return this.rightArm;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    protected HumanoidArm getAttackArm(T livingEntity) {
        HumanoidArm humanoidArm = ((LivingEntity)livingEntity).getMainArm();
        return ((LivingEntity)livingEntity).swingingArm == InteractionHand.MAIN_HAND ? humanoidArm : humanoidArm.getOpposite();
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ArmPose {
        EMPTY,
        ITEM,
        BLOCK,
        BOW_AND_ARROW,
        THROW_SPEAR,
        CROSSBOW_CHARGE,
        CROSSBOW_HOLD;

    }
}

