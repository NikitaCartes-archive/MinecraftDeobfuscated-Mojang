/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import java.util.Random;
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
public class GhastModel<T extends Entity>
extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart[] tentacles = new ModelPart[9];

    public GhastModel(ModelPart modelPart) {
        this.root = modelPart;
        for (int i = 0; i < this.tentacles.length; ++i) {
            this.tentacles[i] = modelPart.getChild(GhastModel.createTentacleName(i));
        }
    }

    private static String createTentacleName(int i) {
        return "tentacle" + i;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f), PartPose.offset(0.0f, 17.6f, 0.0f));
        Random random = new Random(1660L);
        for (int i = 0; i < 9; ++i) {
            float f = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5f + 0.25f) / 2.0f * 2.0f - 1.0f) * 5.0f;
            float g = ((float)(i / 3) / 2.0f * 2.0f - 1.0f) * 5.0f;
            int j = random.nextInt(7) + 8;
            partDefinition.addOrReplaceChild(GhastModel.createTentacleName(i), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, j, 2.0f), PartPose.offset(f, 24.6f, g));
        }
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        for (int k = 0; k < this.tentacles.length; ++k) {
            this.tentacles[k].xRot = 0.2f * Mth.sin(h * 0.3f + (float)k) + 0.4f;
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

