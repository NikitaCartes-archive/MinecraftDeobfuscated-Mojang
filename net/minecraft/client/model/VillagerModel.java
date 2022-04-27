/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;

@Environment(value=EnvType.CLIENT)
public class VillagerModel<T extends Entity>
extends HierarchicalModel<T>
implements HeadedModel,
VillagerHeadModel {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart hatRim;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    protected final ModelPart nose;

    public VillagerModel(ModelPart modelPart) {
        this.root = modelPart;
        this.head = modelPart.getChild("head");
        this.hat = this.head.getChild("hat");
        this.hatRim = this.hat.getChild("hat_rim");
        this.nose = this.head.getChild("nose");
        this.rightLeg = modelPart.getChild("right_leg");
        this.leftLeg = modelPart.getChild("left_leg");
    }

    public static MeshDefinition createBodyModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = 0.5f;
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f), PartPose.ZERO);
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f, new CubeDeformation(0.51f)), PartPose.ZERO);
        partDefinition3.addOrReplaceChild("hat_rim", CubeListBuilder.create().texOffs(30, 47).addBox(-8.0f, -8.0f, -6.0f, 16.0f, 16.0f, 1.0f), PartPose.rotation(-1.5707964f, 0.0f, 0.0f));
        partDefinition2.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0f, -1.0f, -6.0f, 2.0f, 4.0f, 2.0f), PartPose.offset(0.0f, -2.0f, 0.0f));
        PartDefinition partDefinition4 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 20).addBox(-4.0f, 0.0f, -3.0f, 8.0f, 12.0f, 6.0f), PartPose.ZERO);
        partDefinition4.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(0, 38).addBox(-4.0f, 0.0f, -3.0f, 8.0f, 20.0f, 6.0f, new CubeDeformation(0.5f)), PartPose.ZERO);
        partDefinition.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(44, 22).addBox(-8.0f, -2.0f, -2.0f, 4.0f, 8.0f, 4.0f).texOffs(44, 22).addBox(4.0f, -2.0f, -2.0f, 4.0f, 8.0f, 4.0f, true).texOffs(40, 38).addBox(-4.0f, 2.0f, -2.0f, 8.0f, 4.0f, 4.0f), PartPose.offsetAndRotation(0.0f, 3.0f, -1.0f, -0.75f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-2.0f, 12.0f, 0.0f));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(2.0f, 12.0f, 0.0f));
        return meshDefinition;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        boolean bl = false;
        if (entity instanceof AbstractVillager) {
            bl = ((AbstractVillager)entity).getUnhappyCounter() > 0;
        }
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        if (bl) {
            this.head.zRot = 0.3f * Mth.sin(0.45f * h);
            this.head.xRot = 0.4f;
        } else {
            this.head.zRot = 0.0f;
        }
        this.rightLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * g * 0.5f;
        this.leftLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g * 0.5f;
        this.rightLeg.yRot = 0.0f;
        this.leftLeg.yRot = 0.0f;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void hatVisible(boolean bl) {
        this.head.visible = bl;
        this.hat.visible = bl;
        this.hatRim.visible = bl;
    }
}

