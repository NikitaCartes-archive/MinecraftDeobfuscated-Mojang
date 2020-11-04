/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Vex;

@Environment(value=EnvType.CLIENT)
public class VexModel
extends HumanoidModel<Vex> {
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public VexModel(ModelPart modelPart) {
        super(modelPart);
        this.leftLeg.visible = false;
        this.hat.visible = false;
        this.rightWing = modelPart.getChild("right_wing");
        this.leftWing = modelPart.getChild("left_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0f, -1.0f, -2.0f, 6.0f, 10.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 32).addBox(-20.0f, 0.0f, 0.0f, 20.0f, 12.0f, 1.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(0.0f, 0.0f, 0.0f, 20.0f, 12.0f, 1.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.rightWing, this.leftWing));
    }

    @Override
    public void setupAnim(Vex vex, float f, float g, float h, float i, float j) {
        super.setupAnim(vex, f, g, h, i, j);
        if (vex.isCharging()) {
            if (vex.getMainHandItem().isEmpty()) {
                this.rightArm.xRot = 4.712389f;
                this.leftArm.xRot = 4.712389f;
            } else if (vex.getMainArm() == HumanoidArm.RIGHT) {
                this.rightArm.xRot = 3.7699115f;
            } else {
                this.leftArm.xRot = 3.7699115f;
            }
        }
        this.rightLeg.xRot += 0.62831855f;
        this.rightWing.z = 2.0f;
        this.leftWing.z = 2.0f;
        this.rightWing.y = 1.0f;
        this.leftWing.y = 1.0f;
        this.rightWing.yRot = 0.47123894f + Mth.cos(h * 0.8f) * (float)Math.PI * 0.05f;
        this.leftWing.yRot = -this.rightWing.yRot;
        this.leftWing.zRot = -0.47123894f;
        this.leftWing.xRot = 0.47123894f;
        this.rightWing.xRot = 0.47123894f;
        this.rightWing.zRot = 0.47123894f;
    }
}

