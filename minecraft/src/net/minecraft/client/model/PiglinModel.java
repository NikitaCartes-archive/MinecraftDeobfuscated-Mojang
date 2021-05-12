package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;

@Environment(EnvType.CLIENT)
public class PiglinModel<T extends Mob> extends PlayerModel<T> {
	public final ModelPart rightEar = this.head.getChild("right_ear");
	private final ModelPart leftEar = this.head.getChild("left_ear");
	private final PartPose bodyDefault = this.body.storePose();
	private final PartPose headDefault = this.head.storePose();
	private final PartPose leftArmDefault = this.leftArm.storePose();
	private final PartPose rightArmDefault = this.rightArm.storePose();

	public PiglinModel(ModelPart modelPart) {
		super(modelPart, false);
	}

	public static MeshDefinition createMesh(CubeDeformation cubeDeformation) {
		MeshDefinition meshDefinition = PlayerModel.createMesh(cubeDeformation, false);
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDeformation), PartPose.ZERO
		);
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, cubeDeformation)
				.texOffs(31, 1)
				.addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, cubeDeformation)
				.texOffs(2, 4)
				.addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, cubeDeformation)
				.texOffs(2, 0)
				.addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, cubeDeformation),
			PartPose.ZERO
		);
		partDefinition2.addOrReplaceChild(
			"left_ear",
			CubeListBuilder.create().texOffs(51, 6).addBox(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, cubeDeformation),
			PartPose.offsetAndRotation(4.5F, -6.0F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 6))
		);
		partDefinition2.addOrReplaceChild(
			"right_ear",
			CubeListBuilder.create().texOffs(39, 6).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, cubeDeformation),
			PartPose.offsetAndRotation(-4.5F, -6.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 6))
		);
		partDefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		return meshDefinition;
	}

	public void setupAnim(T mob, float f, float g, float h, float i, float j) {
		this.body.loadPose(this.bodyDefault);
		this.head.loadPose(this.headDefault);
		this.leftArm.loadPose(this.leftArmDefault);
		this.rightArm.loadPose(this.rightArmDefault);
		super.setupAnim(mob, f, g, h, i, j);
		float k = (float) (Math.PI / 6);
		float l = h * 0.1F + f * 0.5F;
		float m = 0.08F + g * 0.4F;
		this.leftEar.zRot = (float) (-Math.PI / 6) - Mth.cos(l * 1.2F) * m;
		this.rightEar.zRot = (float) (Math.PI / 6) + Mth.cos(l) * m;
		if (mob instanceof AbstractPiglin abstractPiglin) {
			PiglinArmPose piglinArmPose = abstractPiglin.getArmPose();
			if (piglinArmPose == PiglinArmPose.DANCING) {
				float n = h / 60.0F;
				this.rightEar.zRot = (float) (Math.PI / 6) + (float) (Math.PI / 180.0) * Mth.sin(n * 30.0F) * 10.0F;
				this.leftEar.zRot = (float) (-Math.PI / 6) - (float) (Math.PI / 180.0) * Mth.cos(n * 30.0F) * 10.0F;
				this.head.x = Mth.sin(n * 10.0F);
				this.head.y = Mth.sin(n * 40.0F) + 0.4F;
				this.rightArm.zRot = (float) (Math.PI / 180.0) * (70.0F + Mth.cos(n * 40.0F) * 10.0F);
				this.leftArm.zRot = this.rightArm.zRot * -1.0F;
				this.rightArm.y = Mth.sin(n * 40.0F) * 0.5F + 1.5F;
				this.leftArm.y = Mth.sin(n * 40.0F) * 0.5F + 1.5F;
				this.body.y = Mth.sin(n * 40.0F) * 0.35F;
			} else if (piglinArmPose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && this.attackTime == 0.0F) {
				this.holdWeaponHigh(mob);
			} else if (piglinArmPose == PiglinArmPose.CROSSBOW_HOLD) {
				AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, !mob.isLeftHanded());
			} else if (piglinArmPose == PiglinArmPose.CROSSBOW_CHARGE) {
				AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, mob, !mob.isLeftHanded());
			} else if (piglinArmPose == PiglinArmPose.ADMIRING_ITEM) {
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

		this.leftPants.copyFrom(this.leftLeg);
		this.rightPants.copyFrom(this.rightLeg);
		this.leftSleeve.copyFrom(this.leftArm);
		this.rightSleeve.copyFrom(this.rightArm);
		this.jacket.copyFrom(this.body);
		this.hat.copyFrom(this.head);
	}

	protected void setupAttackAnimation(T mob, float f) {
		if (this.attackTime > 0.0F && mob instanceof Piglin && ((Piglin)mob).getArmPose() == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) {
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
