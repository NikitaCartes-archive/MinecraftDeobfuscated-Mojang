package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;

@Environment(EnvType.CLIENT)
public class IllagerModel<T extends AbstractIllager> extends ListModel<T> implements ArmedModel, HeadedModel {
	private final ModelPart head;
	private final ModelPart hat;
	private final ModelPart body;
	private final ModelPart arms;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart rightArm;
	private final ModelPart leftArm;

	public IllagerModel(float f, float g, int i, int j) {
		this.head = new ModelPart(this).setTexSize(i, j);
		this.head.setPos(0.0F, 0.0F + g, 0.0F);
		this.head.texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, f);
		this.hat = new ModelPart(this, 32, 0).setTexSize(i, j);
		this.hat.addBox(-4.0F, -10.0F, -4.0F, 8.0F, 12.0F, 8.0F, f + 0.45F);
		this.head.addChild(this.hat);
		this.hat.visible = false;
		ModelPart modelPart = new ModelPart(this).setTexSize(i, j);
		modelPart.setPos(0.0F, g - 2.0F, 0.0F);
		modelPart.texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, f);
		this.head.addChild(modelPart);
		this.body = new ModelPart(this).setTexSize(i, j);
		this.body.setPos(0.0F, 0.0F + g, 0.0F);
		this.body.texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, f);
		this.body.texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, f + 0.5F);
		this.arms = new ModelPart(this).setTexSize(i, j);
		this.arms.setPos(0.0F, 0.0F + g + 2.0F, 0.0F);
		this.arms.texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, f);
		ModelPart modelPart2 = new ModelPart(this, 44, 22).setTexSize(i, j);
		modelPart2.mirror = true;
		modelPart2.addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, f);
		this.arms.addChild(modelPart2);
		this.arms.texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, f);
		this.leftLeg = new ModelPart(this, 0, 22).setTexSize(i, j);
		this.leftLeg.setPos(-2.0F, 12.0F + g, 0.0F);
		this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.rightLeg = new ModelPart(this, 0, 22).setTexSize(i, j);
		this.rightLeg.mirror = true;
		this.rightLeg.setPos(2.0F, 12.0F + g, 0.0F);
		this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.rightArm = new ModelPart(this, 40, 46).setTexSize(i, j);
		this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.rightArm.setPos(-5.0F, 2.0F + g, 0.0F);
		this.leftArm = new ModelPart(this, 40, 46).setTexSize(i, j);
		this.leftArm.mirror = true;
		this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.leftArm.setPos(5.0F, 2.0F + g, 0.0F);
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(this.head, this.body, this.leftLeg, this.rightLeg, this.arms, this.rightArm, this.leftArm);
	}

	public void setupAnim(T abstractIllager, float f, float g, float h, float i, float j) {
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.arms.y = 3.0F;
		this.arms.z = -1.0F;
		this.arms.xRot = -0.75F;
		if (this.riding) {
			this.rightArm.xRot = (float) (-Math.PI / 5);
			this.rightArm.yRot = 0.0F;
			this.rightArm.zRot = 0.0F;
			this.leftArm.xRot = (float) (-Math.PI / 5);
			this.leftArm.yRot = 0.0F;
			this.leftArm.zRot = 0.0F;
			this.leftLeg.xRot = -1.4137167F;
			this.leftLeg.yRot = (float) (Math.PI / 10);
			this.leftLeg.zRot = 0.07853982F;
			this.rightLeg.xRot = -1.4137167F;
			this.rightLeg.yRot = (float) (-Math.PI / 10);
			this.rightLeg.zRot = -0.07853982F;
		} else {
			this.rightArm.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 2.0F * g * 0.5F;
			this.rightArm.yRot = 0.0F;
			this.rightArm.zRot = 0.0F;
			this.leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * g * 0.5F;
			this.leftArm.yRot = 0.0F;
			this.leftArm.zRot = 0.0F;
			this.leftLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g * 0.5F;
			this.leftLeg.yRot = 0.0F;
			this.leftLeg.zRot = 0.0F;
			this.rightLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g * 0.5F;
			this.rightLeg.yRot = 0.0F;
			this.rightLeg.zRot = 0.0F;
		}

		AbstractIllager.IllagerArmPose illagerArmPose = abstractIllager.getArmPose();
		if (illagerArmPose == AbstractIllager.IllagerArmPose.ATTACKING) {
			if (abstractIllager.getMainHandItem().isEmpty()) {
				AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, this.attackTime, h);
			} else {
				AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, abstractIllager, this.attackTime, h);
			}
		} else if (illagerArmPose == AbstractIllager.IllagerArmPose.SPELLCASTING) {
			this.rightArm.z = 0.0F;
			this.rightArm.x = -5.0F;
			this.leftArm.z = 0.0F;
			this.leftArm.x = 5.0F;
			this.rightArm.xRot = Mth.cos(h * 0.6662F) * 0.25F;
			this.leftArm.xRot = Mth.cos(h * 0.6662F) * 0.25F;
			this.rightArm.zRot = (float) (Math.PI * 3.0 / 4.0);
			this.leftArm.zRot = (float) (-Math.PI * 3.0 / 4.0);
			this.rightArm.yRot = 0.0F;
			this.leftArm.yRot = 0.0F;
		} else if (illagerArmPose == AbstractIllager.IllagerArmPose.BOW_AND_ARROW) {
			this.rightArm.yRot = -0.1F + this.head.yRot;
			this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
			this.leftArm.xRot = -0.9424779F + this.head.xRot;
			this.leftArm.yRot = this.head.yRot - 0.4F;
			this.leftArm.zRot = (float) (Math.PI / 2);
		} else if (illagerArmPose == AbstractIllager.IllagerArmPose.CROSSBOW_HOLD) {
			AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
		} else if (illagerArmPose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE) {
			AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, abstractIllager, true);
		} else if (illagerArmPose == AbstractIllager.IllagerArmPose.CELEBRATING) {
			this.rightArm.z = 0.0F;
			this.rightArm.x = -5.0F;
			this.rightArm.xRot = Mth.cos(h * 0.6662F) * 0.05F;
			this.rightArm.zRot = 2.670354F;
			this.rightArm.yRot = 0.0F;
			this.leftArm.z = 0.0F;
			this.leftArm.x = 5.0F;
			this.leftArm.xRot = Mth.cos(h * 0.6662F) * 0.05F;
			this.leftArm.zRot = (float) (-Math.PI * 3.0 / 4.0);
			this.leftArm.yRot = 0.0F;
		}

		boolean bl = illagerArmPose == AbstractIllager.IllagerArmPose.CROSSED;
		this.arms.visible = bl;
		this.leftArm.visible = !bl;
		this.rightArm.visible = !bl;
	}

	private ModelPart getArm(HumanoidArm humanoidArm) {
		return humanoidArm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
	}

	public ModelPart getHat() {
		return this.hat;
	}

	@Override
	public ModelPart getHead() {
		return this.head;
	}

	@Override
	public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
		this.getArm(humanoidArm).translateAndRotate(poseStack);
	}
}
