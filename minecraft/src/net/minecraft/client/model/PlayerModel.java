package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

@Environment(EnvType.CLIENT)
public class PlayerModel extends HumanoidModel<PlayerRenderState> {
	private static final String LEFT_SLEEVE = "left_sleeve";
	private static final String RIGHT_SLEEVE = "right_sleeve";
	private static final String LEFT_PANTS = "left_pants";
	private static final String RIGHT_PANTS = "right_pants";
	private final List<ModelPart> bodyParts;
	public final ModelPart leftSleeve;
	public final ModelPart rightSleeve;
	public final ModelPart leftPants;
	public final ModelPart rightPants;
	public final ModelPart jacket;
	private final boolean slim;

	public PlayerModel(ModelPart modelPart, boolean bl) {
		super(modelPart, RenderType::entityTranslucent);
		this.slim = bl;
		this.leftSleeve = this.leftArm.getChild("left_sleeve");
		this.rightSleeve = this.rightArm.getChild("right_sleeve");
		this.leftPants = this.leftLeg.getChild("left_pants");
		this.rightPants = this.rightLeg.getChild("right_pants");
		this.jacket = this.body.getChild("jacket");
		this.bodyParts = List.of(this.head, this.body, this.leftArm, this.rightArm, this.leftLeg, this.rightLeg);
	}

	public static MeshDefinition createMesh(CubeDeformation cubeDeformation, boolean bl) {
		MeshDefinition meshDefinition = HumanoidModel.createMesh(cubeDeformation, 0.0F);
		PartDefinition partDefinition = meshDefinition.getRoot();
		float f = 0.25F;
		if (bl) {
			PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
				"left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubeDeformation), PartPose.offset(5.0F, 2.5F, 0.0F)
			);
			PartDefinition partDefinition3 = partDefinition.addOrReplaceChild(
				"right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubeDeformation), PartPose.offset(-5.0F, 2.5F, 0.0F)
			);
			partDefinition2.addOrReplaceChild(
				"left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubeDeformation.extend(0.25F)), PartPose.ZERO
			);
			partDefinition3.addOrReplaceChild(
				"right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubeDeformation.extend(0.25F)), PartPose.ZERO
			);
		} else {
			PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
				"left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation), PartPose.offset(5.0F, 2.0F, 0.0F)
			);
			PartDefinition partDefinition3 = partDefinition.getChild("right_arm");
			partDefinition2.addOrReplaceChild(
				"left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation.extend(0.25F)), PartPose.ZERO
			);
			partDefinition3.addOrReplaceChild(
				"right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation.extend(0.25F)), PartPose.ZERO
			);
		}

		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation), PartPose.offset(1.9F, 12.0F, 0.0F)
		);
		PartDefinition partDefinition3 = partDefinition.getChild("right_leg");
		partDefinition2.addOrReplaceChild(
			"left_pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation.extend(0.25F)), PartPose.ZERO
		);
		partDefinition3.addOrReplaceChild(
			"right_pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation.extend(0.25F)), PartPose.ZERO
		);
		PartDefinition partDefinition4 = partDefinition.getChild("body");
		partDefinition4.addOrReplaceChild(
			"jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDeformation.extend(0.25F)), PartPose.ZERO
		);
		return meshDefinition;
	}

	public void setupAnim(PlayerRenderState playerRenderState) {
		boolean bl = !playerRenderState.isSpectator;
		this.body.visible = bl;
		this.rightArm.visible = bl;
		this.leftArm.visible = bl;
		this.rightLeg.visible = bl;
		this.leftLeg.visible = bl;
		this.hat.visible = playerRenderState.showHat;
		this.jacket.visible = playerRenderState.showJacket;
		this.leftPants.visible = playerRenderState.showLeftPants;
		this.rightPants.visible = playerRenderState.showRightPants;
		this.leftSleeve.visible = playerRenderState.showLeftSleeve;
		this.rightSleeve.visible = playerRenderState.showRightSleeve;
		super.setupAnim(playerRenderState);
	}

	@Override
	public void setAllVisible(boolean bl) {
		super.setAllVisible(bl);
		this.leftSleeve.visible = bl;
		this.rightSleeve.visible = bl;
		this.leftPants.visible = bl;
		this.rightPants.visible = bl;
		this.jacket.visible = bl;
	}

	@Override
	public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
		this.root().translateAndRotate(poseStack);
		ModelPart modelPart = this.getArm(humanoidArm);
		if (this.slim) {
			float f = 0.5F * (float)(humanoidArm == HumanoidArm.RIGHT ? 1 : -1);
			modelPart.x += f;
			modelPart.translateAndRotate(poseStack);
			modelPart.x -= f;
		} else {
			modelPart.translateAndRotate(poseStack);
		}
	}

	public ModelPart getRandomBodyPart(RandomSource randomSource) {
		return Util.getRandom(this.bodyParts, randomSource);
	}

	protected HumanoidModel.ArmPose getArmPose(PlayerRenderState playerRenderState, HumanoidArm humanoidArm) {
		HumanoidModel.ArmPose armPose = PlayerRenderer.getArmPose(playerRenderState, playerRenderState.mainHandState, InteractionHand.MAIN_HAND);
		HumanoidModel.ArmPose armPose2 = PlayerRenderer.getArmPose(playerRenderState, playerRenderState.offhandState, InteractionHand.OFF_HAND);
		if (armPose.isTwoHanded()) {
			armPose2 = playerRenderState.offhandState.isEmpty ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
		}

		return playerRenderState.mainArm == humanoidArm ? armPose : armPose2;
	}
}
