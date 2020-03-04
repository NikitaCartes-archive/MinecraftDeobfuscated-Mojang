package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;

@Environment(EnvType.CLIENT)
public class PiglinModel<T extends Mob> extends HumanoidModel<T> {
	public final ModelPart earRight;
	public final ModelPart earLeft;

	public PiglinModel(float f, int i, int j) {
		super(f, 0.0F, i, j);
		this.body = new ModelPart(this, 16, 16);
		this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, f);
		this.head = new ModelPart(this);
		this.head.texOffs(0, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, f);
		this.head.texOffs(31, 1).addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, f);
		this.head.texOffs(2, 4).addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, f);
		this.head.texOffs(2, 0).addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, f);
		this.earRight = new ModelPart(this);
		this.earRight.setPos(4.5F, -6.0F, 0.0F);
		this.earRight.texOffs(57, 38).addBox(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, f);
		this.head.addChild(this.earRight);
		this.earLeft = new ModelPart(this);
		this.earLeft.setPos(-4.5F, -6.0F, 0.0F);
		this.head.addChild(this.earLeft);
		this.earLeft.texOffs(57, 22).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, f);
		this.hat = new ModelPart(this);
		this.rightArm = new ModelPart(this);
		this.rightArm.setPos(-5.0F, 2.0F, 0.0F);
		this.rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.leftArm = new ModelPart(this);
		this.leftArm.setPos(5.0F, 2.0F, 0.0F);
		this.leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.rightLeg = new ModelPart(this);
		this.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
		this.rightLeg.texOffs(0, 16).addBox(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.leftLeg = new ModelPart(this);
		this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
		this.leftLeg.texOffs(0, 16).addBox(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
	}

	public void setupAnim(T mob, float f, float g, float h, float i, float j) {
		super.setupAnim(mob, f, g, h, i, j);
		float k = (float) (Math.PI / 6);
		float l = h * 0.1F + f * 0.5F;
		float m = 0.08F + g * 0.4F;
		this.earRight.zRot = (float) (-Math.PI / 6) - Mth.cos(l * 1.2F) * m;
		this.earLeft.zRot = (float) (Math.PI / 6) + Mth.cos(l) * m;
		if (mob instanceof Piglin) {
			Piglin piglin = (Piglin)mob;
			Piglin.PiglinArmPose piglinArmPose = piglin.getArmPose();
			if (piglinArmPose == Piglin.PiglinArmPose.CROSSBOW_HOLD) {
				AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
			} else if (piglinArmPose == Piglin.PiglinArmPose.CROSSBOW_CHARGE) {
				AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, mob, true);
			} else if (piglinArmPose == Piglin.PiglinArmPose.ADMIRING_ITEM) {
				this.leftArm.yRot = 0.5F;
				this.leftArm.xRot = -0.9F;
				this.head.xRot = 0.5F;
				this.head.yRot = 0.0F;
			}
		}
	}
}
