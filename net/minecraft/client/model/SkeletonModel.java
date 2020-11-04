/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
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
    public SkeletonModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-2.0f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(2.0f, 12.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void prepareMobModel(T mob, float f, float g, float h) {
        this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        ItemStack itemStack = ((LivingEntity)mob).getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.is(Items.BOW) && ((Mob)mob).isAggressive()) {
            if (((Mob)mob).getMainArm() == HumanoidArm.RIGHT) {
                this.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            } else {
                this.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        }
        super.prepareMobModel(mob, f, g, h);
    }

    @Override
    public void setupAnim(T mob, float f, float g, float h, float i, float j) {
        super.setupAnim(mob, f, g, h, i, j);
        ItemStack itemStack = ((LivingEntity)mob).getMainHandItem();
        if (((Mob)mob).isAggressive() && (itemStack.isEmpty() || !itemStack.is(Items.BOW))) {
            float k = Mth.sin(this.attackTime * (float)Math.PI);
            float l = Mth.sin((1.0f - (1.0f - this.attackTime) * (1.0f - this.attackTime)) * (float)Math.PI);
            this.rightArm.zRot = 0.0f;
            this.leftArm.zRot = 0.0f;
            this.rightArm.yRot = -(0.1f - k * 0.6f);
            this.leftArm.yRot = 0.1f - k * 0.6f;
            this.rightArm.xRot = -1.5707964f;
            this.leftArm.xRot = -1.5707964f;
            this.rightArm.xRot -= k * 1.2f - l * 0.4f;
            this.leftArm.xRot -= k * 1.2f - l * 0.4f;
            AnimationUtils.bobArms(this.rightArm, this.leftArm, h);
        }
    }

    @Override
    public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
        float f = humanoidArm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        ModelPart modelPart = this.getArm(humanoidArm);
        modelPart.x += f;
        modelPart.translateAndRotate(poseStack);
        modelPart.x -= f;
    }
}

