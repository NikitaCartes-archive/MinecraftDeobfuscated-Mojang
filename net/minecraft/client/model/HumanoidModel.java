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
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
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
    public final ModelPart head;
    public final ModelPart hat;
    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;
    public ArmPose leftArmPose = ArmPose.EMPTY;
    public ArmPose rightArmPose = ArmPose.EMPTY;
    public boolean crouching;
    public float swimAmount;

    public HumanoidModel(ModelPart modelPart) {
        this(modelPart, RenderType::entityCutoutNoCull);
    }

    public HumanoidModel(ModelPart modelPart, Function<ResourceLocation, RenderType> function) {
        super(function, true, 16.0f, 0.0f, 2.0f, 2.0f, 24.0f);
        this.head = modelPart.getChild("head");
        this.hat = modelPart.getChild("hat");
        this.body = modelPart.getChild("body");
        this.rightArm = modelPart.getChild("right_arm");
        this.leftArm = modelPart.getChild("left_arm");
        this.rightLeg = modelPart.getChild("right_leg");
        this.leftLeg = modelPart.getChild("left_leg");
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, float f) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, cubeDeformation.extend(0.5f)), PartPose.offset(0.0f, 0.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(-5.0f, 2.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(5.0f, 2.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(-1.9f, 12.0f + f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(1.9f, 12.0f + f, 0.0f));
        return meshDefinition;
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
        boolean bl4;
        boolean bl = ((LivingEntity)livingEntity).getFallFlyingTicks() > 4;
        boolean bl2 = ((LivingEntity)livingEntity).isVisuallySwimming();
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = bl ? -0.7853982f : (this.swimAmount > 0.0f ? (bl2 ? this.rotlerpRad(this.swimAmount, this.head.xRot, -0.7853982f) : this.rotlerpRad(this.swimAmount, this.head.xRot, j * ((float)Math.PI / 180))) : j * ((float)Math.PI / 180));
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
        this.leftArm.yRot = 0.0f;
        boolean bl3 = ((LivingEntity)livingEntity).getMainArm() == HumanoidArm.RIGHT;
        boolean bl5 = bl4 = bl3 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
        if (bl3 != bl4) {
            this.poseLeftArm(livingEntity);
            this.poseRightArm(livingEntity);
        } else {
            this.poseRightArm(livingEntity);
            this.poseLeftArm(livingEntity);
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
        if (this.swimAmount > 0.0f) {
            float o;
            float n;
            float l = f % 26.0f;
            HumanoidArm humanoidArm = this.getAttackArm(livingEntity);
            float m = humanoidArm == HumanoidArm.RIGHT && this.attackTime > 0.0f ? 0.0f : this.swimAmount;
            float f2 = n = humanoidArm == HumanoidArm.LEFT && this.attackTime > 0.0f ? 0.0f : this.swimAmount;
            if (l < 14.0f) {
                this.leftArm.xRot = this.rotlerpRad(n, this.leftArm.xRot, 0.0f);
                this.rightArm.xRot = Mth.lerp(m, this.rightArm.xRot, 0.0f);
                this.leftArm.yRot = this.rotlerpRad(n, this.leftArm.yRot, (float)Math.PI);
                this.rightArm.yRot = Mth.lerp(m, this.rightArm.yRot, (float)Math.PI);
                this.leftArm.zRot = this.rotlerpRad(n, this.leftArm.zRot, (float)Math.PI + 1.8707964f * this.quadraticArmUpdate(l) / this.quadraticArmUpdate(14.0f));
                this.rightArm.zRot = Mth.lerp(m, this.rightArm.zRot, (float)Math.PI - 1.8707964f * this.quadraticArmUpdate(l) / this.quadraticArmUpdate(14.0f));
            } else if (l >= 14.0f && l < 22.0f) {
                o = (l - 14.0f) / 8.0f;
                this.leftArm.xRot = this.rotlerpRad(n, this.leftArm.xRot, 1.5707964f * o);
                this.rightArm.xRot = Mth.lerp(m, this.rightArm.xRot, 1.5707964f * o);
                this.leftArm.yRot = this.rotlerpRad(n, this.leftArm.yRot, (float)Math.PI);
                this.rightArm.yRot = Mth.lerp(m, this.rightArm.yRot, (float)Math.PI);
                this.leftArm.zRot = this.rotlerpRad(n, this.leftArm.zRot, 5.012389f - 1.8707964f * o);
                this.rightArm.zRot = Mth.lerp(m, this.rightArm.zRot, 1.2707963f + 1.8707964f * o);
            } else if (l >= 22.0f && l < 26.0f) {
                o = (l - 22.0f) / 4.0f;
                this.leftArm.xRot = this.rotlerpRad(n, this.leftArm.xRot, 1.5707964f - 1.5707964f * o);
                this.rightArm.xRot = Mth.lerp(m, this.rightArm.xRot, 1.5707964f - 1.5707964f * o);
                this.leftArm.yRot = this.rotlerpRad(n, this.leftArm.yRot, (float)Math.PI);
                this.rightArm.yRot = Mth.lerp(m, this.rightArm.yRot, (float)Math.PI);
                this.leftArm.zRot = this.rotlerpRad(n, this.leftArm.zRot, (float)Math.PI);
                this.rightArm.zRot = Mth.lerp(m, this.rightArm.zRot, (float)Math.PI);
            }
            o = 0.3f;
            float p = 0.33333334f;
            this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3f * Mth.cos(f * 0.33333334f + (float)Math.PI));
            this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3f * Mth.cos(f * 0.33333334f));
        }
        this.hat.copyFrom(this.head);
    }

    private void poseRightArm(T livingEntity) {
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
                break;
            }
            case BOW_AND_ARROW: {
                this.rightArm.yRot = -0.1f + this.head.yRot;
                this.leftArm.yRot = 0.1f + this.head.yRot + 0.4f;
                this.rightArm.xRot = -1.5707964f + this.head.xRot;
                this.leftArm.xRot = -1.5707964f + this.head.xRot;
                break;
            }
            case CROSSBOW_CHARGE: {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, livingEntity, true);
                break;
            }
            case CROSSBOW_HOLD: {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
                break;
            }
            case SPYGLASS: {
                this.rightArm.xRot = Mth.clamp(this.head.xRot + AnimationUtils.getSpyglassArmXRot(this.rightArm), -2.4f, 3.3f);
                this.rightArm.yRot = Mth.clamp(this.head.yRot + -0.7853982f, -1.1f, 0.0f);
            }
        }
    }

    private void poseLeftArm(T livingEntity) {
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
                break;
            }
            case THROW_SPEAR: {
                this.leftArm.xRot = this.leftArm.xRot * 0.5f - (float)Math.PI;
                this.leftArm.yRot = 0.0f;
                break;
            }
            case BOW_AND_ARROW: {
                this.rightArm.yRot = -0.1f + this.head.yRot - 0.4f;
                this.leftArm.yRot = 0.1f + this.head.yRot;
                this.rightArm.xRot = -1.5707964f + this.head.xRot;
                this.leftArm.xRot = -1.5707964f + this.head.xRot;
                break;
            }
            case CROSSBOW_CHARGE: {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, livingEntity, false);
                break;
            }
            case CROSSBOW_HOLD: {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
                break;
            }
            case SPYGLASS: {
                this.leftArm.xRot = AnimationUtils.getSpyglassArmXRot(this.leftArm);
                this.leftArm.yRot = 0.7853982f;
            }
        }
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
        float i = (h - g) % ((float)Math.PI * 2);
        if (i < (float)(-Math.PI)) {
            i += (float)Math.PI * 2;
        }
        if (i >= (float)Math.PI) {
            i -= (float)Math.PI * 2;
        }
        return g + f * i;
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
        humanoidModel.head.copyFrom(this.head);
        humanoidModel.hat.copyFrom(this.hat);
        humanoidModel.body.copyFrom(this.body);
        humanoidModel.rightArm.copyFrom(this.rightArm);
        humanoidModel.leftArm.copyFrom(this.leftArm);
        humanoidModel.rightLeg.copyFrom(this.rightLeg);
        humanoidModel.leftLeg.copyFrom(this.leftLeg);
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

    private HumanoidArm getAttackArm(T livingEntity) {
        HumanoidArm humanoidArm = ((LivingEntity)livingEntity).getMainArm();
        return ((LivingEntity)livingEntity).swingingArm == InteractionHand.MAIN_HAND ? humanoidArm : humanoidArm.getOpposite();
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ArmPose {
        EMPTY(false),
        ITEM(false),
        BLOCK(false),
        BOW_AND_ARROW(true),
        THROW_SPEAR(false),
        CROSSBOW_CHARGE(true),
        CROSSBOW_HOLD(true),
        SPYGLASS(false);

        private final boolean twoHanded;

        private ArmPose(boolean bl) {
            this.twoHanded = bl;
        }

        public boolean isTwoHanded() {
            return this.twoHanded;
        }
    }
}

