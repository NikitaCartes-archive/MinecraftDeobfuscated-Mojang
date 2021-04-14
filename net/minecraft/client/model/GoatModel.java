/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.goat.Goat;

@Environment(value=EnvType.CLIENT)
public class GoatModel<T extends Goat>
extends QuadrupedModel<T> {
    public GoatModel(ModelPart modelPart) {
        super(modelPart, true, 19.0f, 1.0f, 2.5f, 2.0f, 24);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(2, 61).addBox("left ear", 2.0f, -11.0f, -10.0f, 3.0f, 2.0f, 1.0f).texOffs(2, 61).addBox("right ear", -6.0f, -11.0f, -10.0f, 3.0f, 2.0f, 1.0f).texOffs(23, 52).addBox("goatee", -0.5f, -3.0f, -14.0f, 0.0f, 7.0f, 5.0f), PartPose.offset(1.0f, 14.0f, 0.0f));
        partDefinition2.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(12, 55).addBox(-0.01f, -16.0f, -10.0f, 2.0f, 7.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        partDefinition2.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(12, 55).addBox(-2.99f, -16.0f, -10.0f, 2.0f, 7.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        partDefinition2.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(34, 46).addBox(-3.0f, -4.0f, -8.0f, 5.0f, 7.0f, 10.0f), PartPose.offsetAndRotation(0.0f, -8.0f, -8.0f, 0.9599f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(1, 1).addBox(-4.0f, -17.0f, -7.0f, 9.0f, 11.0f, 16.0f).texOffs(0, 28).addBox(-5.0f, -18.0f, -8.0f, 11.0f, 14.0f, 11.0f), PartPose.offset(0.0f, 24.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(36, 29).addBox(0.0f, 4.0f, 0.0f, 3.0f, 6.0f, 3.0f), PartPose.offset(1.0f, 14.0f, 4.0f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(49, 29).addBox(0.0f, 4.0f, 0.0f, 3.0f, 6.0f, 3.0f), PartPose.offset(-3.0f, 14.0f, 4.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(49, 2).addBox(0.0f, 0.0f, 0.0f, 3.0f, 10.0f, 3.0f), PartPose.offset(-3.0f, 14.0f, -6.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(35, 2).addBox(0.0f, 0.0f, 0.0f, 3.0f, 10.0f, 3.0f), PartPose.offset(1.0f, 14.0f, -6.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(T goat, float f, float g, float h, float i, float j) {
        this.head.getChild((String)"left_horn").visible = !((AgeableMob)goat).isBaby();
        this.head.getChild((String)"right_horn").visible = !((AgeableMob)goat).isBaby();
        super.setupAnim(goat, f, g, h, i, j);
        float k = ((Goat)goat).getRammingXHeadRot();
        if (k != 0.0f) {
            this.head.xRot = k;
        }
    }
}

