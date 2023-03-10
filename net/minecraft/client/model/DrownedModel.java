/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ZombieModel;
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
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(value=EnvType.CLIENT)
public class DrownedModel<T extends Zombie>
extends ZombieModel<T> {
    public DrownedModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(cubeDeformation, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(5.0f, 2.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void prepareMobModel(T zombie, float f, float g, float h) {
        this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        ItemStack itemStack = ((LivingEntity)zombie).getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.is(Items.TRIDENT) && ((Mob)zombie).isAggressive()) {
            if (((Mob)zombie).getMainArm() == HumanoidArm.RIGHT) {
                this.rightArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
            } else {
                this.leftArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
            }
        }
        super.prepareMobModel(zombie, f, g, h);
    }

    @Override
    public void setupAnim(T zombie, float f, float g, float h, float i, float j) {
        super.setupAnim(zombie, f, g, h, i, j);
        if (this.leftArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
            this.leftArm.xRot = this.leftArm.xRot * 0.5f - (float)Math.PI;
            this.leftArm.yRot = 0.0f;
        }
        if (this.rightArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
            this.rightArm.xRot = this.rightArm.xRot * 0.5f - (float)Math.PI;
            this.rightArm.yRot = 0.0f;
        }
        if (this.swimAmount > 0.0f) {
            this.rightArm.xRot = this.rotlerpRad(this.swimAmount, this.rightArm.xRot, -2.5132742f) + this.swimAmount * 0.35f * Mth.sin(0.1f * h);
            this.leftArm.xRot = this.rotlerpRad(this.swimAmount, this.leftArm.xRot, -2.5132742f) - this.swimAmount * 0.35f * Mth.sin(0.1f * h);
            this.rightArm.zRot = this.rotlerpRad(this.swimAmount, this.rightArm.zRot, -0.15f);
            this.leftArm.zRot = this.rotlerpRad(this.swimAmount, this.leftArm.zRot, 0.15f);
            this.leftLeg.xRot -= this.swimAmount * 0.55f * Mth.sin(0.1f * h);
            this.rightLeg.xRot += this.swimAmount * 0.55f * Mth.sin(0.1f * h);
            this.head.xRot = 0.0f;
        }
    }
}

