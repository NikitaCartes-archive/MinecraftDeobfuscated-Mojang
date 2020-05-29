package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.monster.Monster;

@Environment(EnvType.CLIENT)
public abstract class AbstractZombieModel<T extends Monster> extends HumanoidModel<T> {
	protected AbstractZombieModel(float f, float g, int i, int j) {
		super(f, g, i, j);
	}

	public void setupAnim(T monster, float f, float g, float h, float i, float j) {
		super.setupAnim(monster, f, g, h, i, j);
		AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, this.isAggressive(monster), this.attackTime, h);
	}

	public abstract boolean isAggressive(T monster);
}
