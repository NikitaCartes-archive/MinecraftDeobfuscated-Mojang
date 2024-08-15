package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.definitions.FrogAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.FrogRenderState;

@Environment(EnvType.CLIENT)
public class FrogModel extends EntityModel<FrogRenderState> {
	private static final float MAX_WALK_ANIMATION_SPEED = 1.5F;
	private static final float MAX_SWIM_ANIMATION_SPEED = 1.0F;
	private static final float WALK_ANIMATION_SCALE_FACTOR = 2.5F;
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart eyes;
	private final ModelPart tongue;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart croakingBody;

	public FrogModel(ModelPart modelPart) {
		this.root = modelPart.getChild("root");
		this.body = this.root.getChild("body");
		this.head = this.body.getChild("head");
		this.eyes = this.head.getChild("eyes");
		this.tongue = this.body.getChild("tongue");
		this.leftArm = this.body.getChild("left_arm");
		this.rightArm = this.body.getChild("right_arm");
		this.leftLeg = this.root.getChild("left_leg");
		this.rightLeg = this.root.getChild("right_leg");
		this.croakingBody = this.body.getChild("croaking_body");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(3, 1).addBox(-3.5F, -2.0F, -8.0F, 7.0F, 3.0F, 9.0F).texOffs(23, 22).addBox(-3.5F, -1.0F, -8.0F, 7.0F, 0.0F, 9.0F),
			PartPose.offset(0.0F, -2.0F, 4.0F)
		);
		PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild(
			"head",
			CubeListBuilder.create().texOffs(23, 13).addBox(-3.5F, -1.0F, -7.0F, 7.0F, 0.0F, 9.0F).texOffs(0, 13).addBox(-3.5F, -2.0F, -7.0F, 7.0F, 3.0F, 9.0F),
			PartPose.offset(0.0F, -2.0F, -1.0F)
		);
		PartDefinition partDefinition5 = partDefinition4.addOrReplaceChild("eyes", CubeListBuilder.create(), PartPose.offset(-0.5F, 0.0F, 2.0F));
		partDefinition5.addOrReplaceChild(
			"right_eye", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(-1.5F, -3.0F, -6.5F)
		);
		partDefinition5.addOrReplaceChild(
			"left_eye", CubeListBuilder.create().texOffs(0, 5).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(2.5F, -3.0F, -6.5F)
		);
		partDefinition3.addOrReplaceChild(
			"croaking_body",
			CubeListBuilder.create().texOffs(26, 5).addBox(-3.5F, -0.1F, -2.9F, 7.0F, 2.0F, 3.0F, new CubeDeformation(-0.1F)),
			PartPose.offset(0.0F, -1.0F, -5.0F)
		);
		PartDefinition partDefinition6 = partDefinition3.addOrReplaceChild(
			"tongue", CubeListBuilder.create().texOffs(17, 13).addBox(-2.0F, 0.0F, -7.1F, 4.0F, 0.0F, 7.0F), PartPose.offset(0.0F, -1.01F, 1.0F)
		);
		PartDefinition partDefinition7 = partDefinition3.addOrReplaceChild(
			"left_arm", CubeListBuilder.create().texOffs(0, 32).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 3.0F), PartPose.offset(4.0F, -1.0F, -6.5F)
		);
		partDefinition7.addOrReplaceChild(
			"left_hand", CubeListBuilder.create().texOffs(18, 40).addBox(-4.0F, 0.01F, -4.0F, 8.0F, 0.0F, 8.0F), PartPose.offset(0.0F, 3.0F, -1.0F)
		);
		PartDefinition partDefinition8 = partDefinition3.addOrReplaceChild(
			"right_arm", CubeListBuilder.create().texOffs(0, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 3.0F), PartPose.offset(-4.0F, -1.0F, -6.5F)
		);
		partDefinition8.addOrReplaceChild(
			"right_hand", CubeListBuilder.create().texOffs(2, 40).addBox(-4.0F, 0.01F, -5.0F, 8.0F, 0.0F, 8.0F), PartPose.offset(0.0F, 3.0F, 0.0F)
		);
		PartDefinition partDefinition9 = partDefinition2.addOrReplaceChild(
			"left_leg", CubeListBuilder.create().texOffs(14, 25).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 3.0F, 4.0F), PartPose.offset(3.5F, -3.0F, 4.0F)
		);
		partDefinition9.addOrReplaceChild(
			"left_foot", CubeListBuilder.create().texOffs(2, 32).addBox(-4.0F, 0.01F, -4.0F, 8.0F, 0.0F, 8.0F), PartPose.offset(2.0F, 3.0F, 0.0F)
		);
		PartDefinition partDefinition10 = partDefinition2.addOrReplaceChild(
			"right_leg", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 3.0F, 4.0F), PartPose.offset(-3.5F, -3.0F, 4.0F)
		);
		partDefinition10.addOrReplaceChild(
			"right_foot", CubeListBuilder.create().texOffs(18, 32).addBox(-4.0F, 0.01F, -4.0F, 8.0F, 0.0F, 8.0F), PartPose.offset(-2.0F, 3.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 48, 48);
	}

	public void setupAnim(FrogRenderState frogRenderState) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(frogRenderState.jumpAnimationState, FrogAnimation.FROG_JUMP, frogRenderState.ageInTicks);
		this.animate(frogRenderState.croakAnimationState, FrogAnimation.FROG_CROAK, frogRenderState.ageInTicks);
		this.animate(frogRenderState.tongueAnimationState, FrogAnimation.FROG_TONGUE, frogRenderState.ageInTicks);
		if (frogRenderState.isSwimming) {
			this.animateWalk(FrogAnimation.FROG_SWIM, frogRenderState.walkAnimationPos, frogRenderState.walkAnimationSpeed, 1.0F, 2.5F);
		} else {
			this.animateWalk(FrogAnimation.FROG_WALK, frogRenderState.walkAnimationPos, frogRenderState.walkAnimationSpeed, 1.5F, 2.5F);
		}

		this.animate(frogRenderState.swimIdleAnimationState, FrogAnimation.FROG_IDLE_WATER, frogRenderState.ageInTicks);
		this.croakingBody.visible = frogRenderState.croakAnimationState.isStarted();
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}
