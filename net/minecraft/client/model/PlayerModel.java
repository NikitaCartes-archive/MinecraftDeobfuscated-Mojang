/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public class PlayerModel<T extends LivingEntity>
extends HumanoidModel<T> {
    private final List<ModelPart> parts;
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    private final ModelPart cloak;
    private final ModelPart ear;
    private final boolean slim;

    public PlayerModel(ModelPart modelPart2, boolean bl) {
        super(modelPart2, RenderType::entityTranslucent);
        this.slim = bl;
        this.ear = modelPart2.getChild("ear");
        this.cloak = modelPart2.getChild("cloak");
        this.leftSleeve = modelPart2.getChild("left_sleeve");
        this.rightSleeve = modelPart2.getChild("right_sleeve");
        this.leftPants = modelPart2.getChild("left_pants");
        this.rightPants = modelPart2.getChild("right_pants");
        this.jacket = modelPart2.getChild("jacket");
        this.parts = modelPart2.getAllParts().filter(modelPart -> !modelPart.isEmpty()).collect(ImmutableList.toImmutableList());
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, boolean bl) {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(cubeDeformation, 0.0f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("ear", CubeListBuilder.create().texOffs(24, 0).addBox(-3.0f, -6.0f, -1.0f, 6.0f, 6.0f, 1.0f, cubeDeformation), PartPose.ZERO);
        partDefinition.addOrReplaceChild("cloak", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, 0.0f, -1.0f, 10.0f, 16.0f, 1.0f, cubeDeformation, 1.0f, 0.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        float f = 0.25f;
        if (bl) {
            partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(5.0f, 2.5f, 0.0f));
            partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(-5.0f, 2.5f, 0.0f));
            partDefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.offset(5.0f, 2.5f, 0.0f));
            partDefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.offset(-5.0f, 2.5f, 0.0f));
        } else {
            partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(5.0f, 2.0f, 0.0f));
            partDefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.offset(5.0f, 2.0f, 0.0f));
            partDefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.offset(-5.0f, 2.0f, 0.0f));
        }
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation), PartPose.offset(1.9f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.offset(1.9f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.offset(-1.9f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, cubeDeformation.extend(0.25f)), PartPose.ZERO);
        return meshDefinition;
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.leftPants, this.rightPants, this.leftSleeve, this.rightSleeve, this.jacket));
    }

    public void renderEars(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
        this.ear.copyFrom(this.head);
        this.ear.x = 0.0f;
        this.ear.y = 0.0f;
        this.ear.render(poseStack, vertexConsumer, i, j);
    }

    public void renderCloak(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
        this.cloak.render(poseStack, vertexConsumer, i, j);
    }

    @Override
    public void setupAnim(T livingEntity, float f, float g, float h, float i, float j) {
        super.setupAnim(livingEntity, f, g, h, i, j);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
        if (((LivingEntity)livingEntity).getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            if (((Entity)livingEntity).isCrouching()) {
                this.cloak.z = 1.4f;
                this.cloak.y = 1.85f;
            } else {
                this.cloak.z = 0.0f;
                this.cloak.y = 0.0f;
            }
        } else if (((Entity)livingEntity).isCrouching()) {
            this.cloak.z = 0.3f;
            this.cloak.y = 0.8f;
        } else {
            this.cloak.z = -1.1f;
            this.cloak.y = -0.85f;
        }
    }

    @Override
    public void setAllVisible(boolean bl) {
        super.setAllVisible(bl);
        this.leftSleeve.visible = bl;
        this.rightSleeve.visible = bl;
        this.leftPants.visible = bl;
        this.rightPants.visible = bl;
        this.jacket.visible = bl;
        this.cloak.visible = bl;
        this.ear.visible = bl;
    }

    @Override
    public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
        ModelPart modelPart = this.getArm(humanoidArm);
        if (this.slim) {
            float f = 0.5f * (float)(humanoidArm == HumanoidArm.RIGHT ? 1 : -1);
            modelPart.x += f;
            modelPart.translateAndRotate(poseStack);
            modelPart.x -= f;
        } else {
            modelPart.translateAndRotate(poseStack);
        }
    }

    public ModelPart getRandomModelPart(Random random) {
        return this.parts.get(random.nextInt(this.parts.size()));
    }
}

