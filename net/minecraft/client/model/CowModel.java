/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class CowModel<T extends Entity>
extends QuadrupedModel<T> {
    public CowModel(ModelPart modelPart) {
        super(modelPart, false, 10.0f, 4.0f, 2.0f, 2.0f, 24);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 12;
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -6.0f, 8.0f, 8.0f, 6.0f).texOffs(22, 0).addBox("right_horn", -5.0f, -5.0f, -4.0f, 1.0f, 3.0f, 1.0f).texOffs(22, 0).addBox("left_horn", 4.0f, -5.0f, -4.0f, 1.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 4.0f, -8.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 4).addBox(-6.0f, -10.0f, -7.0f, 12.0f, 18.0f, 10.0f).texOffs(52, 0).addBox(-2.0f, 2.0f, -8.0f, 4.0f, 6.0f, 1.0f), PartPose.offsetAndRotation(0.0f, 5.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-4.0f, 12.0f, 7.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(4.0f, 12.0f, 7.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-4.0f, 12.0f, -6.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(4.0f, 12.0f, -6.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    public ModelPart getHead() {
        return this.head;
    }
}

