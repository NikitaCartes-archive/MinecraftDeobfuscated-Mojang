package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class ReactToBell {
	public static BehaviorControl<LivingEntity> create() {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.present(MemoryModuleType.HEARD_BELL_TIME)).apply(instance, memoryAccessor -> (serverLevel, livingEntity, l) -> {
						Raid raid = serverLevel.getRaidAt(livingEntity.blockPosition());
						if (raid == null) {
							livingEntity.getBrain().setActiveActivityIfPossible(Activity.HIDE);
						}

						return true;
					})
		);
	}
}
