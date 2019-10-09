package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.decoration.ArmorStand;

@Environment(EnvType.CLIENT)
public class ArmorStandArmorModel extends HumanoidModel<ArmorStand> {
	public ArmorStandArmorModel(float f) {
		this(f, 64, 32);
	}

	protected ArmorStandArmorModel(float f, int i, int j) {
		super(RenderType::entitySolid, f, 0.0F, i, j);
	}

	public void setupAnim(ArmorStand armorStand, float f, float g, float h, float i, float j, float k) {
		this.head.xRot = (float) (Math.PI / 180.0) * armorStand.getHeadPose().getX();
		this.head.yRot = (float) (Math.PI / 180.0) * armorStand.getHeadPose().getY();
		this.head.zRot = (float) (Math.PI / 180.0) * armorStand.getHeadPose().getZ();
		this.head.setPos(0.0F, 1.0F, 0.0F);
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
		this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
		this.rightLeg.xRot = (float) (Math.PI / 180.0) * armorStand.getRightLegPose().getX();
		this.rightLeg.yRot = (float) (Math.PI / 180.0) * armorStand.getRightLegPose().getY();
		this.rightLeg.zRot = (float) (Math.PI / 180.0) * armorStand.getRightLegPose().getZ();
		this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
		this.hat.copyFrom(this.head);
	}
}
