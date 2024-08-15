package net.minecraft.client.model;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class WolfModel extends EntityModel<WolfRenderState> {
	public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(Set.of("head"));
	private static final String REAL_HEAD = "real_head";
	private static final String UPPER_BODY = "upper_body";
	private static final String REAL_TAIL = "real_tail";
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart realHead;
	private final ModelPart body;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;
	private final ModelPart tail;
	private final ModelPart realTail;
	private final ModelPart upperBody;
	private static final int LEG_SIZE = 8;

	public WolfModel(ModelPart modelPart) {
		this.root = modelPart;
		this.head = modelPart.getChild("head");
		this.realHead = this.head.getChild("real_head");
		this.body = modelPart.getChild("body");
		this.upperBody = modelPart.getChild("upper_body");
		this.rightHindLeg = modelPart.getChild("right_hind_leg");
		this.leftHindLeg = modelPart.getChild("left_hind_leg");
		this.rightFrontLeg = modelPart.getChild("right_front_leg");
		this.leftFrontLeg = modelPart.getChild("left_front_leg");
		this.tail = modelPart.getChild("tail");
		this.realTail = this.tail.getChild("real_tail");
	}

	public static MeshDefinition createMeshDefinition(CubeDeformation cubeDeformation) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		float f = 13.5F;
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(-1.0F, 13.5F, -7.0F));
		partDefinition2.addOrReplaceChild(
			"real_head",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F, cubeDeformation)
				.texOffs(16, 14)
				.addBox(-2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F, cubeDeformation)
				.texOffs(16, 14)
				.addBox(2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F, cubeDeformation)
				.texOffs(0, 10)
				.addBox(-0.5F, -0.001F, -5.0F, 3.0F, 3.0F, 4.0F, cubeDeformation),
			PartPose.ZERO
		);
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(18, 14).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 9.0F, 6.0F, cubeDeformation),
			PartPose.offsetAndRotation(0.0F, 14.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"upper_body",
			CubeListBuilder.create().texOffs(21, 0).addBox(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F, cubeDeformation),
			PartPose.offsetAndRotation(-1.0F, 14.0F, -3.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, cubeDeformation);
		partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-2.5F, 16.0F, 7.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(0.5F, 16.0F, 7.0F));
		partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-2.5F, 16.0F, -4.0F));
		partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(0.5F, 16.0F, -4.0F));
		PartDefinition partDefinition3 = partDefinition.addOrReplaceChild(
			"tail", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 12.0F, 8.0F, (float) (Math.PI / 5), 0.0F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"real_tail", CubeListBuilder.create().texOffs(9, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, cubeDeformation), PartPose.ZERO
		);
		return meshDefinition;
	}

	public void setupAnim(WolfRenderState wolfRenderState) {
		this.body.resetPose();
		this.upperBody.resetPose();
		this.tail.resetPose();
		this.rightHindLeg.resetPose();
		this.leftHindLeg.resetPose();
		this.rightFrontLeg.resetPose();
		this.leftFrontLeg.resetPose();
		float f = wolfRenderState.walkAnimationPos;
		float g = wolfRenderState.walkAnimationSpeed;
		if (wolfRenderState.isAngry) {
			this.tail.yRot = 0.0F;
		} else {
			this.tail.yRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		}

		if (wolfRenderState.isSitting) {
			float h = wolfRenderState.ageScale;
			this.upperBody.y += 2.0F * h;
			this.upperBody.xRot = (float) (Math.PI * 2.0 / 5.0);
			this.upperBody.yRot = 0.0F;
			this.body.y += 4.0F * h;
			this.body.z -= 2.0F * h;
			this.body.xRot = (float) (Math.PI / 4);
			this.tail.y += 9.0F * h;
			this.tail.z -= 2.0F * h;
			this.rightHindLeg.y += 6.7F * h;
			this.rightHindLeg.z -= 5.0F * h;
			this.rightHindLeg.xRot = (float) (Math.PI * 3.0 / 2.0);
			this.leftHindLeg.y += 6.7F * h;
			this.leftHindLeg.z -= 5.0F * h;
			this.leftHindLeg.xRot = (float) (Math.PI * 3.0 / 2.0);
			this.rightFrontLeg.xRot = 5.811947F;
			this.rightFrontLeg.x += 0.01F * h;
			this.rightFrontLeg.y += 1.0F * h;
			this.leftFrontLeg.xRot = 5.811947F;
			this.leftFrontLeg.x -= 0.01F * h;
			this.leftFrontLeg.y += 1.0F * h;
		} else {
			this.rightHindLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
			this.leftHindLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
			this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
			this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		}

		this.realHead.zRot = wolfRenderState.headRollAngle + wolfRenderState.getBodyRollAngle(0.0F);
		this.upperBody.zRot = wolfRenderState.getBodyRollAngle(-0.08F);
		this.body.zRot = wolfRenderState.getBodyRollAngle(-0.16F);
		this.realTail.zRot = wolfRenderState.getBodyRollAngle(-0.2F);
		this.head.xRot = wolfRenderState.xRot * (float) (Math.PI / 180.0);
		this.head.yRot = wolfRenderState.yRot * (float) (Math.PI / 180.0);
		this.tail.xRot = wolfRenderState.tailAngle;
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}
