package net.minecraft.world.entity.animal.axolotl;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class ValidatePlayDead {
	public static BehaviorControl<LivingEntity> create() {
		return BehaviorBuilder.create(
			instance -> instance.group(instance.present(MemoryModuleType.PLAY_DEAD_TICKS), instance.registered(MemoryModuleType.HURT_BY_ENTITY))
					.apply(instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
							int i = instance.<Integer>get(memoryAccessor);
							if (i <= 0) {
								memoryAccessor.erase();
								memoryAccessor2.erase();
								livingEntity.getBrain().useDefaultActivity();
							} else {
								memoryAccessor.set(i - 1);
							}

							return true;
						})
		);
	}
}
