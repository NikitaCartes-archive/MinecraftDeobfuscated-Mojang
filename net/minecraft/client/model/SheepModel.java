/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.Sheep;

@Environment(value=EnvType.CLIENT)
public class SheepModel<T extends Sheep>
extends QuadrupedModel<T> {
    private float headXRot;

    public SheepModel(ModelPart modelPart) {
        super(modelPart, false, 8.0f, 4.0f, 2.0f, 2.0f, 24);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = QuadrupedModel.createBodyMesh(12, CubeDeformation.NONE);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -4.0f, -6.0f, 6.0f, 6.0f, 8.0f), PartPose.offset(0.0f, 6.0f, -8.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-4.0f, -10.0f, -7.0f, 8.0f, 16.0f, 6.0f), PartPose.offsetAndRotation(0.0f, 5.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void prepareMobModel(T sheep, float f, float g, float h) {
        super.prepareMobModel(sheep, f, g, h);
        this.head.y = 6.0f + ((Sheep)sheep).getHeadEatPositionScale(h) * 9.0f;
        this.headXRot = ((Sheep)sheep).getHeadEatAngleScale(h);
    }

    @Override
    public void setupAnim(T sheep, float f, float g, float h, float i, float j) {
        super.setupAnim(sheep, f, g, h, i, j);
        this.head.xRot = this.headXRot;
    }
}

