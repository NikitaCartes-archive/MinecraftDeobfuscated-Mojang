package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Rabbit;

@Environment(EnvType.CLIENT)
public class RabbitModel<T extends Rabbit> extends EntityModel<T> {
	private static final float REAR_JUMP_ANGLE = 50.0F;
	private static final float FRONT_JUMP_ANGLE = -40.0F;
	private static final String LEFT_HAUNCH = "left_haunch";
	private static final String RIGHT_HAUNCH = "right_haunch";
	private final ModelPart leftRearFoot;
	private final ModelPart rightRearFoot;
	private final ModelPart leftHaunch;
	private final ModelPart rightHaunch;
	private final ModelPart body;
	private final ModelPart leftFrontLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart head;
	private final ModelPart rightEar;
	private final ModelPart leftEar;
	private final ModelPart tail;
	private final ModelPart nose;
	private float jumpRotation;
	private static final float NEW_SCALE = 0.6F;

	public RabbitModel(ModelPart modelPart) {
		this.leftRearFoot = modelPart.getChild("left_hind_foot");
		this.rightRearFoot = modelPart.getChild("right_hind_foot");
		this.leftHaunch = modelPart.getChild("left_haunch");
		this.rightHaunch = modelPart.getChild("right_haunch");
		this.body = modelPart.getChild("body");
		this.leftFrontLeg = modelPart.getChild("left_front_leg");
		this.rightFrontLeg = modelPart.getChild("right_front_leg");
		this.head = modelPart.getChild("head");
		this.rightEar = modelPart.getChild("right_ear");
		this.leftEar = modelPart.getChild("left_ear");
		this.tail = modelPart.getChild("tail");
		this.nose = modelPart.getChild("nose");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"left_hind_foot", CubeListBuilder.create().texOffs(26, 24).addBox(-1.0F, 5.5F, -3.7F, 2.0F, 1.0F, 7.0F), PartPose.offset(3.0F, 17.5F, 3.7F)
		);
		partDefinition.addOrReplaceChild(
			"right_hind_foot", CubeListBuilder.create().texOffs(8, 24).addBox(-1.0F, 5.5F, -3.7F, 2.0F, 1.0F, 7.0F), PartPose.offset(-3.0F, 17.5F, 3.7F)
		);
		partDefinition.addOrReplaceChild(
			"left_haunch",
			CubeListBuilder.create().texOffs(30, 15).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 5.0F),
			PartPose.offsetAndRotation(3.0F, 17.5F, 3.7F, (float) (-Math.PI / 9), 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_haunch",
			CubeListBuilder.create().texOffs(16, 15).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 5.0F),
			PartPose.offsetAndRotation(-3.0F, 17.5F, 3.7F, (float) (-Math.PI / 9), 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, -10.0F, 6.0F, 5.0F, 10.0F),
			PartPose.offsetAndRotation(0.0F, 19.0F, 8.0F, (float) (-Math.PI / 9), 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_front_leg",
			CubeListBuilder.create().texOffs(8, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
			PartPose.offsetAndRotation(3.0F, 17.0F, -1.0F, (float) (-Math.PI / 18), 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_front_leg",
			CubeListBuilder.create().texOffs(0, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
			PartPose.offsetAndRotation(-3.0F, 17.0F, -1.0F, (float) (-Math.PI / 18), 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(32, 0).addBox(-2.5F, -4.0F, -5.0F, 5.0F, 4.0F, 5.0F), PartPose.offset(0.0F, 16.0F, -1.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_ear",
			CubeListBuilder.create().texOffs(52, 0).addBox(-2.5F, -9.0F, -1.0F, 2.0F, 5.0F, 1.0F),
			PartPose.offsetAndRotation(0.0F, 16.0F, -1.0F, 0.0F, (float) (-Math.PI / 12), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_ear",
			CubeListBuilder.create().texOffs(58, 0).addBox(0.5F, -9.0F, -1.0F, 2.0F, 5.0F, 1.0F),
			PartPose.offsetAndRotation(0.0F, 16.0F, -1.0F, 0.0F, (float) (Math.PI / 12), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"tail",
			CubeListBuilder.create().texOffs(52, 6).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 2.0F),
			PartPose.offsetAndRotation(0.0F, 20.0F, 7.0F, -0.3490659F, 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"nose", CubeListBuilder.create().texOffs(32, 9).addBox(-0.5F, -2.5F, -5.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 16.0F, -1.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		if (this.young) {
			float l = 1.5F;
			poseStack.pushPose();
			poseStack.scale(0.56666666F, 0.56666666F, 0.56666666F);
			poseStack.translate(0.0F, 1.375F, 0.125F);
			ImmutableList.of(this.head, this.leftEar, this.rightEar, this.nose).forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.scale(0.4F, 0.4F, 0.4F);
			poseStack.translate(0.0F, 2.25F, 0.0F);
			ImmutableList.of(this.leftRearFoot, this.rightRearFoot, this.leftHaunch, this.rightHaunch, this.body, this.leftFrontLeg, this.rightFrontLeg, this.tail)
				.forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
			poseStack.popPose();
		} else {
			poseStack.pushPose();
			poseStack.scale(0.6F, 0.6F, 0.6F);
			poseStack.translate(0.0F, 1.0F, 0.0F);
			ImmutableList.of(
					this.leftRearFoot,
					this.rightRearFoot,
					this.leftHaunch,
					this.rightHaunch,
					this.body,
					this.leftFrontLeg,
					this.rightFrontLeg,
					this.head,
					this.rightEar,
					this.leftEar,
					this.tail,
					this.nose
				)
				.forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
			poseStack.popPose();
		}
	}

	public void setupAnim(T rabbit, float f, float g, float h, float i, float j) {
		float k = h - (float)rabbit.tickCount;
		this.nose.xRot = j * (float) (Math.PI / 180.0);
		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.rightEar.xRot = j * (float) (Math.PI / 180.0);
		this.leftEar.xRot = j * (float) (Math.PI / 180.0);
		this.nose.yRot = i * (float) (Math.PI / 180.0);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.rightEar.yRot = this.nose.yRot - (float) (Math.PI / 12);
		this.leftEar.yRot = this.nose.yRot + (float) (Math.PI / 12);
		this.jumpRotation = Mth.sin(rabbit.getJumpCompletion(k) * (float) Math.PI);
		this.leftHaunch.xRot = (this.jumpRotation * 50.0F - 21.0F) * (float) (Math.PI / 180.0);
		this.rightHaunch.xRot = (this.jumpRotation * 50.0F - 21.0F) * (float) (Math.PI / 180.0);
		this.leftRearFoot.xRot = this.jumpRotation * 50.0F * (float) (Math.PI / 180.0);
		this.rightRearFoot.xRot = this.jumpRotation * 50.0F * (float) (Math.PI / 180.0);
		this.leftFrontLeg.xRot = (this.jumpRotation * -40.0F - 11.0F) * (float) (Math.PI / 180.0);
		this.rightFrontLeg.xRot = (this.jumpRotation * -40.0F - 11.0F) * (float) (Math.PI / 180.0);
	}

	public void prepareMobModel(T rabbit, float f, float g, float h) {
		super.prepareMobModel(rabbit, f, g, h);
		this.jumpRotation = Mth.sin(rabbit.getJumpCompletion(h) * (float) Math.PI);
	}
}
