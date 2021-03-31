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
public class SalmonModel<T extends Entity>
extends HierarchicalModel<T> {
    private static final String BODY_FRONT = "body_front";
    private static final String BODY_BACK = "body_back";
    private final ModelPart root;
    private final ModelPart bodyBack;

    public SalmonModel(ModelPart modelPart) {
        this.root = modelPart;
        this.bodyBack = modelPart.getChild(BODY_BACK);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 20;
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(BODY_FRONT, CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -2.5f, 0.0f, 3.0f, 5.0f, 8.0f), PartPose.offset(0.0f, 20.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild(BODY_BACK, CubeListBuilder.create().texOffs(0, 13).addBox(-1.5f, -2.5f, 0.0f, 3.0f, 5.0f, 8.0f), PartPose.offset(0.0f, 20.0f, 8.0f));
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0f, -2.0f, -3.0f, 2.0f, 4.0f, 3.0f), PartPose.offset(0.0f, 20.0f, 0.0f));
        partDefinition3.addOrReplaceChild("back_fin", CubeListBuilder.create().texOffs(20, 10).addBox(0.0f, -2.5f, 0.0f, 0.0f, 5.0f, 6.0f), PartPose.offset(0.0f, 0.0f, 8.0f));
        partDefinition2.addOrReplaceChild("top_front_fin", CubeListBuilder.create().texOffs(2, 1).addBox(0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 3.0f), PartPose.offset(0.0f, -4.5f, 5.0f));
        partDefinition3.addOrReplaceChild("top_back_fin", CubeListBuilder.create().texOffs(0, 2).addBox(0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 4.0f), PartPose.offset(0.0f, -4.5f, -1.0f));
        partDefinition.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(-4, 0).addBox(-2.0f, 0.0f, 0.0f, 2.0f, 0.0f, 2.0f), PartPose.offsetAndRotation(-1.5f, 21.5f, 0.0f, 0.0f, 0.0f, -0.7853982f));
        partDefinition.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 2.0f), PartPose.offsetAndRotation(1.5f, 21.5f, 0.0f, 0.0f, 0.0f, 0.7853982f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        float k = 1.0f;
        float l = 1.0f;
        if (!((Entity)entity).isInWater()) {
            k = 1.3f;
            l = 1.7f;
        }
        this.bodyBack.yRot = -k * 0.25f * Mth.sin(l * 0.6f * h);
    }
}

