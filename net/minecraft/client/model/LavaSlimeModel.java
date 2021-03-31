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
import net.minecraft.world.entity.monster.Slime;

@Environment(value=EnvType.CLIENT)
public class LavaSlimeModel<T extends Slime>
extends HierarchicalModel<T> {
    private static final int SEGMENT_COUNT = 8;
    private final ModelPart root;
    private final ModelPart[] bodyCubes = new ModelPart[8];

    public LavaSlimeModel(ModelPart modelPart) {
        this.root = modelPart;
        Arrays.setAll(this.bodyCubes, i -> modelPart.getChild(LavaSlimeModel.getSegmentName(i)));
    }

    private static String getSegmentName(int i) {
        return "cube" + i;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        for (int i = 0; i < 8; ++i) {
            int j = 0;
            int k = i;
            if (i == 2) {
                j = 24;
                k = 10;
            } else if (i == 3) {
                j = 24;
                k = 19;
            }
            partDefinition.addOrReplaceChild(LavaSlimeModel.getSegmentName(i), CubeListBuilder.create().texOffs(j, k).addBox(-4.0f, 16 + i, -4.0f, 8.0f, 1.0f, 8.0f), PartPose.ZERO);
        }
        partDefinition.addOrReplaceChild("inside_cube", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 18.0f, -2.0f, 4.0f, 4.0f, 4.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T slime, float f, float g, float h, float i, float j) {
    }

    @Override
    public void prepareMobModel(T slime, float f, float g, float h) {
        float i = Mth.lerp(h, ((Slime)slime).oSquish, ((Slime)slime).squish);
        if (i < 0.0f) {
            i = 0.0f;
        }
        for (int j = 0; j < this.bodyCubes.length; ++j) {
            this.bodyCubes[j].y = (float)(-(4 - j)) * i * 1.7f;
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

