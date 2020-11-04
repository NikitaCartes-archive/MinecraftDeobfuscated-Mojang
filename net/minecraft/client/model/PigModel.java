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
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class PigModel<T extends Entity>
extends QuadrupedModel<T> {
    public PigModel(ModelPart modelPart) {
        super(modelPart, false, 4.0f, 4.0f, 2.0f, 2.0f, 24);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = QuadrupedModel.createBodyMesh(6, cubeDeformation);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f, cubeDeformation).texOffs(16, 16).addBox(-2.0f, 0.0f, -9.0f, 4.0f, 3.0f, 1.0f, cubeDeformation), PartPose.offset(0.0f, 12.0f, -6.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }
}

