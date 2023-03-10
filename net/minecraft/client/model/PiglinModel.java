/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;

@Environment(value=EnvType.CLIENT)
public class PiglinModel<T extends Mob>
extends PlayerModel<T> {
    public final ModelPart rightEar;
    private final ModelPart leftEar;
    private final PartPose bodyDefault;
    private final PartPose headDefault;
    private final PartPose leftArmDefault;
    private final PartPose rightArmDefault;

    public PiglinModel(ModelPart modelPart) {
        super(modelPart, false);
        this.rightEar = this.head.getChild("right_ear");
        this.leftEar = this.head.getChild("left_ear");
        this.bodyDefault = this.body.storePose();
        this.headDefault = this.head.storePose();
        this.leftArmDefault = this.leftArm.storePose();
        this.rightArmDefault = this.rightArm.storePose();
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = PlayerModel.createMesh(cubeDeformation, false);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, cubeDeformation), PartPose.ZERO);
        PiglinModel.addHead(cubeDeformation, meshDefinition);
        partDefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        return meshDefinition;
    }

    public static void addHead(CubeDeformation cubeDeformation, MeshDefinition meshDefinition) {
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, -8.0f, -4.0f, 10.0f, 8.0f, 8.0f, cubeDeformation).texOffs(31, 1).addBox(-2.0f, -4.0f, -5.0f, 4.0f, 4.0f, 1.0f, cubeDeformation).texOffs(2, 4).addBox(2.0f, -2.0f, -5.0f, 1.0f, 2.0f, 1.0f, cubeDeformation).texOffs(2, 0).addBox(-3.0f, -2.0f, -5.0f, 1.0f, 2.0f, 1.0f, cubeDeformation), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(51, 6).addBox(0.0f, 0.0f, -2.0f, 1.0f, 5.0f, 4.0f, cubeDeformation), PartPose.offsetAndRotation(4.5f, -6.0f, 0.0f, 0.0f, 0.0f, -0.5235988f));
        partDefinition2.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(39, 6).addBox(-1.0f, 0.0f, -2.0f, 1.0f, 5.0f, 4.0f, cubeDeformation), PartPose.offsetAndRotation(-4.5f, -6.0f, 0.0f, 0.0f, 0.0f, 0.5235988f));
    }

    @Override
    public void setupAnim(T mob, float f, float g, float h, float i, float j) {
        this.body.loadPose(this.bodyDefault);
        this.head.loadPose(this.headDefault);
        this.leftArm.loadPose(this.leftArmDefault);
        this.rightArm.loadPose(this.rightArmDefault);
        super.setupAnim(mob, f, g, h, i, j);
        float k = 0.5235988f;
        float l = h * 0.1f + f * 0.5f;
        float m = 0.08f + g * 0.4f;
        this.leftEar.zRot = -0.5235988f - Mth.cos(l * 1.2f) * m;
        this.rightEar.zRot = 0.5235988f + Mth.cos(l) * m;
        if (mob instanceof AbstractPiglin) {
            AbstractPiglin abstractPiglin = (AbstractPiglin)mob;
            PiglinArmPose piglinArmPose = abstractPiglin.getArmPose();
            if (piglinArmPose == PiglinArmPose.DANCING) {
                float n = h / 60.0f;
                this.rightEar.zRot = 0.5235988f + (float)Math.PI / 180 * Mth.sin(n * 30.0f) * 10.0f;
                this.leftEar.zRot = -0.5235988f - (float)Math.PI / 180 * Mth.cos(n * 30.0f) * 10.0f;
                this.head.x = Mth.sin(n * 10.0f);
                this.head.y = Mth.sin(n * 40.0f) + 0.4f;
                this.rightArm.zRot = (float)Math.PI / 180 * (70.0f + Mth.cos(n * 40.0f) * 10.0f);
                this.leftArm.zRot = this.rightArm.zRot * -1.0f;
                this.rightArm.y = Mth.sin(n * 40.0f) * 0.5f + 1.5f;
                this.leftArm.y = Mth.sin(n * 40.0f) * 0.5f + 1.5f;
                this.body.y = Mth.sin(n * 40.0f) * 0.35f;
            } else if (piglinArmPose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && this.attackTime == 0.0f) {
                this.holdWeaponHigh(mob);
            } else if (piglinArmPose == PiglinArmPose.CROSSBOW_HOLD) {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, !((Mob)mob).isLeftHanded());
            } else if (piglinArmPose == PiglinArmPose.CROSSBOW_CHARGE) {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, mob, !((Mob)mob).isLeftHanded());
            } else if (piglinArmPose == PiglinArmPose.ADMIRING_ITEM) {
                this.head.xRot = 0.5f;
                this.head.yRot = 0.0f;
                if (((Mob)mob).isLeftHanded()) {
                    this.rightArm.yRot = -0.5f;
                    this.rightArm.xRot = -0.9f;
                } else {
                    this.leftArm.yRot = 0.5f;
                    this.leftArm.xRot = -0.9f;
                }
            }
        } else if (((Entity)mob).getType() == EntityType.ZOMBIFIED_PIGLIN) {
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, ((Mob)mob).isAggressive(), this.attackTime, h);
        }
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
        this.hat.copyFrom(this.head);
    }

    @Override
    protected void setupAttackAnimation(T mob, float f) {
        if (this.attackTime > 0.0f && mob instanceof Piglin && ((Piglin)mob).getArmPose() == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) {
            AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, mob, this.attackTime, f);
            return;
        }
        super.setupAttackAnimation(mob, f);
    }

    private void holdWeaponHigh(T mob) {
        if (((Mob)mob).isLeftHanded()) {
            this.leftArm.xRot = -1.8f;
        } else {
            this.rightArm.xRot = -1.8f;
        }
    }
}

