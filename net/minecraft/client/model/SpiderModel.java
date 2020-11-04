/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class SpiderModel<T extends Entity>
extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightMiddleHindLeg;
    private final ModelPart leftMiddleHindLeg;
    private final ModelPart rightMiddleFrontLeg;
    private final ModelPart leftMiddleFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public SpiderModel(ModelPart modelPart) {
        this.root = modelPart;
        this.head = modelPart.getChild("head");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightMiddleHindLeg = modelPart.getChild("right_middle_hind_leg");
        this.leftMiddleHindLeg = modelPart.getChild("left_middle_hind_leg");
        this.rightMiddleFrontLeg = modelPart.getChild("right_middle_front_leg");
        this.leftMiddleFrontLeg = modelPart.getChild("left_middle_front_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static LayerDefinition createSpiderBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 15;
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 4).addBox(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 15.0f, -3.0f));
        partDefinition.addOrReplaceChild("body0", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f), PartPose.offset(0.0f, 15.0f, 0.0f));
        partDefinition.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 12).addBox(-5.0f, -4.0f, -6.0f, 10.0f, 8.0f, 12.0f), PartPose.offset(0.0f, 15.0f, 9.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(18, 0).addBox(-15.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f);
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(18, 0).addBox(-1.0f, -1.0f, -1.0f, 16.0f, 2.0f, 2.0f);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-4.0f, 15.0f, 2.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder2, PartPose.offset(4.0f, 15.0f, 2.0f));
        partDefinition.addOrReplaceChild("right_middle_hind_leg", cubeListBuilder, PartPose.offset(-4.0f, 15.0f, 1.0f));
        partDefinition.addOrReplaceChild("left_middle_hind_leg", cubeListBuilder2, PartPose.offset(4.0f, 15.0f, 1.0f));
        partDefinition.addOrReplaceChild("right_middle_front_leg", cubeListBuilder, PartPose.offset(-4.0f, 15.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_middle_front_leg", cubeListBuilder2, PartPose.offset(4.0f, 15.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-4.0f, 15.0f, -1.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder2, PartPose.offset(4.0f, 15.0f, -1.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        float k = 0.7853982f;
        this.rightHindLeg.zRot = -0.7853982f;
        this.leftHindLeg.zRot = 0.7853982f;
        this.rightMiddleHindLeg.zRot = -0.58119464f;
        this.leftMiddleHindLeg.zRot = 0.58119464f;
        this.rightMiddleFrontLeg.zRot = -0.58119464f;
        this.leftMiddleFrontLeg.zRot = 0.58119464f;
        this.rightFrontLeg.zRot = -0.7853982f;
        this.leftFrontLeg.zRot = 0.7853982f;
        float l = -0.0f;
        float m = 0.3926991f;
        this.rightHindLeg.yRot = 0.7853982f;
        this.leftHindLeg.yRot = -0.7853982f;
        this.rightMiddleHindLeg.yRot = 0.3926991f;
        this.leftMiddleHindLeg.yRot = -0.3926991f;
        this.rightMiddleFrontLeg.yRot = -0.3926991f;
        this.leftMiddleFrontLeg.yRot = 0.3926991f;
        this.rightFrontLeg.yRot = -0.7853982f;
        this.leftFrontLeg.yRot = 0.7853982f;
        float n = -(Mth.cos(f * 0.6662f * 2.0f + 0.0f) * 0.4f) * g;
        float o = -(Mth.cos(f * 0.6662f * 2.0f + (float)Math.PI) * 0.4f) * g;
        float p = -(Mth.cos(f * 0.6662f * 2.0f + 1.5707964f) * 0.4f) * g;
        float q = -(Mth.cos(f * 0.6662f * 2.0f + 4.712389f) * 0.4f) * g;
        float r = Math.abs(Mth.sin(f * 0.6662f + 0.0f) * 0.4f) * g;
        float s = Math.abs(Mth.sin(f * 0.6662f + (float)Math.PI) * 0.4f) * g;
        float t = Math.abs(Mth.sin(f * 0.6662f + 1.5707964f) * 0.4f) * g;
        float u = Math.abs(Mth.sin(f * 0.6662f + 4.712389f) * 0.4f) * g;
        this.rightHindLeg.yRot += n;
        this.leftHindLeg.yRot += -n;
        this.rightMiddleHindLeg.yRot += o;
        this.leftMiddleHindLeg.yRot += -o;
        this.rightMiddleFrontLeg.yRot += p;
        this.leftMiddleFrontLeg.yRot += -p;
        this.rightFrontLeg.yRot += q;
        this.leftFrontLeg.yRot += -q;
        this.rightHindLeg.zRot += r;
        this.leftHindLeg.zRot += -r;
        this.rightMiddleHindLeg.zRot += s;
        this.leftMiddleHindLeg.zRot += -s;
        this.rightMiddleFrontLeg.zRot += t;
        this.leftMiddleFrontLeg.zRot += -t;
        this.rightFrontLeg.zRot += u;
        this.leftFrontLeg.zRot += -u;
    }
}

