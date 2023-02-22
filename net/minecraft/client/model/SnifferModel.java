/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.definitions.SnifferAnimation;
import net.minecraft.client.model.AgeableHierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.sniffer.Sniffer;

@Environment(value=EnvType.CLIENT)
public class SnifferModel<T extends Sniffer>
extends AgeableHierarchicalModel<T> {
    private static final float WALK_ANIMATION_SPEED_FACTOR = 9000.0f;
    private static final float MAX_WALK_ANIMATION_SPEED = 1.0f;
    private static final float PANIC_ANIMATION_FACTOR = 2.0f;
    private final ModelPart root;
    private final ModelPart head;

    public SnifferModel(ModelPart modelPart) {
        super(0.5f, 24.0f);
        this.root = modelPart.getChild("root");
        this.head = this.root.getChild("bone").getChild("body").getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0f, 5.0f, 0.0f));
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("body", CubeListBuilder.create().texOffs(62, 0).addBox(-12.5f, -14.0f, -20.0f, 25.0f, 24.0f, 40.0f, new CubeDeformation(0.5f)).texOffs(62, 68).addBox(-12.5f, -14.0f, -20.0f, 25.0f, 29.0f, 40.0f, new CubeDeformation(0.0f)).texOffs(87, 68).addBox(-12.5f, 12.0f, -20.0f, 25.0f, 0.0f, 40.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 0.0f, 0.0f));
        partDefinition2.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(32, 87).addBox(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(-7.5f, 10.0f, -15.0f));
        partDefinition2.addOrReplaceChild("right_mid_leg", CubeListBuilder.create().texOffs(32, 105).addBox(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(-7.5f, 10.0f, 0.0f));
        partDefinition2.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(32, 123).addBox(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(-7.5f, 10.0f, 15.0f));
        partDefinition2.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0, 87).addBox(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(7.5f, 10.0f, -15.0f));
        partDefinition2.addOrReplaceChild("left_mid_leg", CubeListBuilder.create().texOffs(0, 105).addBox(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(7.5f, 10.0f, 0.0f));
        partDefinition2.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(0, 123).addBox(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(7.5f, 10.0f, 15.0f));
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("head", CubeListBuilder.create().texOffs(8, 15).addBox(-6.5f, -7.5f, -11.5f, 13.0f, 18.0f, 11.0f, new CubeDeformation(0.0f)).texOffs(8, 4).addBox(-6.5f, 7.5f, -11.5f, 13.0f, 0.0f, 11.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 6.5f, -19.5f));
        partDefinition4.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(2, 0).addBox(0.0f, 0.0f, -3.0f, 1.0f, 19.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offset(6.5f, -7.5f, -4.5f));
        partDefinition4.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(48, 0).addBox(-1.0f, 0.0f, -3.0f, 1.0f, 19.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offset(-6.5f, -7.5f, -4.5f));
        partDefinition4.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(10, 45).addBox(-6.5f, -2.0f, -9.0f, 13.0f, 2.0f, 9.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -4.5f, -11.5f));
        partDefinition4.addOrReplaceChild("lower_beak", CubeListBuilder.create().texOffs(10, 57).addBox(-6.5f, -7.0f, -8.0f, 13.0f, 12.0f, 9.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 2.5f, -12.5f));
        return LayerDefinition.create(meshDefinition, 192, 192);
    }

    @Override
    public void setupAnim(T sniffer, float f, float g, float h, float i, float j) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        float k = Math.min((float)((Entity)sniffer).getDeltaMovement().horizontalDistanceSqr() * 9000.0f, 1.0f);
        float l = k * 2.0f;
        this.animate(((Sniffer)sniffer).walkingAnimationState, SnifferAnimation.SNIFFER_WALK, h, k);
        this.animate(((Sniffer)sniffer).panicAnimationState, SnifferAnimation.SNIFFER_WALK, h, l);
        this.animate(((Sniffer)sniffer).diggingAnimationState, SnifferAnimation.SNIFFER_DIG, h);
        this.animate(((Sniffer)sniffer).searchingAnimationState, SnifferAnimation.SNIFFER_SNIFF_SEARCH, h, k);
        this.animate(((Sniffer)sniffer).sniffingAnimationState, SnifferAnimation.SNIFFER_LONGSNIFF, h);
        this.animate(((Sniffer)sniffer).risingAnimationState, SnifferAnimation.SNIFFER_STAND_UP, h);
        this.animate(((Sniffer)sniffer).feelingHappyAnimationState, SnifferAnimation.SNIFFER_HAPPY, h);
        this.animate(((Sniffer)sniffer).scentingAnimationState, SnifferAnimation.SNIFFER_SNIFFSNIFF, h);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

