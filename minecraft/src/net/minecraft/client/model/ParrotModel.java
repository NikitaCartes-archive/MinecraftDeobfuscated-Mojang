package net.minecraft.client.model;

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
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;

@Environment(EnvType.CLIENT)
public class ParrotModel extends EntityModel<ParrotRenderState> {
	private static final String FEATHER = "feather";
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart head;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;

	public ParrotModel(ModelPart modelPart) {
		this.root = modelPart;
		this.body = modelPart.getChild("body");
		this.tail = modelPart.getChild("tail");
		this.leftWing = modelPart.getChild("left_wing");
		this.rightWing = modelPart.getChild("right_wing");
		this.head = modelPart.getChild("head");
		this.leftLeg = modelPart.getChild("left_leg");
		this.rightLeg = modelPart.getChild("right_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(2, 8).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
			PartPose.offsetAndRotation(0.0F, 16.5F, -3.0F, 0.4937F, 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"tail",
			CubeListBuilder.create().texOffs(22, 1).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 4.0F, 1.0F),
			PartPose.offsetAndRotation(0.0F, 21.07F, 1.16F, 1.015F, 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_wing",
			CubeListBuilder.create().texOffs(19, 8).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F),
			PartPose.offsetAndRotation(1.5F, 16.94F, -2.76F, -0.6981F, (float) -Math.PI, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_wing",
			CubeListBuilder.create().texOffs(19, 8).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F),
			PartPose.offsetAndRotation(-1.5F, 16.94F, -2.76F, -0.6981F, (float) -Math.PI, 0.0F)
		);
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(2, 2).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 15.69F, -2.76F)
		);
		partDefinition2.addOrReplaceChild(
			"head2", CubeListBuilder.create().texOffs(10, 0).addBox(-1.0F, -0.5F, -2.0F, 2.0F, 1.0F, 4.0F), PartPose.offset(0.0F, -2.0F, -1.0F)
		);
		partDefinition2.addOrReplaceChild(
			"beak1", CubeListBuilder.create().texOffs(11, 7).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -0.5F, -1.5F)
		);
		partDefinition2.addOrReplaceChild(
			"beak2", CubeListBuilder.create().texOffs(16, 7).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -1.75F, -2.45F)
		);
		partDefinition2.addOrReplaceChild(
			"feather",
			CubeListBuilder.create().texOffs(2, 18).addBox(0.0F, -4.0F, -2.0F, 0.0F, 5.0F, 4.0F),
			PartPose.offsetAndRotation(0.0F, -2.15F, 0.15F, -0.2214F, 0.0F, 0.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(14, 18).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F);
		partDefinition.addOrReplaceChild("left_leg", cubeListBuilder, PartPose.offsetAndRotation(1.0F, 22.0F, -1.05F, -0.0299F, 0.0F, 0.0F));
		partDefinition.addOrReplaceChild("right_leg", cubeListBuilder, PartPose.offsetAndRotation(-1.0F, 22.0F, -1.05F, -0.0299F, 0.0F, 0.0F));
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public void setupAnim(ParrotRenderState parrotRenderState) {
		this.prepare(parrotRenderState.pose);
		float f = parrotRenderState.walkAnimationPos;
		float g = parrotRenderState.walkAnimationSpeed;
		this.setupAnim(parrotRenderState.pose, parrotRenderState.ageInTicks, f, g, parrotRenderState.flapAngle, parrotRenderState.yRot, parrotRenderState.xRot);
	}

	public void renderOnShoulder(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k, float l) {
		this.prepare(ParrotModel.Pose.ON_SHOULDER);
		this.setupAnim(ParrotModel.Pose.ON_SHOULDER, l, f, g, 0.0F, h, k);
		this.root.render(poseStack, vertexConsumer, i, j);
	}

	private void setupAnim(ParrotModel.Pose pose, float f, float g, float h, float i, float j, float k) {
		this.head.xRot = k * (float) (Math.PI / 180.0);
		this.head.yRot = j * (float) (Math.PI / 180.0);
		switch (pose) {
			case STANDING:
				this.leftLeg.xRot = this.leftLeg.xRot + Mth.cos(g * 0.6662F) * 1.4F * h;
				this.rightLeg.xRot = this.rightLeg.xRot + Mth.cos(g * 0.6662F + (float) Math.PI) * 1.4F * h;
			case FLYING:
			case ON_SHOULDER:
			default:
				float n = i * 0.3F;
				this.head.y += n;
				this.tail.xRot = this.tail.xRot + Mth.cos(g * 0.6662F) * 0.3F * h;
				this.tail.y += n;
				this.body.y += n;
				this.leftWing.zRot = -0.0873F - i;
				this.leftWing.y += n;
				this.rightWing.zRot = 0.0873F + i;
				this.rightWing.y += n;
				this.leftLeg.y += n;
				this.rightLeg.y += n;
			case SITTING:
				break;
			case PARTY:
				float l = Mth.cos(f);
				float m = Mth.sin(f);
				this.head.x += l;
				this.head.y += m;
				this.head.xRot = 0.0F;
				this.head.yRot = 0.0F;
				this.head.zRot = Mth.sin(f) * 0.4F;
				this.body.x += l;
				this.body.y += m;
				this.leftWing.zRot = -0.0873F - i;
				this.leftWing.x += l;
				this.leftWing.y += m;
				this.rightWing.zRot = 0.0873F + i;
				this.rightWing.x += l;
				this.rightWing.y += m;
				this.tail.x += l;
				this.tail.y += m;
		}
	}

	private void prepare(ParrotModel.Pose pose) {
		this.body.resetPose();
		this.head.resetPose();
		this.tail.resetPose();
		this.rightWing.resetPose();
		this.leftWing.resetPose();
		this.leftLeg.resetPose();
		this.rightLeg.resetPose();
		switch (pose) {
			case FLYING:
				this.leftLeg.xRot += (float) (Math.PI * 2.0 / 9.0);
				this.rightLeg.xRot += (float) (Math.PI * 2.0 / 9.0);
			case STANDING:
			case ON_SHOULDER:
			default:
				break;
			case SITTING:
				float f = 1.9F;
				this.head.y++;
				this.tail.xRot += (float) (Math.PI / 6);
				this.tail.y++;
				this.body.y++;
				this.leftWing.zRot = -0.0873F;
				this.leftWing.y++;
				this.rightWing.zRot = 0.0873F;
				this.rightWing.y++;
				this.leftLeg.y++;
				this.rightLeg.y++;
				this.leftLeg.xRot++;
				this.rightLeg.xRot++;
				break;
			case PARTY:
				this.leftLeg.zRot = (float) (-Math.PI / 9);
				this.rightLeg.zRot = (float) (Math.PI / 9);
		}
	}

	public static ParrotModel.Pose getPose(Parrot parrot) {
		if (parrot.isPartyParrot()) {
			return ParrotModel.Pose.PARTY;
		} else if (parrot.isInSittingPose()) {
			return ParrotModel.Pose.SITTING;
		} else {
			return parrot.isFlying() ? ParrotModel.Pose.FLYING : ParrotModel.Pose.STANDING;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Pose {
		FLYING,
		STANDING,
		SITTING,
		PARTY,
		ON_SHOULDER;
	}
}
