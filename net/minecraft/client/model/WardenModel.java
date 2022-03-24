/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.animation.definitions.WardenAnimation;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.monster.warden.Warden;

@Environment(value=EnvType.CLIENT)
public class WardenModel<T extends Warden>
extends HierarchicalModel<T> {
    private static final float DEFAULT_ARM_X_Y = 13.0f;
    private static final float DEFAULT_ARM_Z = 1.0f;
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();
    private final ModelPart root;
    protected final ModelPart bone;
    protected final ModelPart body;
    protected final ModelPart head;
    protected final ModelPart rightTendril;
    protected final ModelPart leftTendril;
    protected final ModelPart leftLeg;
    protected final ModelPart leftArm;
    protected final ModelPart rightArm;
    protected final ModelPart rightLeg;

    public WardenModel(ModelPart modelPart) {
        super(RenderType::entityCutoutNoCull);
        this.root = modelPart;
        this.bone = modelPart.getChild("bone");
        this.body = this.bone.getChild("body");
        this.head = this.body.getChild("head");
        this.rightLeg = this.bone.getChild("right_leg");
        this.leftLeg = this.bone.getChild("left_leg");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightTendril = this.head.getChild("right_tendril");
        this.leftTendril = this.head.getChild("left_tendril");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0f, 24.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0f, -13.0f, -4.0f, 18.0f, 21.0f, 11.0f), PartPose.offset(0.0f, -21.0f, 0.0f));
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0f, -16.0f, -5.0f, 16.0f, 16.0f, 10.0f), PartPose.offset(0.0f, -13.0f, 0.0f));
        partDefinition4.addOrReplaceChild("right_tendril", CubeListBuilder.create().texOffs(52, 32).addBox(-16.0f, -13.0f, 0.0f, 16.0f, 16.0f, 0.0f), PartPose.offset(-8.0f, -12.0f, 0.0f));
        partDefinition4.addOrReplaceChild("left_tendril", CubeListBuilder.create().texOffs(58, 0).addBox(0.0f, -13.0f, 0.0f, 16.0f, 16.0f, 0.0f), PartPose.offset(8.0f, -12.0f, 0.0f));
        partDefinition3.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(44, 50).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 28.0f, 8.0f), PartPose.offset(-13.0f, -13.0f, 1.0f));
        partDefinition3.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 58).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 28.0f, 8.0f), PartPose.offset(13.0f, -13.0f, 1.0f));
        partDefinition2.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(76, 48).addBox(-3.1f, 0.0f, -3.0f, 6.0f, 13.0f, 6.0f), PartPose.offset(-5.9f, -13.0f, 0.0f));
        partDefinition2.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(76, 76).addBox(-2.9f, 0.0f, -3.0f, 6.0f, 13.0f, 6.0f), PartPose.offset(5.9f, -13.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(T warden, float f, float g, float h, float i, float j) {
        float t;
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float k = h - (float)((Warden)warden).tickCount;
        float l = Math.min(0.5f, 3.0f * g);
        float m = h * 0.1f;
        float n = f * 0.8662f;
        float o = Mth.cos(n);
        float p = Mth.sin(n);
        float q = Mth.cos(m);
        float r = Mth.sin(m);
        float s = Math.min(0.35f, l);
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.zRot += 0.3f * p * l;
        this.head.zRot += 0.06f * q;
        this.head.xRot += 1.2f * Mth.cos(n + 1.5707964f) * s;
        this.head.xRot += 0.06f * r;
        this.body.zRot = 0.1f * p * l;
        this.body.zRot += 0.025f * r;
        this.body.xRot = 1.0f * o * s;
        this.body.xRot += 0.025f * q;
        this.leftLeg.xRot = 1.0f * o * l;
        this.rightLeg.xRot = 1.0f * Mth.cos(n + (float)Math.PI) * l;
        this.leftArm.xRot = -(0.8f * o * l);
        this.leftArm.zRot = 0.0f;
        this.rightArm.xRot = -(0.8f * p * l);
        this.rightArm.zRot = 0.0f;
        this.leftArm.yRot = 0.0f;
        this.leftArm.z = 1.0f;
        this.leftArm.x = 13.0f;
        this.leftArm.y = -13.0f;
        this.rightArm.yRot = 0.0f;
        this.rightArm.z = 1.0f;
        this.rightArm.x = -13.0f;
        this.rightArm.y = -13.0f;
        this.leftTendril.xRot = t = ((Warden)warden).getEarAnimation(k) * (float)(Math.cos((double)h * 2.25) * Math.PI * (double)0.1f);
        this.rightTendril.xRot = -t;
        long u = Util.getMillis();
        this.animate(((Warden)warden).attackAnimationState, WardenAnimation.WARDEN_ATTACK, u);
        this.animate(((Warden)warden).diggingAnimationState, WardenAnimation.WARDEN_DIG, u);
        this.animate(((Warden)warden).emergeAnimationState, WardenAnimation.WARDEN_EMERGE, u);
        this.animate(((Warden)warden).roarAnimationState, WardenAnimation.WARDEN_ROAR, u);
        this.animate(((Warden)warden).sniffAnimationState, WardenAnimation.WARDEN_SNIFF, u);
    }

    public void animate(AnimationState animationState2, AnimationDefinition animationDefinition, long l) {
        animationState2.ifStarted(animationState -> KeyframeAnimations.animate(this, animationDefinition, l - animationState.startTime(), 1.0f, ANIMATION_VECTOR_CACHE));
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}

