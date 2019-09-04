package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.decoration.ArmorStand;

@Environment(EnvType.CLIENT)
public class ArmorStandModel extends ArmorStandArmorModel {
	private final ModelPart bodyStick1;
	private final ModelPart bodyStick2;
	private final ModelPart shoulderStick;
	private final ModelPart basePlate;

	public ArmorStandModel() {
		this(0.0F);
	}

	public ArmorStandModel(float f) {
		super(f, 64, 64);
		this.head = new ModelPart(this, 0, 0);
		this.head.addBox(-1.0F, -7.0F, -1.0F, 2, 7, 2, f);
		this.head.setPos(0.0F, 0.0F, 0.0F);
		this.body = new ModelPart(this, 0, 26);
		this.body.addBox(-6.0F, 0.0F, -1.5F, 12, 3, 3, f);
		this.body.setPos(0.0F, 0.0F, 0.0F);
		this.rightArm = new ModelPart(this, 24, 0);
		this.rightArm.addBox(-2.0F, -2.0F, -1.0F, 2, 12, 2, f);
		this.rightArm.setPos(-5.0F, 2.0F, 0.0F);
		this.leftArm = new ModelPart(this, 32, 16);
		this.leftArm.mirror = true;
		this.leftArm.addBox(0.0F, -2.0F, -1.0F, 2, 12, 2, f);
		this.leftArm.setPos(5.0F, 2.0F, 0.0F);
		this.rightLeg = new ModelPart(this, 8, 0);
		this.rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 11, 2, f);
		this.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
		this.leftLeg = new ModelPart(this, 40, 16);
		this.leftLeg.mirror = true;
		this.leftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 11, 2, f);
		this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
		this.bodyStick1 = new ModelPart(this, 16, 0);
		this.bodyStick1.addBox(-3.0F, 3.0F, -1.0F, 2, 7, 2, f);
		this.bodyStick1.setPos(0.0F, 0.0F, 0.0F);
		this.bodyStick1.visible = true;
		this.bodyStick2 = new ModelPart(this, 48, 16);
		this.bodyStick2.addBox(1.0F, 3.0F, -1.0F, 2, 7, 2, f);
		this.bodyStick2.setPos(0.0F, 0.0F, 0.0F);
		this.shoulderStick = new ModelPart(this, 0, 48);
		this.shoulderStick.addBox(-4.0F, 10.0F, -1.0F, 8, 2, 2, f);
		this.shoulderStick.setPos(0.0F, 0.0F, 0.0F);
		this.basePlate = new ModelPart(this, 0, 32);
		this.basePlate.addBox(-6.0F, 11.0F, -6.0F, 12, 1, 12, f);
		this.basePlate.setPos(0.0F, 12.0F, 0.0F);
		this.hat.visible = false;
	}

	@Override
	public void setupAnim(ArmorStand armorStand, float f, float g, float h, float i, float j, float k) {
		super.setupAnim(armorStand, f, g, h, i, j, k);
		this.leftArm.visible = armorStand.isShowArms();
		this.rightArm.visible = armorStand.isShowArms();
		this.basePlate.visible = !armorStand.isNoBasePlate();
		this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
		this.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
		this.bodyStick1.xRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getX();
		this.bodyStick1.yRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getY();
		this.bodyStick1.zRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getZ();
		this.bodyStick2.xRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getX();
		this.bodyStick2.yRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getY();
		this.bodyStick2.zRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getZ();
		this.shoulderStick.xRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getX();
		this.shoulderStick.yRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getY();
		this.shoulderStick.zRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getZ();
		this.basePlate.xRot = 0.0F;
		this.basePlate.yRot = (float) (Math.PI / 180.0) * -armorStand.yRot;
		this.basePlate.zRot = 0.0F;
	}

	public void render(ArmorStand armorStand, float f, float g, float h, float i, float j, float k) {
		super.render(armorStand, f, g, h, i, j, k);
		RenderSystem.pushMatrix();
		if (this.young) {
			float l = 2.0F;
			RenderSystem.scalef(0.5F, 0.5F, 0.5F);
			RenderSystem.translatef(0.0F, 24.0F * k, 0.0F);
			this.bodyStick1.render(k);
			this.bodyStick2.render(k);
			this.shoulderStick.render(k);
			this.basePlate.render(k);
		} else {
			if (armorStand.isCrouching()) {
				RenderSystem.translatef(0.0F, 0.2F, 0.0F);
			}

			this.bodyStick1.render(k);
			this.bodyStick2.render(k);
			this.shoulderStick.render(k);
			this.basePlate.render(k);
		}

		RenderSystem.popMatrix();
	}

	@Override
	public void translateToHand(float f, HumanoidArm humanoidArm) {
		ModelPart modelPart = this.getArm(humanoidArm);
		boolean bl = modelPart.visible;
		modelPart.visible = true;
		super.translateToHand(f, humanoidArm);
		modelPart.visible = bl;
	}
}
