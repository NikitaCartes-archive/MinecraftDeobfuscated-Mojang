/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

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
import net.minecraft.world.entity.monster.Strider;

@Environment(value=EnvType.CLIENT)
public class StriderModel<T extends Strider>
extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart body;
    private final ModelPart rightBottomBristle;
    private final ModelPart rightMiddleBristle;
    private final ModelPart rightTopBristle;
    private final ModelPart leftTopBristle;
    private final ModelPart leftMiddleBristle;
    private final ModelPart leftBottomBristle;

    public StriderModel(ModelPart modelPart) {
        this.root = modelPart;
        this.rightLeg = modelPart.getChild("right_leg");
        this.leftLeg = modelPart.getChild("left_leg");
        this.body = modelPart.getChild("body");
        this.rightBottomBristle = this.body.getChild("right_bottom_bristle");
        this.rightMiddleBristle = this.body.getChild("right_middle_bristle");
        this.rightTopBristle = this.body.getChild("right_top_bristle");
        this.leftTopBristle = this.body.getChild("left_top_bristle");
        this.leftMiddleBristle = this.body.getChild("left_middle_bristle");
        this.leftBottomBristle = this.body.getChild("left_bottom_bristle");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 16.0f, 4.0f), PartPose.offset(-4.0f, 8.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 55).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 16.0f, 4.0f), PartPose.offset(4.0f, 8.0f, 0.0f));
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -6.0f, -8.0f, 16.0f, 14.0f, 16.0f), PartPose.offset(0.0f, 1.0f, 0.0f));
        partDefinition2.addOrReplaceChild("right_bottom_bristle", CubeListBuilder.create().texOffs(16, 65).addBox(-12.0f, 0.0f, 0.0f, 12.0f, 0.0f, 16.0f, true), PartPose.offsetAndRotation(-8.0f, 4.0f, -8.0f, 0.0f, 0.0f, -1.2217305f));
        partDefinition2.addOrReplaceChild("right_middle_bristle", CubeListBuilder.create().texOffs(16, 49).addBox(-12.0f, 0.0f, 0.0f, 12.0f, 0.0f, 16.0f, true), PartPose.offsetAndRotation(-8.0f, -1.0f, -8.0f, 0.0f, 0.0f, -1.134464f));
        partDefinition2.addOrReplaceChild("right_top_bristle", CubeListBuilder.create().texOffs(16, 33).addBox(-12.0f, 0.0f, 0.0f, 12.0f, 0.0f, 16.0f, true), PartPose.offsetAndRotation(-8.0f, -5.0f, -8.0f, 0.0f, 0.0f, -0.87266463f));
        partDefinition2.addOrReplaceChild("left_top_bristle", CubeListBuilder.create().texOffs(16, 33).addBox(0.0f, 0.0f, 0.0f, 12.0f, 0.0f, 16.0f), PartPose.offsetAndRotation(8.0f, -6.0f, -8.0f, 0.0f, 0.0f, 0.87266463f));
        partDefinition2.addOrReplaceChild("left_middle_bristle", CubeListBuilder.create().texOffs(16, 49).addBox(0.0f, 0.0f, 0.0f, 12.0f, 0.0f, 16.0f), PartPose.offsetAndRotation(8.0f, -2.0f, -8.0f, 0.0f, 0.0f, 1.134464f));
        partDefinition2.addOrReplaceChild("left_bottom_bristle", CubeListBuilder.create().texOffs(16, 65).addBox(0.0f, 0.0f, 0.0f, 12.0f, 0.0f, 16.0f), PartPose.offsetAndRotation(8.0f, 3.0f, -8.0f, 0.0f, 0.0f, 1.2217305f));
        return LayerDefinition.create(meshDefinition, 64, 128);
    }

    @Override
    public void setupAnim(Strider strider, float f, float g, float h, float i, float j) {
        g = Math.min(0.25f, g);
        if (!strider.isVehicle()) {
            this.body.xRot = j * ((float)Math.PI / 180);
            this.body.yRot = i * ((float)Math.PI / 180);
        } else {
            this.body.xRot = 0.0f;
            this.body.yRot = 0.0f;
        }
        float k = 1.5f;
        this.body.zRot = 0.1f * Mth.sin(f * 1.5f) * 4.0f * g;
        this.body.y = 2.0f;
        this.body.y -= 2.0f * Mth.cos(f * 1.5f) * 2.0f * g;
        this.leftLeg.xRot = Mth.sin(f * 1.5f * 0.5f) * 2.0f * g;
        this.rightLeg.xRot = Mth.sin(f * 1.5f * 0.5f + (float)Math.PI) * 2.0f * g;
        this.leftLeg.zRot = 0.17453292f * Mth.cos(f * 1.5f * 0.5f) * g;
        this.rightLeg.zRot = 0.17453292f * Mth.cos(f * 1.5f * 0.5f + (float)Math.PI) * g;
        this.leftLeg.y = 8.0f + 2.0f * Mth.sin(f * 1.5f * 0.5f + (float)Math.PI) * 2.0f * g;
        this.rightLeg.y = 8.0f + 2.0f * Mth.sin(f * 1.5f * 0.5f) * 2.0f * g;
        this.rightBottomBristle.zRot = -1.2217305f;
        this.rightMiddleBristle.zRot = -1.134464f;
        this.rightTopBristle.zRot = -0.87266463f;
        this.leftTopBristle.zRot = 0.87266463f;
        this.leftMiddleBristle.zRot = 1.134464f;
        this.leftBottomBristle.zRot = 1.2217305f;
        float l = Mth.cos(f * 1.5f + (float)Math.PI) * g;
        this.rightBottomBristle.zRot += l * 1.3f;
        this.rightMiddleBristle.zRot += l * 1.2f;
        this.rightTopBristle.zRot += l * 0.6f;
        this.leftTopBristle.zRot += l * 0.6f;
        this.leftMiddleBristle.zRot += l * 1.2f;
        this.leftBottomBristle.zRot += l * 1.3f;
        float m = 1.0f;
        float n = 1.0f;
        this.rightBottomBristle.zRot += 0.05f * Mth.sin(h * 1.0f * -0.4f);
        this.rightMiddleBristle.zRot += 0.1f * Mth.sin(h * 1.0f * 0.2f);
        this.rightTopBristle.zRot += 0.1f * Mth.sin(h * 1.0f * 0.4f);
        this.leftTopBristle.zRot += 0.1f * Mth.sin(h * 1.0f * 0.4f);
        this.leftMiddleBristle.zRot += 0.1f * Mth.sin(h * 1.0f * 0.2f);
        this.leftBottomBristle.zRot += 0.05f * Mth.sin(h * 1.0f * -0.4f);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

