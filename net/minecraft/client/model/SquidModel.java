/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import java.util.Arrays;
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
public class SquidModel<T extends Entity>
extends HierarchicalModel<T> {
    private final ModelPart[] tentacles = new ModelPart[8];
    private final ModelPart root;

    public SquidModel(ModelPart modelPart) {
        this.root = modelPart;
        Arrays.setAll(this.tentacles, i -> modelPart.getChild(SquidModel.createTentacleName(i)));
    }

    private static String createTentacleName(int i) {
        return "tentacle" + i;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = -16;
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -8.0f, -6.0f, 12.0f, 16.0f, 12.0f), PartPose.offset(0.0f, 8.0f, 0.0f));
        int j = 8;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(48, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 18.0f, 2.0f);
        for (int k = 0; k < 8; ++k) {
            double d = (double)k * Math.PI * 2.0 / 8.0;
            float f = (float)Math.cos(d) * 5.0f;
            float g = 15.0f;
            float h = (float)Math.sin(d) * 5.0f;
            d = (double)k * Math.PI * -2.0 / 8.0 + 1.5707963267948966;
            float l = (float)d;
            partDefinition.addOrReplaceChild(SquidModel.createTentacleName(k), cubeListBuilder, PartPose.offsetAndRotation(f, 15.0f, h, 0.0f, l, 0.0f));
        }
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        for (ModelPart modelPart : this.tentacles) {
            modelPart.xRot = h;
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

