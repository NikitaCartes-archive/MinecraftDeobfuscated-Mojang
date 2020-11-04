package net.minecraft.world.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ItemSteerable {
	boolean boost();

	void travelWithInput(Vec3 vec3);

	float getSteeringSpeed();

	default boolean travel(Mob mob, ItemBasedSteering itemBasedSteering, Vec3 vec3) {
		if (!mob.isAlive()) {
			return false;
		} else {
			Entity entity = mob.getFirstPassenger();
			if (mob.isVehicle() && mob.canBeControlledByRider() && entity instanceof Player) {
				mob.yRot = entity.yRot;
				mob.yRotO = mob.yRot;
				mob.xRot = entity.xRot * 0.5F;
				mob.setRot(mob.yRot, mob.xRot);
				mob.yBodyRot = mob.yRot;
				mob.yHeadRot = mob.yRot;
				mob.maxUpStep = 1.0F;
				mob.flyingSpeed = mob.getSpeed() * 0.1F;
				if (itemBasedSteering.boosting && itemBasedSteering.boostTime++ > itemBasedSteering.boostTimeTotal) {
					itemBasedSteering.boosting = false;
				}

				if (mob.isControlledByLocalInstance()) {
					float f = this.getSteeringSpeed();
					if (itemBasedSteering.boosting) {
						f += f * 1.15F * Mth.sin((float)itemBasedSteering.boostTime / (float)itemBasedSteering.boostTimeTotal * (float) Math.PI);
					}

					mob.setSpeed(f);
					this.travelWithInput(new Vec3(0.0, 0.0, 1.0));
					mob.lerpSteps = 0;
				} else {
					mob.calculateEntityAnimation(mob, false);
					mob.setDeltaMovement(Vec3.ZERO);
				}

				return true;
			} else {
				mob.maxUpStep = 0.5F;
				mob.flyingSpeed = 0.02F;
				this.travelWithInput(vec3);
				return false;
			}
		}
	}
}
