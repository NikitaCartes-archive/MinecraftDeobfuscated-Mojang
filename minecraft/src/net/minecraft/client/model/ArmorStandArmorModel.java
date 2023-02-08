package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.decoration.ArmorStand;

@Environment(EnvType.CLIENT)
public class ArmorStandArmorModel extends HumanoidModel<ArmorStand> {
	public ArmorStandArmorModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
		MeshDefinition meshDefinition = HumanoidModel.createMesh(cubeDeformation, 0.0F);
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation), PartPose.offset(0.0F, 1.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"hat",
			CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation.extend(0.5F)),
			PartPose.offset(0.0F, 1.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_leg",
			CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation.extend(-0.1F)),
			PartPose.offset(-1.9F, 11.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_leg",
			CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation.extend(-0.1F)),
			PartPose.offset(1.9F, 11.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(ArmorStand armorStand, float f, float g, float h, float i, float j) {
		this.head.xRot = (float) (Math.PI / 180.0) * armorStand.getHeadPose().getX();
		this.head.yRot = (float) (Math.PI / 180.0) * armorStand.getHeadPose().getY();
		this.head.zRot = (float) (Math.PI / 180.0) * armorStand.getHeadPose().getZ();
		this.body.xRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getX();
		this.body.yRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getY();
		this.body.zRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getZ();
		this.leftArm.xRot = (float) (Math.PI / 180.0) * armorStand.getLeftArmPose().getX();
		this.leftArm.yRot = (float) (Math.PI / 180.0) * armorStand.getLeftArmPose().getY();
		this.leftArm.zRot = (float) (Math.PI / 180.0) * armorStand.getLeftArmPose().getZ();
		this.rightArm.xRot = (float) (Math.PI / 180.0) * armorStand.getRightArmPose().getX();
		this.rightArm.yRot = (float) (Math.PI / 180.0) * armorStand.getRightArmPose().getY();
		this.rightArm.zRot = (float) (Math.PI / 180.0) * armorStand.getRightArmPose().getZ();
		this.leftLeg.xRot = (float) (Math.PI / 180.0) * armorStand.getLeftLegPose().getX();
		this.leftLeg.yRot = (float) (Math.PI / 180.0) * armorStand.getLeftLegPose().getY();
		this.leftLeg.zRot = (float) (Math.PI / 180.0) * armorStand.getLeftLegPose().getZ();
		this.rightLeg.xRot = (float) (Math.PI / 180.0) * armorStand.getRightLegPose().getX();
		this.rightLeg.yRot = (float) (Math.PI / 180.0) * armorStand.getRightLegPose().getY();
		this.rightLeg.zRot = (float) (Math.PI / 180.0) * armorStand.getRightLegPose().getZ();
		this.hat.copyFrom(this.head);
	}
}
