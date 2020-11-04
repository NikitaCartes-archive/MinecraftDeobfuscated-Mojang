/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

@Environment(value=EnvType.CLIENT)
public class BoatModel
extends ListModel<Boat> {
    private final ModelPart leftPaddle;
    private final ModelPart rightPaddle;
    private final ModelPart waterPatch;
    private final ImmutableList<ModelPart> parts;

    public BoatModel(ModelPart modelPart) {
        this.leftPaddle = modelPart.getChild("left_paddle");
        this.rightPaddle = modelPart.getChild("right_paddle");
        this.waterPatch = modelPart.getChild("water_patch");
        this.parts = ImmutableList.of(modelPart.getChild("bottom"), modelPart.getChild("back"), modelPart.getChild("front"), modelPart.getChild("right"), modelPart.getChild("left"), this.leftPaddle, this.rightPaddle);
    }

    public static LayerDefinition createBodyModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 32;
        int j = 6;
        int k = 20;
        int l = 4;
        int m = 28;
        partDefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0f, -9.0f, -3.0f, 28.0f, 16.0f, 3.0f), PartPose.offsetAndRotation(0.0f, 3.0f, 1.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 19).addBox(-13.0f, -7.0f, -1.0f, 18.0f, 6.0f, 2.0f), PartPose.offsetAndRotation(-15.0f, 4.0f, 4.0f, 0.0f, 4.712389f, 0.0f));
        partDefinition.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 27).addBox(-8.0f, -7.0f, -1.0f, 16.0f, 6.0f, 2.0f), PartPose.offsetAndRotation(15.0f, 4.0f, 0.0f, 0.0f, 1.5707964f, 0.0f));
        partDefinition.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 35).addBox(-14.0f, -7.0f, -1.0f, 28.0f, 6.0f, 2.0f), PartPose.offsetAndRotation(0.0f, 4.0f, -9.0f, 0.0f, (float)Math.PI, 0.0f));
        partDefinition.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 43).addBox(-14.0f, -7.0f, -1.0f, 28.0f, 6.0f, 2.0f), PartPose.offset(0.0f, 4.0f, 9.0f));
        int n = 20;
        int o = 7;
        int p = 6;
        float f = -5.0f;
        partDefinition.addOrReplaceChild("left_paddle", CubeListBuilder.create().texOffs(62, 0).addBox(-1.0f, 0.0f, -5.0f, 2.0f, 2.0f, 18.0f).addBox(-1.001f, -3.0f, 8.0f, 1.0f, 6.0f, 7.0f), PartPose.offsetAndRotation(3.0f, -5.0f, 9.0f, 0.0f, 0.0f, 0.19634955f));
        partDefinition.addOrReplaceChild("right_paddle", CubeListBuilder.create().texOffs(62, 20).addBox(-1.0f, 0.0f, -5.0f, 2.0f, 2.0f, 18.0f).addBox(0.001f, -3.0f, 8.0f, 1.0f, 6.0f, 7.0f), PartPose.offsetAndRotation(3.0f, -5.0f, -9.0f, 0.0f, (float)Math.PI, 0.19634955f));
        partDefinition.addOrReplaceChild("water_patch", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0f, -9.0f, -3.0f, 28.0f, 16.0f, 3.0f), PartPose.offsetAndRotation(0.0f, -3.0f, 1.0f, 1.5707964f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 128, 64);
    }

    @Override
    public void setupAnim(Boat boat, float f, float g, float h, float i, float j) {
        BoatModel.animatePaddle(boat, 0, this.leftPaddle, f);
        BoatModel.animatePaddle(boat, 1, this.rightPaddle, f);
    }

    public ImmutableList<ModelPart> parts() {
        return this.parts;
    }

    public ModelPart waterPatch() {
        return this.waterPatch;
    }

    private static void animatePaddle(Boat boat, int i, ModelPart modelPart, float f) {
        float g = boat.getRowingTime(i, f);
        modelPart.xRot = (float)Mth.clampedLerp(-1.0471975803375244, -0.2617993950843811, (Mth.sin(-g) + 1.0f) / 2.0f);
        modelPart.yRot = (float)Mth.clampedLerp(-0.7853981852531433, 0.7853981852531433, (Mth.sin(-g + 1.0f) + 1.0f) / 2.0f);
        if (i == 1) {
            modelPart.yRot = (float)Math.PI - modelPart.yRot;
        }
    }

    @Override
    public /* synthetic */ Iterable parts() {
        return this.parts();
    }
}

