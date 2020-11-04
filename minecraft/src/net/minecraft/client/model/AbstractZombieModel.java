package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Monster;

@Environment(EnvType.CLIENT)
public abstract class AbstractZombieModel<T extends Monster> extends HumanoidModel<T> {
	protected AbstractZombieModel(ModelPart modelPart) {
		super(modelPart);
	}

	public void setupAnim(T monster, float f, float g, float h, float i, float j) {
		super.setupAnim(monster, f, g, h, i, j);
		AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, this.isAggressive(monster), this.attackTime, h);
	}

	public abstract boolean isAggressive(T monster);
}
