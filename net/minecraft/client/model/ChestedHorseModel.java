/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

@Environment(value=EnvType.CLIENT)
public class ChestedHorseModel<T extends AbstractChestedHorse>
extends HorseModel<T> {
    private final ModelPart leftChest;
    private final ModelPart rightChest;

    public ChestedHorseModel(ModelPart modelPart) {
        super(modelPart);
        this.leftChest = this.body.getChild("left_chest");
        this.rightChest = this.body.getChild("right_chest");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = HorseModel.createBodyMesh(CubeDeformation.NONE);
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.getChild("body");
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(26, 21).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 8.0f, 3.0f);
        partDefinition2.addOrReplaceChild("left_chest", cubeListBuilder, PartPose.offsetAndRotation(6.0f, -8.0f, 0.0f, 0.0f, -1.5707964f, 0.0f));
        partDefinition2.addOrReplaceChild("right_chest", cubeListBuilder, PartPose.offsetAndRotation(-6.0f, -8.0f, 0.0f, 0.0f, 1.5707964f, 0.0f));
        PartDefinition partDefinition3 = partDefinition.getChild("head_parts").getChild("head");
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(0, 12).addBox(-1.0f, -7.0f, 0.0f, 2.0f, 7.0f, 1.0f);
        partDefinition3.addOrReplaceChild("left_ear", cubeListBuilder2, PartPose.offsetAndRotation(1.25f, -10.0f, 4.0f, 0.2617994f, 0.0f, 0.2617994f));
        partDefinition3.addOrReplaceChild("right_ear", cubeListBuilder2, PartPose.offsetAndRotation(-1.25f, -10.0f, 4.0f, 0.2617994f, 0.0f, -0.2617994f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(T abstractChestedHorse, float f, float g, float h, float i, float j) {
        super.setupAnim(abstractChestedHorse, f, g, h, i, j);
        if (((AbstractChestedHorse)abstractChestedHorse).hasChest()) {
            this.leftChest.visible = true;
            this.rightChest.visible = true;
        } else {
            this.leftChest.visible = false;
            this.rightChest.visible = false;
        }
    }
}

