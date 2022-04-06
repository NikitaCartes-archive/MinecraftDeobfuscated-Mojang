/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.allay.Allay;

@Environment(value=EnvType.CLIENT)
public class AllayModel
extends HierarchicalModel<Allay>
implements ArmedModel {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_wing;
    private final ModelPart left_wing;

    public AllayModel(ModelPart modelPart) {
        this.root = modelPart.getChild("root");
        this.body = this.root.getChild("body");
        this.right_arm = this.body.getChild("right_arm");
        this.left_arm = this.body.getChild("left_arm");
        this.right_wing = this.body.getChild("right_wing");
        this.left_wing = this.body.getChild("left_wing");
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0f, 23.5f, 0.0f));
        partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5f, -5.0f, -2.5f, 5.0f, 5.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -3.99f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5f, 0.0f, -1.0f, 3.0f, 4.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(0, 16).addBox(-1.5f, 0.0f, -1.0f, 3.0f, 5.0f, 2.0f, new CubeDeformation(-0.2f)), PartPose.offset(0.0f, -4.0f, 0.0f));
        partDefinition3.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(23, 0).addBox(-0.75f, -0.5f, -1.0f, 1.0f, 4.0f, 2.0f, new CubeDeformation(-0.01f)), PartPose.offset(-1.75f, 0.5f, 0.0f));
        partDefinition3.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(23, 6).addBox(-0.25f, -0.5f, -1.0f, 1.0f, 4.0f, 2.0f, new CubeDeformation(-0.01f)), PartPose.offset(1.75f, 0.5f, 0.0f));
        partDefinition3.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0f, 1.0f, 0.0f, 0.0f, 5.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(-0.5f, 0.0f, 0.65f));
        partDefinition3.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0f, 1.0f, 0.0f, 0.0f, 5.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(0.5f, 0.0f, 0.65f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(Allay allay, float f, float g, float h, float i, float j) {
        float k = h * 20.0f * ((float)Math.PI / 180) + g;
        this.right_wing.xRot = 0.43633232f;
        this.right_wing.yRot = -0.61086524f + Mth.cos(k) * (float)Math.PI * 0.15f;
        this.left_wing.xRot = 0.43633232f;
        this.left_wing.yRot = 0.61086524f - Mth.cos(k) * (float)Math.PI * 0.15f;
        if (this.isIdle(g)) {
            float l = h * 9.0f * ((float)Math.PI / 180);
            this.root.y = 23.5f + Mth.cos(l) * 0.25f;
            this.right_arm.zRot = 0.43633232f - Mth.cos(l + 4.712389f) * (float)Math.PI * 0.075f;
            this.left_arm.zRot = -0.43633232f + Mth.cos(l + 4.712389f) * (float)Math.PI * 0.075f;
        } else {
            this.root.y = 23.5f;
            this.right_arm.zRot = 0.43633232f;
            this.left_arm.zRot = -0.43633232f;
        }
        if (!allay.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            this.right_arm.xRot = -1.134464f;
            this.right_arm.yRot = 0.27925268f;
            this.right_arm.zRot = (float)(-Math.PI) / 180;
            this.left_arm.xRot = -1.134464f;
            this.left_arm.yRot = -0.20943952f;
            this.left_arm.zRot = (float)Math.PI / 180;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        this.root.render(poseStack, vertexConsumer, i, j);
    }

    @Override
    public void prepareMobModel(Allay allay, float f, float g, float h) {
        this.right_arm.xRot = 0.0f;
        this.right_arm.yRot = 0.0f;
        this.right_arm.zRot = 0.3927f;
        this.left_arm.xRot = 0.0f;
        this.left_arm.yRot = 0.0f;
        this.left_arm.zRot = -0.3927f;
    }

    @Override
    public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
        poseStack.scale(0.7f, 0.7f, 0.7f);
        float f = 1.8f + (this.root.y - 23.5f) / 11.2f;
        poseStack.translate(0.05f, f, 0.2f);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-65.0f));
    }

    private boolean isIdle(float f) {
        return f == 0.0f;
    }
}

