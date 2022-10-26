/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

@Environment(value=EnvType.CLIENT)
public class LlamaModel<T extends AbstractChestedHorse>
extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightChest;
    private final ModelPart leftChest;

    public LlamaModel(ModelPart modelPart) {
        this.head = modelPart.getChild("head");
        this.body = modelPart.getChild("body");
        this.rightChest = modelPart.getChild("right_chest");
        this.leftChest = modelPart.getChild("left_chest");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -14.0f, -10.0f, 4.0f, 4.0f, 9.0f, cubeDeformation).texOffs(0, 14).addBox("neck", -4.0f, -16.0f, -6.0f, 8.0f, 18.0f, 6.0f, cubeDeformation).texOffs(17, 0).addBox("ear", -4.0f, -19.0f, -4.0f, 3.0f, 3.0f, 2.0f, cubeDeformation).texOffs(17, 0).addBox("ear", 1.0f, -19.0f, -4.0f, 3.0f, 3.0f, 2.0f, cubeDeformation), PartPose.offset(0.0f, 7.0f, -6.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(29, 0).addBox(-6.0f, -10.0f, -7.0f, 12.0f, 18.0f, 10.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 5.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_chest", CubeListBuilder.create().texOffs(45, 28).addBox(-3.0f, 0.0f, 0.0f, 8.0f, 8.0f, 3.0f, cubeDeformation), PartPose.offsetAndRotation(-8.5f, 3.0f, 3.0f, 0.0f, 1.5707964f, 0.0f));
        partDefinition.addOrReplaceChild("left_chest", CubeListBuilder.create().texOffs(45, 41).addBox(-3.0f, 0.0f, 0.0f, 8.0f, 8.0f, 3.0f, cubeDeformation), PartPose.offsetAndRotation(5.5f, 3.0f, 3.0f, 0.0f, 1.5707964f, 0.0f));
        int i = 4;
        int j = 14;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(29, 29).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 14.0f, 4.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-3.5f, 10.0f, 6.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(3.5f, 10.0f, 6.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-3.5f, 10.0f, -5.0f));
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(3.5f, 10.0f, -5.0f));
        return LayerDefinition.create(meshDefinition, 128, 64);
    }

    @Override
    public void setupAnim(T abstractChestedHorse, float f, float g, float h, float i, float j) {
        boolean bl;
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        this.rightHindLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        this.leftHindLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.rightFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
        this.leftFrontLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
        this.rightChest.visible = bl = !((AgeableMob)abstractChestedHorse).isBaby() && ((AbstractChestedHorse)abstractChestedHorse).hasChest();
        this.leftChest.visible = bl;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        if (this.young) {
            float l = 2.0f;
            poseStack.pushPose();
            float m = 0.7f;
            poseStack.scale(0.71428573f, 0.64935064f, 0.7936508f);
            poseStack.translate(0.0f, 1.3125f, 0.22f);
            this.head.render(poseStack, vertexConsumer, i, j, f, g, h, k);
            poseStack.popPose();
            poseStack.pushPose();
            float n = 1.1f;
            poseStack.scale(0.625f, 0.45454544f, 0.45454544f);
            poseStack.translate(0.0f, 2.0625f, 0.0f);
            this.body.render(poseStack, vertexConsumer, i, j, f, g, h, k);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.scale(0.45454544f, 0.41322312f, 0.45454544f);
            poseStack.translate(0.0f, 2.0625f, 0.0f);
            ImmutableList.of(this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest).forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
            poseStack.popPose();
        } else {
            ImmutableList.of(this.head, this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest).forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
        }
    }
}

