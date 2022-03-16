/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.frog.Tadpole;

@Environment(value=EnvType.CLIENT)
public class TadpoleModel<T extends Tadpole>
extends AgeableListModel<T> {
    private final ModelPart root;
    private final ModelPart tail;

    public TadpoleModel(ModelPart modelPart) {
        super(true, 8.0f, 3.35f);
        this.root = modelPart;
        this.tail = modelPart.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = 0.0f;
        float g = 22.0f;
        float h = -3.0f;
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -1.0f, 0.0f, 3.0f, 2.0f, 3.0f), PartPose.offset(0.0f, 22.0f, -3.0f));
        partDefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, -1.0f, 0.0f, 0.0f, 2.0f, 7.0f), PartPose.offset(0.0f, 22.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.root);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.tail);
    }

    @Override
    public void setupAnim(T tadpole, float f, float g, float h, float i, float j) {
        float k = ((Entity)tadpole).isInWater() ? 1.0f : 1.5f;
        this.tail.yRot = -k * 0.25f * Mth.sin(0.3f * h);
    }
}

