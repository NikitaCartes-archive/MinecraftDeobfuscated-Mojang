package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class SkeletonModel<T extends Mob & RangedAttackMob> extends HumanoidModel<T> {
	public SkeletonModel() {
		this(0.0F, false);
	}

	public SkeletonModel(float f, boolean bl) {
		super(f, 0.0F, 64, 32);
		if (!bl) {
			this.rightArm = new ModelPart(this, 40, 16);
			this.rightArm.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, f);
			this.rightArm.setPos(-5.0F, 2.0F, 0.0F);
			this.leftArm = new ModelPart(this, 40, 16);
			this.leftArm.mirror = true;
			this.leftArm.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, f);
			this.leftArm.setPos(5.0F, 2.0F, 0.0F);
			this.rightLeg = new ModelPart(this, 0, 16);
			this.rightLeg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, f);
			this.rightLeg.setPos(-2.0F, 12.0F, 0.0F);
			this.leftLeg = new ModelPart(this, 0, 16);
			this.leftLeg.mirror = true;
			this.leftLeg.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, f);
			this.leftLeg.setPos(2.0F, 12.0F, 0.0F);
		}
	}

	public void prepareMobModel(T mob, float f, float g, float h) {
		this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
		this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		ItemStack itemStack = mob.getItemInHand(InteractionHand.MAIN_HAND);
		if (itemStack.getItem() == Items.BOW && mob.isAggressive()) {
			if (mob.getMainArm() == HumanoidArm.RIGHT) {
				this.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
			} else {
				this.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
			}
		}

		super.prepareMobModel(mob, f, g, h);
	}

	public void setupAnim(T mob, float f, float g, float h, float i, float j, float k) {
		super.setupAnim(mob, f, g, h, i, j, k);
		ItemStack itemStack = mob.getMainHandItem();
		if (mob.isAggressive() && (itemStack.isEmpty() || itemStack.getItem() != Items.BOW)) {
			float l = Mth.sin(this.attackTime * (float) Math.PI);
			float m = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float) Math.PI);
			this.rightArm.zRot = 0.0F;
			this.leftArm.zRot = 0.0F;
			this.rightArm.yRot = -(0.1F - l * 0.6F);
			this.leftArm.yRot = 0.1F - l * 0.6F;
			this.rightArm.xRot = (float) (-Math.PI / 2);
			this.leftArm.xRot = (float) (-Math.PI / 2);
			this.rightArm.xRot -= l * 1.2F - m * 0.4F;
			this.leftArm.xRot -= l * 1.2F - m * 0.4F;
			this.rightArm.zRot = this.rightArm.zRot + Mth.cos(h * 0.09F) * 0.05F + 0.05F;
			this.leftArm.zRot = this.leftArm.zRot - (Mth.cos(h * 0.09F) * 0.05F + 0.05F);
			this.rightArm.xRot = this.rightArm.xRot + Mth.sin(h * 0.067F) * 0.05F;
			this.leftArm.xRot = this.leftArm.xRot - Mth.sin(h * 0.067F) * 0.05F;
		}
	}

	@Override
	public void translateToHand(float f, HumanoidArm humanoidArm) {
		float g = humanoidArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		ModelPart modelPart = this.getArm(humanoidArm);
		modelPart.x += g;
		modelPart.translateTo(f);
		modelPart.x -= g;
	}
}
