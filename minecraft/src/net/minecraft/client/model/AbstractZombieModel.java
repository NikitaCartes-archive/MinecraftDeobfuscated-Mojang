package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;

@Environment(EnvType.CLIENT)
public abstract class AbstractZombieModel<S extends ZombieRenderState> extends HumanoidModel<S> {
	protected AbstractZombieModel(ModelPart modelPart) {
		super(modelPart);
	}

	public void setupAnim(S zombieRenderState) {
		super.setupAnim(zombieRenderState);
		float f = zombieRenderState.attackTime;
		AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, zombieRenderState.isAggressive, f, zombieRenderState.ageInTicks);
	}
}
