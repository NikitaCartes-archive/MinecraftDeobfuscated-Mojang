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
import net.minecraft.world.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public class EndermanModel<T extends LivingEntity>
extends HumanoidModel<T> {
    public boolean carrying;
    public boolean creepy;

    public EndermanModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        float f = -14.0f;
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, -14.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartPose partPose = PartPose.offset(0.0f, -13.0f, 0.0f);
        partDefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(-0.5f)), partPose);
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), partPose);
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.offset(0.0f, -14.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 30.0f, 2.0f), PartPose.offset(-5.0f, -12.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0f, -2.0f, -1.0f, 2.0f, 30.0f, 2.0f), PartPose.offset(5.0f, -12.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 30.0f, 2.0f), PartPose.offset(-2.0f, -5.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0f, 0.0f, -1.0f, 2.0f, 30.0f, 2.0f), PartPose.offset(2.0f, -5.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T livingEntity, float f, float g, float h, float i, float j) {
        super.setupAnim(livingEntity, f, g, h, i, j);
        this.head.visible = true;
        int k = -14;
        this.body.xRot = 0.0f;
        this.body.y = -14.0f;
        this.body.z = -0.0f;
        this.rightLeg.xRot -= 0.0f;
        this.leftLeg.xRot -= 0.0f;
        this.rightArm.xRot *= 0.5f;
        this.leftArm.xRot *= 0.5f;
        this.rightLeg.xRot *= 0.5f;
        this.leftLeg.xRot *= 0.5f;
        float l = 0.4f;
        if (this.rightArm.xRot > 0.4f) {
            this.rightArm.xRot = 0.4f;
        }
        if (this.leftArm.xRot > 0.4f) {
            this.leftArm.xRot = 0.4f;
        }
        if (this.rightArm.xRot < -0.4f) {
            this.rightArm.xRot = -0.4f;
        }
        if (this.leftArm.xRot < -0.4f) {
            this.leftArm.xRot = -0.4f;
        }
        if (this.rightLeg.xRot > 0.4f) {
            this.rightLeg.xRot = 0.4f;
        }
        if (this.leftLeg.xRot > 0.4f) {
            this.leftLeg.xRot = 0.4f;
        }
        if (this.rightLeg.xRot < -0.4f) {
            this.rightLeg.xRot = -0.4f;
        }
        if (this.leftLeg.xRot < -0.4f) {
            this.leftLeg.xRot = -0.4f;
        }
        if (this.carrying) {
            this.rightArm.xRot = -0.5f;
            this.leftArm.xRot = -0.5f;
            this.rightArm.zRot = 0.05f;
            this.leftArm.zRot = -0.05f;
        }
        this.rightLeg.z = 0.0f;
        this.leftLeg.z = 0.0f;
        this.rightLeg.y = -5.0f;
        this.leftLeg.y = -5.0f;
        this.head.z = -0.0f;
        this.head.y = -13.0f;
        this.hat.x = this.head.x;
        this.hat.y = this.head.y;
        this.hat.z = this.head.z;
        this.hat.xRot = this.head.xRot;
        this.hat.yRot = this.head.yRot;
        this.hat.zRot = this.head.zRot;
        if (this.creepy) {
            float m = 1.0f;
            this.head.y -= 5.0f;
        }
        int n = -14;
        this.rightArm.setPos(-5.0f, -12.0f, 0.0f);
        this.leftArm.setPos(5.0f, -12.0f, 0.0f);
    }
}

