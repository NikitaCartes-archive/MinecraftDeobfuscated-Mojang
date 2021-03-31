/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.world.entity.animal.Parrot;

@Environment(value=EnvType.CLIENT)
public class ParrotModel
extends HierarchicalModel<Parrot> {
    private static final String FEATHER = "feather";
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart head;
    private final ModelPart feather;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public ParrotModel(ModelPart modelPart) {
        this.root = modelPart;
        this.body = modelPart.getChild("body");
        this.tail = modelPart.getChild("tail");
        this.leftWing = modelPart.getChild("left_wing");
        this.rightWing = modelPart.getChild("right_wing");
        this.head = modelPart.getChild("head");
        this.feather = this.head.getChild(FEATHER);
        this.leftLeg = modelPart.getChild("left_leg");
        this.rightLeg = modelPart.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(2, 8).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 6.0f, 3.0f), PartPose.offset(0.0f, 16.5f, -3.0f));
        partDefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(22, 1).addBox(-1.5f, -1.0f, -1.0f, 3.0f, 4.0f, 1.0f), PartPose.offset(0.0f, 21.07f, 1.16f));
        partDefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(19, 8).addBox(-0.5f, 0.0f, -1.5f, 1.0f, 5.0f, 3.0f), PartPose.offset(1.5f, 16.94f, -2.76f));
        partDefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(19, 8).addBox(-0.5f, 0.0f, -1.5f, 1.0f, 5.0f, 3.0f), PartPose.offset(-1.5f, 16.94f, -2.76f));
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(2, 2).addBox(-1.0f, -1.5f, -1.0f, 2.0f, 3.0f, 2.0f), PartPose.offset(0.0f, 15.69f, -2.76f));
        partDefinition2.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(10, 0).addBox(-1.0f, -0.5f, -2.0f, 2.0f, 1.0f, 4.0f), PartPose.offset(0.0f, -2.0f, -1.0f));
        partDefinition2.addOrReplaceChild("beak1", CubeListBuilder.create().texOffs(11, 7).addBox(-0.5f, -1.0f, -0.5f, 1.0f, 2.0f, 1.0f), PartPose.offset(0.0f, -0.5f, -1.5f));
        partDefinition2.addOrReplaceChild("beak2", CubeListBuilder.create().texOffs(16, 7).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f), PartPose.offset(0.0f, -1.75f, -2.45f));
        partDefinition2.addOrReplaceChild(FEATHER, CubeListBuilder.create().texOffs(2, 18).addBox(0.0f, -4.0f, -2.0f, 0.0f, 5.0f, 4.0f), PartPose.offset(0.0f, -2.15f, 0.15f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(14, 18).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f);
        partDefinition.addOrReplaceChild("left_leg", cubeListBuilder, PartPose.offset(1.0f, 22.0f, -1.05f));
        partDefinition.addOrReplaceChild("right_leg", cubeListBuilder, PartPose.offset(-1.0f, 22.0f, -1.05f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(Parrot parrot, float f, float g, float h, float i, float j) {
        this.setupAnim(ParrotModel.getState(parrot), parrot.tickCount, f, g, h, i, j);
    }

    @Override
    public void prepareMobModel(Parrot parrot, float f, float g, float h) {
        this.prepare(ParrotModel.getState(parrot));
    }

    public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k, int l) {
        this.prepare(State.ON_SHOULDER);
        this.setupAnim(State.ON_SHOULDER, l, f, g, 0.0f, h, k);
        this.root.render(poseStack, vertexConsumer, i, j);
    }

    private void setupAnim(State state, int i, float f, float g, float h, float j, float k) {
        this.head.xRot = k * ((float)Math.PI / 180);
        this.head.yRot = j * ((float)Math.PI / 180);
        this.head.zRot = 0.0f;
        this.head.x = 0.0f;
        this.body.x = 0.0f;
        this.tail.x = 0.0f;
        this.rightWing.x = -1.5f;
        this.leftWing.x = 1.5f;
        switch (state) {
            case SITTING: {
                break;
            }
            case PARTY: {
                float l = Mth.cos(i);
                float m = Mth.sin(i);
                this.head.x = l;
                this.head.y = 15.69f + m;
                this.head.xRot = 0.0f;
                this.head.yRot = 0.0f;
                this.head.zRot = Mth.sin(i) * 0.4f;
                this.body.x = l;
                this.body.y = 16.5f + m;
                this.leftWing.zRot = -0.0873f - h;
                this.leftWing.x = 1.5f + l;
                this.leftWing.y = 16.94f + m;
                this.rightWing.zRot = 0.0873f + h;
                this.rightWing.x = -1.5f + l;
                this.rightWing.y = 16.94f + m;
                this.tail.x = l;
                this.tail.y = 21.07f + m;
                break;
            }
            case STANDING: {
                this.leftLeg.xRot += Mth.cos(f * 0.6662f) * 1.4f * g;
                this.rightLeg.xRot += Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g;
            }
            default: {
                float n = h * 0.3f;
                this.head.y = 15.69f + n;
                this.tail.xRot = 1.015f + Mth.cos(f * 0.6662f) * 0.3f * g;
                this.tail.y = 21.07f + n;
                this.body.y = 16.5f + n;
                this.leftWing.zRot = -0.0873f - h;
                this.leftWing.y = 16.94f + n;
                this.rightWing.zRot = 0.0873f + h;
                this.rightWing.y = 16.94f + n;
                this.leftLeg.y = 22.0f + n;
                this.rightLeg.y = 22.0f + n;
            }
        }
    }

    private void prepare(State state) {
        this.feather.xRot = -0.2214f;
        this.body.xRot = 0.4937f;
        this.leftWing.xRot = -0.6981f;
        this.leftWing.yRot = (float)(-Math.PI);
        this.rightWing.xRot = -0.6981f;
        this.rightWing.yRot = (float)(-Math.PI);
        this.leftLeg.xRot = -0.0299f;
        this.rightLeg.xRot = -0.0299f;
        this.leftLeg.y = 22.0f;
        this.rightLeg.y = 22.0f;
        this.leftLeg.zRot = 0.0f;
        this.rightLeg.zRot = 0.0f;
        switch (state) {
            case FLYING: {
                this.leftLeg.xRot += 0.6981317f;
                this.rightLeg.xRot += 0.6981317f;
                break;
            }
            case SITTING: {
                float f = 1.9f;
                this.head.y = 17.59f;
                this.tail.xRot = 1.5388988f;
                this.tail.y = 22.97f;
                this.body.y = 18.4f;
                this.leftWing.zRot = -0.0873f;
                this.leftWing.y = 18.84f;
                this.rightWing.zRot = 0.0873f;
                this.rightWing.y = 18.84f;
                this.leftLeg.y += 1.9f;
                this.rightLeg.y += 1.9f;
                this.leftLeg.xRot += 1.5707964f;
                this.rightLeg.xRot += 1.5707964f;
                break;
            }
            case PARTY: {
                this.leftLeg.zRot = -0.34906584f;
                this.rightLeg.zRot = 0.34906584f;
                break;
            }
        }
    }

    private static State getState(Parrot parrot) {
        if (parrot.isPartyParrot()) {
            return State.PARTY;
        }
        if (parrot.isInSittingPose()) {
            return State.SITTING;
        }
        if (parrot.isFlying()) {
            return State.FLYING;
        }
        return State.STANDING;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum State {
        FLYING,
        STANDING,
        SITTING,
        PARTY,
        ON_SHOULDER;

    }
}

