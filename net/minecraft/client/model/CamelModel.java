/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.definitions.CamelAnimation;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

@Environment(value=EnvType.CLIENT)
public class CamelModel<T extends Camel>
extends HierarchicalModel<T> {
    private static final float MAX_WALK_ANIMATION_SPEED = 2.0f;
    private static final float WALK_ANIMATION_SCALE_FACTOR = 2.5f;
    private static final String SADDLE = "saddle";
    private static final String BRIDLE = "bridle";
    private static final String REINS = "reins";
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart[] saddleParts;
    private final ModelPart[] ridingParts;

    public CamelModel(ModelPart modelPart) {
        this.root = modelPart;
        ModelPart modelPart2 = modelPart.getChild("body");
        this.head = modelPart2.getChild("head");
        this.saddleParts = new ModelPart[]{modelPart2.getChild(SADDLE), this.head.getChild(BRIDLE)};
        this.ridingParts = new ModelPart[]{this.head.getChild(REINS)};
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeDeformation cubeDeformation = new CubeDeformation(0.1f);
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 25).addBox(-7.5f, -12.0f, -23.5f, 15.0f, 12.0f, 27.0f), PartPose.offset(0.0f, 4.0f, 9.5f));
        partDefinition2.addOrReplaceChild("hump", CubeListBuilder.create().texOffs(74, 0).addBox(-4.5f, -5.0f, -5.5f, 9.0f, 5.0f, 11.0f), PartPose.offset(0.0f, -12.0f, -10.0f));
        partDefinition2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(122, 0).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 14.0f, 0.0f), PartPose.offset(0.0f, -9.0f, 3.5f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(60, 24).addBox(-3.5f, -7.0f, -15.0f, 7.0f, 8.0f, 19.0f).texOffs(21, 0).addBox(-3.5f, -21.0f, -15.0f, 7.0f, 14.0f, 7.0f).texOffs(50, 0).addBox(-2.5f, -21.0f, -21.0f, 5.0f, 5.0f, 6.0f), PartPose.offset(0.0f, -3.0f, -19.5f));
        partDefinition3.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(45, 0).addBox(-0.5f, 0.5f, -1.0f, 3.0f, 1.0f, 2.0f), PartPose.offset(3.0f, -21.0f, -9.5f));
        partDefinition3.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(67, 0).addBox(-2.5f, 0.5f, -1.0f, 3.0f, 1.0f, 2.0f), PartPose.offset(-3.0f, -21.0f, -9.5f));
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(58, 16).addBox(-2.5f, 2.0f, -2.5f, 5.0f, 21.0f, 5.0f), PartPose.offset(4.9f, 1.0f, 9.5f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(94, 16).addBox(-2.5f, 2.0f, -2.5f, 5.0f, 21.0f, 5.0f), PartPose.offset(-4.9f, 1.0f, 9.5f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5f, 2.0f, -2.5f, 5.0f, 21.0f, 5.0f), PartPose.offset(4.9f, 1.0f, -10.5f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5f, 2.0f, -2.5f, 5.0f, 21.0f, 5.0f), PartPose.offset(-4.9f, 1.0f, -10.5f));
        partDefinition2.addOrReplaceChild(SADDLE, CubeListBuilder.create().texOffs(74, 64).addBox(-4.5f, -17.0f, -15.5f, 9.0f, 5.0f, 11.0f, cubeDeformation).texOffs(92, 114).addBox(-3.5f, -20.0f, -15.5f, 7.0f, 3.0f, 11.0f, cubeDeformation).texOffs(0, 89).addBox(-7.5f, -12.0f, -23.5f, 15.0f, 12.0f, 27.0f, cubeDeformation), PartPose.offset(0.0f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild(REINS, CubeListBuilder.create().texOffs(98, 42).addBox(3.51f, -18.0f, -17.0f, 0.0f, 7.0f, 15.0f).texOffs(84, 57).addBox(-3.5f, -18.0f, -2.0f, 7.0f, 7.0f, 0.0f).texOffs(98, 42).addBox(-3.51f, -18.0f, -17.0f, 0.0f, 7.0f, 15.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild(BRIDLE, CubeListBuilder.create().texOffs(60, 87).addBox(-3.5f, -7.0f, -15.0f, 7.0f, 8.0f, 19.0f, cubeDeformation).texOffs(21, 64).addBox(-3.5f, -21.0f, -15.0f, 7.0f, 14.0f, 7.0f, cubeDeformation).texOffs(50, 64).addBox(-2.5f, -21.0f, -21.0f, 5.0f, 5.0f, 6.0f, cubeDeformation).texOffs(74, 70).addBox(2.5f, -19.0f, -18.0f, 1.0f, 2.0f, 2.0f).texOffs(74, 70).mirror().addBox(-3.5f, -19.0f, -18.0f, 1.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(T camel, float f, float g, float h, float i, float j) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.applyHeadRotation(camel, i, j, h);
        this.toggleInvisibleParts(camel);
        this.animateWalk(CamelAnimation.CAMEL_WALK, f, g, 2.0f, 2.5f);
        this.animate(((Camel)camel).sitAnimationState, CamelAnimation.CAMEL_SIT, h, 1.0f);
        this.animate(((Camel)camel).sitPoseAnimationState, CamelAnimation.CAMEL_SIT_POSE, h, 1.0f);
        this.animate(((Camel)camel).sitUpAnimationState, CamelAnimation.CAMEL_STANDUP, h, 1.0f);
        this.animate(((Camel)camel).idleAnimationState, CamelAnimation.CAMEL_IDLE, h, 1.0f);
        this.animate(((Camel)camel).dashAnimationState, CamelAnimation.CAMEL_DASH, h, 1.0f);
    }

    private void applyHeadRotation(T camel, float f, float g, float h) {
        f = Mth.clamp(f, -30.0f, 30.0f);
        g = Mth.clamp(g, -25.0f, 45.0f);
        if (((Camel)camel).getJumpCooldown() > 0) {
            float i = h - (float)((Camel)camel).tickCount;
            float j = 45.0f * ((float)((Camel)camel).getJumpCooldown() - i) / 55.0f;
            g = Mth.clamp(g + j, -25.0f, 70.0f);
        }
        this.head.yRot = f * ((float)Math.PI / 180);
        this.head.xRot = g * ((float)Math.PI / 180);
    }

    private void toggleInvisibleParts(T camel) {
        boolean bl = ((AbstractHorse)camel).isSaddled();
        boolean bl2 = ((Entity)camel).isVehicle();
        for (ModelPart modelPart : this.saddleParts) {
            modelPart.visible = bl;
        }
        for (ModelPart modelPart : this.ridingParts) {
            modelPart.visible = bl2 && bl;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        if (this.young) {
            float l = 2.0f;
            float m = 1.1f;
            poseStack.pushPose();
            poseStack.scale(0.45454544f, 0.41322312f, 0.45454544f);
            poseStack.translate(0.0f, 2.0625f, 0.0f);
            this.root().render(poseStack, vertexConsumer, i, j, f, g, h, k);
            poseStack.popPose();
        } else {
            this.root().render(poseStack, vertexConsumer, i, j, f, g, h, k);
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

