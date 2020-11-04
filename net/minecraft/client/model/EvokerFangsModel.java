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
public class EvokerFangsModel<T extends Entity>
extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart base;
    private final ModelPart upperJaw;
    private final ModelPart lowerJaw;

    public EvokerFangsModel(ModelPart modelPart) {
        this.root = modelPart;
        this.base = modelPart.getChild("base");
        this.upperJaw = modelPart.getChild("upper_jaw");
        this.lowerJaw = modelPart.getChild("lower_jaw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 10.0f, 12.0f, 10.0f), PartPose.offset(-5.0f, 24.0f, -5.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(40, 0).addBox(0.0f, 0.0f, 0.0f, 4.0f, 14.0f, 8.0f);
        partDefinition.addOrReplaceChild("upper_jaw", cubeListBuilder, PartPose.offset(1.5f, 24.0f, -4.0f));
        partDefinition.addOrReplaceChild("lower_jaw", cubeListBuilder, PartPose.offsetAndRotation(-1.5f, 24.0f, 4.0f, 0.0f, (float)Math.PI, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        float k = f * 2.0f;
        if (k > 1.0f) {
            k = 1.0f;
        }
        k = 1.0f - k * k * k;
        this.upperJaw.zRot = (float)Math.PI - k * 0.35f * (float)Math.PI;
        this.lowerJaw.zRot = (float)Math.PI + k * 0.35f * (float)Math.PI;
        float l = (f + Mth.sin(f * 2.7f)) * 0.6f * 12.0f;
        this.lowerJaw.y = this.upperJaw.y = 24.0f - l;
        this.base.y = this.upperJaw.y;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

