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
import net.minecraft.world.entity.animal.frog.Frog;

@Environment(EnvType.CLIENT)
public class FrogModel<T extends Frog> extends HierarchicalModel<T> {
	private static final float WALK_ANIMATION_SPEED_FACTOR = 200.0F;
	public static final float MAX_WALK_ANIMATION_SPEED = 8.0F;
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

	public void setupAnim(T frog, float f, float g, float h, float i, float j) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		float k = Math.min((float)frog.getDeltaMovement().lengthSqr() * 200.0F, 8.0F);
		this.animate(frog.jumpAnimationState, FrogAnimation.FROG_JUMP, h);
		this.animate(frog.croakAnimationState, FrogAnimation.FROG_CROAK, h);
		this.animate(frog.tongueAnimationState, FrogAnimation.FROG_TONGUE, h);
		this.animate(frog.walkAnimationState, FrogAnimation.FROG_WALK, h, k);
		this.animate(frog.swimAnimationState, FrogAnimation.FROG_SWIM, h);
		this.animate(frog.swimIdleAnimationState, FrogAnimation.FROG_IDLE_WATER, h);
		this.croakingBody.visible = frog.croakAnimationState.isStarted();
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}
