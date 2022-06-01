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
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.allay.Allay;

@Environment(value=EnvType.CLIENT)
public class AllayModel
extends HierarchicalModel<Allay>
implements ArmedModel {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_wing;
    private final ModelPart left_wing;
    private static final float FLYING_ANIMATION_X_ROT = 0.6981317f;
    private static final float MAX_HAND_HOLDING_ITEM_X_ROT_RAD = -0.7853982f;
    private static final float MIN_HAND_HOLDING_ITEM_X_ROT_RAD = -1.0471976f;

    public AllayModel(ModelPart modelPart) {
        this.root = modelPart.getChild("root");
        this.head = this.root.getChild("head");
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
        float r;
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        float k = h * 20.0f * ((float)Math.PI / 180) + g;
        float l = Mth.cos(k) * (float)Math.PI * 0.15f;
        float m = h - (float)allay.tickCount;
        float n = h * 9.0f * ((float)Math.PI / 180);
        float o = Math.min(g / 0.3f, 1.0f);
        float p = 1.0f - o;
        float q = allay.getHoldingItemAnimationProgress(m);
        this.right_wing.xRot = 0.43633232f;
        this.right_wing.yRot = -0.61086524f + l;
        this.left_wing.xRot = 0.43633232f;
        this.left_wing.yRot = 0.61086524f - l;
        this.body.xRot = r = o * 0.6981317f;
        float s = Mth.lerp(q, r, Mth.lerp(o, -1.0471976f, -0.7853982f));
        this.root.y += (float)Math.cos(n) * 0.25f * p;
        this.right_arm.xRot = s;
        this.left_arm.xRot = s;
        float t = p * (1.0f - q);
        float u = 0.43633232f - Mth.cos(n + 4.712389f) * (float)Math.PI * 0.075f * t;
        this.left_arm.zRot = -u;
        this.right_arm.zRot = u;
        this.right_arm.yRot = 0.27925268f * q;
        this.left_arm.yRot = -0.27925268f * q;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        this.root.render(poseStack, vertexConsumer, i, j);
    }

    @Override
    public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
        float f = -1.5f;
        float g = 1.5f;
        this.root.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        poseStack.translate(0.0, -0.09375, 0.09375);
        poseStack.mulPose(Vector3f.XP.rotation(this.right_arm.xRot + 0.43633232f));
        poseStack.scale(0.7f, 0.7f, 0.7f);
        poseStack.translate(0.0625, 0.0, 0.0);
    }
}

