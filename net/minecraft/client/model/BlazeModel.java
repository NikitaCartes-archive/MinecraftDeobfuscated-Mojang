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
public class BlazeModel<T extends Entity>
extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart[] upperBodyParts;
    private final ModelPart head;

    public BlazeModel(ModelPart modelPart) {
        this.root = modelPart;
        this.head = modelPart.getChild("head");
        this.upperBodyParts = new ModelPart[12];
        Arrays.setAll(this.upperBodyParts, i -> modelPart.getChild(BlazeModel.getPartName(i)));
    }

    private static String getPartName(int i) {
        return "part" + i;
    }

    public static LayerDefinition createBodyLayer() {
        float j;
        float h;
        float g;
        int i;
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO);
        float f = 0.0f;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 16).addBox(0.0f, 0.0f, 0.0f, 2.0f, 8.0f, 2.0f);
        for (i = 0; i < 4; ++i) {
            g = Mth.cos(f) * 9.0f;
            h = -2.0f + Mth.cos((float)(i * 2) * 0.25f);
            j = Mth.sin(f) * 9.0f;
            partDefinition.addOrReplaceChild(BlazeModel.getPartName(i), cubeListBuilder, PartPose.offset(g, h, j));
            f += 1.5707964f;
        }
        f = 0.7853982f;
        for (i = 4; i < 8; ++i) {
            g = Mth.cos(f) * 7.0f;
            h = 2.0f + Mth.cos((float)(i * 2) * 0.25f);
            j = Mth.sin(f) * 7.0f;
            partDefinition.addOrReplaceChild(BlazeModel.getPartName(i), cubeListBuilder, PartPose.offset(g, h, j));
            f += 1.5707964f;
        }
        f = 0.47123894f;
        for (i = 8; i < 12; ++i) {
            g = Mth.cos(f) * 5.0f;
            h = 11.0f + Mth.cos((float)i * 1.5f * 0.5f);
            j = Mth.sin(f) * 5.0f;
            partDefinition.addOrReplaceChild(BlazeModel.getPartName(i), cubeListBuilder, PartPose.offset(g, h, j));
            f += 1.5707964f;
        }
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        int l;
        float k = h * (float)Math.PI * -0.1f;
        for (l = 0; l < 4; ++l) {
            this.upperBodyParts[l].y = -2.0f + Mth.cos(((float)(l * 2) + h) * 0.25f);
            this.upperBodyParts[l].x = Mth.cos(k) * 9.0f;
            this.upperBodyParts[l].z = Mth.sin(k) * 9.0f;
            k += 1.5707964f;
        }
        k = 0.7853982f + h * (float)Math.PI * 0.03f;
        for (l = 4; l < 8; ++l) {
            this.upperBodyParts[l].y = 2.0f + Mth.cos(((float)(l * 2) + h) * 0.25f);
            this.upperBodyParts[l].x = Mth.cos(k) * 7.0f;
            this.upperBodyParts[l].z = Mth.sin(k) * 7.0f;
            k += 1.5707964f;
        }
        k = 0.47123894f + h * (float)Math.PI * -0.05f;
        for (l = 8; l < 12; ++l) {
            this.upperBodyParts[l].y = 11.0f + Mth.cos(((float)l * 1.5f + h) * 0.5f);
            this.upperBodyParts[l].x = Mth.cos(k) * 5.0f;
            this.upperBodyParts[l].z = Mth.sin(k) * 5.0f;
            k += 1.5707964f;
        }
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
    }
}

