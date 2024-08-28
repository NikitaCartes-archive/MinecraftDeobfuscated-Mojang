package net.minecraft.client.model;

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
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart head;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;

	public ParrotModel(ModelPart modelPart) {
		super(modelPart);
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

	public void setupAnim(ParrotRenderState parrotRenderState) {
		super.setupAnim(parrotRenderState);
		this.prepare(parrotRenderState.pose);
		this.head.xRot = parrotRenderState.xRot * (float) (Math.PI / 180.0);
		this.head.yRot = parrotRenderState.yRot * (float) (Math.PI / 180.0);
		switch (parrotRenderState.pose) {
			case STANDING:
				this.leftLeg.xRot = this.leftLeg.xRot + Mth.cos(parrotRenderState.walkAnimationPos * 0.6662F) * 1.4F * parrotRenderState.walkAnimationSpeed;
				this.rightLeg.xRot = this.rightLeg.xRot
					+ Mth.cos(parrotRenderState.walkAnimationPos * 0.6662F + (float) Math.PI) * 1.4F * parrotRenderState.walkAnimationSpeed;
			case FLYING:
			case ON_SHOULDER:
			default:
				float h = parrotRenderState.flapAngle * 0.3F;
				this.head.y += h;
				this.tail.xRot = this.tail.xRot + Mth.cos(parrotRenderState.walkAnimationPos * 0.6662F) * 0.3F * parrotRenderState.walkAnimationSpeed;
				this.tail.y += h;
				this.body.y += h;
				this.leftWing.zRot = -0.0873F - parrotRenderState.flapAngle;
				this.leftWing.y += h;
				this.rightWing.zRot = 0.0873F + parrotRenderState.flapAngle;
				this.rightWing.y += h;
				this.leftLeg.y += h;
				this.rightLeg.y += h;
			case SITTING:
				break;
			case PARTY:
				float f = Mth.cos(parrotRenderState.ageInTicks);
				float g = Mth.sin(parrotRenderState.ageInTicks);
				this.head.x += f;
				this.head.y += g;
				this.head.xRot = 0.0F;
				this.head.yRot = 0.0F;
				this.head.zRot = Mth.sin(parrotRenderState.ageInTicks) * 0.4F;
				this.body.x += f;
				this.body.y += g;
				this.leftWing.zRot = -0.0873F - parrotRenderState.flapAngle;
				this.leftWing.x += f;
				this.leftWing.y += g;
				this.rightWing.zRot = 0.0873F + parrotRenderState.flapAngle;
				this.rightWing.x += f;
				this.rightWing.y += g;
				this.tail.x += f;
				this.tail.y += g;
		}
	}

	private void prepare(ParrotModel.Pose pose) {
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
