/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;

@Environment(value=EnvType.CLIENT)
public class HoglinModel<T extends Mob>
extends AgeableListModel<T> {
    private final ModelPart head;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart body;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart mane;

    public HoglinModel(ModelPart modelPart) {
        super(true, 8.0f, 6.0f, 1.9f, 2.0f, 24.0f);
        this.body = modelPart.getChild("body");
        this.mane = this.body.getChild("mane");
        this.head = modelPart.getChild("head");
        this.rightEar = this.head.getChild("right_ear");
        this.leftEar = this.head.getChild("left_ear");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(1, 1).addBox(-8.0f, -7.0f, -13.0f, 16.0f, 14.0f, 26.0f), PartPose.offset(0.0f, 7.0f, 0.0f));
        partDefinition2.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(90, 33).addBox(0.0f, 0.0f, -9.0f, 0.0f, 10.0f, 19.0f, new CubeDeformation(0.001f)), PartPose.offset(0.0f, -14.0f, -5.0f));
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(61, 1).addBox(-7.0f, -3.0f, -19.0f, 14.0f, 6.0f, 19.0f), PartPose.offsetAndRotation(0.0f, 2.0f, -12.0f, 0.87266463f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(1, 1).addBox(-6.0f, -1.0f, -2.0f, 6.0f, 1.0f, 4.0f), PartPose.offsetAndRotation(-6.0f, -2.0f, -3.0f, 0.0f, 0.0f, -0.6981317f));
        partDefinition3.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(1, 6).addBox(0.0f, -1.0f, -2.0f, 6.0f, 1.0f, 4.0f), PartPose.offsetAndRotation(6.0f, -2.0f, -3.0f, 0.0f, 0.0f, 0.6981317f));
        partDefinition3.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(10, 13).addBox(-1.0f, -11.0f, -1.0f, 2.0f, 11.0f, 2.0f), PartPose.offset(-7.0f, 2.0f, -12.0f));
        partDefinition3.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(1, 13).addBox(-1.0f, -11.0f, -1.0f, 2.0f, 11.0f, 2.0f), PartPose.offset(7.0f, 2.0f, -12.0f));
        int i = 14;
        int j = 11;
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(66, 42).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 14.0f, 6.0f), PartPose.offset(-4.0f, 10.0f, -8.5f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(41, 42).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 14.0f, 6.0f), PartPose.offset(4.0f, 10.0f, -8.5f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(21, 45).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 11.0f, 5.0f), PartPose.offset(-5.0f, 13.0f, 10.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(0, 45).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 11.0f, 5.0f), PartPose.offset(5.0f, 13.0f, 10.0f));
        return LayerDefinition.create(meshDefinition, 128, 64);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightFrontLeg, this.leftFrontLeg, this.rightHindLeg, this.leftHindLeg);
    }

    @Override
    public void setupAnim(T mob, float f, float g, float h, float i, float j) {
        this.rightEar.zRot = -0.6981317f - g * Mth.sin(f);
        this.leftEar.zRot = 0.6981317f + g * Mth.sin(f);
        this.head.yRot = i * ((float)Math.PI / 180);
        int k = ((HoglinBase)mob).getAttackAnimationRemainingTicks();
        float l = 1.0f - (float)Mth.abs(10 - 2 * k) / 10.0f;
        this.head.xRot = Mth.lerp(l, 0.87266463f, -0.34906584f);
        if (((LivingEntity)mob).isBaby()) {
            this.head.y = Mth.lerp(l, 2.0f, 5.0f);
            this.mane.z = -3.0f;
        } else {
            this.head.y = 2.0f;
            this.mane.z = -7.0f;
        }
        float m = 1.2f;
        this.rightFrontLeg.xRot = Mth.cos(f) * 1.2f * g;
        this.rightHindLeg.xRot = this.leftFrontLeg.xRot = Mth.cos(f + (float)Math.PI) * 1.2f * g;
        this.leftHindLeg.xRot = this.rightFrontLeg.xRot;
    }
}

