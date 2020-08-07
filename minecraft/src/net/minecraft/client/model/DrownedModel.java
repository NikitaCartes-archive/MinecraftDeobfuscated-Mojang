package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class DrownedModel<T extends Zombie> extends ZombieModel<T> {
	public DrownedModel(float f, float g, int i, int j) {
		super(f, g, i, j);
		this.rightArm = new ModelPart(this, 32, 48);
		this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.rightArm.setPos(-5.0F, 2.0F + g, 0.0F);
		this.rightLeg = new ModelPart(this, 16, 48);
		this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.rightLeg.setPos(-1.9F, 12.0F + g, 0.0F);
	}

	public DrownedModel(float f, boolean bl) {
		super(f, 0.0F, 64, bl ? 32 : 64);
	}

	public void prepareMobModel(T zombie, float f, float g, float h) {
		this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
		this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		ItemStack itemStack = zombie.getItemInHand(InteractionHand.MAIN_HAND);
		if (itemStack.getItem() == Items.TRIDENT && zombie.isAggressive()) {
			if (zombie.getMainArm() == HumanoidArm.RIGHT) {
				this.rightArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
			} else {
				this.leftArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
			}
		}

		super.prepareMobModel(zombie, f, g, h);
	}

	public void setupAnim(T zombie, float f, float g, float h, float i, float j) {
		super.setupAnim(zombie, f, g, h, i, j);
		if (this.leftArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
			this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) Math.PI;
			this.leftArm.yRot = 0.0F;
		}

		if (this.rightArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
			this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) Math.PI;
			this.rightArm.yRot = 0.0F;
		}

		if (this.swimAmount > 0.0F) {
			this.rightArm.xRot = this.rotlerpRad(this.swimAmount, this.rightArm.xRot, (float) (-Math.PI * 4.0 / 5.0)) + this.swimAmount * 0.35F * Mth.sin(0.1F * h);
			this.leftArm.xRot = this.rotlerpRad(this.swimAmount, this.leftArm.xRot, (float) (-Math.PI * 4.0 / 5.0)) - this.swimAmount * 0.35F * Mth.sin(0.1F * h);
			this.rightArm.zRot = this.rotlerpRad(this.swimAmount, this.rightArm.zRot, -0.15F);
			this.leftArm.zRot = this.rotlerpRad(this.swimAmount, this.leftArm.zRot, 0.15F);
			this.leftLeg.xRot = this.leftLeg.xRot - this.swimAmount * 0.55F * Mth.sin(0.1F * h);
			this.rightLeg.xRot = this.rightLeg.xRot + this.swimAmount * 0.55F * Mth.sin(0.1F * h);
			this.head.xRot = 0.0F;
		}
	}
}
