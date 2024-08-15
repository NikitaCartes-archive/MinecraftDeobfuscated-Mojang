package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;

@Environment(EnvType.CLIENT)
public class PiglinModel extends AbstractPiglinModel<PiglinRenderState> {
	public PiglinModel(ModelPart modelPart) {
		super(modelPart);
	}

	public void setupAnim(PiglinRenderState piglinRenderState) {
		super.setupAnim(piglinRenderState);
		float f = (float) (Math.PI / 6);
		float g = piglinRenderState.attackTime;
		PiglinArmPose piglinArmPose = piglinRenderState.armPose;
		if (piglinArmPose == PiglinArmPose.DANCING) {
			float h = piglinRenderState.ageInTicks / 60.0F;
			this.rightEar.zRot = (float) (Math.PI / 6) + (float) (Math.PI / 180.0) * Mth.sin(h * 30.0F) * 10.0F;
			this.leftEar.zRot = (float) (-Math.PI / 6) - (float) (Math.PI / 180.0) * Mth.cos(h * 30.0F) * 10.0F;
			this.head.x = this.head.x + Mth.sin(h * 10.0F);
			this.head.y = this.head.y + Mth.sin(h * 40.0F) + 0.4F;
			this.rightArm.zRot = (float) (Math.PI / 180.0) * (70.0F + Mth.cos(h * 40.0F) * 10.0F);
			this.leftArm.zRot = this.rightArm.zRot * -1.0F;
			this.rightArm.y = this.rightArm.y + (Mth.sin(h * 40.0F) * 0.5F - 0.5F);
			this.leftArm.y = this.leftArm.y + Mth.sin(h * 40.0F) * 0.5F + 0.5F;
			this.body.y = this.body.y + Mth.sin(h * 40.0F) * 0.35F;
		} else if (piglinArmPose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && g == 0.0F) {
			this.holdWeaponHigh(piglinRenderState);
		} else if (piglinArmPose == PiglinArmPose.CROSSBOW_HOLD) {
			AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, piglinRenderState.mainArm == HumanoidArm.RIGHT);
		} else if (piglinArmPose == PiglinArmPose.CROSSBOW_CHARGE) {
			AnimationUtils.animateCrossbowCharge(
				this.rightArm, this.leftArm, piglinRenderState.maxCrossbowChageDuration, piglinRenderState.ticksUsingItem, piglinRenderState.mainArm == HumanoidArm.RIGHT
			);
		} else if (piglinArmPose == PiglinArmPose.ADMIRING_ITEM) {
			this.head.xRot = 0.5F;
			this.head.yRot = 0.0F;
			if (piglinRenderState.mainArm == HumanoidArm.LEFT) {
				this.rightArm.yRot = -0.5F;
				this.rightArm.xRot = -0.9F;
			} else {
				this.leftArm.yRot = 0.5F;
				this.leftArm.xRot = -0.9F;
			}
		}
	}

	protected void setupAttackAnimation(PiglinRenderState piglinRenderState, float f) {
		float g = piglinRenderState.attackTime;
		if (g > 0.0F && piglinRenderState.armPose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) {
			AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, piglinRenderState.mainArm, g, piglinRenderState.ageInTicks);
		} else {
			super.setupAttackAnimation(piglinRenderState, f);
		}
	}

	private void holdWeaponHigh(PiglinRenderState piglinRenderState) {
		if (piglinRenderState.mainArm == HumanoidArm.LEFT) {
			this.leftArm.xRot = -1.8F;
		} else {
			this.rightArm.xRot = -1.8F;
		}
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
}
