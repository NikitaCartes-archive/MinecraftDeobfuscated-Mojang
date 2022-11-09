package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.schedule.Activity;

public class WakeUp {
	public static BehaviorControl<LivingEntity> create() {
		return BehaviorBuilder.create(instance -> instance.point((serverLevel, livingEntity, l) -> {
				if (!livingEntity.getBrain().isActive(Activity.REST) && livingEntity.isSleeping()) {
					livingEntity.stopSleeping();
					return true;
				} else {
					return false;
				}
			}));
	}
}
