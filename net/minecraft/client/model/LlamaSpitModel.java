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
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class LlamaSpitModel<T extends Entity>
extends HierarchicalModel<T> {
    private static final String MAIN = "main";
    private final ModelPart root;

    public LlamaSpitModel(ModelPart modelPart) {
        this.root = modelPart;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 2;
        partDefinition.addOrReplaceChild(MAIN, CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, -4.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, 0.0f, -4.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(2.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, 2.0f, 0.0f, 2.0f, 2.0f, 2.0f).addBox(0.0f, 0.0f, 2.0f, 2.0f, 2.0f, 2.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

