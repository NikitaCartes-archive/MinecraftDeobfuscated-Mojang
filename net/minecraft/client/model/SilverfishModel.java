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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class SilverfishModel<T extends Entity>
extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart[] bodyParts = new ModelPart[7];
    private final ModelPart[] bodyLayers = new ModelPart[3];
    private static final int[][] BODY_SIZES = new int[][]{{3, 2, 2}, {4, 3, 2}, {6, 4, 3}, {3, 3, 3}, {2, 2, 3}, {2, 1, 2}, {1, 1, 2}};
    private static final int[][] BODY_TEXS = new int[][]{{0, 0}, {0, 4}, {0, 9}, {0, 16}, {0, 22}, {11, 0}, {13, 4}};

    public SilverfishModel(ModelPart modelPart) {
        this.root = modelPart;
        Arrays.setAll(this.bodyParts, i -> modelPart.getChild(SilverfishModel.getSegmentName(i)));
        Arrays.setAll(this.bodyLayers, i -> modelPart.getChild(SilverfishModel.getLayerName(i)));
    }

    private static String getLayerName(int i) {
        return "layer" + i;
    }

    private static String getSegmentName(int i) {
        return "segment" + i;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float[] fs = new float[7];
        float f = -3.5f;
        for (int i = 0; i < 7; ++i) {
            partDefinition.addOrReplaceChild(SilverfishModel.getSegmentName(i), CubeListBuilder.create().texOffs(BODY_TEXS[i][0], BODY_TEXS[i][1]).addBox((float)BODY_SIZES[i][0] * -0.5f, 0.0f, (float)BODY_SIZES[i][2] * -0.5f, BODY_SIZES[i][0], BODY_SIZES[i][1], BODY_SIZES[i][2]), PartPose.offset(0.0f, 24 - BODY_SIZES[i][1], f));
            fs[i] = f;
            if (i >= 6) continue;
            f += (float)(BODY_SIZES[i][2] + BODY_SIZES[i + 1][2]) * 0.5f;
        }
        partDefinition.addOrReplaceChild(SilverfishModel.getLayerName(0), CubeListBuilder.create().texOffs(20, 0).addBox(-5.0f, 0.0f, (float)BODY_SIZES[2][2] * -0.5f, 10.0f, 8.0f, BODY_SIZES[2][2]), PartPose.offset(0.0f, 16.0f, fs[2]));
        partDefinition.addOrReplaceChild(SilverfishModel.getLayerName(1), CubeListBuilder.create().texOffs(20, 11).addBox(-3.0f, 0.0f, (float)BODY_SIZES[4][2] * -0.5f, 6.0f, 4.0f, BODY_SIZES[4][2]), PartPose.offset(0.0f, 20.0f, fs[4]));
        partDefinition.addOrReplaceChild(SilverfishModel.getLayerName(2), CubeListBuilder.create().texOffs(20, 18).addBox(-3.0f, 0.0f, (float)BODY_SIZES[4][2] * -0.5f, 6.0f, 5.0f, BODY_SIZES[1][2]), PartPose.offset(0.0f, 19.0f, fs[1]));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        for (int k = 0; k < this.bodyParts.length; ++k) {
            this.bodyParts[k].yRot = Mth.cos(h * 0.9f + (float)k * 0.15f * (float)Math.PI) * (float)Math.PI * 0.05f * (float)(1 + Math.abs(k - 2));
            this.bodyParts[k].x = Mth.sin(h * 0.9f + (float)k * 0.15f * (float)Math.PI) * (float)Math.PI * 0.2f * (float)Math.abs(k - 2);
        }
        this.bodyLayers[0].yRot = this.bodyParts[2].yRot;
        this.bodyLayers[1].yRot = this.bodyParts[4].yRot;
        this.bodyLayers[1].x = this.bodyParts[4].x;
        this.bodyLayers[2].yRot = this.bodyParts[1].yRot;
        this.bodyLayers[2].x = this.bodyParts[1].x;
    }
}

