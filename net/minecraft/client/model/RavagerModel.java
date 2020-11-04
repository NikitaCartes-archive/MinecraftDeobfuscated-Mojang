/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

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
import net.minecraft.world.entity.monster.Ravager;

@Environment(value=EnvType.CLIENT)
public class RavagerModel
extends HierarchicalModel<Ravager> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart mouth;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart neck;

    public RavagerModel(ModelPart modelPart) {
        this.root = modelPart;
        this.neck = modelPart.getChild("neck");
        this.head = this.neck.getChild("head");
        this.mouth = this.head.getChild("mouth");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 16;
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(68, 73).addBox(-5.0f, -1.0f, -18.0f, 10.0f, 10.0f, 18.0f), PartPose.offset(0.0f, -7.0f, 5.5f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -20.0f, -14.0f, 16.0f, 20.0f, 16.0f).texOffs(0, 0).addBox(-2.0f, -6.0f, -18.0f, 4.0f, 8.0f, 4.0f), PartPose.offset(0.0f, 16.0f, -17.0f));
        partDefinition3.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(74, 55).addBox(0.0f, -14.0f, -2.0f, 2.0f, 14.0f, 4.0f), PartPose.offsetAndRotation(-10.0f, -14.0f, -8.0f, 1.0995574f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(74, 55).mirror().addBox(0.0f, -14.0f, -2.0f, 2.0f, 14.0f, 4.0f), PartPose.offsetAndRotation(8.0f, -14.0f, -8.0f, 1.0995574f, 0.0f, 0.0f));
        partDefinition3.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(0, 36).addBox(-8.0f, 0.0f, -16.0f, 16.0f, 3.0f, 16.0f), PartPose.offset(0.0f, -2.0f, 2.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 55).addBox(-7.0f, -10.0f, -7.0f, 14.0f, 16.0f, 20.0f).texOffs(0, 91).addBox(-6.0f, 6.0f, -7.0f, 12.0f, 13.0f, 18.0f), PartPose.offsetAndRotation(0.0f, 1.0f, 2.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(96, 0).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 37.0f, 8.0f), PartPose.offset(-8.0f, -13.0f, 18.0f));
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(96, 0).mirror().addBox(-4.0f, 0.0f, -4.0f, 8.0f, 37.0f, 8.0f), PartPose.offset(8.0f, -13.0f, 18.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 37.0f, 8.0f), PartPose.offset(-8.0f, -13.0f, -5.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-4.0f, 0.0f, -4.0f, 8.0f, 37.0f, 8.0f), PartPose.offset(8.0f, -13.0f, -5.0f));
        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(Ravager ravager, float f, float g, float h, float i, float j) {
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        float k = 0.4f * g;
        this.rightHindLeg.xRot = Mth.cos(f * 0.6662f) * k;
        this.leftHindLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * k;
        this.rightFrontLeg.xRot = Mth.cos(f * 0.6662f + (float)Math.PI) * k;
        this.leftFrontLeg.xRot = Mth.cos(f * 0.6662f) * k;
    }

    @Override
    public void prepareMobModel(Ravager ravager, float f, float g, float h) {
        super.prepareMobModel(ravager, f, g, h);
        int i = ravager.getStunnedTick();
        int j = ravager.getRoarTick();
        int k = 20;
        int l = ravager.getAttackTick();
        int m = 10;
        if (l > 0) {
            float n = Mth.triangleWave((float)l - h, 10.0f);
            float o = (1.0f + n) * 0.5f;
            float p = o * o * o * 12.0f;
            float q = p * Mth.sin(this.neck.xRot);
            this.neck.z = -6.5f + p;
            this.neck.y = -7.0f - q;
            float r = Mth.sin(((float)l - h) / 10.0f * (float)Math.PI * 0.25f);
            this.mouth.xRot = 1.5707964f * r;
            this.mouth.xRot = l > 5 ? Mth.sin(((float)(-4 + l) - h) / 4.0f) * (float)Math.PI * 0.4f : 0.15707964f * Mth.sin((float)Math.PI * ((float)l - h) / 10.0f);
        } else {
            float n = -1.0f;
            float o = -1.0f * Mth.sin(this.neck.xRot);
            this.neck.x = 0.0f;
            this.neck.y = -7.0f - o;
            this.neck.z = 5.5f;
            boolean bl = i > 0;
            this.neck.xRot = bl ? 0.21991149f : 0.0f;
            this.mouth.xRot = (float)Math.PI * (bl ? 0.05f : 0.01f);
            if (bl) {
                double d = (double)i / 40.0;
                this.neck.x = (float)Math.sin(d * 10.0) * 3.0f;
            } else if (j > 0) {
                float q = Mth.sin(((float)(20 - j) - h) / 20.0f * (float)Math.PI * 0.25f);
                this.mouth.xRot = 1.5707964f * q;
            }
        }
    }
}

