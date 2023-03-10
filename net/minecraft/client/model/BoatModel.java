/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.WaterPatchModel;
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
extends ListModel<Boat>
implements WaterPatchModel {
    private static final String LEFT_PADDLE = "left_paddle";
    private static final String RIGHT_PADDLE = "right_paddle";
    private static final String WATER_PATCH = "water_patch";
    private static final String BOTTOM = "bottom";
    private static final String BACK = "back";
    private static final String FRONT = "front";
    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private final ModelPart leftPaddle;
    private final ModelPart rightPaddle;
    private final ModelPart waterPatch;
    private final ImmutableList<ModelPart> parts;

    public BoatModel(ModelPart modelPart) {
        this.leftPaddle = modelPart.getChild(LEFT_PADDLE);
        this.rightPaddle = modelPart.getChild(RIGHT_PADDLE);
        this.waterPatch = modelPart.getChild(WATER_PATCH);
        this.parts = this.createPartsBuilder(modelPart).build();
    }

    protected ImmutableList.Builder<ModelPart> createPartsBuilder(ModelPart modelPart) {
        ImmutableList.Builder<ModelPart> builder = new ImmutableList.Builder<ModelPart>();
        builder.add(new ModelPart[]{modelPart.getChild(BOTTOM), modelPart.getChild(BACK), modelPart.getChild(FRONT), modelPart.getChild(RIGHT), modelPart.getChild(LEFT), this.leftPaddle, this.rightPaddle});
        return builder;
    }

    public static void createChildren(PartDefinition partDefinition) {
        int i = 32;
        int j = 6;
        int k = 20;
        int l = 4;
        int m = 28;
        partDefinition.addOrReplaceChild(BOTTOM, CubeListBuilder.create().texOffs(0, 0).addBox(-14.0f, -9.0f, -3.0f, 28.0f, 16.0f, 3.0f), PartPose.offsetAndRotation(0.0f, 3.0f, 1.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(BACK, CubeListBuilder.create().texOffs(0, 19).addBox(-13.0f, -7.0f, -1.0f, 18.0f, 6.0f, 2.0f), PartPose.offsetAndRotation(-15.0f, 4.0f, 4.0f, 0.0f, 4.712389f, 0.0f));
        partDefinition.addOrReplaceChild(FRONT, CubeListBuilder.create().texOffs(0, 27).addBox(-8.0f, -7.0f, -1.0f, 16.0f, 6.0f, 2.0f), PartPose.offsetAndRotation(15.0f, 4.0f, 0.0f, 0.0f, 1.5707964f, 0.0f));
        partDefinition.addOrReplaceChild(RIGHT, CubeListBuilder.create().texOffs(0, 35).addBox(-14.0f, -7.0f, -1.0f, 28.0f, 6.0f, 2.0f), PartPose.offsetAndRotation(0.0f, 4.0f, -9.0f, 0.0f, (float)Math.PI, 0.0f));
        partDefinition.addOrReplaceChild(LEFT, CubeListBuilder.create().texOffs(0, 43).addBox(-14.0f, -7.0f, -1.0f, 28.0f, 6.0f, 2.0f), PartPose.offset(0.0f, 4.0f, 9.0f));
        int n = 20;
        int o = 7;
        int p = 6;
        float f = -5.0f;
        partDefinition.addOrReplaceChild(LEFT_PADDLE, CubeListBuilder.create().texOffs(62, 0).addBox(-1.0f, 0.0f, -5.0f, 2.0f, 2.0f, 18.0f).addBox(-1.001f, -3.0f, 8.0f, 1.0f, 6.0f, 7.0f), PartPose.offsetAndRotation(3.0f, -5.0f, 9.0f, 0.0f, 0.0f, 0.19634955f));
        partDefinition.addOrReplaceChild(RIGHT_PADDLE, CubeListBuilder.create().texOffs(62, 20).addBox(-1.0f, 0.0f, -5.0f, 2.0f, 2.0f, 18.0f).addBox(0.001f, -3.0f, 8.0f, 1.0f, 6.0f, 7.0f), PartPose.offsetAndRotation(3.0f, -5.0f, -9.0f, 0.0f, (float)Math.PI, 0.19634955f));
        partDefinition.addOrReplaceChild(WATER_PATCH, CubeListBuilder.create().texOffs(0, 0).addBox(-14.0f, -9.0f, -3.0f, 28.0f, 16.0f, 3.0f), PartPose.offsetAndRotation(0.0f, -3.0f, 1.0f, 1.5707964f, 0.0f, 0.0f));
    }

    public static LayerDefinition createBodyModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        BoatModel.createChildren(partDefinition);
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

    @Override
    public ModelPart waterPatch() {
        return this.waterPatch;
    }

    private static void animatePaddle(Boat boat, int i, ModelPart modelPart, float f) {
        float g = boat.getRowingTime(i, f);
        modelPart.xRot = Mth.clampedLerp(-1.0471976f, -0.2617994f, (Mth.sin(-g) + 1.0f) / 2.0f);
        modelPart.yRot = Mth.clampedLerp(-0.7853982f, 0.7853982f, (Mth.sin(-g + 1.0f) + 1.0f) / 2.0f);
        if (i == 1) {
            modelPart.yRot = (float)Math.PI - modelPart.yRot;
        }
    }

    @Override
    public /* synthetic */ Iterable parts() {
        return this.parts();
    }
}

