/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Turtle;

@Environment(value=EnvType.CLIENT)
public class TurtleModel<T extends Turtle>
extends QuadrupedModel<T> {
    private static final String EGG_BELLY = "egg_belly";
    private final ModelPart eggBelly;

    public TurtleModel(ModelPart modelPart) {
        super(modelPart, true, 120.0f, 0.0f, 9.0f, 6.0f, 120);
        this.eggBelly = modelPart.getChild(EGG_BELLY);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(3, 0).addBox(-3.0f, -1.0f, -3.0f, 6.0f, 5.0f, 6.0f), PartPose.offset(0.0f, 19.0f, -10.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(7, 37).addBox("shell", -9.5f, 3.0f, -10.0f, 19.0f, 20.0f, 6.0f).texOffs(31, 1).addBox("belly", -5.5f, 3.0f, -13.0f, 11.0f, 18.0f, 3.0f), PartPose.offsetAndRotation(0.0f, 11.0f, -10.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(EGG_BELLY, CubeListBuilder.create().texOffs(70, 33).addBox(-4.5f, 3.0f, -14.0f, 9.0f, 18.0f, 1.0f), PartPose.offsetAndRotation(0.0f, 11.0f, -10.0f, 1.5707964f, 0.0f, 0.0f));
        boolean i = true;
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(1, 23).addBox(-2.0f, 0.0f, 0.0f, 4.0f, 1.0f, 10.0f), PartPose.offset(-3.5f, 22.0f, 11.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(1, 12).addBox(-2.0f, 0.0f, 0.0f, 4.0f, 1.0f, 10.0f), PartPose.offset(3.5f, 22.0f, 11.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(27, 30).addBox(-13.0f, 0.0f, -2.0f, 13.0f, 1.0f, 5.0f), PartPose.offset(-5.0f, 21.0f, -4.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(27, 24).addBox(0.0f, 0.0f, -2.0f, 13.0f, 1.0f, 5.0f), PartPose.offset(5.0f, 21.0f, -4.0f));
        return LayerDefinition.create(meshDefinition, 128, 64);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.eggBelly));
    }

    @Override
    public void setupAnim(T turtle, float f, float g, float h, float i, float j) {
        super.setupAnim(turtle, f, g, h, i, j);
        this.rightHindLeg.xRot = Mth.cos(f * 0.6662f * 0.6f) * 0.5f * g;
        this.leftHindLeg.xRot = Mth.cos(f * 0.6662f * 0.6f + (float)Math.PI) * 0.5f * g;
        this.rightFrontLeg.zRot = Mth.cos(f * 0.6662f * 0.6f + (float)Math.PI) * 0.5f * g;
        this.leftFrontLeg.zRot = Mth.cos(f * 0.6662f * 0.6f) * 0.5f * g;
        this.rightFrontLeg.xRot = 0.0f;
        this.leftFrontLeg.xRot = 0.0f;
        this.rightFrontLeg.yRot = 0.0f;
        this.leftFrontLeg.yRot = 0.0f;
        this.rightHindLeg.yRot = 0.0f;
        this.leftHindLeg.yRot = 0.0f;
        if (!((Entity)turtle).isInWater() && ((Entity)turtle).isOnGround()) {
            float k = ((Turtle)turtle).isLayingEgg() ? 4.0f : 1.0f;
            float l = ((Turtle)turtle).isLayingEgg() ? 2.0f : 1.0f;
            float m = 5.0f;
            this.rightFrontLeg.yRot = Mth.cos(k * f * 5.0f + (float)Math.PI) * 8.0f * g * l;
            this.rightFrontLeg.zRot = 0.0f;
            this.leftFrontLeg.yRot = Mth.cos(k * f * 5.0f) * 8.0f * g * l;
            this.leftFrontLeg.zRot = 0.0f;
            this.rightHindLeg.yRot = Mth.cos(f * 5.0f + (float)Math.PI) * 3.0f * g;
            this.rightHindLeg.xRot = 0.0f;
            this.leftHindLeg.yRot = Mth.cos(f * 5.0f) * 3.0f * g;
            this.leftHindLeg.xRot = 0.0f;
        }
        this.eggBelly.visible = !this.young && ((Turtle)turtle).hasEgg();
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        boolean bl = this.eggBelly.visible;
        if (bl) {
            poseStack.pushPose();
            poseStack.translate(0.0f, -0.08f, 0.0f);
        }
        super.renderToBuffer(poseStack, vertexConsumer, i, j, f, g, h, k);
        if (bl) {
            poseStack.popPose();
        }
    }
}

