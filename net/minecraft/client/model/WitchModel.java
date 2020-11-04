/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.VillagerModel;
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
public class WitchModel<T extends Entity>
extends VillagerModel<T> {
    private boolean holdingItem;

    public WitchModel(ModelPart modelPart) {
        super(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = VillagerModel.createBodyModel();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f), PartPose.ZERO);
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 64).addBox(0.0f, 0.0f, 0.0f, 10.0f, 2.0f, 10.0f), PartPose.offset(-5.0f, -10.03125f, -5.0f));
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("hat2", CubeListBuilder.create().texOffs(0, 76).addBox(0.0f, 0.0f, 0.0f, 7.0f, 4.0f, 7.0f), PartPose.offsetAndRotation(1.75f, -4.0f, 2.0f, -0.05235988f, 0.0f, 0.02617994f));
        PartDefinition partDefinition5 = partDefinition4.addOrReplaceChild("hat3", CubeListBuilder.create().texOffs(0, 87).addBox(0.0f, 0.0f, 0.0f, 4.0f, 4.0f, 4.0f), PartPose.offsetAndRotation(1.75f, -4.0f, 2.0f, -0.10471976f, 0.0f, 0.05235988f));
        partDefinition5.addOrReplaceChild("hat4", CubeListBuilder.create().texOffs(0, 95).addBox(0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 1.0f, new CubeDeformation(0.25f)), PartPose.offsetAndRotation(1.75f, -2.0f, 2.0f, -0.20943952f, 0.0f, 0.10471976f));
        PartDefinition partDefinition6 = partDefinition2.getChild("nose");
        partDefinition6.addOrReplaceChild("mole", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 3.0f, -6.75f, 1.0f, 1.0f, 1.0f, new CubeDeformation(-0.25f)), PartPose.offset(0.0f, -2.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 128);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        super.setupAnim(entity, f, g, h, i, j);
        this.nose.setPos(0.0f, -2.0f, 0.0f);
        float k = 0.01f * (float)(((Entity)entity).getId() % 10);
        this.nose.xRot = Mth.sin((float)((Entity)entity).tickCount * k) * 4.5f * ((float)Math.PI / 180);
        this.nose.yRot = 0.0f;
        this.nose.zRot = Mth.cos((float)((Entity)entity).tickCount * k) * 2.5f * ((float)Math.PI / 180);
        if (this.holdingItem) {
            this.nose.setPos(0.0f, 1.0f, -1.5f);
            this.nose.xRot = -0.9f;
        }
    }

    public ModelPart getNose() {
        return this.nose;
    }

    public void setHoldingItem(boolean bl) {
        this.holdingItem = bl;
    }
}

