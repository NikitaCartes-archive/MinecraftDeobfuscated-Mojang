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
public class CodModel<T extends Entity>
extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart tailFin;

    public CodModel(ModelPart modelPart) {
        this.root = modelPart;
        this.tailFin = modelPart.getChild("tail_fin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 22;
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -2.0f, 0.0f, 2.0f, 4.0f, 7.0f), PartPose.offset(0.0f, 22.0f, 0.0f));
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(11, 0).addBox(-1.0f, -2.0f, -3.0f, 2.0f, 4.0f, 3.0f), PartPose.offset(0.0f, 22.0f, 0.0f));
        partDefinition.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 22.0f, -3.0f));
        partDefinition.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(22, 1).addBox(-2.0f, 0.0f, -1.0f, 2.0f, 0.0f, 2.0f), PartPose.offsetAndRotation(-1.0f, 23.0f, 0.0f, 0.0f, 0.0f, -0.7853982f));
        partDefinition.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(22, 4).addBox(0.0f, 0.0f, -1.0f, 2.0f, 0.0f, 2.0f), PartPose.offsetAndRotation(1.0f, 23.0f, 0.0f, 0.0f, 0.0f, 0.7853982f));
        partDefinition.addOrReplaceChild("tail_fin", CubeListBuilder.create().texOffs(22, 3).addBox(0.0f, -2.0f, 0.0f, 0.0f, 4.0f, 4.0f), PartPose.offset(0.0f, 22.0f, 7.0f));
        partDefinition.addOrReplaceChild("top_fin", CubeListBuilder.create().texOffs(20, -6).addBox(0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 6.0f), PartPose.offset(0.0f, 20.0f, 0.0f));
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
        this.tailFin.yRot = -k * 0.45f * Mth.sin(0.6f * h);
    }
}

