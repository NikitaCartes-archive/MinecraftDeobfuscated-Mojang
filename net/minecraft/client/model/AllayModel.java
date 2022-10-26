/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.client.renderer.RenderType;
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
    private static final float FLYING_ANIMATION_X_ROT = 0.7853982f;
    private static final float MAX_HAND_HOLDING_ITEM_X_ROT_RAD = -1.134464f;
    private static final float MIN_HAND_HOLDING_ITEM_X_ROT_RAD = -1.0471976f;

    public AllayModel(ModelPart modelPart) {
        super(RenderType::entityTranslucent);
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
        partDefinition3.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0f, 1.0f, 0.0f, 0.0f, 5.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(-0.5f, 0.0f, 0.6f));
        partDefinition3.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0f, 1.0f, 0.0f, 0.0f, 5.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(0.5f, 0.0f, 0.6f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(Allay allay, float f, float g, float h, float i, float j) {
        float t;
        float s;
        float r;
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float k = h * 20.0f * ((float)Math.PI / 180) + f;
        float l = Mth.cos(k) * (float)Math.PI * 0.15f + g;
        float m = h - (float)allay.tickCount;
        float n = h * 9.0f * ((float)Math.PI / 180);
        float o = Math.min(g / 0.3f, 1.0f);
        float p = 1.0f - o;
        float q = allay.getHoldingItemAnimationProgress(m);
        if (allay.isDancing()) {
            r = h * 8.0f * ((float)Math.PI / 180) + g;
            s = Mth.cos(r) * 16.0f * ((float)Math.PI / 180);
            t = allay.getSpinningProgress(m);
            float u = Mth.cos(r) * 14.0f * ((float)Math.PI / 180);
            float v = Mth.cos(r) * 30.0f * ((float)Math.PI / 180);
            this.root.yRot = allay.isSpinning() ? (float)Math.PI * 4 * t : this.root.yRot;
            this.root.zRot = s * (1.0f - t);
            this.head.yRot = v * (1.0f - t);
            this.head.zRot = u * (1.0f - t);
        } else {
            this.head.xRot = j * ((float)Math.PI / 180);
            this.head.yRot = i * ((float)Math.PI / 180);
        }
        this.right_wing.xRot = 0.43633232f * (1.0f - o);
        this.right_wing.yRot = -0.7853982f + l;
        this.left_wing.xRot = 0.43633232f * (1.0f - o);
        this.left_wing.yRot = 0.7853982f - l;
        this.body.xRot = o * 0.7853982f;
        r = q * Mth.lerp(o, -1.0471976f, -1.134464f);
        this.root.y += (float)Math.cos(n) * 0.25f * p;
        this.right_arm.xRot = r;
        this.left_arm.xRot = r;
        s = p * (1.0f - q);
        t = 0.43633232f - Mth.cos(n + 4.712389f) * (float)Math.PI * 0.075f * s;
        this.left_arm.zRot = -t;
        this.right_arm.zRot = t;
        this.right_arm.yRot = 0.27925268f * q;
        this.left_arm.yRot = -0.27925268f * q;
    }

    @Override
    public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
        float f = 1.0f;
        float g = 3.0f;
        this.root.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        poseStack.translate(0.0f, 0.0625f, 0.1875f);
        poseStack.mulPose(Axis.XP.rotation(this.right_arm.xRot));
        poseStack.scale(0.7f, 0.7f, 0.7f);
        poseStack.translate(0.0625f, 0.0f, 0.0f);
    }
}

