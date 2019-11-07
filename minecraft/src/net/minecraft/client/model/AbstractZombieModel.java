package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;

@Environment(EnvType.CLIENT)
public abstract class AbstractZombieModel<T extends Monster> extends HumanoidModel<T> {
	protected AbstractZombieModel(float f, float g, int i, int j) {
		super(f, g, i, j);
	}

	public void setupAnim(T monster, float f, float g, float h, float i, float j) {
		super.setupAnim(monster, f, g, h, i, j);
		boolean bl = this.isAggressive(monster);
		float k = Mth.sin(this.attackTime * (float) Math.PI);
		float l = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float) Math.PI);
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;
		this.rightArm.yRot = -(0.1F - k * 0.6F);
		this.leftArm.yRot = 0.1F - k * 0.6F;
		float m = (float) -Math.PI / (bl ? 1.5F : 2.25F);
		this.rightArm.xRot = m;
		this.leftArm.xRot = m;
		this.rightArm.xRot += k * 1.2F - l * 0.4F;
		this.leftArm.xRot += k * 1.2F - l * 0.4F;
		this.rightArm.zRot = this.rightArm.zRot + Mth.cos(h * 0.09F) * 0.05F + 0.05F;
		this.leftArm.zRot = this.leftArm.zRot - (Mth.cos(h * 0.09F) * 0.05F + 0.05F);
		this.rightArm.xRot = this.rightArm.xRot + Mth.sin(h * 0.067F) * 0.05F;
		this.leftArm.xRot = this.leftArm.xRot - Mth.sin(h * 0.067F) * 0.05F;
	}

	public abstract boolean isAggressive(T monster);
}
