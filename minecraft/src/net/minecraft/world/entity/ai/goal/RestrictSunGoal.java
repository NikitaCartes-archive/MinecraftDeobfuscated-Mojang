package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;

public class RestrictSunGoal extends Goal {
	private final PathfinderMob mob;

	public RestrictSunGoal(PathfinderMob pathfinderMob) {
		this.mob = pathfinderMob;
	}

	@Override
	public boolean canUse() {
		return this.mob.level.isDay() && this.mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && this.mob.getNavigation() instanceof GroundPathNavigation;
	}

	@Override
	public void start() {
		((GroundPathNavigation)this.mob.getNavigation()).setAvoidSun(true);
	}

	@Override
	public void stop() {
		((GroundPathNavigation)this.mob.getNavigation()).setAvoidSun(false);
	}
}
