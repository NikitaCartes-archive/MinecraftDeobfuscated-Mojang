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
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;

@Environment(EnvType.CLIENT)
public class ArmorStandArmorModel extends HumanoidModel<ArmorStandRenderState> {
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

	public void setupAnim(ArmorStandRenderState armorStandRenderState) {
		super.setupAnim(armorStandRenderState);
		this.head.xRot = (float) (Math.PI / 180.0) * armorStandRenderState.headPose.getX();
		this.head.yRot = (float) (Math.PI / 180.0) * armorStandRenderState.headPose.getY();
		this.head.zRot = (float) (Math.PI / 180.0) * armorStandRenderState.headPose.getZ();
		this.body.xRot = (float) (Math.PI / 180.0) * armorStandRenderState.bodyPose.getX();
		this.body.yRot = (float) (Math.PI / 180.0) * armorStandRenderState.bodyPose.getY();
		this.body.zRot = (float) (Math.PI / 180.0) * armorStandRenderState.bodyPose.getZ();
		this.leftArm.xRot = (float) (Math.PI / 180.0) * armorStandRenderState.leftArmPose.getX();
		this.leftArm.yRot = (float) (Math.PI / 180.0) * armorStandRenderState.leftArmPose.getY();
		this.leftArm.zRot = (float) (Math.PI / 180.0) * armorStandRenderState.leftArmPose.getZ();
		this.rightArm.xRot = (float) (Math.PI / 180.0) * armorStandRenderState.rightArmPose.getX();
		this.rightArm.yRot = (float) (Math.PI / 180.0) * armorStandRenderState.rightArmPose.getY();
		this.rightArm.zRot = (float) (Math.PI / 180.0) * armorStandRenderState.rightArmPose.getZ();
		this.leftLeg.xRot = (float) (Math.PI / 180.0) * armorStandRenderState.leftLegPose.getX();
		this.leftLeg.yRot = (float) (Math.PI / 180.0) * armorStandRenderState.leftLegPose.getY();
		this.leftLeg.zRot = (float) (Math.PI / 180.0) * armorStandRenderState.leftLegPose.getZ();
		this.rightLeg.xRot = (float) (Math.PI / 180.0) * armorStandRenderState.rightLegPose.getX();
		this.rightLeg.yRot = (float) (Math.PI / 180.0) * armorStandRenderState.rightLegPose.getY();
		this.rightLeg.zRot = (float) (Math.PI / 180.0) * armorStandRenderState.rightLegPose.getZ();
	}
}
