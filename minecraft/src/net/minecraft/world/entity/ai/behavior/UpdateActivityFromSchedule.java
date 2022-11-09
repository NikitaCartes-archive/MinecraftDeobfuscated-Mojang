package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;

public class UpdateActivityFromSchedule {
	public static BehaviorControl<LivingEntity> create() {
		return BehaviorBuilder.create(instance -> instance.point((serverLevel, livingEntity, l) -> {
				livingEntity.getBrain().updateActivityFromSchedule(serverLevel.getDayTime(), serverLevel.getGameTime());
				return true;
			}));
	}
}
