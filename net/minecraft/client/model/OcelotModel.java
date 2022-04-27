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
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class OcelotModel<T extends Entity>
extends AgeableListModel<T> {
    private static final int CROUCH_STATE = 0;
    private static final int WALK_STATE = 1;
    private static final int SPRINT_STATE = 2;
    protected static final int SITTING_STATE = 3;
    private static final float XO = 0.0f;
    private static final float YO = 16.0f;
    private static final float ZO = -9.0f;
    private static final float HEAD_WALK_Y = 15.0f;
    private static final float HEAD_WALK_Z = -9.0f;
    private static final float BODY_WALK_Y = 12.0f;
    private static final float BODY_WALK_Z = -10.0f;
    private static final float TAIL_1_WALK_Y = 15.0f;
    private static final float TAIL_1_WALK_Z = 8.0f;
    private static final float TAIL_2_WALK_Y = 20.0f;
    private static final float TAIL_2_WALK_Z = 14.0f;
    protected static final float BACK_LEG_Y = 18.0f;
    protected static final float BACK_LEG_Z = 5.0f;
    protected static final float FRONT_LEG_Y = 14.1f;
    private static final float FRONT_LEG_Z = -5.0f;
    private static final String TAIL_1 = "tail1";
    private static final String TAIL_2 = "tail2";
    protected final ModelPart leftHindLeg;
    protected final ModelPart rightHindLeg;
    protected final ModelPart leftFrontLeg;
    protected final ModelPart rightFrontLeg;
    protected final ModelPart tail1;
    protected final ModelPart tail2;
    protected final ModelPart head;
    protected final ModelPart body;
    protected int state = 1;

    public OcelotModel(ModelPart modelPart) {
        super(true, 10.0f, 4.0f);
        this.head = modelPart.getChild("head");
        this.body = modelPart.getChild("body");
        this.tail1 = modelPart.getChild(TAIL_1);
        this.tail2 = modelPart.getChild(TAIL_2);
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
    }

    public static MeshDefinition createBodyMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().addBox("main", -2.5f, -2.0f, -3.0f, 5.0f, 4.0f, 5.0f, cubeDeformation).addBox("nose", -1.5f, -0.001f, -4.0f, 3, 2, 2, cubeDeformation, 0, 24).addBox("ear1", -2.0f, -3.0f, 0.0f, 1, 1, 2, cubeDeformation, 0, 10).addBox("ear2", 1.0f, -3.0f, 0.0f, 1, 1, 2, cubeDeformation, 6, 10), PartPose.offset(0.0f, 15.0f, -9.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(20, 0).addBox(-2.0f, 3.0f, -8.0f, 4.0f, 16.0f, 6.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 12.0f, -10.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(TAIL_1, CubeListBuilder.create().texOffs(0, 15).addBox(-0.5f, 0.0f, 0.0f, 1.0f, 8.0f, 1.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 15.0f, 8.0f, 0.9f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(TAIL_2, CubeListBuilder.create().texOffs(4, 15).addBox(-0.5f, 0.0f, 0.0f, 1.0f, 8.0f, 1.0f, cubeDeformation), PartPose.offset(0.0f, 20.0f, 14.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(8, 13).addBox(-1.0f, 0.0f, 1.0f, 2.0f, 6.0f, 2.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(1.1f, 18.0f, 5.0f));
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-1.1f, 18.0f, 5.0f));
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(40, 0).addBox(-1.0f, 0.0f, 0.0f, 2.0f, 10.0f, 2.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder2, PartPose.offset(1.2f, 14.1f, -5.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder2, PartPose.offset(-1.2f, 14.1f, -5.0f));
        return meshDefinition;
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.leftHindLeg, this.rightHindLeg, this.leftFrontLeg, this.rightFrontLeg, this.tail1, this.tail2);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        if (this.state != 3) {
            this.body.xRot = 1.5707964f;
            if (this.state == 2) {
                this.leftHindLeg.xRot = Mth.cos(f * 0.6662f) * g;
                this.rightHindLeg.xRot = Mth.cos(f * 0.6662f + 0.3f) * g;
                this.leftFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI + 0.3f) * g;
                this.rightFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * g;
                this.tail2.xRot = 1.7278761f + 0.31415927f * Mth.cos(f) * g;
            } else {
                this.leftHindLeg.xRot = Mth.cos(f * 0.6662f) * g;
                this.rightHindLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * g;
                this.leftFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * g;
                this.rightFrontLeg.xRot = Mth.cos(f * 0.6662f) * g;
                this.tail2.xRot = this.state == 1 ? 1.7278761f + 0.7853982f * Mth.cos(f) * g : 1.7278761f + 0.47123894f * Mth.cos(f) * g;
            }
        }
    }

    @Override
    public void prepareMobModel(T entity, float f, float g, float h) {
        this.body.y = 12.0f;
        this.body.z = -10.0f;
        this.head.y = 15.0f;
        this.head.z = -9.0f;
        this.tail1.y = 15.0f;
        this.tail1.z = 8.0f;
        this.tail2.y = 20.0f;
        this.tail2.z = 14.0f;
        this.leftFrontLeg.y = 14.1f;
        this.leftFrontLeg.z = -5.0f;
        this.rightFrontLeg.y = 14.1f;
        this.rightFrontLeg.z = -5.0f;
        this.leftHindLeg.y = 18.0f;
        this.leftHindLeg.z = 5.0f;
        this.rightHindLeg.y = 18.0f;
        this.rightHindLeg.z = 5.0f;
        this.tail1.xRot = 0.9f;
        if (((Entity)entity).isCrouching()) {
            this.body.y += 1.0f;
            this.head.y += 2.0f;
            this.tail1.y += 1.0f;
            this.tail2.y += -4.0f;
            this.tail2.z += 2.0f;
            this.tail1.xRot = 1.5707964f;
            this.tail2.xRot = 1.5707964f;
            this.state = 0;
        } else if (((Entity)entity).isSprinting()) {
            this.tail2.y = this.tail1.y;
            this.tail2.z += 2.0f;
            this.tail1.xRot = 1.5707964f;
            this.tail2.xRot = 1.5707964f;
            this.state = 2;
        } else {
            this.state = 1;
        }
    }
}

