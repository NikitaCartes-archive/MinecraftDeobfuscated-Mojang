/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class TropicalFishModelA<T extends Entity>
extends ColorableHierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart tail;

    public TropicalFishModelA(ModelPart modelPart) {
        this.root = modelPart;
        this.tail = modelPart.getChild("tail");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 22;
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -1.5f, -3.0f, 2.0f, 3.0f, 6.0f, cubeDeformation), PartPose.offset(0.0f, 22.0f, 0.0f));
        partDefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(22, -6).addBox(0.0f, -1.5f, 0.0f, 0.0f, 3.0f, 6.0f, cubeDeformation), PartPose.offset(0.0f, 22.0f, 3.0f));
        partDefinition.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(2, 16).addBox(-2.0f, -1.0f, 0.0f, 2.0f, 2.0f, 0.0f, cubeDeformation), PartPose.offsetAndRotation(-1.0f, 22.5f, 0.0f, 0.0f, 0.7853982f, 0.0f));
        partDefinition.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(2, 12).addBox(0.0f, -1.0f, 0.0f, 2.0f, 2.0f, 0.0f, cubeDeformation), PartPose.offsetAndRotation(1.0f, 22.5f, 0.0f, 0.0f, -0.7853982f, 0.0f));
        partDefinition.addOrReplaceChild("top_fin", CubeListBuilder.create().texOffs(10, -5).addBox(0.0f, -3.0f, 0.0f, 0.0f, 3.0f, 6.0f, cubeDeformation), PartPose.offset(0.0f, 20.5f, -3.0f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        float k = 1.0f;
        if (!((Entity)entity).isInWater()) {
            k = 1.5f;
        }
        this.tail.yRot = -k * 0.45f * Mth.sin(0.6f * h);
    }
}

