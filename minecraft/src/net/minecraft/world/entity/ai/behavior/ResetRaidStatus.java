package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class ResetRaidStatus {
	public static BehaviorControl<LivingEntity> create() {
		return BehaviorBuilder.create(instance -> instance.point((serverLevel, livingEntity, l) -> {
				if (serverLevel.random.nextInt(20) != 0) {
					return false;
				} else {
					Brain<?> brain = livingEntity.getBrain();
					Raid raid = serverLevel.getRaidAt(livingEntity.blockPosition());
					if (raid == null || raid.isStopped() || raid.isLoss()) {
						brain.setDefaultActivity(Activity.IDLE);
						brain.updateActivityFromSchedule(serverLevel.getDayTime(), serverLevel.getGameTime());
					}

					return true;
				}
			}));
	}
}
