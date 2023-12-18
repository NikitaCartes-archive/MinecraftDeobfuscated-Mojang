package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.definitions.ArmadilloAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

@Environment(EnvType.CLIENT)
public class ArmadilloModel extends AgeableHierarchicalModel<Armadillo> {
	private static final float BABY_Y_OFFSET = 16.02F;
	private static final float MAX_HEAD_ROTATION_EXTENT = 32.5F;
	private static final float MAX_DOWN_HEAD_ROTATION_EXTENT = 25.0F;
	private static final float MAX_UP_HEAD_ROTATION_EXTENT = 22.5F;
	private static final float MAX_WALK_ANIMATION_SPEED = 16.5F;
	private static final float WALK_ANIMATION_SCALE_FACTOR = 2.5F;
	private static final String HEAD_CUBE = "head_cube";
	private static final String RIGHT_EAR_CUBE = "right_ear_cube";
	private static final String LEFT_EAR_CUBE = "left_ear_cube";
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart cube;
	private final ModelPart head;
	private final ModelPart tail;

	public ArmadilloModel(ModelPart modelPart) {
		super(0.6F, 16.02F);
		this.root = modelPart;
		this.body = modelPart.getChild("body");
		this.rightHindLeg = modelPart.getChild("right_hind_leg");
		this.leftHindLeg = modelPart.getChild("left_hind_leg");
		this.head = this.body.getChild("head");
		this.tail = this.body.getChild("tail");
		this.cube = modelPart.getChild("cube");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create()
				.texOffs(0, 20)
				.addBox(-4.0F, -7.0F, -10.0F, 8.0F, 8.0F, 12.0F, new CubeDeformation(0.3F))
				.texOffs(0, 40)
				.addBox(-4.0F, -7.0F, -10.0F, 8.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 21.0F, 4.0F)
		);
		partDefinition2.addOrReplaceChild(
			"tail",
			CubeListBuilder.create().texOffs(44, 53).addBox(-0.5F, -0.0865F, 0.0933F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, 0.5061F, 0.0F, 0.0F)
		);
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, -11.0F));
		partDefinition3.addOrReplaceChild(
			"head_cube",
			CubeListBuilder.create().texOffs(43, 15).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F)
		);
		PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-1.0F, -1.0F, 0.0F));
		partDefinition4.addOrReplaceChild(
			"right_ear_cube",
			CubeListBuilder.create().texOffs(43, 10).addBox(-2.0F, -3.0F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(-0.5F, 0.0F, -0.6F, 0.1886F, -0.3864F, -0.0718F)
		);
		PartDefinition partDefinition5 = partDefinition3.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(1.0F, -2.0F, 0.0F));
		partDefinition5.addOrReplaceChild(
			"left_ear_cube",
			CubeListBuilder.create().texOffs(47, 10).addBox(0.0F, -3.0F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.5F, 1.0F, -0.6F, 0.1886F, 0.3864F, 0.0718F)
		);
		partDefinition.addOrReplaceChild(
			"right_hind_leg",
			CubeListBuilder.create().texOffs(51, 31).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(-2.0F, 21.0F, 4.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_hind_leg",
			CubeListBuilder.create().texOffs(42, 31).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(2.0F, 21.0F, 4.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_front_leg",
			CubeListBuilder.create().texOffs(51, 43).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(-2.0F, 21.0F, -4.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_front_leg",
			CubeListBuilder.create().texOffs(42, 43).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
			PartPose.offset(2.0F, 21.0F, -4.0F)
		);
		partDefinition.addOrReplaceChild(
			"cube",
			CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -10.0F, -6.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 24.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public void setupAnim(Armadillo armadillo, float f, float g, float h, float i, float j) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		if (armadillo.shouldHideInShell()) {
			this.body.skipDraw = true;
			this.leftHindLeg.visible = false;
			this.rightHindLeg.visible = false;
			this.tail.visible = false;
			this.cube.visible = true;
		} else {
			this.body.skipDraw = false;
			this.leftHindLeg.visible = true;
			this.rightHindLeg.visible = true;
			this.tail.visible = true;
			this.cube.visible = false;
			this.head.xRot = Mth.clamp(j, -22.5F, 25.0F) * (float) (Math.PI / 180.0);
			this.head.yRot = Mth.clamp(i, -32.5F, 32.5F) * (float) (Math.PI / 180.0);
		}

		this.animateWalk(ArmadilloAnimation.ARMADILLO_WALK, f, g, 16.5F, 2.5F);
		this.animate(armadillo.ballAnimationState, ArmadilloAnimation.ARMADILLO_BALL, h, 1.0F);
		this.animate(armadillo.ballJumpAnimationState, ArmadilloAnimation.ARMADILLO_BALL_JUMP, h, 1.0F);
	}
}
