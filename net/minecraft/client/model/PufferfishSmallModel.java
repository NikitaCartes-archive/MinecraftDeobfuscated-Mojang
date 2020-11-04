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
public class PufferfishSmallModel<T extends Entity>
extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart leftFin;
    private final ModelPart rightFin;

    public PufferfishSmallModel(ModelPart modelPart) {
        this.root = modelPart;
        this.leftFin = modelPart.getChild("left_fin");
        this.rightFin = modelPart.getChild("right_fin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 23;
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 27).addBox(-1.5f, -2.0f, -1.5f, 3.0f, 2.0f, 3.0f), PartPose.offset(0.0f, 23.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(24, 6).addBox(-1.5f, 0.0f, -1.5f, 1.0f, 1.0f, 1.0f), PartPose.offset(0.0f, 20.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(28, 6).addBox(0.5f, 0.0f, -1.5f, 1.0f, 1.0f, 1.0f), PartPose.offset(0.0f, 20.0f, 0.0f));
        partDefinition.addOrReplaceChild("back_fin", CubeListBuilder.create().texOffs(-3, 0).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 0.0f, 3.0f), PartPose.offset(0.0f, 22.0f, 1.5f));
        partDefinition.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(25, 0).addBox(-1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 2.0f), PartPose.offset(-1.5f, 22.0f, -1.5f));
        partDefinition.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(25, 0).addBox(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 2.0f), PartPose.offset(1.5f, 22.0f, -1.5f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        this.rightFin.zRot = -0.2f + 0.4f * Mth.sin(h * 0.2f);
        this.leftFin.zRot = 0.2f - 0.4f * Mth.sin(h * 0.2f);
    }
}

