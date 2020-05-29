package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;

@Environment(EnvType.CLIENT)
public class PiglinModel<T extends Mob> extends PlayerModel<T> {
	public final ModelPart earRight;
	public final ModelPart earLeft;

	public PiglinModel(float f, int i, int j) {
		super(f, false);
		this.texWidth = i;
		this.texHeight = j;
		this.body = new ModelPart(this, 16, 16);
		this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, f);
		this.head = new ModelPart(this);
		this.head.texOffs(0, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, f);
		this.head.texOffs(31, 1).addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, f);
		this.head.texOffs(2, 4).addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, f);
		this.head.texOffs(2, 0).addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, f);
		this.earRight = new ModelPart(this);
		this.earRight.setPos(4.5F, -6.0F, 0.0F);
		this.earRight.texOffs(51, 6).addBox(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, f);
		this.head.addChild(this.earRight);
		this.earLeft = new ModelPart(this);
		this.earLeft.setPos(-4.5F, -6.0F, 0.0F);
		this.earLeft.texOffs(39, 6).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, f);
		this.head.addChild(this.earLeft);
		this.hat = new ModelPart(this);
	}

	public void setupAnim(T mob, float f, float g, float h, float i, float j) {
		super.setupAnim(mob, f, g, h, i, j);
		float k = (float) (Math.PI / 6);
		float l = h * 0.1F + f * 0.5F;
		float m = 0.08F + g * 0.4F;
		this.earRight.zRot = (float) (-Math.PI / 6) - Mth.cos(l * 1.2F) * m;
		this.earLeft.zRot = (float) (Math.PI / 6) + Mth.cos(l) * m;
		if (mob.getType() == EntityType.PIGLIN) {
			Piglin piglin = (Piglin)mob;
			Piglin.PiglinArmPose piglinArmPose = piglin.getArmPose();
			if (piglinArmPose == Piglin.PiglinArmPose.DANCING) {
				float n = h / 60.0F;
				this.earLeft.zRot = (float) (Math.PI / 6) + (float) (Math.PI / 180.0) * Mth.sin(n * 30.0F) * 10.0F;
				this.earRight.zRot = (float) (-Math.PI / 6) - (float) (Math.PI / 180.0) * Mth.cos(n * 30.0F) * 10.0F;
				this.head.x = Mth.sin(n * 10.0F);
				this.head.y = Mth.sin(n * 40.0F);
				this.rightArm.zRot = (float) (Math.PI / 180.0) * (70.0F + Mth.cos(n * 40.0F) * 10.0F);
				this.leftArm.zRot = this.rightArm.zRot * -1.0F;
				this.body.y = Mth.sin(n * 40.0F) * 0.35F;
			} else if (piglinArmPose == Piglin.PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && this.attackTime == 0.0F) {
				this.holdWeaponHigh(mob);
			} else if (piglinArmPose == Piglin.PiglinArmPose.CROSSBOW_HOLD) {
				AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, !mob.isLeftHanded());
			} else if (piglinArmPose == Piglin.PiglinArmPose.CROSSBOW_CHARGE) {
				AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, mob, !mob.isLeftHanded());
			} else if (piglinArmPose == Piglin.PiglinArmPose.ADMIRING_ITEM) {
				this.head.xRot = 0.5F;
				this.head.yRot = 0.0F;
				if (mob.isLeftHanded()) {
					this.rightArm.yRot = -0.5F;
					this.rightArm.xRot = -0.9F;
				} else {
					this.leftArm.yRot = 0.5F;
					this.leftArm.xRot = -0.9F;
				}
			}
		} else if (mob.getType() == EntityType.ZOMBIFIED_PIGLIN) {
			AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, mob.isAggressive(), this.attackTime, h);
		}
	}

	protected void setupAttackAnimation(T mob, float f) {
		if (this.attackTime > 0.0F && mob instanceof Piglin && ((Piglin)mob).getArmPose() == Piglin.PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) {
			AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, mob, this.attackTime, f);
		} else {
			super.setupAttackAnimation(mob, f);
		}
	}

	private void holdWeaponHigh(T mob) {
		if (mob.isLeftHanded()) {
			this.leftArm.xRot = -1.8F;
		} else {
			this.rightArm.xRot = -1.8F;
		}
	}
}
